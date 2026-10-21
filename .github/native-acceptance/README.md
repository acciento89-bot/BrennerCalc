# Brenner native service acceptance

Manual-only GitHub KVM job, separate from signing/upload. It builds the Brenner debug candidate from the checked-out commit and this independent native UiAutomator host. It uses official API35 x86_64 Google APIs (not Play), Pixel7,2048MiB. No personal device or app data is used. No billing, redemption, restore, AI, account or external messaging test is included.

The exact existing external `brennerRealOilVisitsSourcesHistoryRotationColdReloadAndShare` method is copied without edits: oil inputs, source notes, two visits, history comparison, rotation, exact persisted JSON, real cold process restart and actual ACTION_SEND to the local QA receiver. No test record is injected into the product archive. Only that method and shared helper bodies are retained; MAG/Hydro methods, package queries and installation dependencies are absent. `lineage.json` pins the original file/method hashes. The fixture uses exact emulator SDK35 instead of36. After actual landscape-dialog evidence, the scroll helper computes both swipe endpoints inside the visible scroll pane; all business assertions, inputs, waits and the original method remain unchanged. Shared helper stale requery/SystemUI diagnostic behavior remains intact.

The app candidate is1.1.0(6); installation verifies actual package metadata. The test checks the debug flag before using `run-as`. No product, dependency, version, icon, Apple or signing source is changed by this harness.

## Source checks

`python3 -m unittest discover -s .github/native-acceptance/tests -v`

These are mocked adb/config checks, never native acceptance. `bash -n .github/native-acceptance/run-cloud.sh` checks shell syntax. A real workflow run is required to establish host compilation and native behavior.

## Cloud run and evidence

Root reviews/integrates and manually dispatches `android-native-acceptance.yml` on the intended exact source commit. The workflow has contents:read only, no secrets or store/sign actions. It builds both own APKs and host from that checkout; separate roles preserve all three hashes. Instrumentation has a300second limit and the job a20minute limit. A failed test remains failed; no dialog is dismissed and no fallback replaces assertions.

The always-upload artifact retains build logs, source commit, APK hashes, installed identity, instrumentation/partial timeout output, native PNG/XML/events/selected JSON/share payload, the actual private service archive, screen/window/ANR/crash/logcat and result. Extraction failure prevents a passing receipt. Android ANR/unresponsive environment is a blocker, never proof of an app defect. Screenshots can include Android dialogs and are evidence, not store galleries.

`run-cloud.sh` owns only action-created AVD `BrennerNativeAcceptanceApi35` and serial emulator-5554. EXIT trap stops that emulator, deletes that AVD and verifies exact name/serial absent; nonzero inventory commands fail cleanup verification. Emulator action retains its own post cleanup too. Full runner-machine termination is ultimately cleaned by GitHub; an aborted job without cleanup receipt must not be called locally verified cleanup. No local VM/build/device was used to prepare this source.

Do not call source/mock checks or a workflow declaration native success. Retain actual failed workflow logs and inspect them before any bounded retry. No app error suppression, purchase test or store delivery is authorized by this file.
