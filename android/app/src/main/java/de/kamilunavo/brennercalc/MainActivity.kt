package de.kamilunavo.brennercalc

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val billing = remember { BillingManager(applicationContext) }
            BrennerCalcRoot(activity = this, billing = billing)
        }
    }
}

private enum class BrennerScreen { HOME, OIL, GAS, WATER, PRO }

@Composable
private fun BrennerCalcRoot(activity: Activity, billing: BillingManager) {
    var language by remember { mutableStateOf(AppLanguage.DE) }
    var screen by remember { mutableStateOf(BrennerScreen.HOME) }

    BackHandler(enabled = screen != BrennerScreen.HOME) { screen = BrennerScreen.HOME }

    BrennerTheme {
        Scaffold(
            containerColor = Navy950,
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0C1B2E), Navy950, Color(0xFF050C16)),
                        ),
                    )
                    .padding(padding),
            ) {
                Column(Modifier.fillMaxSize()) {
                    BrandBar(language, billing.isPro) { language = it }
                    when (screen) {
                        BrennerScreen.HOME -> HomeScreen(language, billing) { destination ->
                            screen = when {
                                destination == BrennerScreen.OIL -> BrennerScreen.OIL
                                destination == BrennerScreen.GAS && billing.isPro -> BrennerScreen.GAS
                                destination == BrennerScreen.WATER && billing.isPro -> BrennerScreen.WATER
                                else -> BrennerScreen.PRO
                            }
                        }
                        BrennerScreen.OIL -> OilCalculator(language) { screen = BrennerScreen.HOME }
                        BrennerScreen.GAS -> GasCalculator(language) { screen = BrennerScreen.HOME }
                        BrennerScreen.WATER -> WaterCalculator(language) { screen = BrennerScreen.HOME }
                        BrennerScreen.PRO -> ProGate(language, activity, billing) { screen = BrennerScreen.HOME }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    language: AppLanguage,
    billing: BillingManager,
    onOpen: (BrennerScreen) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        HeroPanel(language)
        SectionEyebrow(if (language == AppLanguage.DE) "RECHNER" else "CALCULATORS")
        ToolCard(
            code = "ÖL",
            title = if (language == AppLanguage.DE) "Ölbrenner" else "Oil burner",
            subtitle = if (language == AppLanguage.DE)
                "Düse, Pumpendruck und Leistung sicher zusammenführen."
            else
                "Connect nozzle size, pump pressure and output.",
            accent = Flame,
            onClick = { onOpen(BrennerScreen.OIL) },
        )
        ToolCard(
            code = "GAS",
            title = if (language == AppLanguage.DE) "Gasleistung" else "Gas output",
            subtitle = if (language == AppLanguage.DE)
                "Volumenstrom und Heizwert in kW umrechnen."
            else
                "Convert gas flow and calorific value to kW.",
            accent = FlameSoft,
            locked = !billing.isPro,
            onClick = { onOpen(BrennerScreen.GAS) },
        )
        ToolCard(
            code = "H₂O",
            title = if (language == AppLanguage.DE) "Heizwasser" else "Hydronics",
            subtitle = if (language == AppLanguage.DE)
                "Volumenstrom aus Leistung und Spreizung bestimmen."
            else
                "Calculate flow from output and temperature spread.",
            accent = Sky,
            locked = !billing.isPro,
            onClick = { onOpen(BrennerScreen.WATER) },
        )

        if (!billing.isPro) {
            ProStrip(language, billing.productPrice) { onOpen(BrennerScreen.PRO) }
            TextButton(
                onClick = billing::restorePurchases,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.textButtonColors(contentColor = Ink300),
            ) {
                Text(if (language == AppLanguage.DE) "Käufe wiederherstellen" else "Restore purchases")
            }
        }

        billing.statusMessage?.let { StatusBanner(it) }
        NoteCard(
            if (language == AppLanguage.DE)
                "Rechenhilfe für Fachkräfte. Herstellerangaben, Normen, Messwerte und die Verbrennungsanalyse haben immer Vorrang."
            else
                "Calculation aid for trained professionals. Manufacturer data, standards, measurements and combustion analysis always take precedence.",
        )
    }
}

@Composable
private fun OilCalculator(language: AppLanguage, onBack: () -> Unit) {
    var reverse by remember { mutableStateOf(false) }
    var ratedGph by remember { mutableStateOf("0,50") }
    var desiredKw by remember { mutableStateOf("20") }
    var pressure by remember { mutableStateOf("10") }
    var calorific by remember { mutableStateOf("10") }
    var efficiency by remember { mutableStateOf("90") }

    ToolScreen(
        code = "ÖL",
        accent = Flame,
        title = if (language == AppLanguage.DE) "Ölbrenner" else "Oil burner",
        subtitle = if (language == AppLanguage.DE)
            "Düsengröße und Leistung bei realem Pumpendruck."
        else
            "Nozzle size and output at the actual pump pressure.",
        backLabel = if (language == AppLanguage.DE) "Rechner" else "Calculators",
        onBack = onBack,
    ) {
        InputCard(if (language == AppLanguage.DE) "Berechnungsweg" else "Calculation mode") {
            TwoWaySelector(
                firstSelected = !reverse,
                firstTitle = if (language == AppLanguage.DE) "Düse → kW" else "Nozzle → kW",
                secondTitle = if (language == AppLanguage.DE) "kW → Düse" else "kW → nozzle",
                onFirst = { reverse = false },
                onSecond = { reverse = true },
            )
        }
        InputCard(if (language == AppLanguage.DE) "Eingabewerte" else "Input values") {
            if (reverse) {
                NumberField(if (language == AppLanguage.DE) "Gewünschte Nutzleistung" else "Desired output", "kW", desiredKw) { desiredKw = it }
            } else {
                NumberField(if (language == AppLanguage.DE) "Düsengröße bei 7 bar" else "Nozzle rating at 7 bar", "USgal/h", ratedGph) { ratedGph = it }
            }
            NumberField(if (language == AppLanguage.DE) "Pumpendruck" else "Pump pressure", "bar", pressure) { pressure = it }
            NumberField(if (language == AppLanguage.DE) "Heizwert" else "Calorific value", "kWh/l", calorific) { calorific = it }
            NumberField(if (language == AppLanguage.DE) "Wirkungsgrad" else "Efficiency", "%", efficiency) { efficiency = it }
        }

        if (reverse) {
            val result = CalculatorEngine.oilReverse(desiredKw.number(), pressure.number(), calorific.number(), efficiency.number())
            ResultPanel(
                eyebrow = if (language == AppLanguage.DE) "EMPFOHLENE DÜSE" else "REQUIRED NOZZLE",
                primaryValue = fmt(result.requiredRatedGPH, 3),
                primaryUnit = "USgal/h",
                metrics = listOf(
                    Metric(if (language == AppLanguage.DE) "Tatsächlicher Durchsatz" else "Actual flow", fmt(result.actualGPH, 3), "USgal/h"),
                    Metric(if (language == AppLanguage.DE) "Ölmenge" else "Oil flow", fmt(result.litersPerHour), "l/h"),
                    Metric(if (language == AppLanguage.DE) "Feuerungsleistung" else "Input", fmt(result.inputKW), "kW"),
                ),
                accent = Flame,
            )
        } else {
            val result = CalculatorEngine.oil(ratedGph.number(), pressure.number(), calorific.number(), efficiency.number())
            ResultPanel(
                eyebrow = if (language == AppLanguage.DE) "NUTZLEISTUNG" else "OUTPUT",
                primaryValue = fmt(result.outputKW),
                primaryUnit = "kW",
                metrics = listOf(
                    Metric(if (language == AppLanguage.DE) "Feuerungsleistung" else "Input", fmt(result.inputKW), "kW"),
                    Metric(if (language == AppLanguage.DE) "Ölmenge" else "Oil flow", fmt(result.litersPerHour), "l/h"),
                    Metric(if (language == AppLanguage.DE) "Tatsächlicher Durchsatz" else "Actual flow", fmt(result.actualGPH, 3), "USgal/h"),
                ),
                accent = Flame,
            )
        }
        NoteCard(
            if (language == AppLanguage.DE)
                "Düsenangaben beziehen sich auf 7 bar Referenzdruck. Der reale Durchsatz wird mit dem eingestellten Pumpendruck korrigiert."
            else
                "Nozzle ratings use a 7 bar reference pressure. Actual flow is corrected using the selected pump pressure.",
        )
    }
}

@Composable
private fun GasCalculator(language: AppLanguage, onBack: () -> Unit) {
    var reverse by remember { mutableStateOf(false) }
    var flow by remember { mutableStateOf("2,0") }
    var desiredKw by remember { mutableStateOf("20") }
    var calorific by remember { mutableStateOf("10,5") }
    var efficiency by remember { mutableStateOf("95") }

    ToolScreen(
        code = "GAS",
        accent = FlameSoft,
        title = if (language == AppLanguage.DE) "Gasleistung" else "Gas output",
        subtitle = if (language == AppLanguage.DE) "Gasvolumenstrom und Brennerleistung direkt umrechnen." else "Convert gas flow and burner output directly.",
        backLabel = if (language == AppLanguage.DE) "Rechner" else "Calculators",
        onBack = onBack,
    ) {
        InputCard(if (language == AppLanguage.DE) "Berechnungsweg" else "Calculation mode") {
            TwoWaySelector(!reverse, "m³/h → kW", "kW → m³/h", { reverse = false }, { reverse = true })
        }
        InputCard(if (language == AppLanguage.DE) "Eingabewerte" else "Input values") {
            if (reverse) {
                NumberField(if (language == AppLanguage.DE) "Gewünschte Nutzleistung" else "Desired output", "kW", desiredKw) { desiredKw = it }
            } else {
                NumberField(if (language == AppLanguage.DE) "Gasvolumenstrom" else "Gas flow", "m³/h", flow) { flow = it }
            }
            NumberField(if (language == AppLanguage.DE) "Heizwert" else "Calorific value", "kWh/m³", calorific) { calorific = it }
            NumberField(if (language == AppLanguage.DE) "Wirkungsgrad" else "Efficiency", "%", efficiency) { efficiency = it }
        }
        if (reverse) {
            val result = CalculatorEngine.gasReverse(desiredKw.number(), calorific.number(), efficiency.number())
            ResultPanel(
                if (language == AppLanguage.DE) "BENÖTIGTER VOLUMENSTROM" else "REQUIRED FLOW",
                fmt(result.cubicMetersPerHour, 3),
                "m³/h",
                listOf(Metric(if (language == AppLanguage.DE) "Feuerungsleistung" else "Input", fmt(result.inputKW), "kW")),
                FlameSoft,
            )
        } else {
            val result = CalculatorEngine.gas(flow.number(), calorific.number(), efficiency.number())
            ResultPanel(
                if (language == AppLanguage.DE) "NUTZLEISTUNG" else "OUTPUT",
                fmt(result.outputKW),
                "kW",
                listOf(Metric(if (language == AppLanguage.DE) "Feuerungsleistung" else "Input", fmt(result.inputKW), "kW")),
                FlameSoft,
            )
        }
    }
}

@Composable
private fun WaterCalculator(language: AppLanguage, onBack: () -> Unit) {
    var power by remember { mutableStateOf("20") }
    var deltaT by remember { mutableStateOf("20") }
    val result = CalculatorEngine.water(power.number(), deltaT.number())

    ToolScreen(
        code = "H₂O",
        accent = Sky,
        title = if (language == AppLanguage.DE) "Heizwasser" else "Hydronics",
        subtitle = if (language == AppLanguage.DE) "Volumenstrom aus Leistung und Spreizung." else "Flow from heat output and temperature spread.",
        backLabel = if (language == AppLanguage.DE) "Rechner" else "Calculators",
        onBack = onBack,
    ) {
        InputCard(if (language == AppLanguage.DE) "Eingabewerte" else "Input values") {
            NumberField(if (language == AppLanguage.DE) "Leistung" else "Heat output", "kW", power) { power = it }
            NumberField(if (language == AppLanguage.DE) "Spreizung" else "Temperature difference", "ΔT K", deltaT) { deltaT = it }
        }
        ResultPanel(
            eyebrow = if (language == AppLanguage.DE) "VOLUMENSTROM" else "FLOW",
            primaryValue = fmt(result.litersPerHour),
            primaryUnit = "l/h",
            metrics = listOf(
                Metric(if (language == AppLanguage.DE) "Pro Minute" else "Per minute", fmt(result.litersPerMinute), "l/min"),
                Metric(if (language == AppLanguage.DE) "Pro Stunde" else "Per hour", fmt(result.cubicMetersPerHour, 3), "m³/h"),
            ),
            accent = Sky,
        )
        NoteCard(if (language == AppLanguage.DE) "Berechnung für Wasser mit dem Faktor 1,163." else "Calculation for water using the factor 1.163.")
    }
}

@Composable
private fun ProGate(language: AppLanguage, activity: Activity, billing: BillingManager, onBack: () -> Unit) {
    ToolScreen(
        code = "PRO",
        accent = Flame,
        title = "BrennerCalc Pro",
        subtitle = if (language == AppLanguage.DE) "Mehr Werkzeuge. Einmal kaufen. Dauerhaft nutzen." else "More tools. One purchase. Yours permanently.",
        backLabel = if (language == AppLanguage.DE) "Rechner" else "Calculators",
        onBack = onBack,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF3A2415), Color(0xFF16263A))), RoundedCornerShape(26.dp))
                .border(1.dp, Flame.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
                .padding(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionEyebrow(if (language == AppLanguage.DE) "DAUERHAFTE FREISCHALTUNG" else "PERMANENT UNLOCK")
                Text(
                    if (language == AppLanguage.DE) "Gas und Heizwasser im selben Profi-Workflow." else "Gas and hydronics in the same professional workflow.",
                    style = MaterialTheme.typography.headlineMedium,
                )
                FeatureLine(if (language == AppLanguage.DE) "Gasleistung vorwärts und rückwärts" else "Forward and reverse gas calculations")
                FeatureLine(if (language == AppLanguage.DE) "Heizwasser-Volumenstrom in drei Einheiten" else "Hydronic flow in three units")
                FeatureLine(if (language == AppLanguage.DE) "Kein Abo und kein Konto" else "No subscription and no account")
                PrimaryAction(
                    label = if (language == AppLanguage.DE)
                        "Pro freischalten${billing.productPrice?.let { " · $it" } ?: ""}"
                    else
                        "Unlock Pro${billing.productPrice?.let { " · $it" } ?: ""}",
                    enabled = billing.billingReady,
                ) { billing.launchPurchase(activity) }
                OutlinedButton(
                    onClick = billing::restorePurchases,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, Hairline),
                ) {
                    Text(if (language == AppLanguage.DE) "Käufe wiederherstellen" else "Restore purchases")
                }
            }
        }
        billing.statusMessage?.let { StatusBanner(it) }
    }
}

@Composable
private fun ToolScreen(
    code: String,
    accent: Color,
    title: String,
    subtitle: String,
    backLabel: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TextButton(onClick = onBack, colors = ButtonDefaults.textButtonColors(contentColor = Ink300)) {
            Text("←  $backLabel")
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CodeTile(code, accent)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.headlineMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Ink300)
            }
        }
        Spacer(Modifier.height(2.dp))
        content()
    }
}

@Composable
private fun ProStrip(language: AppLanguage, price: String?, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Flame.copy(alpha = 0.10f),
        shape = RoundedCornerShape(19.dp),
        border = BorderStroke(1.dp, Flame.copy(alpha = 0.30f)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusPill("PRO", Flame)
            Column(Modifier.weight(1f)) {
                Text(if (language == AppLanguage.DE) "Alle Profi-Rechner" else "All professional calculators", fontWeight = FontWeight.Bold)
                Text(
                    if (language == AppLanguage.DE) "Einmalkauf${price?.let { " · $it" } ?: ""}" else "One-time purchase${price?.let { " · $it" } ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink300,
                )
            }
            Text("›", color = Flame, fontSize = 26.sp)
        }
    }
}

@Composable
private fun FeatureLine(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(Flame, CircleShape))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Ink300)
    }
}

private fun String.number(): Double = replace(',', '.').toDoubleOrNull() ?: 0.0
private fun fmt(value: Double, digits: Int = 2): String = String.format(Locale.GERMANY, "%.${digits}f", value)
