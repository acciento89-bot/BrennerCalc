import Foundation

enum VisitKind: String, Codable, CaseIterable {
    case oil, oilReverse, gas, gasReverse, water
    var titleKey: String { "visits.kind.\(rawValue)" }
    var requiredInputs: [String] {
        switch self {
        case .oil: return ["nozzle", "pressure", "calorific", "efficiency"]
        case .oilReverse: return ["power", "pressure", "calorific", "efficiency"]
        case .gas: return ["flow", "calorific", "efficiency"]
        case .gasReverse: return ["power", "calorific", "efficiency"]
        case .water: return ["power", "deltaT"]
        }
    }
    func unit(for key: String, input: Bool) -> String {
        switch key {
        case "nozzle", "actualGPH": return "USgal/h"
        case "pressure": return "bar"
        case "efficiency": return "%"
        case "deltaT": return "K"
        case "calorific": return self == .oil || self == .oilReverse ? "kWh/l" : "kWh/m³"
        case "flow": return self == .water || self == .oil || self == .oilReverse ? "l/h" : "m³/h"
        case "flowMinute": return "l/min"
        case "flowM3": return "m³/h"
        default: return "kW"
        }
    }
}

enum VisitError: Error { case invalidInput, invalidArchive, futureVersion, emptyName }

struct VisitSnapshot: Identifiable, Codable, Equatable {
    let id: UUID
    let kind: VisitKind
    let inputs: [String: Double]
    let results: [String: Double]
    let note: String
    let source: String
    let date: Date

    init(kind: VisitKind, inputs: [String: Double], note: String, source: String, date: Date = Date()) throws {
        guard Set(inputs.keys) == Set(kind.requiredInputs), inputs.values.allSatisfy({ $0.isFinite && $0 >= 0 }),
              date.timeIntervalSince1970.isFinite else { throw VisitError.invalidInput }
        if let efficiency = inputs["efficiency"], efficiency > 120 { throw VisitError.invalidInput }
        if kind == .water && inputs["deltaT"] == 0 { throw VisitError.invalidInput }
        if kind == .oilReverse || kind == .gasReverse {
            guard inputs["calorific", default: 0] > 0, inputs["efficiency", default: 0] > 0 else { throw VisitError.invalidInput }
            if kind == .oilReverse && inputs["pressure"] == 0 { throw VisitError.invalidInput }
        }
        self.id = UUID(); self.kind = kind; self.inputs = inputs
        self.note = note.trimmingCharacters(in: .whitespacesAndNewlines)
        self.source = source.trimmingCharacters(in: .whitespacesAndNewlines); self.date = date
        switch kind {
        case .oil:
            let r = CalculatorEngine.oil(ratedGPH: inputs["nozzle"]!, pressureBar: inputs["pressure"]!, calorificValueKWhPerLiter: inputs["calorific"]!, efficiencyPercent: inputs["efficiency"]!)
            results = ["actualGPH": r.actualGPH, "flow": r.litersPerHour, "input": r.inputKW, "output": r.outputKW]
        case .oilReverse:
            let r = CalculatorEngine.oilReverse(desiredOutputKW: inputs["power"]!, pressureBar: inputs["pressure"]!, calorificValueKWhPerLiter: inputs["calorific"]!, efficiencyPercent: inputs["efficiency"]!)
            results = ["nozzle": r.requiredRatedGPH, "actualGPH": r.actualGPH, "flow": r.litersPerHour, "input": r.inputKW]
        case .gas:
            let r = CalculatorEngine.gas(cubicMetersPerHour: inputs["flow"]!, calorificValueKWhPerM3: inputs["calorific"]!, efficiencyPercent: inputs["efficiency"]!)
            results = ["input": r.inputKW, "output": r.outputKW]
        case .gasReverse:
            let r = CalculatorEngine.gasReverse(desiredOutputKW: inputs["power"]!, calorificValueKWhPerM3: inputs["calorific"]!, efficiencyPercent: inputs["efficiency"]!)
            results = ["flow": r.cubicMetersPerHour, "input": r.inputKW]
        case .water:
            let r = CalculatorEngine.water(powerKW: inputs["power"]!, deltaT: inputs["deltaT"]!)
            results = ["flow": r.litersPerHour, "flowMinute": r.litersPerMinute, "flowM3": r.cubicMetersPerHour]
        }
        guard results.values.allSatisfy(\.isFinite) else { throw VisitError.invalidInput }
    }

    func difference(from previous: VisitSnapshot) -> [String: Double]? {
        guard kind == previous.kind else { return nil }
        return results.mapValues { $0 }.reduce(into: [:]) { delta, item in delta[item.key] = item.value - previous.results[item.key, default: 0] }
    }

    func validate() throws {
        let expected = try VisitSnapshot(kind: kind, inputs: inputs, note: note, source: source, date: date)
        guard expected.results == results else { throw VisitError.invalidArchive }
    }
}

struct ServiceProject: Identifiable, Codable, Equatable {
    var id = UUID()
    var name: String
    var notes: String
    var visits: [VisitSnapshot] = []

    func report(language: String) -> String {
        let de = language.hasPrefix("de")
        func label(_ key: String) -> String {
            let labels: [String: (String, String)] = [
                "oil": ("Öl · Düse", "Oil · nozzle"), "oilReverse": ("Öl · Leistung", "Oil · power"),
                "gas": ("Gas · Volumenstrom", "Gas · flow"), "gasReverse": ("Gas · Leistung", "Gas · power"), "water": ("Heizwasser", "Heating water"),
                "nozzle": ("Nenndüse bei 7 bar", "Rated nozzle at 7 bar"), "pressure": ("Pumpendruck", "Pump pressure"),
                "calorific": ("Heizwert", "Calorific value"), "efficiency": ("Wirkungsgrad", "Efficiency"),
                "power": ("Leistung", "Power"), "deltaT": ("Temperaturdifferenz", "Temperature difference"),
                "flow": ("Volumenstrom", "Flow"), "flowMinute": ("Volumenstrom / Minute", "Flow / minute"),
                "flowM3": ("Volumenstrom m³", "Flow m³"), "input": ("Eingangsleistung", "Input power"),
                "output": ("Nutzleistung", "Output power"), "actualGPH": ("Tatsächlicher Düsendurchsatz", "Actual nozzle flow")]
            let pair = labels[key] ?? (key, key); return de ? pair.0 : pair.1
        }
        let formatter = NumberFormatter(); formatter.locale = Locale(identifier: de ? "de_DE" : "en_GB"); formatter.maximumFractionDigits = 4
        func values(_ data: [String: Double], kind: VisitKind) -> String {
            data.keys.sorted().map { "\(label($0)): \(formatter.string(from: NSNumber(value: data[$0]!)) ?? "—") \(kind.unit(for: $0, input: true))" }.joined(separator: "\n")
        }
        var lines = ["BrennerCalc — \(de ? "Servicebesuche" : "Service visits")", name, notes]
        let ordered = visits.sorted { $0.date < $1.date }
        for (index, visit) in ordered.enumerated() {
            lines += ["", "\(index + 1). \(label(visit.kind.rawValue)) · \(ISO8601DateFormatter().string(from: visit.date))", de ? "Eingegebene Werte:" : "Entered inputs:", values(visit.inputs, kind: visit.kind), "\(de ? "Quelle" : "Source"): \(visit.source)", "\(de ? "Notiz" : "Note"): \(visit.note)", de ? "Berechnete Ergebnisse:" : "Calculated results:", values(visit.results, kind: visit.kind)]
            if let prior = ordered.prefix(index).last(where: { $0.kind == visit.kind }), let delta = visit.difference(from: prior) {
                lines += ["\(de ? "Änderung gegenüber" : "Change since") \(ISO8601DateFormatter().string(from: prior.date)):", values(delta, kind: visit.kind)]
            }
        }
        lines += ["", de ? "Rechenhilfe aus eingegebenen Werten. Keine Zertifizierung oder Messbescheinigung. Herstellerangaben und fachgerechte Messungen haben Vorrang." : "Calculation aid from entered values. Not a certification or measurement certificate. Manufacturer specifications and professional measurements take precedence."]
        return lines.joined(separator: "\n")
    }
}

struct VisitArchive {
    let url: URL
    private struct Envelope: Codable { let schemaVersion: Int; let projects: [ServiceProject] }
    func load() throws -> [ServiceProject] {
        guard FileManager.default.fileExists(atPath: url.path) else { return [] }
        let data = try Data(contentsOf: url)
        let decoder = JSONDecoder()
        let projects: [ServiceProject]
        if let envelope = try? decoder.decode(Envelope.self, from: data) {
            guard envelope.schemaVersion == 1 else { throw VisitError.futureVersion }
            projects = envelope.projects
        } else { projects = try decoder.decode([ServiceProject].self, from: data) }
        try validate(projects)
        return projects
    }
    func save(_ projects: [ServiceProject]) throws {
        // Verify existing data before any replacement; recovery must never silently discard it.
        _ = try load()
        try validate(projects)
        let data = try JSONEncoder().encode(Envelope(schemaVersion: 1, projects: projects))
        try FileManager.default.createDirectory(at: url.deletingLastPathComponent(), withIntermediateDirectories: true)
        try data.write(to: url, options: .atomic)
    }
    private func validate(_ projects: [ServiceProject]) throws {
        guard Set(projects.map(\.id)).count == projects.count else { throw VisitError.invalidArchive }
        for project in projects {
            guard !project.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
                  Set(project.visits.map(\.id)).count == project.visits.count else { throw VisitError.invalidArchive }
            try project.visits.forEach { try $0.validate() }
        }
    }
}
