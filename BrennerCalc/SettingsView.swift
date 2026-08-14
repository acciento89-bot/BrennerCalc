import SwiftUI

struct SettingsView: View {
    @Environment(ProStore.self) private var store
    @Environment(\.dismiss) private var dismiss
    @AppStorage("appLanguage") private var appLanguage = "de"
    @State private var showPaywall = false

    var body: some View {
        NavigationStack {
            ZStack {
                AppTheme.background.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 18) {
                        settingsCard {
                            VStack(alignment: .leading, spacing: 12) {
                                Text("settings.language")
                                    .font(.headline)
                                Picker("settings.language", selection: $appLanguage) {
                                    Text("Deutsch").tag("de")
                                    Text("English").tag("en")
                                }
                                .pickerStyle(.segmented)
                            }
                        }

                        settingsCard {
                            HStack(spacing: 14) {
                                Image(systemName: store.isPro ? "checkmark.seal.fill" : "lock.circle.fill")
                                    .font(.title2)
                                    .foregroundStyle(AppTheme.accent)
                                VStack(alignment: .leading, spacing: 4) {
                                    if store.isPro {
                                        Text("settings.pro.active")
                                            .font(.headline)
                                        Text("settings.pro.activeNote")
                                            .font(.subheadline)
                                            .foregroundStyle(AppTheme.muted)
                                    } else {
                                        Text("settings.pro.free")
                                            .font(.headline)
                                        Text("settings.pro.freeNote")
                                            .font(.subheadline)
                                            .foregroundStyle(AppTheme.muted)
                                    }
                                }
                                Spacer()
                            }

                            if !store.isPro {
                                Button("settings.pro.unlock") { showPaywall = true }
                                    .buttonStyle(BorderedProminentButtonStyle())
                                    .tint(AppTheme.accent)
                            }

                            Button("pro.restore") {
                                Task { await store.restore() }
                            }
                            .font(.subheadline.weight(.semibold))
                        }

                        settingsCard {
                            VStack(alignment: .leading, spacing: 9) {
                                Text("settings.about")
                                    .font(.headline)
                                Text("settings.aboutText")
                                    .foregroundStyle(AppTheme.muted)
                                Text("Version 1.0.0")
                                    .font(.caption)
                                    .foregroundStyle(AppTheme.muted)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        settingsCard {
                            HStack(alignment: .top, spacing: 10) {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .foregroundStyle(AppTheme.accent)
                                Text("settings.disclaimer")
                                    .font(.footnote)
                                    .foregroundStyle(AppTheme.muted)
                            }
                        }
                    }
                    .padding(18)
                }
            }
            .navigationTitle("settings.title")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("common.done") { dismiss() }
                }
            }
            .sheet(isPresented: $showPaywall) {
                ProPaywallView()
            }
        }
        .tint(AppTheme.accent)
    }

    private func settingsCard<Content: View>(@ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            content()
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppTheme.cardGradient, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
        .overlay {
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .stroke(AppTheme.line, lineWidth: 1)
        }
    }
}

#Preview {
    SettingsView().environment(ProStore.preview)
}
