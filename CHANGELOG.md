# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2026-10-26

### Added
- Add **Telegram Bot Integration** (`MowatibTelegramManager` and `MowatibTelegramSettingsScreen`) allowing real-time synchronization between the app and Telegram.
- Add deep linking to Telegram bots (`@MowatibAppBot` and `@userinfobot`) to retrieve Chat IDs in a single tap.
- Add instant connection tester that dispatches verification messages to Telegram directly via Bot API.
- Add automated Telegram notification triggers for habit completions, Pomodoro sessions, daily recaps, and screen time limit breaches (accountability partner mode).
- Add support for custom self-hosted Bot Tokens via `@BotFather`.
- Add 7 Android Glance home screen widgets with Material 3 Expressive shapes:
  - `MowatibFocusWidget`: Real-time streak tracking, current focus state, and quick launch.
  - `MowatibPomodoroWidget`: Interactive focus timer with direct start/pause controls.
  - `MowatibTasksWidget`: Daily checklist with toggleable task status.
  - `MowatibHabitsWidget`: Habit progression, streaks, and completion state.
  - `MowatibMotivationalQuoteWidget`: Curated daily Arabic wisdom and discipline quotes.
  - `MowatibQuickDashboardWidget`: Glanceable summary of screen time, tasks, and habit streaks.
  - `MowatibAmbientAudioWidget`: Quick toggle for ambient concentration audio.
- Add Smart Tasks management system (`MowatibTaskManager` and `MowatibTaskCard`) with category tagging, persistence, and completion tracking.
- Add Habit & Streak tracking engine (`MowatibHabitsCard` and `MowatibHabitsManager`) with streak counts and automated daily resets.
- Add Ambient Focus Audio engine (`MowatibAmbientSoundManager` and `AmbientSoundCard`) for concentration soundscapes.
- Add bundled offline font family **IBM Plex Sans Arabic** (Regular, Medium, SemiBold, Bold) in `res/font/`.
- Add bundled offline font family **Thmanyah Sans** (Regular, Medium, Bold).
- Add Material Vector Drawables (SVGs / XML vectors) replacing all Unicode chat emojis:
  - `ic_telegram`, `ic_fire_department_outlined`, `ic_timer`, `ic_bolt`, `ic_phone_android`, `ic_star`, `ic_task_alt`, `ic_radio_button_unchecked`, `ic_spa`, `ic_headphones`, `ic_auto_awesome`, `ic_play_arrow`, `ic_pause`.

### Changed
- Change `applicationId` to `com.bizenx.mowatib` to eliminate package conflicts and allow seamless side-by-side installation with legacy packages.
- Change internal broadcast actions and authorities from `com.etrisad.zenith.action.*` to `com.bizenx.mowatib.action.*`.
- Change default system font to IBM Plex Sans Arabic for native RTL Arabic and Latin typographical balance.
- Overhaul color palette to warm Mowatib creamy tones (`#FDFBF7` light, `#1C1B18` dark) with earthy olive accents (`#3D5622` and `#D2E3B8`).
- Update all metadata, descriptions, app branding, screenshots, and store assets to reflect Mowatib by biZenX.

### Fixed
- Fix application collision preventing installation when Zenith was installed on the same device.
- Fix broken Arabic text layout and bidirectional glyph reversal in mixed-language labels.
- Fix font selector dropdown crash and missing font weight definitions.
- Fix unresolved references in Glance widgets for daily usage data synchronization.
- Fix UI inconsistent text rendering across varying Android OS versions.

### Security
- Isolate `FileProvider` authority to `com.bizenx.mowatib.fileprovider` to prevent intent interception by third-party packages.
- Restrict internal broadcast receivers to explicit Mowatib action namespaces.
- Remove external Ko-fi donation hooks, telemetry, and unverified external links.

---

## [1.0.0] - 2026-10-23

### Added
- Initial fork and foundation release under GPLv3.
- Core screen time monitoring and app blocking mechanisms.
- PausePoint mindful pause and friction screens.
- Basic Pomodoro and bedtime focus scheduling.
