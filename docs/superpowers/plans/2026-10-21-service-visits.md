# Saved Service Visits Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement task-by-task.

**Goal:** Persist appliance projects and comparable timestamped calculator visits with share output.
**Architecture:** Unchanged calculation engine, immutable Codable snapshots, atomic versioned JSON repository, observable store and SwiftUI project/save screens.
**Tech Stack:** Swift Foundation, SwiftUI iOS17.
**Spec:** docs/superpowers/specs/2026-10-21-service-visits.md

## Global Constraints
Preserve product identity, formulas/units and IAP. Root serially owns all compile/test/device workloads. DE/EN. No release claim from source.

## Review Focus
- Invalid numeric input rejected before capture.
- Corrupt/future archives remain untouched and saving blocked.
- Mixed tools never produce a misleading comparison.
- Disk failure keeps visible state unchanged.
- Project choice and unsaved sheet state survive flexible layout.

### Task 1: Domain, persistence and report
Files: Tests/main.swift, Tests/run-core-tests.sh; create BrennerCalc/ServiceVisits.swift.
- [x] Write tests for real formula snapshots, incompatible comparison, input validation, roundtrip, missing file, legacy/future/corrupt archive, share context.
- [x] Root run `sh Tests/run-core-tests.sh`; observed missing feature RED; receipt core-red.log.
- [x] Implement VisitSnapshot.init(kind:inputs:note:source:date:), difference(from:), ServiceProject.report(language:), VisitArchive.load()/save(_:).
- [x] Root run same command; exit0, all assertions pass; receipt core-green.log.

### Task 2: App integration
Files: new ServiceVisitsView.swift; calculator views, ContentView, App, strings, pbxproj.
- [x] Integrate observable store, project CRUD, snapshot save sheet and ShareLink report using tested domain.
- [x] Register sources explicitly, localize DE/EN; preserve existing entitlement gating.
- [ ] Root native compile and native QA; resolve observed failures.
- [ ] Record exact evidence, remaining native release gates, draft metadata; commit after root verification.

### Task3: Android feature parity (new explicit human requirement)
Files: Android ServiceVisits.kt, ServiceVisitsUi.kt, MainActivity.kt; add cached Gson2.11.0 dependency; Tests ServiceVisitsTest.kt.
- [x] Inspect native architecture, original5modes, billing and version.
- [x] Write JUnit feature tests for formulas/comparison/validation, real JSON persistence, migration/corruption/future schema, share and write failure.
- [x] Root ran feature Gradle suite for missingfeature RED (android-red.log).
- [x] Implement immutable model and atomic versioned JSON using unchanged Kotlin CalculatorEngine; same DE/EN reports.
- [x] Root ran feature+existing full unit suite GREEN (android-green1.log).
- [x] Integrate Compose list/detail/create/save/delete/share, state preservation and German/English copy; keep existing Pro entry restrictions.
- [x] Root built Android Debug and repeated full unit suite: android-green2.log BUILD SUCCESSFUL; 11tests, zero failures/errors/skips.
- [ ] Root completes native Android parity QA and native iOS/Duo acceptance.
