package de.kamilunavo.brennercalc

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val billing = remember { BillingManager(applicationContext) }
            BrennerCalcRoot(activity = this, billing = billing)
        }
    }
}

enum class AppLanguage { DE, EN }
enum class CalculatorTab { OIL, GAS, WATER }

@Composable
private fun BrennerCalcRoot(activity: Activity, billing: BillingManager) {
    var language by remember { mutableStateOf(AppLanguage.DE) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = CalculatorTab.entries

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFFA726),
            secondary = Color(0xFF29B6F6),
            background = Color(0xFF111318),
            surface = Color(0xFF1A1D24),
        )
    ) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Header(language) { language = it }
                ScrollableTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(tabTitle(tab, language)) },
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (tabs[selectedTab]) {
                        CalculatorTab.OIL -> OilCalculator(language)
                        CalculatorTab.GAS -> if (billing.isPro) GasCalculator(language) else ProGate(language, activity, billing)
                        CalculatorTab.WATER -> if (billing.isPro) WaterCalculator(language) else ProGate(language, activity, billing)
                    }

                    Spacer(Modifier.height(8.dp))
                    if (billing.isPro) {
                        Text(
                            if (language == AppLanguage.DE) "BrennerCalc Pro aktiv" else "BrennerCalc Pro active",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        OutlinedButton(onClick = billing::restorePurchases, modifier = Modifier.fillMaxWidth()) {
                            Text(if (language == AppLanguage.DE) "Käufe wiederherstellen" else "Restore purchases")
                        }
                    }

                    billing.statusMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Text(
                        if (language == AppLanguage.DE)
                            "Rechenhilfe für Fachkräfte. Herstellerangaben, Normen und Messwerte haben Vorrang."
                        else
                            "Calculation aid for trained professionals. Manufacturer data, standards and measurements take precedence.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(language: AppLanguage, onLanguage: (AppLanguage) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text("BrennerCalc", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(
            if (language == AppLanguage.DE) "Schnelle SHK-Rechner" else "Fast calculators for HVAC pros",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            if (language == AppLanguage.DE) {
                Button(onClick = { onLanguage(AppLanguage.DE) }) { Text("DE") }
            } else {
                OutlinedButton(onClick = { onLanguage(AppLanguage.DE) }) { Text("DE") }
            }
            if (language == AppLanguage.EN) {
                Button(onClick = { onLanguage(AppLanguage.EN) }) { Text("EN") }
            } else {
                OutlinedButton(onClick = { onLanguage(AppLanguage.EN) }) { Text("EN") }
            }
        }
    }
}

private fun tabTitle(tab: CalculatorTab, language: AppLanguage): String = when (tab) {
    CalculatorTab.OIL -> if (language == AppLanguage.DE) "Ölbrenner" else "Oil burner"
    CalculatorTab.GAS -> "Gas"
    CalculatorTab.WATER -> if (language == AppLanguage.DE) "Heizwasser" else "Hydronics"
}

@Composable
private fun OilCalculator(language: AppLanguage) {
    var reverse by remember { mutableStateOf(false) }
    var ratedGph by remember { mutableStateOf("0,50") }
    var desiredKw by remember { mutableStateOf("20") }
    var pressure by remember { mutableStateOf("10") }
    var calorific by remember { mutableStateOf("10") }
    var efficiency by remember { mutableStateOf("90") }

    CalculatorCard(if (language == AppLanguage.DE) "Ölbrenner" else "Oil burner") {
        ModeButtons(
            firstSelected = !reverse,
            firstTitle = if (language == AppLanguage.DE) "Düse → Leistung" else "Nozzle → output",
            secondTitle = if (language == AppLanguage.DE) "Leistung → Düse" else "Output → nozzle",
            onFirst = { reverse = false },
            onSecond = { reverse = true },
        )

        if (reverse) {
            NumberField(if (language == AppLanguage.DE) "Gewünschte Leistung kW" else "Desired output kW", desiredKw) { desiredKw = it }
        } else {
            NumberField(if (language == AppLanguage.DE) "Düsengröße USgal/h @ 7 bar" else "Nozzle USgal/h @ 7 bar", ratedGph) { ratedGph = it }
        }
        NumberField(if (language == AppLanguage.DE) "Pumpendruck bar" else "Pump pressure bar", pressure) { pressure = it }
        NumberField(if (language == AppLanguage.DE) "Heizwert kWh/l" else "Calorific value kWh/l", calorific) { calorific = it }
        NumberField(if (language == AppLanguage.DE) "Wirkungsgrad %" else "Efficiency %", efficiency) { efficiency = it }

        if (reverse) {
            val result = CalculatorEngine.oilReverse(desiredKw.number(), pressure.number(), calorific.number(), efficiency.number())
            ResultLine(if (language == AppLanguage.DE) "Benötigte Düse" else "Required nozzle", "${fmt(result.requiredRatedGPH, 3)} USgal/h")
            ResultLine(if (language == AppLanguage.DE) "Tatsächlicher Durchsatz" else "Actual flow", "${fmt(result.actualGPH, 3)} USgal/h")
            ResultLine(if (language == AppLanguage.DE) "Ölmenge" else "Oil flow", "${fmt(result.litersPerHour)} l/h")
            ResultLine(if (language == AppLanguage.DE) "Feuerungsleistung" else "Input", "${fmt(result.inputKW)} kW")
        } else {
            val result = CalculatorEngine.oil(ratedGph.number(), pressure.number(), calorific.number(), efficiency.number())
            ResultLine(if (language == AppLanguage.DE) "Tatsächlicher Durchsatz" else "Actual flow", "${fmt(result.actualGPH, 3)} USgal/h")
            ResultLine(if (language == AppLanguage.DE) "Ölmenge" else "Oil flow", "${fmt(result.litersPerHour)} l/h")
            ResultLine(if (language == AppLanguage.DE) "Feuerungsleistung" else "Input", "${fmt(result.inputKW)} kW")
            ResultLine(if (language == AppLanguage.DE) "Nutzleistung" else "Output", "${fmt(result.outputKW)} kW")
        }
    }
}

@Composable
private fun GasCalculator(language: AppLanguage) {
    var reverse by remember { mutableStateOf(false) }
    var flow by remember { mutableStateOf("2,0") }
    var desiredKw by remember { mutableStateOf("20") }
    var calorific by remember { mutableStateOf("10,5") }
    var efficiency by remember { mutableStateOf("95") }

    CalculatorCard("Gas") {
        ModeButtons(
            firstSelected = !reverse,
            firstTitle = if (language == AppLanguage.DE) "Volumen → Leistung" else "Flow → output",
            secondTitle = if (language == AppLanguage.DE) "Leistung → Volumen" else "Output → flow",
            onFirst = { reverse = false },
            onSecond = { reverse = true },
        )

        if (reverse) {
            NumberField(if (language == AppLanguage.DE) "Gewünschte Leistung kW" else "Desired output kW", desiredKw) { desiredKw = it }
        } else {
            NumberField(if (language == AppLanguage.DE) "Gasvolumenstrom m³/h" else "Gas flow m³/h", flow) { flow = it }
        }
        NumberField(if (language == AppLanguage.DE) "Heizwert kWh/m³" else "Calorific value kWh/m³", calorific) { calorific = it }
        NumberField(if (language == AppLanguage.DE) "Wirkungsgrad %" else "Efficiency %", efficiency) { efficiency = it }

        if (reverse) {
            val result = CalculatorEngine.gasReverse(desiredKw.number(), calorific.number(), efficiency.number())
            ResultLine(if (language == AppLanguage.DE) "Benötigter Volumenstrom" else "Required flow", "${fmt(result.cubicMetersPerHour, 3)} m³/h")
            ResultLine(if (language == AppLanguage.DE) "Feuerungsleistung" else "Input", "${fmt(result.inputKW)} kW")
        } else {
            val result = CalculatorEngine.gas(flow.number(), calorific.number(), efficiency.number())
            ResultLine(if (language == AppLanguage.DE) "Feuerungsleistung" else "Input", "${fmt(result.inputKW)} kW")
            ResultLine(if (language == AppLanguage.DE) "Nutzleistung" else "Output", "${fmt(result.outputKW)} kW")
        }
    }
}

@Composable
private fun WaterCalculator(language: AppLanguage) {
    var power by remember { mutableStateOf("20") }
    var deltaT by remember { mutableStateOf("20") }
    val result = CalculatorEngine.water(power.number(), deltaT.number())

    CalculatorCard(if (language == AppLanguage.DE) "Heizwasser" else "Hydronics") {
        NumberField(if (language == AppLanguage.DE) "Leistung kW" else "Heat output kW", power) { power = it }
        NumberField(if (language == AppLanguage.DE) "Spreizung ΔT K" else "Temperature difference ΔT K", deltaT) { deltaT = it }
        ResultLine(if (language == AppLanguage.DE) "Volumenstrom" else "Flow", "${fmt(result.litersPerHour)} l/h")
        ResultLine(if (language == AppLanguage.DE) "Volumenstrom" else "Flow", "${fmt(result.litersPerMinute)} l/min")
        ResultLine(if (language == AppLanguage.DE) "Volumenstrom" else "Flow", "${fmt(result.cubicMetersPerHour, 3)} m³/h")
    }
}

@Composable
private fun ProGate(language: AppLanguage, activity: Activity, billing: BillingManager) {
    CalculatorCard("BrennerCalc Pro") {
        Text(
            if (language == AppLanguage.DE)
                "Gas- und Heizwasser-Rechner dauerhaft freischalten. Einmaliger Kauf, kein Abo."
            else
                "Permanently unlock gas and hydronic calculators. One-time purchase, no subscription."
        )
        Button(
            onClick = { billing.launchPurchase(activity) },
            modifier = Modifier.fillMaxWidth(),
            enabled = billing.billingReady,
        ) {
            val price = billing.productPrice?.let { " · $it" } ?: ""
            Text(if (language == AppLanguage.DE) "Pro freischalten$price" else "Unlock Pro$price")
        }
        OutlinedButton(onClick = billing::restorePurchases, modifier = Modifier.fillMaxWidth()) {
            Text(if (language == AppLanguage.DE) "Käufe wiederherstellen" else "Restore purchases")
        }
    }
}

@Composable
private fun ModeButtons(
    firstSelected: Boolean,
    firstTitle: String,
    secondTitle: String,
    onFirst: () -> Unit,
    onSecond: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (firstSelected) Button(onClick = onFirst, modifier = Modifier.fillMaxWidth()) { Text(firstTitle) }
        else OutlinedButton(onClick = onFirst, modifier = Modifier.fillMaxWidth()) { Text(firstTitle) }

        if (!firstSelected) Button(onClick = onSecond, modifier = Modifier.fillMaxWidth()) { Text(secondTitle) }
        else OutlinedButton(onClick = onSecond, modifier = Modifier.fillMaxWidth()) { Text(secondTitle) }
    }
}

@Composable
private fun CalculatorCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            content()
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { next ->
            if (next.all { it.isDigit() || it == ',' || it == '.' || it == '-' }) onValueChange(next)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ResultLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.LightGray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

private fun String.number(): Double = replace(',', '.').toDoubleOrNull() ?: 0.0
private fun fmt(value: Double, digits: Int = 2): String = String.format(Locale.GERMANY, "%.${digits}f", value)
