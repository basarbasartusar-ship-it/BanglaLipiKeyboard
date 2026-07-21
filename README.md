# Bangla Lipi Keyboard

A custom Android keyboard (IME). Tap Bengali letters — the keyboard silently
converts them to Roman/English spelling and types *that* into whatever app
you're using (WhatsApp, Messenger, Notes, anything).

Example: tapping আ-ম-ি (আমি) inserts **Ami** into the text field.

## How to build it

You need **Android Studio** (free, from developer.android.com) installed on
your computer — this project can't be compiled inside this chat.

1. Download/unzip this project folder.
2. Open Android Studio → **Open** → select the `BanglaLipiKeyboard` folder.
3. Let Gradle sync (first time takes a few minutes; it downloads build tools).
4. Plug in your Android phone (with USB debugging on) or use an emulator.
5. Click **Run ▶**. This installs the app.
6. Open the installed app once — it has two buttons:
   - **কীবোর্ড সক্রিয় করুন** → takes you to Settings → turn on "Bangla Lipi (Bengali → Roman)".
   - **কীবোর্ড পরিবর্তন করুন** → opens the keyboard picker so you can switch to it.
7. Open any app, tap a text field, switch to this keyboard, and start typing.

## What's included / what's simplified

- Covers all common vowels, consonants, vowel-signs (matra), and the virama (্).
- A few rare/derived letters (ঋ, ষ, ড়, ঢ়, য়, ৎ) and digits aren't on the
  keyboard layout yet, though the transliteration engine already understands
  them — you can add keys for them by copying the pattern in
  `app/src/main/res/xml/keyboard_bengali.xml` and
  `BengaliTransliterationIME.kt`'s `codeMap`.
- The layout is a simple grid (not the exact spacing of a shipped keyboard) —
  feel free to restyle `keyboard_view.xml` and the XML layout once it's running.

## Project structure

```
app/src/main/
  AndroidManifest.xml              — declares the IME service
  java/com/example/banglalipi/
    TransliterationEngine.kt       — the Bengali → Roman conversion rules
    BengaliTransliterationIME.kt   — the keyboard service itself
    MainActivity.kt                — simple screen to enable/switch keyboard
  res/xml/
    method.xml                    — IME subtype declaration
    keyboard_bengali.xml          — key layout (which glyph on which key)
  res/layout/keyboard_view.xml     — how the keyboard looks
```
