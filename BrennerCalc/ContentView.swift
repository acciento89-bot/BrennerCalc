import SwiftUI

struct ContentView: View {
    @Environment(ProStore.self) private var store
    @State private var showPaywall = false
    @State private var showSettings = false

    var body: some View {
        NavigationStack {
            ZStack {
                AppTheme.background.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 22) {
                        hero
                        calculatorGrid
                        safetyNote
                    }
                    .padding(.horizontal, 18)
                    .padding(.bottom, 36)
                }
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showSettings = true
                    } label: {
                        Image(systemName: "gearshape.fill")
                    }
                    .accessibilityLabel("settings.title")
                }
            }
            .sheet(isPresented: $showPaywall) {
                ProPaywallView()
            }
            .sheet(isPresented: $showSettings) {
                SettingsView()
            }
        }
        .tint(AppTheme.accent)
    }

    private var hero: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(spacing: 12) {
                ZStack {
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .fill(AppTheme.accentGradient)
                        .frame(width: 54, height: 54)
                    Image(systemName: "flame.fill")
                        .font(.system(size: 25, weight: .bold))
                        .foregroundStyle(.white)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text("BrennerCalc")
                        .font(.system(size: 28, weight: .black, design: .rounded))
                    Text("home.subtitle")
                        .font(.subheadline)
                        .foregroundStyle(AppTheme.muted)
                }

                Spacer()

                if store.isPro {
                    Text("PRO")
                        .font(.caption.bold())
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(AppTheme.accent.opacity(0.16), in: Capsule())
                        .foregroundStyle(AppTheme.accent)
                }
            }

            Text("home.hero")
                .font(.system(size: 34, weight: .bold, design: .rounded))
                .fixedSize(horizontal: false, vertical: true)

            Text("home.lead")
                .font(.body)
                .foregroundStyle(AppTheme.muted)
                .lineSpacing(4)
        }
        .padding(.top, 18)
    }

    private var calculatorGrid: some View {
        VStack(spacing: 14) {
            NavigationLink {
                OilNozzleCalculatorView()
            } label: {
                CalculatorCard(
                    icon: "flame.circle.fill",
                    title: "calculator.oil.title",
                    subtitle: "calculator.oil.subtitle",
                    locked: false
                )
            }
            .buttonStyle(.plain)

            if store.isPro {
                NavigationLink {
                    GasCalculatorView()
                } label: {
                    CalculatorCard(
                        icon: "gauge.with.dots.needle.67percent",
                        title: "calculator.gas.title",
                        subtitle: "calculator.gas.subtitle",
                        locked: false
                    )
                }
                .buttonStyle(.plain)

                NavigationLink {
                    HydronicCalculatorView()
                } label: {
                    CalculatorCard(
                        icon: "drop.circle.fill",
                        title: "calculator.water.title",
                        subtitle: "calculator.water.subtitle",
                        locked: false
                    )
                }
                .buttonStyle(.plain)
            } else {
                Button { showPaywall = true } label: {
                    CalculatorCard(
                        icon: "gauge.with.dots.needle.67percent",
                        title: "calculator.gas.title",
                        subtitle: "calculator.gas.subtitle",
                        locked: true
                    )
                }
                .buttonStyle(.plain)

                Button { showPaywall = true } label: {
                    CalculatorCard(
                        icon: "drop.circle.fill",
                        title: "calculator.water.title",
                        subtitle: "calculator.water.subtitle",
                        locked: true
                    )
                }
                .buttonStyle(.plain)
            }
        }
    }

    private var safetyNote: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "checkmark.shield.fill")
                .foregroundStyle(AppTheme.accent)
            Text("home.safety")
                .font(.footnote)
                .foregroundStyle(AppTheme.muted)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppTheme.panel, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
    }
}

private struct CalculatorCard: View {
    let icon: String
    let title: LocalizedStringKey
    let subtitle: LocalizedStringKey
    let locked: Bool

    var body: some View {
        HStack(spacing: 16) {
            ZStack {
                RoundedRectangle(cornerRadius: 15, style: .continuous)
                    .fill(AppTheme.accent.opacity(0.12))
                    .frame(width: 52, height: 52)
                Image(systemName: icon)
                    .font(.system(size: 23, weight: .semibold))
                    .foregroundStyle(AppTheme.accent)
            }

            VStack(alignment: .leading, spacing: 5) {
                HStack(spacing: 8) {
                    Text(title).font(.headline)
                    if locked {
                        Text("PRO")
                            .font(.system(size: 9, weight: .black))
                            .foregroundStyle(AppTheme.accent)
                    }
                }
                Text(subtitle)
                    .font(.subheadline)
                    .foregroundStyle(AppTheme.muted)
                    .multilineTextAlignment(.leading)
            }

            Spacer(minLength: 8)
            Image(systemName: locked ? "lock.fill" : "chevron.right")
                .foregroundStyle(locked ? AppTheme.accent : AppTheme.muted)
        }
        .padding(18)
        .background(AppTheme.cardGradient, in: RoundedRectangle(cornerRadius: 22, style: .continuous))
        .overlay {
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .stroke(AppTheme.line, lineWidth: 1)
        }
    }
}

#Preview {
    ContentView()
        .environment(ProStore.preview)
}
