TvPrayer - prayer times (Diyanet approximation) and adhan for Android TV (Android 8+).

Build (GitHub): create an empty repository, upload ALL these files (including the .github folder),
open Actions > "Build TvPrayer APK" > Run workflow, then download the "TvPrayer-apk" artifact.

Adhan audio (not included - use a recording you are allowed to use):
  adb push adhan.mp3 /sdcard/Android/data/com.tvremote.prayer/files/adhan.mp3
Optional texts shown on the main screen, one per line (UTF-8):
  adb push quotes.txt /sdcard/Android/data/com.tvremote.prayer/files/quotes.txt
(Open the app once first so the folder exists.)
