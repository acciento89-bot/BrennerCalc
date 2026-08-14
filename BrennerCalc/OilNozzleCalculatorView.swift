import SwiftUI

struct OilNozzleCalculatorView: View {
    @State private var mode = 0
    @State private var nozzleGPH = 0.50
    @State private var desiredOutputKW = 20.0
    @State private var pressureBar = 10.0
    @State private var calorificValue = 10.0
    @State private var efficiency = 90.0

    private var forward: CalculatorEngine.OilResult {
        CalculatorEngine.oil(
            ratedGPH: nozzleGPH,
            pressureBar: pressureBar,
            calorificValueKWhPerLiter: calorificValue,
            efficiencyPercent: efficiency
        )
    }

    private var reverse: CalculatorEngine.OilReverseResult {
        CalculatorEngine.oilReverse(
            desiredOutputKW: desiredOutputKW,
            pressureBar: pressureBar,
            calorificValueKWhPerLiter: calorificValue,
            efficiencyPercent: efficiency
        )
    }

    var body: some View {
        CalculatorScreen(title: "calculator.oil.title", subtitle: "oil.lead") {
            Picker("oil.mode", selection: $mode) {
                Text("oil.mode.nozzle").tag(0)
                Text("oil.mode.power").tag(1)
            }
            .pickerStyle(.segmented)

            InputCard {
                if mode == 0 {
                    NumberInputRow(title: "oil.nozzle", unit: "USgal/h", value: $nozzleGPH)
                } else {
                    NumberInputRow(title: "oil.desiredPower", unit: "kW", value: $desiredOutputKW)
                }
                Divider().overlay(AppTheme.line)
                NumberInputRow(title: "oil.pressure", unit: "bar", value: $pressureBar)
                Divider().overlay(AppTheme.line)
                NumberInputRow(title: "common.calorific", unit: "kWh/l", value: $calorificValue)
                Divider().overlay(AppTheme.line)
                NumberInputRow(title: "common.efficiency", unit: "%", value: $efficiency)
            }

            VStack(spacing: 12) {
                if mode == 0 {
                    ResultCard(icon: "fuelpump.fill", title: "oil.actualFlowGPH", value: forward.actualGPH, unit: "USgal/h")
                    ResultCard(icon: "drop.fill", title: "oil.actualFlowL", value: forward.litersPerHour, unit: "l/h")
                    ResultCard(icon: "bolt.fill", title: "common.inputPower", value: forward.inputKW, unit: "kW")
                    ResultCard(icon: "flame.fill", title: "common.outputPower", value: forward.outputKW, unit: "kW")
                } else {
                    ResultCard(icon: "scope", title: "oil.requiredNozzle", value: reverse.requiredRatedGPH, unit: "USgal/h", digits: 2...3)
                    ResultCard(icon: "fuelpump.fill", title: "oil.actualFlowGPH", value: reverse.actualGPH, unit: "USgal/h")
                    ResultCard(icon: "drop.fill", title: "oil.actualFlowL", value: reverse.litersPerHour, unit: "l/h")
                    ResultCard(icon: "bolt.fill", title: "common.inputPower", value: reverse.inputKW, unit: "kW")
                }
            }

            FormulaNote(text: "oil.formulaNote")
        }
        .navigationTitle("calculator.oil.short")
    }
}

#Preview {
    NavigationStack { OilNozzleCalculatorView() }
}
