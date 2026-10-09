import Foundation
import CoreLocation
import Combine

struct LiveSource: Decodable {
    let id: String
    let city: String
    let enabled: Bool
    let endpoint: String?
    let approvedForProduction: Bool?
    let licenseUrl: String?

    var usable: Bool {
        guard enabled, approvedForProduction == true,
              let licenseUrl, licenseUrl.hasPrefix("https://"),
              let endpoint, let url = URL(string: endpoint),
              url.scheme == "https", url.user == nil, url.password == nil,
              url.port == nil, url.query == nil, url.fragment == nil,
              let host = url.host, !host.isEmpty else { return false }
        return true
    }
}
struct LiveConfig: Decodable {
    let schemaVersion: Int
    let providers: [LiveSource]
}
struct TrackedVehicle: Codable, Identifiable {
    let id: String
    let route: String
    let mode: String
    let lat: Double
    let lon: Double
    let observedAt: Int64
    var coordinate: CLLocationCoordinate2D { .init(latitude: lat, longitude: lon) }
}
struct TrackedArrival: Codable, Identifiable {
    let id: String
    let stopId: String
    let route: String
    let mode: String
    let scheduledAt: Int64?
    let predictedAt: Int64?
    let updatedAt: Int64

    func fresh(now: Int64) -> Bool {
        updatedAt >= now - 90 && updatedAt <= now + 15
    }
    func minutes(now: Int64) -> Int? {
        guard fresh(now: now), let t = predictedAt ?? scheduledAt,
              t >= now - 30, t <= now + 7200 else { return nil }
        return Int(ceil(Double(max(0, t - now)) / 60.0))
    }
    func predicted(now: Int64) -> Bool {
        predictedAt != nil && fresh(now: now)
    }
}
struct LiveSnapshot: Codable {
    let schemaVersion: Int
    let city: String
    let provider: String
    let generatedAt: Int64
    let vehicles: [TrackedVehicle]
    let arrivals: [TrackedArrival]

    func verified(city: String, provider: String, now: Int64) -> LiveSnapshot? {
        guard schemaVersion == 1, self.city == city, self.provider == provider,
              generatedAt >= now - 90, generatedAt <= now + 15,
              vehicles.count <= 1500, arrivals.count <= 6000 else { return nil }
        let modes = Set(["bus", "tram", "train", "ferry", "catamaran"])
        let validVehicles = vehicles.filter { v in
            v.lat.isFinite && v.lon.isFinite && (40.0...47.0).contains(v.lat) &&
            (12.0...20.0).contains(v.lon) && modes.contains(v.mode) &&
            v.observedAt >= now - 90 && v.observedAt <= now + 15
        }
        let validArrivals = arrivals.filter { a in
            modes.contains(a.mode) && a.updatedAt >= now - 90 && a.updatedAt <= now + 15 &&
            (a.predictedAt != nil || a.scheduledAt != nil)
        }
        let uniqueVehicles = validVehicles.map { v in
            TrackedVehicle(id: provider + ":" + v.id, route: v.route, mode: v.mode,
                           lat: v.lat, lon: v.lon, observedAt: v.observedAt)
        }
        let uniqueArrivals = validArrivals.map { a in
            TrackedArrival(id: provider + ":" + a.id, stopId: a.stopId,
                           route: a.route, mode: a.mode, scheduledAt: a.scheduledAt,
                           predictedAt: a.predictedAt, updatedAt: a.updatedAt)
        }
        return LiveSnapshot(schemaVersion: 1, city: city, provider: provider, generatedAt: generatedAt,
                            vehicles: uniqueVehicles, arrivals: uniqueArrivals)
    }
}

@MainActor
final class LiveStore: ObservableObject {
    @Published private(set) var snapshots: [LiveSnapshot] = []
    @Published private(set) var clock = Int64(Date().timeIntervalSince1970)
    private var activeCity: String?
    func tick() { clock = Int64(Date().timeIntervalSince1970) }
    private let sources: [LiveSource] = {
        guard let url = Bundle.main.url(forResource: "live_sources", withExtension: "json"),
              let raw = try? Data(contentsOf: url),
              let config = try? JSONDecoder().decode(LiveConfig.self, from: raw),
              config.schemaVersion == 1 else { return [] }
        return config.providers
    }()
    private func current(city: String) -> [LiveSnapshot] {
        snapshots.compactMap {
            $0.verified(city: city, provider: $0.provider, now: clock)
        }
    }
    func vehicles(in city: String) -> [TrackedVehicle] {
        current(city: city).flatMap(\.vehicles)
    }
    func arrivals(in city: String) -> [TrackedArrival] {
        current(city: city).flatMap(\.arrivals)
    }

    func refresh(city: String) async {
        if activeCity != city {
            activeCity = city
            snapshots = []
        }
        let candidates = sources.filter { $0.usable && $0.city == city }
        guard !candidates.isEmpty else { return }
        var updates: [LiveSnapshot] = []
        for source in candidates {
            if Task.isCancelled { return }
            guard let text = source.endpoint, let url = URL(string: text),
                  url.scheme == "https", url.user == nil, url.password == nil,
                  url.port == nil, url.query == nil, url.fragment == nil,
                  let host = url.host, !host.isEmpty else { continue }
            do {
                var request = URLRequest(url: url)
                request.timeoutInterval = 5
                request.setValue("application/json", forHTTPHeaderField: "Accept")
                let (data, response) = try await URLSession.shared.data(for: request)
                guard data.count <= 524288,
                      let response = response as? HTTPURLResponse,
                      response.statusCode == 200,
                      response.url?.host == url.host,
                      response.url?.scheme == "https" else { continue }
                let snapshot = try JSONDecoder().decode(LiveSnapshot.self, from: data)
                if let valid = snapshot.verified(city: city, provider: source.id,
                                                 now: Int64(Date().timeIntervalSince1970)) {
                    updates.append(valid)
                }
            } catch {
                // A failed source cannot be replaced with an invented vehicle location.
            }
        }
        if !Task.isCancelled && activeCity == city { snapshots = updates }
    }
}
