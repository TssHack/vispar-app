<p align="center">
  <img src="app/src/main/res/drawable-nodpi/splash_logo.png" alt="Vispar" width="140">
</p>

<h1 align="center">Vispar</h1>

<p align="center">
  <b>Movies and series behind liquid glass.</b><br>
  A native Android and Android TV client, written in Kotlin and Jetpack Compose.
</p>

<p align="center">
  <a href="https://github.com/TssHack/vispar-app/actions/workflows/android.yml"><img alt="Android builds" src="https://github.com/TssHack/vispar-app/actions/workflows/android.yml/badge.svg"></a>
  <a href="https://github.com/TssHack/vispar-app/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/TssHack/vispar-app?label=release"></a>
  <img alt="Kotlin 2.4" src="https://img.shields.io/badge/Kotlin-2.4-7F52FF?logo=kotlin&logoColor=white">
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white">
  <img alt="Android 7.0+" src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white">
  <a href="LICENSE"><img alt="MIT license" src="https://img.shields.io/badge/license-MIT-black"></a>
</p>

<p align="center">
  <a href="https://tsshack.github.io/vispar-website/"><b>Website</b></a> · <a href="https://github.com/TssHack/vispar-app/releases/latest"><b>Download</b></a> · <a href="README.fa.md">فارسی</a> · <a href="https://ehsanfazli.ir">Developer</a>
</p>

<!-- screenshots:hero -->

---

## Why Vispar

Vispar is a Persian-first streaming client that is built natively for Android. The whole interface is drawn as liquid glass: panels blur and bend what is behind them, buttons swell under the finger, and the player controls refract the film that is playing.

- **One app, two rooms.** A floating glass tab bar on phones and tablets, a sidebar with focus rings and D-pad navigation on Android TV.
- **Right-to-left from the start.** Persian is the default language, English is one tap away, and layout direction follows the language.
- **Small.** The release APK is about 3.7 MB.

## Highlights

| | |
| --- | --- |
| **Liquid glass interface** | Real-time blur, lens refraction and specular highlights, with four materials to choose from: Clear, Glass, Frosted and No transparency. |
| **Player** | Built on Media3 ExoPlayer with HLS and DASH. Playback speed from 0.75x to 2x, configurable skip interval, resume from the last position, autoplay, styled subtitles and a buffering indicator. |
| **Continue watching** | A shelf on the Movies and Series tabs that picks up where you stopped. |
| **Favourites** | Save films and series, remove them one by one or clear the list. |
| **Search** | One search across movies and series. |
| **Account and sync** | Sign in with a mobile number and an SMS code. Favourites and Continue Watching stay the same on your phone and your TV, and you can sign out other devices remotely. |
| **Downloads** | Pick a quality and hand the file to the browser, ADM or VLC. |
| **Personal** | Light, dark or system theme, monochrome or system accent colours, compact posters, haptic feedback. |
| **Android TV** | Launcher banner, glass sidebar, focus rings on every control, and a player you can drive with the remote. |

<!-- screenshots:phone -->

<!-- screenshots:tv -->

## Download

Every [release](https://github.com/TssHack/vispar-app/releases/latest) carries signed APKs in two editions.

| Edition | File | Use it for |
| --- | --- | --- |
| Phone and tablet | `Vispar-<version>-android-<abi>.apk` | Phones and tablets. It also switches to the TV interface on a device that reports itself as a television. |
| Android TV | `Vispar-<version>-android-tv-<abi>.apk` | TVs and TV boxes. It always uses the TV interface, including on boxes that identify as a tablet. |

Pick the architecture that matches the device, or take `universal` if you are not sure.

| `<abi>` | Devices |
| --- | --- |
| `universal` | Everything. Slightly larger. |
| `arm64-v8a` | Most phones from the last several years, newer TVs and boxes. |
| `armeabi-v7a` | Older phones and many TV boxes. |
| `x86_64`, `x86` | Emulators and Intel-based devices. |

Both editions share one application ID and one signing key, so either can be installed over the other. `SHA256SUMS.txt` in each release lists the checksums.

Vispar needs Android 7.0 (API 24) or newer. Full blur needs Android 12, and refraction needs Android 13; older devices fall back to a frosted tint.

## Remote control

| Key | In the app | In the player |
| --- | --- | --- |
| D-pad | Move focus | Left and right seek, up and down show the controls |
| OK | Open | Play or pause |
| Back | Go back | Close the player |
| Media keys | | Play, pause, seek |

## Build from source

You need JDK 17 or newer and the Android SDK (API 37). Android Studio installs both.

```bash
git clone https://github.com/TssHack/vispar-app.git
```

```bash
cd vispar-app
```

```bash
./gradlew assembleRelease
```

The APKs land in `app/build/outputs/apk/mobile/release` and `app/build/outputs/apk/tv/release`. To build one edition only, run `assembleMobileRelease` or `assembleTvRelease`.

### Signing

Without a key, release builds are signed with the debug key so they still install for testing. To sign with your own key, set these before building:

| Variable | Value |
| --- | --- |
| `VISPAR_KEYSTORE` | Path to the keystore file |
| `VISPAR_KEYSTORE_PASSWORD` | Keystore password |
| `VISPAR_KEY_ALIAS` | Optional. Defaults to `vispar` |
| `VISPAR_KEY_PASSWORD` | Optional. Defaults to the keystore password |

### Releases

[`android.yml`](.github/workflows/android.yml) builds both editions for every architecture on each push to `main`. Pushing a tag that matches the version, such as `v1.0.0`, signs the APKs with the key stored in the repository secrets `VISPAR_KEYSTORE_BASE64` and `VISPAR_KEYSTORE_PASSWORD`, checks every signature, and attaches the files to the GitHub release.

## Project layout

```
app/src/main/java/com/fazli/vispar
├── MainActivity.kt            Single activity, navigation host
├── VideoPlayerActivity.kt     Player
├── account/                   Sign-in, sync, announcements and forced updates
├── data/                      API client, models and repositories
├── navigation/                Routes, glass tab bar, TV sidebar
├── screens/                   Movies, series, details, search, favourites, settings
├── ui/glass/                  The liquid glass design system
├── ui/theme/                  Theme and theme settings
└── utils/                     Device, language, storage and watch history helpers
```

### The glass design system

Everything on screen is assembled from a small set of components in `ui/glass`:

| Component | What it does |
| --- | --- |
| `AmbientBackground` | The neutral canvas: pure black in dark mode, white to system grey in light mode. |
| `GlassSurface`, `glass()` | Blurred, saturated, lens-refracted panels. Nested controls refract their parent panel. |
| `LiquidButton` | Capsule buttons that swell and follow the finger like a drop of water. |
| `LiquidTabs` | The floating tab bar and segmented controls, with a draggable lens that stretches with velocity. |
| `LiquidSlider` | A glass thumb that magnifies the track while dragged. |
| `LiquidToggle` | A switch with a liquid glass thumb. |
| `GlassScaffold` | A floating glass top bar with a progressive blur under the status bar. |
| `GlassDialog` | Dialogs drawn in an in-window overlay so they can refract the whole screen. |

The player renders into a `TextureView`, which is what lets its controls refract the video.

## Built with

- [Kotlin](https://kotlinlang.org) and [Jetpack Compose](https://developer.android.com/compose) with Material 3
- [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) (Backdrop) for blur, refraction and highlights
- [Media3 ExoPlayer](https://developer.android.com/media/media3) for playback
- [Coil](https://coil-kt.github.io/coil/) for images
- [OkHttp](https://square.github.io/okhttp/), kotlinx.serialization and coroutines for networking
- Navigation Compose and Leanback

## Related

[TssHack/vispar](https://github.com/TssHack/vispar) is the cross-platform Vispar, built with React Native for Android, iOS, the web and desktop.

## Contributing

Issues and pull requests are welcome. For anything larger than a small fix, please open an issue first so the approach can be agreed before you write the code.

## License

[MIT](LICENSE)

## Author

**Ehsan Fazli** · [ehsanfazli.ir](https://ehsanfazli.ir) · [@TssHack](https://github.com/TssHack)
