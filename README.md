# Bangla Custom Keyboard (com.custom.bang)

A full Android system keyboard (InputMethodService) with a Bangla layout,
built with Kotlin + XML, styled with a dark glassmorphism look over a
galaxy/pink-nebula background.

## ⚠️ About the background image
No image file was actually attached in our chat, so `res/drawable/keyboard_background.xml`
currently uses a **gradient placeholder** that approximates a galaxy/pink nebula look.
To use your real wallpaper:

1. Put your image at `app/src/main/res/drawable-nodpi/nebula_wallpaper.png` (or `.jpg`)
2. Open `app/src/main/res/drawable/keyboard_background.xml` and replace the `<layer-list>`
   contents with:
   ```xml
   <bitmap xmlns:android="http://schemas.android.com/apk/res/android"
       android:src="@drawable/nebula_wallpaper"
       android:gravity="fill" />
   ```

## How to open & run
1. Open Android Studio → **Open** → select the `BanglaKeyboard` folder.
2. Let Gradle sync (it needs internet access the first time to download dependencies).
3. Run the app on a device/emulator (API 24+). This installs the app and registers the IME.
4. In the app, tap **কীবোর্ড সক্রিয় করুন** (Enable keyboard) → toggle on "Bangla Keyboard"
   in system settings → go back → tap **কীবোর্ড পরিবর্তন করুন** (Switch keyboard) → pick
   "Bangla Keyboard" from the picker.
5. Open any text field (e.g. Messages) to see the keyboard.

## What's implemented
- **Layout**: exact 5 rows + bottom row you specified, built dynamically from
  `KeyboardData.kt` (easy to edit/extend there).
- **Long-press conjuncts**: e.g. long-press ক → ক্ক, ক্ত, ক্র, ক্ষ, ক্ল (see
  `KeyboardData.longPressMap` — add more entries freely).
- **?123**: toggles a Bangla-numeral + symbol layout.
- **🌐 Globe**: switches to the next system keyboard (`switchToNextInputMethod`).
- **Settings / grid icons**: open the app's `MainActivity`.
- **Mic**: launches a transparent trampoline `VoiceTypingActivity` that runs
  `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` (Google Speech) and commits the
  recognized text back into the field.
- **Image / GIF icons**: launch a trampoline `ImagePickerActivity` (system
  document picker) and commit the picked image via `InputConnectionCompat`
  / `commitContent` — works in apps that declare `EditorInfo.contentMimeTypes`
  support (e.g. Gboard-compatible chat apps). Apps that don't support inline
  images will show a toast explaining that.
- **Clipboard icon**: shows the last ~10 copied text snippets (tracked via
  `ClipboardManager.OnPrimaryClipChangedListener`), tap to re-insert.
- **Delete key**: tap to delete one character, long-press to repeat-delete.
- **Enter key**: sends the field's editor action (Search/Go/Done/etc.) when
  the app declares one, otherwise sends a literal Enter keypress.
- **Arrows (←/→)**: move the cursor left/right.

## What's stubbed / left for you to extend
- **Emoji panel** (`btnEmoji`): currently shows a toast placeholder. Hook in
  an emoji grid (RecyclerView) inside `BanglaInputMethodService` where noted.
- **Handwriting icon**: toast placeholder only — real handwriting recognition
  is a much bigger feature (ML Kit Digital Ink Recognition would be the
  natural fit if you want to build it out).
- **4×4 grid icon**: currently opens the app's settings screen; repurpose as
  you like (e.g. a themes picker).

## Permissions used
- `RECORD_AUDIO` — for the mic/voice typing feature (requested at runtime).
- `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` — for the image/GIF picker
  (Android's `ACTION_OPEN_DOCUMENT` picker generally doesn't need these on
  modern Android, but they're declared for compatibility).

## Project structure
```
BanglaKeyboard/
├── app/src/main/java/com/custom/bang/
│   ├── MainActivity.kt              # setup/settings screen
│   ├── BanglaInputMethodService.kt  # the keyboard itself
│   ├── KeyboardData.kt              # layout rows + long-press map
│   ├── VoiceTypingActivity.kt       # mic trampoline activity
│   ├── ImagePickerActivity.kt       # image/GIF picker trampoline activity
│   ├── ClipboardHistory.kt          # clipboard tracking helper
│   └── InputBridges.kt              # callbacks from trampoline activities → service
├── app/src/main/res/
│   ├── layout/                      # keyboard_view.xml, popups, activity_main.xml
│   ├── drawable/                    # backgrounds, key shapes, vector icons
│   ├── values/                      # colors, strings, styles
│   └── xml/method.xml               # IME subtype declaration
```

## Package name
`com.custom.bang`, as requested.
