import Foundation

enum CalculatorEngine {
    static let oilReferencePressureBar = 7.0
    static let litersPerUSGallon = 3.785411784
    static let waterHeatCapacityFactor = 1.163

    struct OilResult {
        let actualGPH: Double
        let litersPerHour: Double
        let inputKW: Double
        let outputKW: Double
    }

    struct OilReverseResult {
        let requiredRatedGPH: Double
        let actualGPH: Double
        let litersPerHour: Double
        let inputKW: Double
    }

    struct GasResult {
        let inputKW: Double
        let outputKW: Double
    }

    struct GasReverseResult {
        let cubicMetersPerHour: Double
        let inputKW: Double
    }

    struct WaterResult {
        let litersPerHour: Double
        let litersPerMinute: Double
        let cubicMetersPerHour: Double
    }

    static func oil(
        ratedGPH: Double,
        pressureBar: Double,
        calorificValueKWhPerLiter: Double,
        efficiencyPercent: Double
    ) -> OilResult {
        let safePressure = max(0, pressureBar)
        let actualGPH = max(0, ratedGPH) * sqrt(safePressure / oilReferencePressureBar)
        let litersPerHour = actualGPH * litersPerUSGallon
        let inputKW = litersPerHour * max(0, calorificValueKWhPerLiter)
        let outputKW = inputKW * clampedEfficiency(efficiencyPercent)
        return OilResult(actualGPH: actualGPH, litersPerHour: litersPerHour, inputKW: inputKW, outputKW: outputKW)
    }

    static func oilReverse(
        desiredOutputKW: Double,
        pressureBar: Double,
        calorificValueKWhPerLiter: Double,
        efficiencyPercent: Double
    ) -> OilReverseResult {
        let efficiency = clampedEfficiency(efficiencyPercent)
        let calorificValue = max(0, calorificValueKWhPerLiter)
        let pressure = max(0, pressureBar)

        guard desiredOutputKW > 0, efficiency > 0, calorificValue > 0, pressure > 0 else {
            return OilReverseResult(requiredRatedGPH: 0, actualGPH: 0, litersPerHour: 0, inputKW: 0)
        }

        let inputKW = desiredOutputKW / efficiency
        let litersPerHour = inputKW / calorificValue
        let actualGPH = litersPerHour / litersPerUSGallon
        let requiredRatedGPH = actualGPH / sqrt(pressure / oilReferencePressureBar)

        return OilReverseResult(
            requiredRatedGPH: requiredRatedGPH,
            actualGPH: actualGPH,
            litersPerHour: litersPerHour,
            inputKW: inputKW
        )
    }

    static func gas(
        cubicMetersPerHour: Double,
        calorificValueKWhPerM3: Double,
        efficiencyPercent: Double
    ) -> GasResult {
        let inputKW = max(0, cubicMetersPerHour) * max(0, calorificValueKWhPerM3)
        return GasResult(inputKW: inputKW, outputKW: inputKW * clampedEfficiency(efficiencyPercent))
    }

    static func gasReverse(
        desiredOutputKW: Double,
        calorificValueKWhPerM3: Double,
        efficiencyPercent: Double
    ) -> GasReverseResult {
        let efficiency = clampedEfficiency(efficiencyPercent)
        let calorificValue = max(0, calorificValueKWhPerM3)
        guard desiredOutputKW > 0, efficiency > 0, calorificValue > 0 else {
            return GasReverseResult(cubicMetersPerHour: 0, inputKW: 0)
        }
        let inputKW = desiredOutputKW / efficiency
        return GasReverseResult(cubicMetersPerHour: inputKW / calorificValue, inputKW: inputKW)
    }

    static func water(powerKW: Double, deltaT: Double) -> WaterResult {
        guard powerKW > 0, deltaT > 0 else {
            return WaterResult(litersPerHour: 0, litersPerMinute: 0, cubicMetersPerHour: 0)
        }
        let litersPerHour = powerKW * 1000 / (waterHeatCapacityFactor * deltaT)
        return WaterResult(
            litersPerHour: litersPerHour,
            litersPerMinute: litersPerHour / 60,
            cubicMetersPerHour: litersPerHour / 1000
        )
    }

    private static func clampedEfficiency(_ percent: Double) -> Double {
        min(max(percent, 0), 120) / 100
    }
}
