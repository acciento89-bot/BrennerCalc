import SwiftUI

@main
struct BrennerCalcApp: App {
    @State private var store = ProStore()
    @AppStorage("appLanguage") private var appLanguage = "de"

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(store)
                .environment(\.locale, Locale(identifier: appLanguage))
                .preferredColorScheme(.dark)
                .task {
                    await store.refresh()
                }
        }
    }
}
