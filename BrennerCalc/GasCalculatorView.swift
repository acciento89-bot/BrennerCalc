import SwiftUI

struct GasCalculatorView: View {
    @State private var mode = 0
    @State private var cubicMetersPerHour = 2.0
    @State private var desiredOutputKW = 18.0
    @State private var calorificValue = 10.0
    @State private var efficiency = 90.0

    private var forward: CalculatorEngine.GasResult {
        CalculatorEngine.gas(
            cubicMetersPerHour: cubicMetersPerHour,
            calorificValueKWhPerM3: calorificValue,
            efficiencyPercent: efficiency
        )
    }

    private var reverse: CalculatorEngine.GasReverseResult {
        CalculatorEngine.gasReverse(
            desiredOutputKW: desiredOutputKW,
            calorificValueKWhPerM3: calorificValue,
            efficiencyPercent: efficiency
        )
    }

    var body: some View {
        CalculatorScreen(title: "calculator.gas.title", subtitle: "gas.lead") {
            Picker("gas.mode", selection: $mode) {
                Text("gas.mode.flow").tag(0)
                Text("gas.mode.power").tag(1)
            }
            .pickerStyle(.segmented)

            InputCard {
                if mode == 0 {
                    NumberInputRow(title: "gas.flow", unit: "m³/h", value: $cubicMetersPerHour)
                } else {
                    NumberInputRow(title: "gas.desiredPower", unit: "kW", value: $desiredOutputKW)
                }
                Divider().overlay(AppTheme.line)
                NumberInputRow(title: "common.calorific", unit: "kWh/m³", value: $calorificValue)
                Divider().overlay(AppTheme.line)
                NumberInputRow(title: "common.efficiency", unit: "%", value: $efficiency)
            }

            VStack(spacing: 12) {
                if mode == 0 {
                    ResultCard(icon: "bolt.fill", title: "common.inputPower", value: forward.inputKW, unit: "kW")
                    ResultCard(icon: "flame.fill", title: "common.outputPower", value: forward.outputKW, unit: "kW")
                } else {
                    ResultCard(icon: "gauge.with.dots.needle.67percent", title: "gas.requiredFlow", value: reverse.cubicMetersPerHour, unit: "m³/h")
                    ResultCard(icon: "bolt.fill", title: "common.inputPower", value: reverse.inputKW, unit: "kW")
                }
            }

            FormulaNote(text: "gas.formulaNote")
        }
        .navigationTitle("calculator.gas.short")
    }
}

#Preview {
    NavigationStack { GasCalculatorView() }
}
