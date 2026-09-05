package de.kamilunavo.brennercalc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppLanguage { DE, EN }

internal val Navy950 = Color(0xFF07111F)
internal val Navy900 = Color(0xFF0A1728)
internal val Navy850 = Color(0xFF0E1E32)
internal val Navy800 = Color(0xFF12253D)
internal val Ink100 = Color(0xFFF6F8FC)
internal val Ink300 = Color(0xFFA9B7C9)
internal val Ink500 = Color(0xFF6E8199)
internal val Flame = Color(0xFFFF7A1A)
internal val FlameSoft = Color(0xFFFFB23F)
internal val Sky = Color(0xFF4DB6FF)
internal val Hairline = Color(0xFF243A55)

private val BrennerColors = darkColorScheme(
    primary = Flame,
    onPrimary = Color(0xFF321300),
    secondary = Sky,
    background = Navy950,
    onBackground = Ink100,
    surface = Navy850,
    onSurface = Ink100,
    surfaceVariant = Navy800,
    onSurfaceVariant = Ink300,
    outline = Hairline,
    error = Color(0xFFFF7272),
)

private val BrennerTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.7).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
)

@Composable
internal fun BrennerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = BrennerColors, typography = BrennerTypography, content = content)
}

@Composable
internal fun BrandBar(language: AppLanguage, isPro: Boolean, onLanguage: (AppLanguage) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        BrandMark("BC")
        Column(Modifier.weight(1f)) {
            Text("BrennerCalc", style = MaterialTheme.typography.titleMedium)
            Text(
                if (language == AppLanguage.DE) "Kamilunavo · SHK Tools" else "Kamilunavo · HVAC Tools",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink500,
            )
        }
        if (isPro) StatusPill("PRO", Flame)
        LanguageSwitch(language, onLanguage)
    }
}

@Composable
internal fun HeroPanel(language: AppLanguage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(listOf(Color(0xFF1B3550), Color(0xFF10233A))),
                shape = RoundedCornerShape(28.dp),
            )
            .border(BorderStroke(1.dp, Color(0xFF2C4865)), RoundedCornerShape(28.dp))
            .padding(22.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
            SectionEyebrow(if (language == AppLanguage.DE) "FÜR DEN EINSATZ VOR ORT" else "BUILT FOR FIELD WORK")
            Text(
                if (language == AppLanguage.DE) "Brennerwerte.\nDirekt im Einsatz." else "Burner values.\nReady on site.",
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                if (language == AppLanguage.DE)
                    "Schnelle, nachvollziehbare Berechnungen ohne Konto, Werbung oder Ablenkung."
                else
                    "Fast, transparent calculations without an account, ads or distractions.",
                style = MaterialTheme.typography.bodyLarge,
                color = Ink300,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip("Offline")
                InfoChip("DE / EN")
                InfoChip(if (language == AppLanguage.DE) "Kein Abo" else "No subscription")
            }
        }
    }
}

@Composable
internal fun ToolCard(
    code: String,
    title: String,
    subtitle: String,
    accent: Color,
    locked: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2034)),
        border = BorderStroke(1.dp, Hairline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            CodeTile(code, accent)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    if (locked) StatusPill("PRO", accent)
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink300,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(if (locked) "+" else "›", fontSize = 27.sp, color = if (locked) accent else Ink500)
        }
    }
}

@Composable
internal fun InputCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2034)),
        border = BorderStroke(1.dp, Hairline),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(color = Hairline)
            content()
        }
    }
}

@Composable
internal fun NumberField(label: String, unit: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink300)
        OutlinedTextField(
            value = value,
            onValueChange = { next ->
                if (next.all { it.isDigit() || it == ',' || it == '.' || it == '-' }) onValueChange(next)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text(unit, color = Ink300, fontWeight = FontWeight.SemiBold) },
            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Flame,
                unfocusedBorderColor = Hairline,
                focusedContainerColor = Navy900,
                unfocusedContainerColor = Navy900,
                cursorColor = Flame,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

internal data class Metric(val label: String, val value: String, val unit: String)

@Composable
internal fun ResultPanel(
    eyebrow: String,
    primaryValue: String,
    primaryUnit: String,
    metrics: List<Metric>,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF122943)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.42f)),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            SectionEyebrow(eyebrow, accent)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(primaryValue, fontSize = 40.sp, lineHeight = 42.sp, fontWeight = FontWeight.Black)
                Text(primaryUnit, style = MaterialTheme.typography.titleMedium, color = accent, modifier = Modifier.padding(bottom = 4.dp))
            }
            if (metrics.isNotEmpty()) HorizontalDivider(color = Hairline)
            metrics.forEach { metric ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(metric.label, style = MaterialTheme.typography.bodyMedium, color = Ink300, modifier = Modifier.weight(1f))
                    Text(metric.value, style = MaterialTheme.typography.titleMedium)
                    Text("  ${metric.unit}", style = MaterialTheme.typography.bodyMedium, color = Ink500)
                }
            }
        }
    }
}

@Composable
internal fun TwoWaySelector(
    firstSelected: Boolean,
    firstTitle: String,
    secondTitle: String,
    onFirst: () -> Unit,
    onSecond: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Navy900, RoundedCornerShape(15.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Segment(firstTitle, firstSelected, onFirst, Modifier.weight(1f))
        Segment(secondTitle, !firstSelected, onSecond, Modifier.weight(1f))
    }
}

@Composable
internal fun Segment(title: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Flame else Color.Transparent,
        contentColor = if (selected) Color(0xFF2C1000) else Ink300,
    ) {
        Box(Modifier.padding(horizontal = 8.dp, vertical = 11.dp), contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

@Composable
internal fun LanguageSwitch(language: AppLanguage, onLanguage: (AppLanguage) -> Unit) {
    Row(
        modifier = Modifier.background(Navy850, RoundedCornerShape(11.dp)).border(1.dp, Hairline, RoundedCornerShape(11.dp)).padding(3.dp),
    ) {
        LanguageOption("DE", language == AppLanguage.DE) { onLanguage(AppLanguage.DE) }
        LanguageOption("EN", language == AppLanguage.EN) { onLanguage(AppLanguage.EN) }
    }
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, color = if (selected) Flame else Color.Transparent, shape = RoundedCornerShape(8.dp)) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            color = if (selected) Color(0xFF2C1000) else Ink300,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
        )
    }
}

@Composable
internal fun CodeTile(label: String, accent: Color) {
    Box(
        modifier = Modifier.size(54.dp).background(accent.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = accent, fontSize = if (label.length > 3) 11.sp else 15.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun BrandMark(label: String) {
    Box(
        modifier = Modifier.size(42.dp).background(Brush.linearGradient(listOf(Flame, FlameSoft)), RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF2C1000))
    }
}

@Composable
internal fun StatusPill(label: String, accent: Color) {
    Surface(color = accent.copy(alpha = 0.14f), shape = CircleShape, border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))) {
        Text(label, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
private fun InfoChip(label: String) {
    Surface(color = Color.White.copy(alpha = 0.055f), shape = CircleShape, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))) {
        Text(label, color = Ink300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
internal fun SectionEyebrow(text: String, accent: Color = Flame) {
    Text(text, color = accent, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.2.sp)
}

@Composable
internal fun NoteCard(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.035f), RoundedCornerShape(17.dp))
            .border(1.dp, Hairline.copy(alpha = 0.75f), RoundedCornerShape(17.dp)).padding(15.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("i", color = Sky, fontWeight = FontWeight.Black, modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Ink300, modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun StatusBanner(text: String) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f), shape = RoundedCornerShape(15.dp)) {
        Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(13.dp))
    }
}

@Composable
internal fun PrimaryAction(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Flame, contentColor = Color(0xFF2C1000)),
    ) {
        Text(label)
    }
}
