import SwiftUI

@main
struct BrennerCalcApp: App {
    @State private var store = ProStore()
    @State private var visits = ServiceVisitStore()
    @AppStorage("appLanguage") private var appLanguage = "de"

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(store)
                .environment(visits)
                .environment(\.locale, Locale(identifier: appLanguage))
                .preferredColorScheme(.dark)
                .task {
                    await store.refresh()
                }
        }
    }
}
