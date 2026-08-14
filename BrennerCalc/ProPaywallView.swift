import SwiftUI

struct ProPaywallView: View {
    @Environment(ProStore.self) private var store
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ZStack {
                AppTheme.background.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 24) {
                        ZStack {
                            Circle()
                                .fill(AppTheme.accentGradient)
                                .frame(width: 84, height: 84)
                            Image(systemName: "wrench.and.screwdriver.fill")
                                .font(.system(size: 34, weight: .bold))
                                .foregroundStyle(.white)
                        }
                        .padding(.top, 18)

                        VStack(spacing: 10) {
                            Text("pro.title")
                                .font(.system(size: 32, weight: .bold, design: .rounded))
                            Text("pro.subtitle")
                                .foregroundStyle(AppTheme.muted)
                                .multilineTextAlignment(.center)
                        }

                        VStack(spacing: 12) {
                            ProFeature(icon: "gauge.with.dots.needle.67percent", title: "pro.feature.gas")
                            ProFeature(icon: "drop.circle.fill", title: "pro.feature.water")
                            ProFeature(icon: "arrow.triangle.2.circlepath", title: "pro.feature.future")
                            ProFeature(icon: "nosign", title: "pro.feature.noAds")
                        }

                        VStack(spacing: 12) {
                            Button {
                                Task {
                                    await store.purchase()
                                    if store.isPro { dismiss() }
                                }
                            } label: {
                                HStack {
                                    if store.isLoading {
                                        ProgressView().tint(.white)
                                    }
                                    Text("pro.buy")
                                    Spacer()
                                    Text(store.product?.displayPrice ?? "4,99 €")
                                }
                                .font(.headline)
                                .padding(.horizontal, 18)
                                .frame(maxWidth: .infinity, minHeight: 54)
                                .background(AppTheme.accentGradient, in: RoundedRectangle(cornerRadius: 16))
                                .foregroundStyle(.white)
                            }
                            .disabled(store.isLoading)

                            Button("pro.restore") {
                                Task {
                                    await store.restore()
                                    if store.isPro { dismiss() }
                                }
                            }
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(AppTheme.muted)

                            if let errorMessage = store.errorMessage {
                                Text(errorMessage)
                                    .font(.footnote)
                                    .foregroundStyle(.red.opacity(0.85))
                                    .multilineTextAlignment(.center)
                            }

                            Text("pro.note")
                                .font(.caption)
                                .foregroundStyle(AppTheme.muted)
                                .multilineTextAlignment(.center)
                        }
                    }
                    .padding(22)
                }
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("common.close") { dismiss() }
                }
            }
            .task { await store.refresh() }
        }
        .tint(AppTheme.accent)
    }
}

private struct ProFeature: View {
    let icon: String
    let title: LocalizedStringKey

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: icon)
                .foregroundStyle(AppTheme.accent)
                .frame(width: 34, height: 34)
                .background(AppTheme.accent.opacity(0.1), in: RoundedRectangle(cornerRadius: 10))
            Text(title)
                .font(.body.weight(.semibold))
            Spacer()
            Image(systemName: "checkmark.circle.fill")
                .foregroundStyle(AppTheme.accent)
        }
        .padding(14)
        .background(AppTheme.panel, in: RoundedRectangle(cornerRadius: 16))
    }
}

#Preview {
    ProPaywallView().environment(ProStore.preview)
}
