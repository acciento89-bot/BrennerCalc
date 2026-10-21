import Foundation

func require(_ condition: Bool, _ message: String) {
    if !condition { fatalError(message) }
}
let when = Date(timeIntervalSince1970: 1_800_000_000)
let before = try VisitSnapshot(kind: .oil, inputs: ["nozzle": 0.5, "pressure": 7, "calorific": 10, "efficiency": 90], note: "Before", source: "Nameplate", date: when)
let after = try VisitSnapshot(kind: .oil, inputs: ["nozzle": 0.6, "pressure": 7, "calorific": 10, "efficiency": 90], note: "After", source: "Manufacturer", date: when.addingTimeInterval(60))
require(abs(before.results["output"]! - 17.034353028) < 0.00001, "Existing oil formula preserved")
require(abs(after.difference(from: before)!["output"]! - 3.4068706056) < 0.00001, "Comparable visit delta")
let water = try VisitSnapshot(kind: .water, inputs: ["power": 20, "deltaT": 20], note: "", source: "", date: when)
require(water.difference(from: before) == nil, "Never compare different tools")
let gas = try VisitSnapshot(kind: .gasReverse, inputs: ["power": 18, "calorific": 10, "efficiency": 90], note: "", source: "", date: when)
require(gas.results["flow"] == 2, "Reverse gas preserved")
for invalid in [Double.nan, Double.infinity, -1] {
    do { _ = try VisitSnapshot(kind: .water, inputs: ["power": invalid, "deltaT": 20], note: "", source: "", date: when); fatalError("Rejected invalid input") } catch {}
}
do { _ = try VisitSnapshot(kind: .water, inputs: ["power": 20, "deltaT": 0], note: "", source: "", date: when); fatalError("Zero denominator rejected") } catch {}
let oilReverse = try VisitSnapshot(kind: .oilReverse, inputs: ["power": before.results["output"]!, "pressure": 7, "calorific": 10, "efficiency": 90], note: "", source: "", date: when)
require(abs(oilReverse.results["nozzle"]! - 0.5) < 0.000001, "Reverse oil preserved")
let gasForward = try VisitSnapshot(kind: .gas, inputs: ["flow": 2, "calorific": 10, "efficiency": 90], note: "", source: "", date: when)
require(gasForward.results["output"] == 18, "Gas formula preserved")
require(abs(water.results["flow"]! - 859.8452278589855) < 0.00001, "Water units preserved")
let folder = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString)
defer { try? FileManager.default.removeItem(at: folder) }
let disk = VisitArchive(url: folder.appendingPathComponent("visits.json"))
require(try disk.load().isEmpty, "First install starts empty")
let project = ServiceProject(name: "Boiler A", notes: "Basement", visits: [before, after])
try disk.save([project])
let restored = try disk.load()
require(restored == [project], "Cold restart roundtrip")
let report = restored[0].report(language: "en")
require(report.contains("Nameplate") && report.contains("Before") && report.contains("kW") && report.contains("Not a certification"), "Share record retains context and safety")
let original = try Data(contentsOf: disk.url)
try Data("{broken".utf8).write(to: disk.url)
do { _ = try disk.load(); fatalError("Corruption surfaced") } catch {}
require(try Data(contentsOf: disk.url) == Data("{broken".utf8), "Never overwrite unreadable archive")
try original.write(to: disk.url)
let legacy = try JSONEncoder().encode([project])
try legacy.write(to: disk.url)
require(try disk.load() == [project], "Versionless archive migration")
try Data("{\"schemaVersion\":999,\"projects\":[]}".utf8).write(to: disk.url)
do { _ = try disk.load(); fatalError("Future version surfaced") } catch {}
print("PASS: visit formulas, comparison, validation, persistence, migration, corruption, export")
