# BrennerCalc — Google Play release metadata

## Android identity

- App name: BrennerCalc
- Application ID: `de.kamilunavo.brennercalc`
- Version name: `1.0.0`
- Version code: `1`
- Category: Tools
- Target SDK: Android 16 / API 36
- Minimum SDK: API 26
- Distribution artifact: Android App Bundle (`.aab`)

## Google Play Billing

- Product: BrennerCalc Pro
- Product ID: `de.kamilunavo.brennercalc.pro`
- Product type: one-time product / non-consumable entitlement
- Intended base price: EUR 4.99
- Unlocks: Gas and Hydronics calculators
- Restore: ownership is refreshed through Google Play Billing

## German store listing

### Short description
Schnelle SHK-Rechner für Ölbrenner, Gasleistung und Heizwasser.

### Full description
BrennerCalc ist ein kompaktes Rechenwerkzeug für Heizungs-, SHK- und Servicetechniker.

Die App konzentriert sich bewusst auf schnelle Berechnungen ohne Account, Werbung oder unnötige Ablenkung.

Funktionen:
- Ölbrenner: Düsengröße und Pumpendruck berücksichtigen sowie Ölmenge und Leistung berechnen
- Rückwärtsberechnung: gewünschte Leistung in benötigte Düsengröße umrechnen
- Gas: Volumenstrom und Heizwert in Brennerleistung umrechnen
- Heizwasser: benötigten Volumenstrom aus Leistung und Spreizung berechnen
- Ergebnisse in l/h, l/min, m³/h und kW
- Deutsch und Englisch
- Offline nutzbar

BrennerCalc Pro schaltet die zusätzlichen Gas- und Heizwasser-Rechner dauerhaft mit einem einmaligen In-App-Kauf frei.

Wichtiger Hinweis:
BrennerCalc ist eine Rechenhilfe für fachkundige Anwender. Herstellerangaben, geltende Normen, Inbetriebnahmemessungen und die Verbrennungsanalyse haben immer Vorrang.

## English store listing

### Short description
Fast HVAC calculators for oil burners, gas output and hydronic flow.

### Full description
BrennerCalc is a compact calculation tool for heating, HVAC and service technicians.

The app focuses on fast calculations without accounts, advertising or unnecessary distractions.

Features:
- Oil burner: account for nozzle size and pump pressure and calculate oil flow and output
- Reverse calculation: convert desired output into required nozzle size
- Gas: convert volume flow and heating value into burner input/output
- Hydronics: calculate required water flow from heat output and temperature difference
- Results in l/h, l/min, m³/h and kW
- German and English
- Works offline

BrennerCalc Pro permanently unlocks the additional gas and hydronic calculators with a one-time in-app purchase.

Important:
BrennerCalc is a calculation aid for trained professionals. Manufacturer specifications, applicable standards, commissioning measurements and combustion analysis always take precedence.

## URLs

- Support: `https://kamilunavo.com/support`
- Privacy policy: `https://kamilunavo.com/brennercalc/privacy`

## Data safety

BrennerCalc itself has:
- no account;
- no advertising;
- no analytics;
- no tracking;
- no Kamilunavo backend for calculation inputs.

Calculation values stay on device. Google Play Billing is used for the optional one-time Pro entitlement. The final Play Console Data safety answers must also reflect any data handling declared by the Google Play Billing SDK and Google Play services at submission time.

## Play Console release gates

- [ ] Create app `BrennerCalc` with application ID `de.kamilunavo.brennercalc`.
- [ ] Create one-time product `de.kamilunavo.brennercalc.pro` and set price/availability.
- [ ] Complete App content, target audience, ads declaration and Data safety.
- [ ] Add privacy policy and support details.
- [ ] Upload Android App Bundle produced by Android CI.
- [ ] Run internal/closed testing and verify purchase + restore on a licensed tester account.
- [ ] Complete required closed-test period if the developer account is subject to Google's new personal-account testing requirement.
- [ ] Promote to production when all policy and testing gates are green.
