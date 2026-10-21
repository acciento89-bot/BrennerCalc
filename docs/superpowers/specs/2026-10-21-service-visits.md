# Saved service visits

Approved scope: local appliance/service projects containing immutable timestamped snapshots from all five existing calculator modes. Save current inputs, mode, calculated outputs, input-source and visit notes. Compare consecutive snapshots only for the same mode and share a readable DE/EN visit record. This is an entered/calculated worksheet, never measured certification.

Source audit: pinned main db8f236dd2535a5945828d295cb36ccd62f0ddd2 has only transient calculator state; no saved project/history/export feature. This pin is not proved to be the live binary source.

Architecture: Foundation Codable value models call the unchanged CalculatorEngine; versioned JSON archive uses atomic writes and refuses unknown/corrupt data. Observable UI store commits disk before publishing. SwiftUI projects list/detail and calculator save sheet reuse current Pro gating. All projects work offline. New files are explicitly registered in the existing Xcode project.

Constraints: preserve bundle de.kamilunavo.brennercalc and product de.kamilunavo.brennercalc.pro. No real transactions, publication, native/device workloads by this worker. iOS17 minimum. DE/EN. No formula changes. Android parity must pass its independent unit/build/native checks. Duo native acceptance remains pending.

Acceptance: create/reopen projects; add before/after snapshots from forward/reverse oil and gas and water; compare compatible snapshots; delete after confirmation; share full input/source context; surface disk failures without clobbering data. Empty install, versionless migration, corruption, future schema, nonfinite/negative/invalid denominator, formulas and report tested. Root performs native compile and compact/expanded Duo acceptance.

## Authorized Android parity extension
Android must implement all matching project/snapshot/source-note/comparison/share/persistence/error behavior in native Compose, preserving existing CalculatorEngine and BillingManager, package, version and historical build/sign/upload workflows. The repository's existing Android source is1.0.2(3), not independently verified live-store baseline. Root alone executes Gradle RED/GREEN/build and native QA. Use explicit Gson JSON tree codec (cached Gson2.11.0) and same-directory atomic file replacement; no version bumps.
