package de.kamilunavo.brennercalc

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.Instant

private fun AppLanguage.visitText(de: String, en: String) = if (this == AppLanguage.DE) de else en
private fun visitError(error: String, language: AppLanguage) = when (error) {
    "load" -> language.visitText("Gespeicherte Besuche konnten nicht geladen werden. Die Datei bleibt erhalten. Änderungen sind gesperrt, bis die Daten wieder geladen werden können.", "Saved visits could not be loaded. The file is preserved. Changes are blocked until the data can be loaded again.")
    "share" -> language.visitText("Das Protokoll konnte nicht geteilt werden. Bitte erneut versuchen.", "The record could not be shared. Please try again.")
    else -> language.visitText("Die Änderung konnte nicht gespeichert werden. Ihre bisherigen Daten bleiben erhalten. Bitte erneut versuchen.", "The change could not be saved. Your existing data is preserved. Please try again.")
}

@Composable
private fun VisitValues(values: Map<String, Double>, kind: VisitKind, language: AppLanguage) {
    values.toSortedMap().forEach { (key, value) ->
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(visitValueLabel(key, language == AppLanguage.DE), style = MaterialTheme.typography.bodySmall, color = Ink300)
            Text("${visitNumber(value, language == AppLanguage.DE)} ${kind.unit(key)}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
internal fun ServiceProjectsScreen(language: AppLanguage, store: ServiceVisitStore, onBack: () -> Unit) {
    var selectedID by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var deletingProject by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingVisit by rememberSaveable { mutableStateOf<String?>(null) }
    val project = store.projects.firstOrNull { it.id == selectedID }
    val context = LocalContext.current
    BackHandler(enabled = selectedID != null) { selectedID = null }
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TextButton(onClick = { if (selectedID != null) selectedID = null else onBack() }) {
            Text("← " + language.visitText(if (selectedID != null) "Anlagen" else "Rechner", if (selectedID != null) "Appliances" else "Calculators"))
        }
        Text(language.visitText("Anlagen & Servicebesuche", "Appliances & service visits"), style = MaterialTheme.typography.headlineMedium)
        if (store.loadFailed) StatusBanner(visitError("load", language))
        else store.error?.let { StatusBanner(visitError(it, language)) }
        if (store.loadFailed) {
            TextButton(onClick = store::reload) { Text(language.visitText("Erneut laden", "Retry loading")) }
        }
        if (project == null) {
            PrimaryAction(language.visitText("Neue Anlage", "New appliance"), enabled = !store.loadFailed) { store.error = null; creating = true }
            if (store.projects.isEmpty() && !store.loadFailed) {
                NoteCard(language.visitText("Noch keine Anlagen. Legen Sie eine Anlage an und speichern Sie Berechnungen als Servicebesuche.", "No appliances yet. Create an appliance and save calculations as service visits."))
            }
            store.projects.forEach { item ->
                InputCard(item.name) {
                    if (item.notes.isNotBlank()) Text(item.notes)
                    Text("${language.visitText("Besuche", "Visits")}: ${item.visits.size}")
                    TextButton(onClick = { selectedID = item.id }) { Text(language.visitText("Besuchshistorie öffnen", "Open visit history")) }
                    TextButton(onClick = { deletingProject = item.id }) { Text(language.visitText("Anlage löschen", "Delete appliance"), color = MaterialTheme.colorScheme.error) }
                }
            }
            NoteCard(language.visitText("Öffnen Sie einen Rechner und wählen Sie „Besuch speichern“, um Eingaben und Ergebnisse festzuhalten.", "Open a calculator and choose “Save visit” to keep its inputs and results."))
        } else {
            InputCard(project.name) {
                if (project.notes.isNotBlank()) Text(project.notes)
                TextButton(onClick = {
                    if (!store.loadFailed) store.error = null
                    try {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, project.report(if (language == AppLanguage.DE) "de" else "en"))
                        }
                        context.startActivity(Intent.createChooser(intent, language.visitText("Besuchsprotokoll teilen", "Share visit record")))
                    } catch (_: Exception) { store.error = "share" }
                }) { Text(language.visitText("Besuchsprotokoll teilen", "Share visit record")) }
            }
            if (project.visits.isEmpty()) NoteCard(language.visitText("Noch keine Besuche. Speichern Sie eine Berechnung für diese Anlage.", "No visits yet. Save a calculation for this appliance."))
            project.visits.sortedByDescending { it.date }.forEach { visit ->
                InputCard(visit.kind.title(language == AppLanguage.DE)) {
                    Text(Instant.ofEpochMilli(visit.date).toString(), color = Ink300)
                    if (visit.note.isNotBlank()) Text(visit.note)
                    if (visit.source.isNotBlank()) Text("${language.visitText("Quelle der Eingabewerte", "Input source")}: ${visit.source}")
                    Text(language.visitText("Eingegebene Werte", "Entered inputs"), style = MaterialTheme.typography.titleMedium)
                    VisitValues(visit.inputs, visit.kind, language)
                    Text(language.visitText("Berechnete Ergebnisse", "Calculated results"), style = MaterialTheme.typography.titleMedium)
                    VisitValues(visit.results, visit.kind, language)
                    project.visits.filter { it.kind == visit.kind && it.date < visit.date }.maxByOrNull { it.date }?.let { prior ->
                        Text(language.visitText("Änderung gegenüber vorherigem gleichen Rechenmodus", "Change since previous visit in the same mode"), style = MaterialTheme.typography.titleMedium)
                        Text(Instant.ofEpochMilli(prior.date).toString(), color = Ink300)
                        VisitValues(visit.difference(prior)!!, visit.kind, language)
                    }
                    TextButton(onClick = { deletingVisit = visit.id }) { Text(language.visitText("Besuch löschen", "Delete visit"), color = MaterialTheme.colorScheme.error) }
                }
            }
            NoteCard(visitDisclaimer(language == AppLanguage.DE))
        }
    }
    if (creating) NewServiceProjectDialog(language, store, onDismiss = { creating = false }) { selectedID = it; creating = false }
    if (deletingProject != null || deletingVisit != null) {
        AlertDialog(
            onDismissRequest = { deletingProject = null; deletingVisit = null },
            title = { Text(language.visitText("Löschen bestätigen", "Confirm deletion")) },
            text = { Text(if (deletingProject != null) language.visitText("Anlage mit allen Besuchen löschen?", "Delete appliance and all visits?") else language.visitText("Diesen Besuch unwiderruflich löschen?", "Permanently delete this visit?")) },
            confirmButton = {
                TextButton(onClick = {
                    store.error = null
                    if (deletingProject != null) {
                        store.commit(store.projects.filterNot { it.id == deletingProject })
                    } else {
                        store.commit(store.projects.map { if (it.id == selectedID) it.copy(visits = it.visits.filterNot { visit -> visit.id == deletingVisit }) else it })
                    }
                    deletingProject = null; deletingVisit = null
                }) { Text(language.visitText("Löschen", "Delete")) }
            },
            dismissButton = { TextButton(onClick = { deletingProject = null; deletingVisit = null }) { Text(language.visitText("Abbrechen", "Cancel")) } },
        )
    }
}

@Composable
private fun NewServiceProjectDialog(language: AppLanguage, store: ServiceVisitStore, onDismiss: () -> Unit, onCreated: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        modifier = Modifier.imePadding(), onDismissRequest = onDismiss,
        title = { Text(language.visitText("Neue Anlage", "New appliance")) },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(language.visitText("Anlagenname", "Appliance name")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(language.visitText("Standort / Anlagennotiz", "Location / appliance note")) }, modifier = Modifier.fillMaxWidth())
                store.error?.let { StatusBanner(visitError(it, language)) }
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank() && !store.loadFailed, onClick = {
            store.error = null
            val project = ServiceProject(name = name.trim(), notes = notes)
            if (store.commit(store.projects + project)) onCreated(project.id)
        }) { Text(language.visitText("Anlegen", "Create")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(language.visitText("Abbrechen", "Cancel")) } },
    )
}

@Composable
internal fun VisitCaptureAction(language: AppLanguage, store: ServiceVisitStore, kind: VisitKind, inputs: Map<String, Double>) {
    var showing by rememberSaveable { mutableStateOf(false) }
    PrimaryAction(language.visitText("Besuch speichern", "Save visit")) {
        if (!store.loadFailed) store.error = null
        showing = true
    }
    if (showing) SaveServiceVisitDialog(language, store, kind, inputs) { showing = false }
}

@Composable
private fun SaveServiceVisitDialog(language: AppLanguage, store: ServiceVisitStore, kind: VisitKind, inputs: Map<String, Double>, onDismiss: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var source by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        modifier = Modifier.imePadding(), onDismissRequest = onDismiss,
        title = { Text(language.visitText("Besuch speichern", "Save visit")) },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (store.loadFailed) StatusBanner(visitError("load", language))
                Text(language.visitText("Anlage", "Appliance"), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { selected = null }) { Text((if (selected == null) "✓ " else "") + language.visitText("Neue Anlage", "New appliance")) }
                store.projects.forEach { project ->
                    TextButton(onClick = { selected = project.id }) { Text((if (selected == project.id) "✓ " else "") + project.name) }
                }
                if (selected == null) OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(language.visitText("Anlagenname", "Appliance name")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text(language.visitText("Quelle der Eingabewerte", "Input source")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(language.visitText("Besuchsnotiz, z. B. Vorher / Nachher", "Visit note, e.g. Before / After")) }, modifier = Modifier.fillMaxWidth())
                Text(language.visitText("Eingegebene Werte", "Entered inputs"), style = MaterialTheme.typography.titleMedium)
                VisitValues(inputs, kind, language)
                NoteCard(visitDisclaimer(language == AppLanguage.DE))
                if (invalid) StatusBanner(language.visitText("Besuch nicht gespeichert: Nur endliche, nicht negative Eingaben sind zulässig. ΔT muss positiv sein. Rückwärtsrechnung benötigt positive Heizwerte, Wirkungsgrad und Öldruck; maximal 120 % Wirkungsgrad.", "Visit not saved: inputs must be finite and non-negative. ΔT must be positive. Reverse calculations need positive calorific value, efficiency and oil pressure; efficiency must not exceed 120%."))
                if (!store.loadFailed) store.error?.let { StatusBanner(visitError(it, language)) }
            }
        },
        confirmButton = {
            TextButton(enabled = !store.loadFailed && (selected != null || name.isNotBlank()), onClick = {
                store.error = null
                val visit = try { VisitSnapshot.capture(kind, inputs, note, source) } catch (_: IllegalArgumentException) { invalid = true; null }
                if (visit != null) {
                    val updated = if (selected == null) store.projects + ServiceProject(name = name.trim(), notes = "", visits = listOf(visit)) else {
                        if (store.projects.none { it.id == selected }) { store.error = "save"; return@TextButton }
                        store.projects.map { if (it.id == selected) it.copy(visits = it.visits + visit) else it }
                    }
                    if (store.commit(updated)) onDismiss()
                }
            }) { Text(language.visitText("Speichern", "Save")) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(language.visitText("Abbrechen", "Cancel")) } },
    )
}
