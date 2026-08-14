import SwiftUI

struct HydronicCalculatorView: View {
    @State private var powerKW = 20.0
    @State private var deltaT = 20.0

    private var result: CalculatorEngine.WaterResult {
        CalculatorEngine.water(powerKW: powerKW, deltaT: deltaT)
    }

    var body: some View {
        CalculatorScreen(title: "calculator.water.title", subtitle: "water.lead") {
            InputCard {
                NumberInputRow(title: "water.power", unit: "kW", value: $powerKW)
                Divider().overlay(AppTheme.line)
                NumberInputRow(title: "water.deltaT", unit: "K", value: $deltaT)
            }

            VStack(spacing: 12) {
                ResultCard(icon: "drop.fill", title: "water.flowLH", value: result.litersPerHour, unit: "l/h", digits: 0...0)
                ResultCard(icon: "drop.fill", title: "water.flowLMin", value: result.litersPerMinute, unit: "l/min")
                ResultCard(icon: "arrow.left.arrow.right", title: "water.flowM3H", value: result.cubicMetersPerHour, unit: "m³/h")
            }

            FormulaNote(text: "water.formulaNote")
        }
        .navigationTitle("calculator.water.short")
    }
}

#Preview {
    NavigationStack { HydronicCalculatorView() }
}
