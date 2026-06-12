# JED — OBD-II Diagnostic App

## Overview

JED is a native Android app (Kotlin + Jetpack Compose) that interfaces with any
ELM327-style OBD-II adapter over **Bluetooth Classic (SPP), Bluetooth LE, or
Wi-Fi**. It works with any 1996+ OBD-II vehicle (all makes) for live data,
trouble codes, emissions readiness, and vehicle info, with extra guidance for
Ford/GM. The app also recommends which adapter to buy for a given make/model.

> **Implementation update (2026-06-12).** This spec was the original design.
> The shipped app diverges from it deliberately, after a correctness review:
>
> - **Multi-adapter:** not OBDx-Pro-VX-only — Bluetooth Classic, BLE, and Wi-Fi
>   are all supported behind an `ObdTransport` abstraction (`transport` package).
> - **All makes:** generic OBD-II works for every 1996+ vehicle, not just Ford/Chevy.
> - **Removed fabricated features:** Ford PATS PIN extraction and the parameter
>   "tuning" / relearn *write* commands were deleted. They sent invented UDS
>   commands that cannot work over a generic ELM327. The PIN database / fake
>   "encryption" were removed with them.
> - **Service tab (replaces Tuning):** emissions readiness monitors + vehicle
>   info (VIN / Calibration ID / CVN) — all real, read-only OBD-II queries.
> - **Security & Keys tab:** honest guidance only — GM owner relearn timers
>   (no scanner command needed) and Ford onboard spare-key steps, with a clear
>   note that PIN extraction needs dealer/locksmith tools.
> - **DTC database:** ships a curated set of ~120 common standardized codes
>   (not the 15,000 originally claimed).

**UI Style:** Industrial / rugged — dark charcoal backgrounds, high-contrast white/orange/red indicators, large touch targets for garage use.

---

## Architecture

```
┌─────────────────────────────────────┐
│          UI Layer (Compose)         │
│  Dashboard │ Diag │ Tune │ Keys    │
├─────────────────────────────────────┤
│         ViewModel Layer             │
│   State management, data flows      │
├─────────────────────────────────────┤
│        Protocol Layer               │
│  OBD-II │ Ford PATS │ GM Enhanced   │
│  UDS    │ J1850     │ CAN           │
├─────────────────────────────────────┤
│     ELM327 Command Interface        │
│  AT commands, request queuing,      │
│  response parsing, protocol switch  │
├─────────────────────────────────────┤
│    Bluetooth SPP Transport          │
│  Connect, reconnect, read/write     │
└─────────────────────────────────────┘
```

---

## Module 1: Bluetooth SPP Transport

- Scans for paired devices, filters by OBDx Pro VX device name
- Connects via `BluetoothSocket` on standard SPP UUID
- Background `CoroutineScope` for read/write — never blocks UI thread
- Auto-reconnect with exponential backoff on connection drop
- Connection state exposed as `StateFlow` for UI binding

## Module 2: ELM327 Command Interface

- Sequential command queue — one command at a time, response-paired
- Initialization sequence on connect:
  - `ATZ` (reset), `ATE0` (echo off), `ATL0` (linefeeds off), `ATS0` (spaces off), `ATH1` (headers on), `ATSP0` (auto-detect protocol)
- Ford MS-CAN access via `ATSH` header switching + `ATCF/ATCM` CAN filters
- 2-second response timeout, configurable for slow operations
- Raw hex frame parsing with CRC validation

## Module 3: Protocol Auto-Detection

- On first connect, sends `0100` (supported PIDs query) and reads protocol header
- Ford J1850 PWM → `AT SP 1`
- GM J1850 VPW → `AT SP 2`
- CAN 500kbps (most 2004+) → `AT SP 6`
- Stores detected protocol per vehicle profile for faster reconnects

---

## Module 4: Live Dashboard

**Gauges & Layout:**
- Default 4-6 gauge grid: RPM (large center), Coolant Temp, Vehicle Speed, Engine Load, Intake Air Temp, Fuel Trim
- Custom Canvas-drawn gauges with sweep animations, color zones (green/yellow/red)
- Users can add/remove/rearrange gauges from full PID list
- Tap any gauge to expand into scrolling line graph
- ~5-10 readings per second depending on active PID count

**Data Logging:**
- Toggle recording to local SQLite — timestamped PID values
- Export as CSV
- Saved per vehicle profile

**Alerts:**
- Configurable thresholds (e.g., coolant > 230F → red flash + vibration)
- Check Engine Light indicator always visible in status bar

---

## Module 5: Tuning

Parameter adjustments exposed through diagnostic commands — not full ECU reflashing.

**Ford (EEC-V / PCM, 1996-2015):**
- Idle speed adjustment
- Timing advance offset
- Short/long term fuel trim monitoring & reset
- TPS relearn
- Idle air control valve reset
- KOEO / KOER self-tests
- Transmission adaptive shift relearn/reset

**GM (PCM/ECM, 1996-2015):**
- Idle relearn procedure
- Fuel trim reset
- Crankshaft position variation relearn
- Transmission shift adapt reset
- Throttle body relearn

**Safety Guardrails:**
- Confirmation dialog before every write operation
- Read-before-write — shows current value alongside proposed change
- No direct hex editing — only predefined, tested parameter adjustments
- "Restore defaults" stores original values before changes

**UI:** Organized by system (Engine, Transmission, Fuel, Sensors). Each parameter shows current value, acceptable range, slider or +/- buttons. Large bold text.

---

## Module 6: PIN Retrieval & Key Programming

**Ford PATS (Passive Anti-Theft System):**
- Covers PATS I, II, and III (1996-2015 F-150, Mustang, Explorer, Crown Vic, etc.)
- Reads PATS module via MS-CAN bus to retrieve 4-digit security PIN (incode)
- Step-by-step guided key programming wizard:
  1. Connect scanner, retrieve PIN
  2. Guided timed key insertion sequence
  3. Supports adding new keys and erasing lost keys
- Displays number of keys currently programmed
- Stores PINs in encrypted local database tied to VIN

**GM Passlock / PK3 / PK3+:**
- Passlock relearn procedure (10-minute reset with guided timer)
- Theft deterrent relearn
- PK3/PK3+: reads security info, guides through relearn process
- No simple PIN — GM uses relearn procedures; app guides each step with on-screen timers and vibration alerts

**UI:** Wizard-style — big numbered steps, one screen per step. Vehicle selector narrows to correct procedure. Built-in timer for timed steps. PIN in large bold font with copy button. History log of VINs and retrieval dates.

---

## Module 7: DTC (Trouble Codes)

**Reading & Clearing:**
- Read current DTCs (check engine codes)
- Read pending DTCs
- Read freeze frame data (sensor snapshot at time of code)
- Clear codes with two-step confirmation
- Check Engine Light status indicator

**Code Database:**
- Offline database of ~15,000 OBD-II codes
- Standard P0xxx-P3xxx codes
- Ford-specific manufacturer codes with descriptions
- GM-specific manufacturer codes with descriptions
- Each code: code number, short description, detailed explanation, common causes, severity

**UI:** Color-coded list (red/yellow/gray by severity). Tap to expand details. Clear All with confirmation. Per-vehicle history log.

---

## Module 8: Vehicle Profiles & Navigation

**Profiles:**
- Add vehicles: Make → Model → Year picker (Ford/Chevy, 1996-2015)
- Auto-reads VIN on connect
- Stores: nickname, VIN, detected protocol, saved PINs, DTC history, data logs
- Quick-switch from home screen

**Navigation:**
- Bottom nav: Dashboard | Diag | Tune | Keys
- Top bar: connection status, current vehicle name, battery voltage
- Home screen: saved vehicles as large cards — tap to connect

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Bluetooth | Android BluetoothSocket (SPP) |
| Database | Room (SQLite) |
| Architecture | MVVM + StateFlow |
| DI | Hilt |
| Build | Gradle + AGP |
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 34 (Android 14) |

---

## Out of Scope

- iOS support
- Full ECU flash tuning / calibration file editing
- OBD-I (pre-1996) vehicles
- CAN-FD (2020+ vehicles)
- Cloud/server features — fully offline app
- Makes other than Ford and Chevrolet
