package de.kamilunavo.brennercalc

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {
    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 0.0001) {
        assertEquals(expected, actual, tolerance)
    }

    @Test
    fun oilAtReferencePressureKeepsRatedFlow() {
        val result = CalculatorEngine.oil(
            ratedGPH = 0.5,
            pressureBar = 7.0,
            calorificValueKWhPerLiter = 10.0,
            efficiencyPercent = 90.0,
        )
        assertClose(0.5, result.actualGPH)
        assertClose(1.892705892, result.litersPerHour)
        assertClose(17.034353028, result.outputKW)
    }

    @Test
    fun oilReverseRoundTripsOutput() {
        val forward = CalculatorEngine.oil(0.65, 12.0, 10.0, 92.0)
        val reverse = CalculatorEngine.oilReverse(forward.outputKW, 12.0, 10.0, 92.0)
        assertClose(0.65, reverse.requiredRatedGPH)
    }

    @Test
    fun gasCalculationMatchesFormula() {
        val result = CalculatorEngine.gas(2.0, 10.5, 95.0)
        assertClose(21.0, result.inputKW)
        assertClose(19.95, result.outputKW)
    }

    @Test
    fun hydronicFlowMatches1163Factor() {
        val result = CalculatorEngine.water(powerKW = 20.0, deltaT = 20.0)
        assertClose(859.8452278589854, result.litersPerHour)
        assertClose(14.330753797649757, result.litersPerMinute)
        assertClose(0.8598452278589854, result.cubicMetersPerHour)
    }

    @Test
    fun invalidReverseInputsReturnZero() {
        assertClose(0.0, CalculatorEngine.oilReverse(10.0, 0.0, 10.0, 90.0).requiredRatedGPH)
        assertClose(0.0, CalculatorEngine.gasReverse(10.0, 0.0, 90.0).cubicMetersPerHour)
        assertClose(0.0, CalculatorEngine.water(10.0, 0.0).litersPerHour)
    }
}
