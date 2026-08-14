# BrennerCalc

BrennerCalc is a compact iPhone/iPad utility for heating and service technicians.

## MVP

- **Ölbrenner** — nozzle size (USgal/h), pump pressure, calorific value and efficiency → actual flow and burner output. Also works in reverse from desired output to required nozzle size.
- **Gas** — gas volume flow, calorific value and efficiency → input/output power. Pro.
- **Heizwasser** — heat output + ΔT → required water flow in l/h, l/min and m³/h. Pro.
- **DE / EN** language selector.
- **BrennerCalc Pro** as a one-time non-consumable StoreKit 2 unlock. Product ID: `de.kamilunavo.brennercalc.pro`.
- No account, backend, analytics or ads.

## Technical basis

- SwiftUI
- iOS / iPadOS 17+
- StoreKit 2
- Bundle ID: `de.kamilunavo.brennercalc`

## Calculation assumptions

### Oil nozzle

Oil nozzle capacities are treated as ratings at **7 bar**. The pressure correction uses:

`Q₂ = Q₁ × √(p₂ / 7 bar)`

The user can adjust the fuel calorific value and combustion efficiency. The app therefore does not assume a fixed burner efficiency.

### Gas

`P_input [kW] = volume flow [m³/h] × calorific value [kWh/m³]`

`P_output = P_input × efficiency`

### Heating water

For water near typical heating-system temperatures:

`flow [l/h] = power [kW] × 1000 / (1.163 × ΔT [K])`

## Important

BrennerCalc is a calculation aid for trained professionals. Manufacturer specifications, applicable standards, commissioning measurements and combustion analysis always take precedence.

## Sources

- Danfoss Burner Components / Oil Nozzles — nozzle reference point and burner nozzle calculator documentation.
- Apple StoreKit documentation — non-consumable In-App Purchase and StoreKit 2.

© 2026 Kamilunavo
