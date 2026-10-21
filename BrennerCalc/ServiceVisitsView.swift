import SwiftUI
import Observation

@MainActor @Observable
final class ServiceVisitStore {
    private(set) var projects: [ServiceProject] = []
    private(set) var loadFailed = false
    var errorKey: String?
    private let archive: VisitArchive

    init() {
        let directory = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
        archive = VisitArchive(url: directory.appendingPathComponent("BrennerCalc/service-visits.json"))
        reload()
    }
    func reload() {
        do { projects = try archive.load(); loadFailed = false; errorKey = nil }
        catch { loadFailed = true; errorKey = "visits.loadError" }
    }
    @discardableResult func commit(_ updated: [ServiceProject]) -> Bool {
        guard !loadFailed else { errorKey = "visits.loadError"; return false }
        do { try archive.save(updated); projects = updated; return true }
        catch { errorKey = "visits.saveError"; return false }
    }
    @discardableResult func add(name: String, notes: String) -> UUID? {
        let clean = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !clean.isEmpty else { errorKey = "visits.nameRequired"; return nil }
        let project = ServiceProject(name: clean, notes: notes)
        return commit(projects + [project]) ? project.id : nil
    }
    func save(_ visit: VisitSnapshot, to id: UUID) -> Bool {
        var next = projects
        guard let index = next.firstIndex(where: { $0.id == id }) else { errorKey = "visits.saveError"; return false }
        next[index].visits.append(visit)
        return commit(next)
    }
}

private struct VisitErrorAlert: ViewModifier {
    @Environment(ServiceVisitStore.self) private var store
    func body(content: Content) -> some View {
        content.alert("visits.error", isPresented: Binding(get: { store.errorKey != nil }, set: { if !$0 { store.errorKey = nil } })) {
            Button("common.done") { store.errorKey = nil }
        } message: { Text(LocalizedStringKey(store.errorKey ?? "visits.saveError")) }
    }
}

struct ServiceProjectsView: View {
    @Environment(ServiceVisitStore.self) private var store
    @State private var showCreate = false
    @State private var deleteID: UUID?
    var body: some View {
        List {
            if store.loadFailed {
                Section {
                    Text("visits.loadError")
                    Button("visits.retry") { store.reload() }
                }
            } else if store.projects.isEmpty {
                ContentUnavailableView("visits.empty", systemImage: "folder", description: Text("visits.emptyHint"))
            }
            ForEach(store.projects) { project in
                NavigationLink {
                    ServiceProjectDetailView(projectID: project.id)
                } label: {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(project.name).font(.headline)
                        if !project.notes.isEmpty { Text(project.notes).font(.subheadline).foregroundStyle(.secondary) }
                        Text("\(project.visits.count)").font(.caption).accessibilityLabel(Text("visits.count") + Text(" \(project.visits.count)"))
                    }
                }
                .swipeActions { Button("visits.delete", role: .destructive) { deleteID = project.id } }
            }
            Section { Text("visits.captureHint").font(.footnote).foregroundStyle(.secondary) }
        }
        .navigationTitle("visits.projects")
        .toolbar { Button("visits.newProject", systemImage: "plus") { showCreate = true }.disabled(store.loadFailed) }
        .sheet(isPresented: $showCreate) { NewServiceProjectView() }
        .confirmationDialog("visits.deleteProject", isPresented: Binding(get: { deleteID != nil }, set: { if !$0 { deleteID = nil } }), titleVisibility: .visible) {
            Button("visits.delete", role: .destructive) {
                if let id = deleteID { _ = store.commit(store.projects.filter { $0.id != id }) }
                deleteID = nil
            }
        }
        .modifier(VisitErrorAlert())
    }
}

private struct NewServiceProjectView: View {
    @Environment(ServiceVisitStore.self) private var store
    @Environment(\.dismiss) private var dismiss
    @State private var name = ""
    @State private var notes = ""
    var body: some View {
        NavigationStack {
            Form {
                TextField("visits.projectName", text: $name)
                TextField("visits.projectNotes", text: $notes, axis: .vertical)
            }
            .navigationTitle("visits.newProject")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("visits.cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("visits.create") { if store.add(name: name, notes: notes) != nil { dismiss() } }
                        .disabled(name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
            .modifier(VisitErrorAlert())
        }
    }
}

struct ServiceProjectDetailView: View {
    @Environment(ServiceVisitStore.self) private var store
    @Environment(\.locale) private var locale
    let projectID: UUID
    @State private var deleteID: UUID?
    private var project: ServiceProject? { store.projects.first { $0.id == projectID } }
    var body: some View {
        List {
            if let project {
                Section {
                    Text(project.name).font(.headline)
                    if !project.notes.isEmpty { Text(project.notes) }
                    ShareLink(item: project.report(language: locale.identifier)) { Label("visits.share", systemImage: "square.and.arrow.up") }
                }
                if project.visits.isEmpty { Text("visits.noVisits") }
                ForEach(project.visits.sorted { $0.date > $1.date }) { visit in
                    Section {
                        Text(LocalizedStringKey(visit.kind.titleKey)).font(.headline)
                        Text(visit.date, format: .dateTime.year().month().day().hour().minute()).foregroundStyle(.secondary)
                        if !visit.note.isEmpty { Text(visit.note) }
                        if !visit.source.isEmpty { LabeledContent("visits.source", value: visit.source) }
                        Text("visits.entered").font(.subheadline.bold())
                        VisitValuesView(values: visit.inputs, kind: visit.kind)
                        Text("visits.calculated").font(.subheadline.bold())
                        VisitValuesView(values: visit.results, kind: visit.kind)
                        if let prior = project.visits.filter({ $0.kind == visit.kind && $0.date < visit.date }).max(by: { $0.date < $1.date }), let delta = visit.difference(from: prior) {
                            Text("visits.change").font(.subheadline.bold())
                            Text(prior.date, format: .dateTime.year().month().day().hour().minute()).font(.caption)
                            VisitValuesView(values: delta, kind: visit.kind)
                        }
                        Button("visits.deleteVisit", role: .destructive) { deleteID = visit.id }
                    }
                }
                Section { Text("visits.disclaimer").font(.footnote).foregroundStyle(.secondary) }
            }
        }
        .navigationTitle("visits.history")
        .confirmationDialog("visits.deleteVisitConfirm", isPresented: Binding(get: { deleteID != nil }, set: { if !$0 { deleteID = nil } }), titleVisibility: .visible) {
            Button("visits.delete", role: .destructive) {
                var next = store.projects
                if let index = next.firstIndex(where: { $0.id == projectID }) {
                    next[index].visits.removeAll { $0.id == deleteID }
                    _ = store.commit(next)
                }
                deleteID = nil
            }
        }
        .modifier(VisitErrorAlert())
    }
}

private struct VisitValuesView: View {
    let values: [String: Double]
    let kind: VisitKind
    var body: some View {
        ForEach(values.keys.sorted(), id: \.self) { key in
            VStack(alignment: .leading, spacing: 3) {
                Text(LocalizedStringKey("visits.value." + key)).font(.caption).foregroundStyle(.secondary)
                Text("\(values[key]!, format: .number.precision(.fractionLength(0...4))) \(kind.unit(for: key, input: true))")
                    .monospacedDigit().textSelection(.enabled)
            }
            .accessibilityElement(children: .combine)
        }
    }
}

struct SaveServiceVisitButton: View {
    let kind: VisitKind
    let inputs: [String: Double]
    @State private var showing = false
    var body: some View {
        Button { showing = true } label: { Label("visits.saveVisit", systemImage: "folder.badge.plus") }
            .buttonStyle(.borderedProminent)
            .sheet(isPresented: $showing) { SaveServiceVisitView(kind: kind, inputs: inputs) }
    }
}

private struct SaveServiceVisitView: View {
    @Environment(ServiceVisitStore.self) private var store
    @Environment(\.dismiss) private var dismiss
    let kind: VisitKind
    let inputs: [String: Double]
    @State private var selected: UUID?
    @State private var newName = ""
    @State private var source = ""
    @State private var note = ""
    @State private var invalid = false
    var body: some View {
        NavigationStack {
            Form {
                Section("visits.project") {
                    Picker("visits.project", selection: $selected) {
                        Text("visits.newProject").tag(nil as UUID?)
                        ForEach(store.projects) { Text($0.name).tag(Optional($0.id)) }
                    }
                    if selected == nil { TextField("visits.projectName", text: $newName) }
                }
                Section("visits.context") {
                    TextField("visits.source", text: $source, axis: .vertical)
                    TextField("visits.note", text: $note, axis: .vertical)
                }
                Section("visits.entered") { VisitValuesView(values: inputs, kind: kind) }
                Section { Text("visits.disclaimer").font(.footnote) }
                if invalid { Text("visits.invalidInput").foregroundStyle(.red) }
            }
            .navigationTitle("visits.saveVisit")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("visits.cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("visits.save") { save() }.disabled(store.loadFailed || (selected == nil && newName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty))
                }
            }
            .modifier(VisitErrorAlert())
        }
    }
    private func save() {
        guard let visit = try? VisitSnapshot(kind: kind, inputs: inputs, note: note, source: source) else { invalid = true; return }
        if let selected {
            if store.save(visit, to: selected) { dismiss() }
        } else {
            // New project and first visit are one atomic archive transaction.
            let project = ServiceProject(name: newName.trimmingCharacters(in: .whitespacesAndNewlines), notes: "", visits: [visit])
            if store.commit(store.projects + [project]) { dismiss() }
        }
    }
}
