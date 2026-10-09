import SwiftUI
import MapKit

struct RootView: View {
    @EnvironmentObject private var state: AppState
    @State private var selection = 0
    @State private var focusedStop: Stop?

    var body: some View {
        TabView(selection: $selection) {
            HomeView(onMap: { stop in
                state.chooseCity(stop.city)
                focusedStop = stop
                selection = 1
            })
            .tabItem { Label("Početna", systemImage: "house.fill") }.tag(0)

            MapScreen(focus: focusedStop)
                .tabItem { Label("Karta", systemImage: "map.fill") }.tag(1)

            DeparturesView()
                .tabItem { Label("Polasci", systemImage: "clock.fill") }.tag(2)

            MoreView(onMap: { stop in
                state.chooseCity(stop.city)
                focusedStop = stop
                selection = 1
            })
            .tabItem { Label("Više", systemImage: "person.fill") }.tag(3)
        }
    }
}

struct BrandBar: View {
    @EnvironmentObject private var state: AppState

    var body: some View {
        HStack {
            HStack(spacing: 10) {
                Image(systemName: "point.topleft.down.curvedto.point.bottomright.up")
                    .font(.title2.bold()).foregroundStyle(Brand.teal)
                Text("KRENI").font(.system(size: 28, weight: .black, design: .rounded))
            }
            Spacer()
            Menu {
                ForEach(state.catalog.cities) { city in
                    Button(city.name) { state.chooseCity(city.id) }
                }
            } label: {
                Label(state.city.name, systemImage: "chevron.down")
                    .labelStyle(.titleAndIcon)
                    .font(.subheadline.bold())
            }
            .accessibilityLabel("Odabrani grad: \(state.city.name)")
        }.padding(.vertical, 6)
    }
}

struct HomeView: View {
    @EnvironmentObject private var state: AppState
    let onMap: (Stop) -> Void
    @State private var search = ""
    @State private var showPlanner = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    BrandBar()
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Krenimo zajedno.")
                            .font(.system(size: 29, weight: .bold, design: .rounded))
                        Text("Brže. Jednostavnije. Cijelom Hrvatskom.")
                            .font(.subheadline)
                        Button { showPlanner = true } label: {
                            Label("Planiraj put", systemImage: "point.topleft.down.curvedto.point.bottomright.up")
                                .frame(maxWidth: .infinity).padding(7)
                        }.buttonStyle(.borderedProminent).tint(Brand.teal)
                            .foregroundStyle(Brand.navy)
                    }
                    .foregroundStyle(.white)
                    .padding(22)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(
                        LinearGradient(colors: [Brand.navy, Brand.blue],
                                       startPoint: .topLeading, endPoint: .bottomTrailing),
                        in: RoundedRectangle(cornerRadius: 24)
                    )
                    HStack(spacing: 10) {
                        ForEach(Transport.modes.filter { $0 != "tram" || state.city.trams },
                                id: \.self) { mode in
                            VStack(spacing: 6) {
                                Image(systemName: Transport.symbol(mode))
                                    .font(.title3).foregroundStyle(Brand.blue)
                                Text(Transport.name(mode)).font(.system(size: 10))
                                    .lineLimit(1).minimumScaleFactor(0.7)
                            }.frame(maxWidth: .infinity)
                        }
                    }.padding(.vertical, 6)
                    Text("Stanice i terminali").font(.title2.bold())
                    TextField("Pretraži stanice i luke", text: $search)
                        .textFieldStyle(.roundedBorder)
                        .accessibilityLabel("Pretraži stanice i luke")
                    let stops = state.cityStops.filter {
                        search.isEmpty || $0.name.localizedCaseInsensitiveContains(search)
                    }
                    if stops.isEmpty {
                        Text("Nema rezultata pretraživanja.")
                            .foregroundStyle(.secondary)
                    }
                    ForEach(stops) { stop in
                        StopRow(stop: stop, onMap: { onMap(stop) })
                    }
                }.padding(18)
            }
            .background(Brand.canvas.opacity(0.5))
            .sheet(isPresented: $showPlanner) { PlannerView() }
        }
    }
}

struct StopRow: View {
    @EnvironmentObject private var state: AppState
    let stop: Stop
    let onMap: () -> Void
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: Transport.symbol(stop.modes.first ?? "bus"))
                .font(.title2).foregroundStyle(Brand.blue).frame(width: 40)
            Button(action: onMap) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(stop.name).font(.subheadline.bold())
                    Text(stop.modes.map(Transport.name).joined(separator: " · "))
                        .font(.caption).foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }.buttonStyle(.plain)
            Button { state.toggleFavorite(stop.id) } label: {
                Image(systemName: state.favorites.contains(stop.id) ?
                      "star.fill" : "star")
                    .foregroundStyle(Brand.blue)
                    .padding(8)
            }
            .accessibilityLabel(state.favorites.contains(stop.id) ?
                                "Ukloni iz omiljenih" : "Spremi u omiljene")
            Button(action: onMap) {
                Image(systemName: "chevron.right").padding(5)
            }.accessibilityLabel("Otvori na karti")
        }
        .padding(13)
        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 18))
    }
}

struct MapScreen: View {
    @EnvironmentObject private var state: AppState
    let focus: Stop?
    @State private var selectedMode = "all"
    @State private var selection: String?
    @State private var position: MapCameraPosition = .automatic

    private var stops: [Stop] {
        state.cityStops.filter { selectedMode == "all" || $0.modes.contains(selectedMode) }
    }

    var body: some View {
        VStack(spacing: 4) {
            BrandBar().padding(.horizontal, 18)
            ScrollView(.horizontal) {
                HStack {
                    ForEach(["all"] + Transport.modes.filter { $0 != "tram" || state.city.trams },
                            id: \.self) { mode in
                        Button(mode == "all" ? "Sve" : Transport.name(mode)) {
                            selectedMode = mode
                        }
                        .buttonStyle(.bordered)
                        .tint(selectedMode == mode ? Brand.blue : .secondary)
                    }
                }.padding(.horizontal, 16)
            }.scrollIndicators(.hidden)
            Map(position: $position, selection: $selection) {
                ForEach(stops) { stop in
                    Marker(stop.name, systemImage:
                           Transport.symbol(stop.modes.first ?? "bus"),
                           coordinate: stop.coordinate)
                        .tint(Brand.blue)
                        .tag(stop.id)
                }
            }
            .mapControls { MapCompass() }
            .accessibilityLabel("Karta stanica i terminala")
            .onAppear { focusMap() }
            .onChange(of: state.selectedCity) { _, _ in focusMap() }
            .onChange(of: focus?.id) { _, _ in focusMap() }
            .overlay(alignment: .bottomLeading) {
                Text("© Apple Maps").font(.caption2).padding(7)
                    .background(.regularMaterial, in: Capsule()).padding(8)
            }
            if let stop = stops.first(where: { $0.id == selection }) {
                StopRow(stop: stop, onMap: { focusStop(stop) })
                    .padding(.horizontal, 12)
            }
            Text("Odaberi stanicu za pregled i spremanje.")
                .font(.caption).foregroundStyle(.secondary)
                .padding(.bottom, 8)
        }
    }

    private func focusMap() {
        let lat = focus?.city == state.selectedCity ? focus!.lat : state.city.lat
        let lon = focus?.city == state.selectedCity ? focus!.lon : state.city.lon
        position = .region(.init(center: .init(latitude: lat, longitude: lon),
                                 span: .init(latitudeDelta: 0.09, longitudeDelta: 0.09)))
        selection = focus?.city == state.selectedCity ? focus?.id : nil
    }
    private func focusStop(_ stop: Stop) {
        position = .region(.init(center: stop.coordinate,
                                 span: .init(latitudeDelta: 0.02, longitudeDelta: 0.02)))
    }
}

struct DeparturesView: View {
    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                Text("Polasci").font(.largeTitle.bold())
                Label("Polasci trenutačno nisu dostupni.", systemImage: "clock")
                    .font(.headline)
                Text("Prije putovanja provjeri red vožnje prijevoznika.")
                    .foregroundStyle(.secondary)
                Spacer()
            }
            .padding(22)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

struct MoreView: View {
    @EnvironmentObject private var state: AppState
    let onMap: (Stop) -> Void

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    BrandBar()
                    Text("Omiljeno").font(.largeTitle.bold())
                    if state.savedStops.isEmpty {
                        Text("Spremi svoje stanice dodirom na zvjezdicu.")
                            .foregroundStyle(.secondary)
                    }
                    ForEach(state.savedStops) { stop in
                        StopRow(stop: stop, onMap: { onMap(stop) })
                    }
                    Text("Postavke").font(.title2.bold()).padding(.top, 14)
                    Toggle("Tamna tema", isOn: $state.dark)
                        .padding(14)
                        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 15))
                    LabeledContent("Jezik", value: "Hrvatski")
                    LabeledContent("Privatnost", value: "Podaci ostaju na uređaju")
                }.padding(18)
            }
        }
    }
}

struct PlannerView: View {
    @EnvironmentObject private var state: AppState
    @Environment(\.dismiss) private var dismiss
    @State private var origin = ""
    @State private var destination = ""
    @State private var inspected = false

    var body: some View {
        NavigationStack {
            Form {
                Section("Polazište") {
                    Picker("Odakle?", selection: $origin) {
                        Text("Odaberi").tag("")
                        ForEach(state.catalog.stops) { stop in
                            Text("\(cityName(stop)) · \(stop.name)").tag(stop.id)
                        }
                    }
                }
                Section("Odredište") {
                    Picker("Kamo?", selection: $destination) {
                        Text("Odaberi").tag("")
                        ForEach(state.catalog.stops) { stop in
                            Text("\(cityName(stop)) · \(stop.name)").tag(stop.id)
                        }
                    }
                }
                Button("Pregledaj putovanje") { inspected = true }
                    .disabled(origin.isEmpty || destination.isEmpty || origin == destination)
                if inspected {
                    Section("Putovanje") {
                        Text("Za provjereno putovanje potreban je red vožnje prijevoznika.")
                        Text("Provjeri vrijeme polaska i mogućnost presjedanja.")
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .navigationTitle("Planer putovanja")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) { Button("Zatvori") { dismiss() } }
            }
        }
    }

    private func cityName(_ stop: Stop) -> String {
        state.catalog.cities.first(where: { $0.id == stop.city })?.name ?? stop.city
    }
}
