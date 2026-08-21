package de.kamilunavo.brennercalc

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object CalculatorEngine {
    const val OIL_REFERENCE_PRESSURE_BAR = 7.0
    const val LITERS_PER_US_GALLON = 3.785411784
    const val WATER_HEAT_CAPACITY_FACTOR = 1.163

    data class OilResult(
        val actualGPH: Double,
        val litersPerHour: Double,
        val inputKW: Double,
        val outputKW: Double,
    )

    data class OilReverseResult(
        val requiredRatedGPH: Double,
        val actualGPH: Double,
        val litersPerHour: Double,
        val inputKW: Double,
    )

    data class GasResult(val inputKW: Double, val outputKW: Double)
    data class GasReverseResult(val cubicMetersPerHour: Double, val inputKW: Double)

    data class WaterResult(
        val litersPerHour: Double,
        val litersPerMinute: Double,
        val cubicMetersPerHour: Double,
    )

    fun oil(
        ratedGPH: Double,
        pressureBar: Double,
        calorificValueKWhPerLiter: Double,
        efficiencyPercent: Double,
    ): OilResult {
        val safePressure = max(0.0, pressureBar)
        val actualGPH = max(0.0, ratedGPH) * sqrt(safePressure / OIL_REFERENCE_PRESSURE_BAR)
        val litersPerHour = actualGPH * LITERS_PER_US_GALLON
        val inputKW = litersPerHour * max(0.0, calorificValueKWhPerLiter)
        val outputKW = inputKW * clampedEfficiency(efficiencyPercent)
        return OilResult(actualGPH, litersPerHour, inputKW, outputKW)
    }

    fun oilReverse(
        desiredOutputKW: Double,
        pressureBar: Double,
        calorificValueKWhPerLiter: Double,
        efficiencyPercent: Double,
    ): OilReverseResult {
        val efficiency = clampedEfficiency(efficiencyPercent)
        val calorificValue = max(0.0, calorificValueKWhPerLiter)
        val pressure = max(0.0, pressureBar)
        if (desiredOutputKW <= 0 || efficiency <= 0 || calorificValue <= 0 || pressure <= 0) {
            return OilReverseResult(0.0, 0.0, 0.0, 0.0)
        }

        val inputKW = desiredOutputKW / efficiency
        val litersPerHour = inputKW / calorificValue
        val actualGPH = litersPerHour / LITERS_PER_US_GALLON
        val requiredRatedGPH = actualGPH / sqrt(pressure / OIL_REFERENCE_PRESSURE_BAR)
        return OilReverseResult(requiredRatedGPH, actualGPH, litersPerHour, inputKW)
    }

    fun gas(
        cubicMetersPerHour: Double,
        calorificValueKWhPerM3: Double,
        efficiencyPercent: Double,
    ): GasResult {
        val inputKW = max(0.0, cubicMetersPerHour) * max(0.0, calorificValueKWhPerM3)
        return GasResult(inputKW, inputKW * clampedEfficiency(efficiencyPercent))
    }

    fun gasReverse(
        desiredOutputKW: Double,
        calorificValueKWhPerM3: Double,
        efficiencyPercent: Double,
    ): GasReverseResult {
        val efficiency = clampedEfficiency(efficiencyPercent)
        val calorificValue = max(0.0, calorificValueKWhPerM3)
        if (desiredOutputKW <= 0 || efficiency <= 0 || calorificValue <= 0) {
            return GasReverseResult(0.0, 0.0)
        }
        val inputKW = desiredOutputKW / efficiency
        return GasReverseResult(inputKW / calorificValue, inputKW)
    }

    fun water(powerKW: Double, deltaT: Double): WaterResult {
        if (powerKW <= 0 || deltaT <= 0) return WaterResult(0.0, 0.0, 0.0)
        val litersPerHour = powerKW * 1000 / (WATER_HEAT_CAPACITY_FACTOR * deltaT)
        return WaterResult(
            litersPerHour = litersPerHour,
            litersPerMinute = litersPerHour / 60,
            cubicMetersPerHour = litersPerHour / 1000,
        )
    }

    private fun clampedEfficiency(percent: Double): Double = min(max(percent, 0.0), 120.0) / 100.0
}
