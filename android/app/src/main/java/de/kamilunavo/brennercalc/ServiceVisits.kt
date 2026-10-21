package de.kamilunavo.brennercalc

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale
import java.util.UUID

enum class VisitKind(val wire: String, val keys: Set<String>) {
    OIL("oil", setOf("nozzle", "pressure", "calorific", "efficiency")),
    OIL_REVERSE("oilReverse", setOf("power", "pressure", "calorific", "efficiency")),
    GAS("gas", setOf("flow", "calorific", "efficiency")),
    GAS_REVERSE("gasReverse", setOf("power", "calorific", "efficiency")),
    WATER("water", setOf("power", "deltaT"));

    fun title(german: Boolean): String = when (this) {
        OIL -> if (german) "Öl · Düse → Leistung" else "Oil · nozzle → power"
        OIL_REVERSE -> if (german) "Öl · Leistung → Düse" else "Oil · power → nozzle"
        GAS -> if (german) "Gas · Volumenstrom → Leistung" else "Gas · flow → power"
        GAS_REVERSE -> if (german) "Gas · Leistung → Volumenstrom" else "Gas · power → flow"
        WATER -> if (german) "Heizwasser" else "Heating water"
    }
    fun unit(key: String): String = when (key) {
        "nozzle", "actualGPH" -> "USgal/h"
        "pressure" -> "bar"
        "efficiency" -> "%"
        "deltaT" -> "K"
        "calorific" -> if (this == OIL || this == OIL_REVERSE) "kWh/l" else "kWh/m³"
        "flow" -> if (this == GAS || this == GAS_REVERSE) "m³/h" else "l/h"
        "flowMinute" -> "l/min"
        "flowM3" -> "m³/h"
        else -> "kW"
    }
}

internal fun visitValueLabel(key: String, german: Boolean): String {
    val labels = mapOf(
        "nozzle" to ("Nenndüse bei 7 bar" to "Rated nozzle at 7 bar"), "actualGPH" to ("Tatsächlicher Düsendurchsatz" to "Actual nozzle flow"),
        "pressure" to ("Pumpendruck" to "Pump pressure"), "efficiency" to ("Wirkungsgrad" to "Efficiency"),
        "deltaT" to ("Temperaturdifferenz" to "Temperature difference"), "calorific" to ("Heizwert" to "Calorific value"),
        "flow" to ("Volumenstrom" to "Flow"), "flowMinute" to ("Volumenstrom / Minute" to "Flow / minute"), "flowM3" to ("Volumenstrom m³" to "Flow m³"),
        "input" to ("Eingangsleistung" to "Input power"), "output" to ("Nutzleistung" to "Output power"), "power" to ("Leistung" to "Power")
    )
    val pair = labels[key] ?: (key to key)
    return if (german) pair.first else pair.second
}
internal fun visitNumber(value: Double, german: Boolean): String = NumberFormat.getNumberInstance(if (german) Locale.GERMANY else Locale.UK).apply { maximumFractionDigits = 4 }.format(value)
internal fun visitDisclaimer(german: Boolean) = if (german) "Rechenhilfe aus eingegebenen Werten. Keine Zertifizierung oder Messbescheinigung. Herstellerangaben und fachgerechte Messungen haben Vorrang." else "Calculation aid from entered values. Not a certification or measurement certificate. Manufacturer specifications and professional measurements take precedence."

data class VisitSnapshot private constructor(
    val id: String, val kind: VisitKind, val inputs: Map<String, Double>, val results: Map<String, Double>,
    val note: String, val source: String, val date: Long,
) {
    companion object {
        fun capture(kind: VisitKind, inputs: Map<String, Double>, note: String, source: String, date: Long = System.currentTimeMillis()): VisitSnapshot {
            require(inputs.keys == kind.keys && inputs.values.all { it.isFinite() && it >= 0 })
            require((inputs["efficiency"] ?: 0.0) <= 120)
            if (kind == VisitKind.WATER) require(inputs.getValue("deltaT") > 0)
            if (kind == VisitKind.OIL_REVERSE || kind == VisitKind.GAS_REVERSE) {
                require(inputs.getValue("calorific") > 0 && inputs.getValue("efficiency") > 0)
                if (kind == VisitKind.OIL_REVERSE) require(inputs.getValue("pressure") > 0)
            }
            val result = when (kind) {
                VisitKind.OIL -> CalculatorEngine.oil(inputs.getValue("nozzle"), inputs.getValue("pressure"), inputs.getValue("calorific"), inputs.getValue("efficiency")).let {
                    mapOf("actualGPH" to it.actualGPH, "flow" to it.litersPerHour, "input" to it.inputKW, "output" to it.outputKW)
                }
                VisitKind.OIL_REVERSE -> CalculatorEngine.oilReverse(inputs.getValue("power"), inputs.getValue("pressure"), inputs.getValue("calorific"), inputs.getValue("efficiency")).let {
                    mapOf("nozzle" to it.requiredRatedGPH, "actualGPH" to it.actualGPH, "flow" to it.litersPerHour, "input" to it.inputKW)
                }
                VisitKind.GAS -> CalculatorEngine.gas(inputs.getValue("flow"), inputs.getValue("calorific"), inputs.getValue("efficiency")).let { mapOf("input" to it.inputKW, "output" to it.outputKW) }
                VisitKind.GAS_REVERSE -> CalculatorEngine.gasReverse(inputs.getValue("power"), inputs.getValue("calorific"), inputs.getValue("efficiency")).let { mapOf("flow" to it.cubicMetersPerHour, "input" to it.inputKW) }
                VisitKind.WATER -> CalculatorEngine.water(inputs.getValue("power"), inputs.getValue("deltaT")).let { mapOf("flow" to it.litersPerHour, "flowMinute" to it.litersPerMinute, "flowM3" to it.cubicMetersPerHour) }
            }
            require(result.values.all { it.isFinite() })
            return VisitSnapshot(UUID.randomUUID().toString(), kind, inputs.toMap(), result, note.trim(), source.trim(), date)
        }
        internal fun decode(json: JsonObject): VisitSnapshot {
            val kind = VisitKind.entries.first { it.wire == json.string("kind") }
            val snapshot = capture(kind, json.numbers("inputs"), json.string("note"), json.string("source"), json.long("date"))
            require(snapshot.results == json.numbers("results"))
            val id = json.string("id"); require(UUID.fromString(id).toString() == id)
            return snapshot.copy(id = id)
        }
    }
    fun difference(previous: VisitSnapshot): Map<String, Double>? = if (kind != previous.kind) null else results.mapValues { (key, value) -> value - previous.results.getValue(key) }
    internal fun validate() { require(UUID.fromString(id).toString() == id); require(capture(kind, inputs, note, source, date).results == results) }
    internal fun json(): JsonObject = JsonObject().apply {
        addProperty("id", id); addProperty("kind", kind.wire); addProperty("note", note); addProperty("source", source); addProperty("date", date)
        add("inputs", inputs.json()); add("results", results.json())
    }
}

data class ServiceProject(val id: String = UUID.randomUUID().toString(), val name: String, val notes: String, val visits: List<VisitSnapshot> = emptyList()) {
    fun report(language: String): String {
        val de = language.startsWith("de")
        fun l(german: String, english: String) = if (de) german else english
        fun values(data: Map<String, Double>, kind: VisitKind) = data.toSortedMap().map { (key, value) -> "${visitValueLabel(key, de)}: ${visitNumber(value, de)} ${kind.unit(key)}" }
        val lines = mutableListOf("BrennerCalc — ${l("Servicebesuche", "Service visits")}", name, notes)
        val ordered = visits.sortedBy { it.date }
        ordered.forEachIndexed { index, visit ->
            lines += listOf("", "${index + 1}. ${visit.kind.title(de)} · ${Instant.ofEpochMilli(visit.date)}", l("Eingegebene Werte:", "Entered inputs:"))
            lines += values(visit.inputs, visit.kind)
            lines += listOf("${l("Quelle", "Source")}: ${visit.source}", "${l("Notiz", "Note")}: ${visit.note}", l("Berechnete Ergebnisse:", "Calculated results:"))
            lines += values(visit.results, visit.kind)
            ordered.take(index).lastOrNull { it.kind == visit.kind }?.let { prior ->
                lines += "${l("Änderung gegenüber", "Change since")} ${Instant.ofEpochMilli(prior.date)}:"
                lines += values(visit.difference(prior)!!, visit.kind)
            }
        }
        lines += listOf("", visitDisclaimer(de)); return lines.joinToString("\n")
    }
    internal fun json() = JsonObject().apply {
        addProperty("id", id); addProperty("name", name); addProperty("notes", notes)
        add("visits", JsonArray().apply { visits.forEach { add(it.json()) } })
    }
}

class VisitArchive(val file: File) {
    fun load(): List<ServiceProject> {
        if (!file.exists()) return emptyList()
        val root = JsonParser.parseString(file.readText(Charsets.UTF_8))
        val array = if (root.isJsonArray) root.asJsonArray else root.asJsonObject.let {
            require(it.long("schemaVersion") == 1L) { "Unsupported archive version" }
            it.getAsJsonArray("projects") ?: error("Missing projects")
        }
        val projects = array.map { element ->
            val item = element.asJsonObject
            ServiceProject(item.string("id"), item.string("name"), item.string("notes"), item.getAsJsonArray("visits").map { VisitSnapshot.decode(it.asJsonObject) })
        }
        validate(projects); return projects
    }
    fun save(projects: List<ServiceProject>) {
        load(); validate(projects)
        val document = JsonObject().apply {
            addProperty("schemaVersion", 1)
            add("projects", JsonArray().apply { projects.forEach { add(it.json()) } })
        }
        val parent = file.absoluteFile.parentFile ?: error("Missing storage directory")
        require(parent.isDirectory || parent.mkdirs()) { "Cannot create storage directory" }
        val temporary = File.createTempFile("visits-", ".tmp", parent)
        try {
            FileOutputStream(temporary).use { stream -> stream.write(document.toString().toByteArray(Charsets.UTF_8)); stream.fd.sync() }
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally { temporary.delete() }
    }
    private fun validate(projects: List<ServiceProject>) {
        require(projects.map { it.id }.toSet().size == projects.size)
        projects.forEach { project ->
            require(UUID.fromString(project.id).toString() == project.id && project.name.isNotBlank())
            require(project.visits.map { it.id }.toSet().size == project.visits.size)
            project.visits.forEach { it.validate() }
        }
    }
}

class ServiceVisitStore(file: File) {
    private val archive = VisitArchive(file)
    var projects by mutableStateOf<List<ServiceProject>>(emptyList()); private set
    var loadFailed by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null)
    init { reload() }
    fun reload() {
        try { projects = archive.load(); loadFailed = false; error = null }
        catch (_: Exception) { loadFailed = true; error = "load" }
    }
    fun commit(updated: List<ServiceProject>): Boolean {
        if (loadFailed) { error = "load"; return false }
        return try { archive.save(updated); projects = updated.toList(); true }
        catch (_: Exception) { error = "save"; false }
    }
}

private fun Map<String, Double>.json() = JsonObject().also { objectValue -> forEach { (key, value) -> objectValue.addProperty(key, value) } }
private fun JsonObject.string(key: String): String {
    val item = get(key); require(item != null && item.isJsonPrimitive && item.asJsonPrimitive.isString)
    return item.asString
}
private fun JsonObject.long(key: String): Long {
    val item = get(key); require(item != null && item.isJsonPrimitive && item.asJsonPrimitive.isNumber)
    return item.asBigDecimal.longValueExact()
}
private fun JsonObject.numbers(key: String): Map<String, Double> {
    val item = getAsJsonObject(key) ?: error("Missing values")
    return item.entrySet().associate { (name, value) ->
        require(value.isJsonPrimitive && value.asJsonPrimitive.isNumber)
        name to value.asDouble.also { require(it.isFinite()) }
    }
}
