# Bangla Keyboard — Phase 1 (Core Keyboard)

## 📱 Building this with no PC (phone-only workflow)

This project includes `.github/workflows/build.yml`, so GitHub itself compiles the APK —
you never need Android Studio or a computer. Steps, all doable from a phone browser:

1. Create a free GitHub account if you don't have one (github.com).
2. Create a new repository (e.g. `bangla-keyboard`).
3. Upload every file/folder from this project using GitHub's web **"Add file → Upload
   files"** (same method used for the IMO Chat deployment) — including the hidden
   `.github/workflows/build.yml` file, which drives the build.
4. Go to the repo's **Actions** tab. A workflow run should start automatically (or tap
   **"Build Debug APK" → "Run workflow"** to trigger it manually).
5. Wait for the green checkmark (a few minutes).
6. Open the finished run → scroll to **Artifacts** → download **`BanglaKeyboard-debug-apk`**.
   This downloads a `.zip`; open it to get `app-debug.apk`.
7. On your phone, open that APK file from your Downloads/Files app and tap **Install**
   (you may need to allow "Install unknown apps" for your browser/file manager once).
8. Open the installed **Bangla Keyboard** app → enable it in system settings → switch to
   it in any text field, exactly as described below.

No emulator needed — you're installing and testing the real keyboard directly on your
own phone, which is actually the best way to test an IME anyway.

---

An offline-first Android IME (Input Method Editor) written in Kotlin, supporting English
(QWERTY), Bangla Probhat, Bangla Bijoy, and Banglish phonetic typing, with MVVM + Room
architecture for word learning and a Personal Dictionary.

This is **Phase 1** of a larger roadmap (see below). It is a real, compilable Android
Studio project — not a mockup — but it intentionally ships a focused feature set so every
line of code is genuine and testable, rather than a giant pile of stubbed-out features.

## What's included in Phase 1

- ✅ Real `InputMethodService` (`BanglaKeyboardService`) registered as a system keyboard
- ✅ 4 layouts: QWERTY (English), Probhat (Bangla), Bijoy (Bangla), Banglish (phonetic)
- ✅ Rule-based Banglish → Bangla transliteration engine (Avro-style)
- ✅ Symbols/numbers page, Shift/Caps-lock, Backspace (incl. long-press repeat)
- ✅ Auto-capitalization, double-space-for-period, smart backspace
- ✅ Word learning + prefix-based suggestion bar, backed by Room
- ✅ Personal Dictionary (add/export) and incognito-mode-aware learning
- ✅ Light / Dark / AMOLED themes, key sound, haptic feedback, key popup preview
- ✅ Keyboard height adjustment slider, one-handed mode toggle (setting persisted)
- ✅ Material 3 Settings screen (MVVM: `SettingsViewModel` + `SettingsActivity`)
- ✅ Unit tests for the phonetic engine and the dictionary repository
- ✅ Instrumented test for the Settings screen

## Explicitly NOT in Phase 1 (see Roadmap)

Emoji/GIF/sticker keyboard, clipboard manager, voice typing, swipe typing, floating
keyboard, TensorFlow Lite prediction, OCR, translate, downloadable language packs,
tablet/foldable-specific layouts. These are large, independent subsystems best built
(and tested) one at a time on top of this working core.

---

## Project structure

```
BanglaKeyboard/
├── app/
│   ├── src/main/java/com/banglakb/keyboard/
│   │   ├── data/                 # KeyModel, KeyboardLayoutData, enums
│   │   │   └── layouts/          # QwertyLayout, ProbhatLayout, BijoyLayout, Banglish engine
│   │   ├── db/                   # Room: WordEntity, WordDao, AppDatabase
│   │   ├── repository/           # DictionaryRepository (learning/suggestions/import-export)
│   │   ├── ime/                  # BanglaKeyboardService (IME), KeyboardView (custom View)
│   │   ├── ui/settings/          # SettingsActivity + SettingsViewModel (MVVM)
│   │   └── util/                 # Prefs (SharedPreferences wrapper), AppTheme
│   ├── src/test/                 # JVM unit tests (phonetic engine, repository)
│   ├── src/androidTest/          # Instrumented UI test
│   └── src/main/res/             # layouts, values, xml/method.xml, drawable, mipmap
├── build.gradle.kts / settings.gradle.kts / gradle.properties
└── README.md
```

### Why this architecture

- **MVVM**: `SettingsViewModel` exposes `LiveData`/state to `SettingsActivity`; the
  Activity contains no business logic, only view binding + listeners.
- **Repository pattern**: `DictionaryRepository` is the single source of truth for word
  suggestions/learning, sitting between the Room `WordDao` and both the IME service and
  the Settings screen — so both can share the same dictionary without duplicating logic.
- **Room**: `words` table stores both auto-learned words and user-added Personal
  Dictionary entries (`isUserAdded` flag distinguishes them), enabling word-frequency-based
  prediction entirely offline.
- **IME layer is layout-agnostic**: `BanglaKeyboardService` doesn't know Bangla-specific
  details — it just renders whatever `KeyboardLayoutData` is active and asks the
  `BanglishPhoneticEngine` to transliterate when `isPhonetic == true`. Adding a new layout
  is just adding a new `KeyboardLayoutData` object.

---

## ⚠️ Important accuracy note on Probhat/Bijoy layouts

`ProbhatLayout.kt` and `BijoyLayout.kt` contain a **best-effort starting key-map**, clearly
flagged with `NOTE ON ACCURACY` comments in the source. Official Probhat/Bijoy charts are
published by BSTI/Bangladesh Computer Council and by the Keyman/SIL and Avro/UniBijoy
projects — before shipping this to real users who touch-type in Probhat or Bijoy, verify
every key against an authoritative chart source, e.g.:
- Probhat: https://help.keyman.com/keyboard/bangla_probhat/current-version
- Bijoy/UniBijoy: official UniBijoy layout chart

Every key is defined in one flat list per file specifically so corrections are one-line
changes.

---

## Build instructions

### Requirements
- Android Studio Koala (2024.1) or newer
- JDK 17 (bundled with recent Android Studio)
- Android SDK Platform 35, Build-Tools 35.x

### Steps
1. Open Android Studio → **Open** → select the `BanglaKeyboard/` folder.
2. Android Studio will detect there's no `gradle-wrapper.jar` yet (only
   `gradle-wrapper.properties` is included, since it must be a binary file generated by
   Gradle, not hand-written). When prompted, click **"OK"**/**"Sync Now"** — Android
   Studio will offer to regenerate the wrapper automatically. Alternatively, if you have
   Gradle installed locally, run: `gradle wrapper --gradle-version 8.9` inside the project
   root once before opening it in Android Studio.
3. Let Gradle sync (downloads AGP 8.5.2, Kotlin 1.9.24, KSP, Room, etc.)
4. Run the `app` configuration on an emulator (API 29+) or physical device.
5. On first launch you'll see the Settings screen. Tap **"Enable in system settings"**,
   turn on **Bangla Keyboard** under *System → Languages & input → On-screen keyboard*.
6. Tap **"Switch keyboard"** (or use the global keyboard-switch button) and pick
   **Bangla Keyboard** in any text field to start typing.
7. Use the 🌐 language-switch key on the keyboard itself to cycle
   QWERTY → Probhat → Bijoy → Banglish.

### Running tests
- Unit tests: `./gradlew testDebugUnitTest`
- Instrumented tests (needs a connected device/emulator): `./gradlew connectedDebugAndroidTest`

---

## Roadmap (Phase 2+)

Each of these is intended to be built as its own vertical slice on top of the Phase 1
core, in roughly this order:

1. Emoji keyboard + search + recent emoji
2. Clipboard manager (with pin + search)
3. Text editing toolbar (select all / cut / copy / paste / share), cursor control, gesture delete
4. Multi-language word prediction upgrade: next-word prediction, autocorrect, spell checker
5. Floating keyboard + one-handed resize handles (currently only a persisted setting, no
   floating window yet)
6. Themes: custom background image, key border styling, key press animation
7. GIF/sticker support (likely via a Giphy-compatible content provider)
8. Voice typing (`RecognizerIntent` / `SpeechRecognizer`) and swipe typing (gesture path decoding)
9. TensorFlow Lite on-device prediction model (replacing the simple frequency-based
   suggester with a real language model), downloadable language packs
10. OCR text extraction, translate-selected-text
11. Backup/restore of full settings (Personal Dictionary import/export already works)
12. Tablet/foldable layouts, landscape optimization, accessibility (TalkBack) pass
13. Crash logging (e.g. Firebase Crashlytics behind a privacy toggle, since this app is
    privacy-first / local-processing by default)

---

## Privacy

All word learning and suggestions are processed and stored **locally** in Room —
nothing is sent to a server. Incognito mode (`EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING`,
set automatically by password/incognito fields) disables word learning for that session.
