import SwiftUI

@main
struct KRENIApp: App {
    @StateObject private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(appState)
                .tint(Brand.blue)
                .preferredColorScheme(appState.dark ? .dark : .light)
        }
    }
}

enum Brand {
    static let navy = Color(red: 7 / 255, green: 25 / 255, blue: 54 / 255)
    static let blue = Color(red: 20 / 255, green: 107 / 255, blue: 252 / 255)
    static let teal = Color(red: 2 / 255, green: 205 / 255, blue: 189 / 255)
    static let canvas = Color(red: 245 / 255, green: 248 / 255, blue: 252 / 255)
}
