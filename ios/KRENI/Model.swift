import Foundation
import CoreLocation

struct City: Codable, Hashable, Identifiable {
    let id: String
    let name: String
    let lat: Double
    let lon: Double
    let trams: Bool
    var coordinate: CLLocationCoordinate2D { .init(latitude: lat, longitude: lon) }
}

struct Stop: Codable, Hashable, Identifiable {
    let id: String
    let city: String
    let name: String
    let lat: Double
    let lon: Double
    let modes: [String]
    var coordinate: CLLocationCoordinate2D { .init(latitude: lat, longitude: lon) }
}

struct Catalog: Codable {
    let schemaVersion: Int
    let cities: [City]
    let stops: [Stop]

    func validate() throws {
        guard schemaVersion == 1,
              Set(cities.map(\.id)).count == cities.count,
              Set(stops.map(\.id)).count == stops.count,
              stops.allSatisfy({ stop in
                  cities.contains(where: { $0.id == stop.city }) &&
                  (!stop.modes.contains("tram") ||
                   cities.first(where: { $0.id == stop.city })?.trams == true)
              })
        else { throw CatalogError.invalid }
    }

    static func load() throws -> Catalog {
        guard let url = Bundle.main.url(forResource: "catalog", withExtension: "json")
        else { throw CatalogError.missing }
        let catalog = try JSONDecoder().decode(Catalog.self, from: Data(contentsOf: url))
        try catalog.validate()
        return catalog
    }
}

enum CatalogError: Error { case missing, invalid }

struct LocalPreferences: Codable {
    var city: String = "zagreb"
    var dark: Bool = false
    var favorites: Set<String> = []
}

@MainActor
final class AppState: ObservableObject {
    @Published private(set) var catalog: Catalog
    @Published private(set) var selectedCity: String
    @Published private(set) var favorites: Set<String>
    @Published var dark: Bool { didSet { save() } }

    init() {
        let fallback = Catalog(schemaVersion: 1, cities: [
            City(id: "zagreb", name: "Zagreb", lat: 45.8131, lon: 15.9775, trams: true)
        ], stops: [])
        let loaded = (try? Catalog.load()) ?? fallback
        let pref = Self.read()
        let chosenCity = loaded.cities.contains(where: { $0.id == pref.city }) ?
            pref.city : loaded.cities[0].id
        let safeFavorites = Set(pref.favorites.filter { id in
            loaded.stops.contains(where: { $0.id == id })
        })
        catalog = loaded
        selectedCity = chosenCity
        favorites = safeFavorites
        dark = pref.dark
    }

    var city: City {
        catalog.cities.first(where: { $0.id == selectedCity }) ?? catalog.cities[0]
    }
    var cityStops: [Stop] { catalog.stops.filter { $0.city == selectedCity } }
    var savedStops: [Stop] { catalog.stops.filter { favorites.contains($0.id) } }

    func chooseCity(_ id: String) {
        guard catalog.cities.contains(where: { $0.id == id }) else { return }
        selectedCity = id
        save()
    }

    func toggleFavorite(_ id: String) {
        guard catalog.stops.contains(where: { $0.id == id }) else { return }
        if favorites.contains(id) { favorites.remove(id) }
        else { favorites.insert(id) }
        save()
    }

    private static var fileURL: URL? {
        FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first?
            .appendingPathComponent("kreni_preferences.json")
    }

    private static func read() -> LocalPreferences {
        guard let file = fileURL, let data = try? Data(contentsOf: file),
              let result = try? JSONDecoder().decode(LocalPreferences.self, from: data)
        else { return LocalPreferences() }
        return result
    }

    private func save() {
        guard let file = Self.fileURL else { return }
        do {
            try FileManager.default.createDirectory(
                at: file.deletingLastPathComponent(),
                withIntermediateDirectories: true
            )
            let prefs = LocalPreferences(
                city: selectedCity, dark: dark, favorites: favorites
            )
            let data = try JSONEncoder().encode(prefs)
            try data.write(to: file, options: .atomic)
        } catch {
            // Ne prekidati rad aplikacije ako spremanje nije dostupno.
        }
    }
}

enum Transport {
    static let modes = ["bus", "tram", "train", "ferry", "catamaran"]
    static func name(_ id: String) -> String {
        switch id {
        case "bus": return "Autobusi"
        case "tram": return "Tramvaji"
        case "train": return "Vlakovi"
        case "ferry": return "Trajekti"
        default: return "Katamarani"
        }
    }
    static func symbol(_ id: String) -> String {
        switch id {
        case "bus": return "bus.fill"
        case "tram": return "tram.fill"
        case "train": return "train.side.front.car"
        case "ferry": return "ferry.fill"
        default: return "sailboat.fill"
        }
    }
}
