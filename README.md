# ShieldOS

Native Android TV launcher for NVIDIA Shield TV Pro.

## Features (v0.1)
- D-pad navigation and remote-friendly app grid
- Lists launchable TV apps, with fallback to other launchable apps
- App launching via PackageManager
- Dark TV layout, live clock, quick access to Android settings
- Home launcher intent filter; no root required

## Build
GitHub Actions builds a debug APK on pushes to main. Download it under **Actions > ShieldOS APK > latest run > Artifacts**.

Manual build (JDK 17, Android SDK): `gradle assembleDebug`.

## Install over LAN
Enable Network debugging on Shield TV, then:
```bash
adb connect SHIELD_IP:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Accept the Android debugging authorization on the TV. Press HOME and choose ShieldOS if the OS shows the launcher chooser. Keep the stock launcher installed until ShieldOS has been tested.

Package: `com.shieldos.launcher`.
