# RedX v2.3.0 Audit Report

Full audit of the repository's v2.2.0 source tree and committed `redx.apk`, with the
issues found and fixes applied in v2.3.0. A separate user-provided APK was not present
outside the repository, so runtime verification below covers the rebuilt v2.3.0 APK.
All changes are verified by `./gradlew testDebugUnitTest lintDebug assembleRelease`.

## Summary

| Metric | v2.2.0 | v2.3.0 |
| --- | ---: | ---: |
| Release APK size | 16.8 MB | **3.38 MiB signed (3.54 MB decimal)** |
| Android Lint findings (debug) | 46 | **0** |
| Force-unwrap (`!!`) sites in UI/network | 12 | **0** |
| Unit tests | 23 | **29** |
| R8 code/resource shrinking | disabled | **enabled** |
| Release signing | debug key | **real keystore or unsigned** |
| compileSdk / targetSdk | 36 | **37** |

## Critical correctness bugs

### 1. Crash on race between state updates and modal rendering
`RedXMainScreen` force-unwrapped `uiState.selectedPost`, `quickActionsPost`,
`crosspostTargetPost`, `viewedUserName`, and `activeFlairFilter` after a separate
null check. Because the state flow can emit between the check and the read
(for example a logout or `selectPost(null)` landing mid-composition), these were
genuine `NullPointerException` sites in the detail sheet, quick-actions sheet,
crosspost dialog, and profile viewer.

**Fix:** capture each nullable into a local val and smart-cast, so the composable
renders a consistent snapshot.

### 2. Theme switching did not repaint the feed
`RedXPaletteState.current` was a plain `var` written during composition. Compose
could not observe it, so every custom component (`AmoledBackground`, `RedditOrange`,
`TextPrimary`, and 15 more accessors) kept rendering the previous theme's colors
until an unrelated recomposition happened. Switching themes produced a half-painted
mixed-palette UI.

**Fix:** back the palette with `mutableStateOf` and assign it inside `SideEffect`
so composition stays side-effect free while the accessors are properly observable.
Covered by `RedXPaletteStateTest`.

### 3. Videos restarted themselves after leaving the app
The player's lifecycle observer resumed playback on `ON_RESUME` whenever
`autoPlay` was true, ignoring that the user had deliberately paused the video.
Returning from a background app restarted audio unexpectedly.

**Fix:** record the real playing state at `ON_PAUSE` and only resume what was
actually playing.

### 4. Battery drain from a permanent polling loop
`VideoPlayerView` polled `currentPosition` every 200 ms forever, including while
paused, buffering-idle, or off-screen. With several cards composed this kept the
CPU awake continuously.

**Fix:** the polling loop now runs only while playing or scrubbing, and takes one
final position sample when it stops.

### 5. Mute toggle ignored the caller's setting
`mutedState` was initialised once from `isMuted` and never resynchronised, so
changing the mute preference did not affect already-composed players.

**Fix:** a `LaunchedEffect(isMuted)` keeps the player volume and icon in sync.

### 6. Error banners never went away
Vote failures, save failures, crosspost results, and sign-in prompts all wrote to
`errorMessage` and left it set. A single transient failure pinned an error above
the feed for the rest of the session.

**Fix:** `showTransientMessage()` in the ViewModel auto-clears after 5 seconds and
cancels any superseded message, with `clearError()` cancelling the timer.

### 7. Fragile `Result` unwrapping in the feed pipeline
`RedditFeedService.fetchFeed` used `getOrNull()!!` in three places after separate
`isSuccess` checks. The logic was correct only by coincidence and would crash if
the checks and the unwrap ever drifted apart.

**Fix:** unwrap once with `getOrNull()?.takeIf { it.isNotEmpty() }` and rebuild the
success result from the concrete value.

## Release engineering and security

### 8. Release builds were signed with the debug key
`signingConfig = signingConfigs.getByName("debug")` shipped a publicly known key,
meaning any party could produce an "update" that Android would accept as the same
app.

**Fix:** real signing config read from git-ignored `keystore.properties` or
`REDX_KEYSTORE_*` environment variables. When no key is configured, the release
build is left unsigned rather than silently debug-signed. `keystore.properties`,
`*.jks`, and `*.keystore` are now git-ignored.

### 9. No code shrinking: 16.8 MB APK
`isMinifyEnabled = false` shipped the entire Compose, Media3, Coil, and OkHttp
surface unshrunk.

**Fix:** R8 with resource shrinking enabled, plus real ProGuard rules that keep
line numbers for readable crash reports and silence the optional OkHttp platform
providers. Result: **16.8 MB to 3.38 MiB signed (3.54 MB decimal), an 80% reduction.**

### 10. Debug and release builds could not coexist
Both used `com.example.redx`, so installing a test build removed the release one.

**Fix:** debug builds use the `.debug` application ID suffix and a `-debug`
version name suffix.

## Dependencies and platform

### 11. Eleven outdated dependencies, one stale toolchain
Lint reported ten `GradleDependency` and two `NewerVersionAvailable` findings.

**Fix:** core-ktx 1.18.0 to 1.19.0, lifecycle 2.10.0 to 2.11.0, Compose BOM
2026.03.01 to 2026.09.00, OkHttp 5.3.0 to 5.5.0, Media3 1.11.0 to 1.11.1, Kotlin
Compose plugin 2.3.20 to 2.4.20, and compileSdk/targetSdk 36 to 37 (required by
the newer AndroidX artifacts).

## Code quality and UI polish

### 12. 27 non-idiomatic SharedPreferences and Uri call sites
Replaced manual `prefs.edit().put(...).apply()` chains with `androidx.core.content.edit {}`
across all six persistence managers and the ViewModel, and `Uri.parse` /
`TextUtils.htmlEncode` with the `toUri()` / `htmlEncode()` KTX extensions. This
also removes the risk of a forgotten `.apply()` silently dropping a write.

### 13. Inaccessible dismiss controls
The error banners used a bare `Text("✕")` with `clickable`, giving no content
description for TalkBack and a touch target far below the 48 dp minimum.

**Fix:** real `IconButton` + `Icons.Default.Close` with a `contentDescription`.

### 13a. Six of nine toolbar actions were off-screen
Found by running the signed build on an emulator: the phone header packed nine
`IconButton`s into a `horizontalScroll` row. On a normal phone width only Search,
Refresh, and Account were reachable; View style, Hide-read, Filters, Saved,
Jump-to-subreddit, and Settings were scrolled out of view with no affordance
indicating they existed.

**Fix:** the three highest-frequency actions stay on the bar and the remaining six
move into a proper Material 3 overflow `DropdownMenu` with labels and icons, themed
to the active palette with a visible border.

### 13b. Settings header showed a hardcoded, wrong version
`SettingsSheet` rendered a literal `"v2.0 • AMOLED Edition"` even though the app
shipped as 2.1.0 and 2.2.0.

**Fix:** the label now reads `versionName` from `PackageManager`, so it can never
drift from the actual build again.

### 14. Compose API convention violations
Four composables (`PostDetailContent`, `SubredditBar`, `VideoPlayerView`, and the
phone screen) placed `modifier` after other optional parameters, breaking the
documented Compose ordering convention. All call sites use named arguments, so
reordering is source compatible.

### 15. Bitmaps in a densityless folder
`redx_logo.png` and `ic_launcher_foreground.png` sat in `res/drawable/`, causing
Android to rescale them per device. Moved to `res/drawable-nodpi/`.

## Test coverage added

- `RedXPaletteStateTest` — palette updates propagate to every accessor; all five
  themes resolve to distinct accents.
- `RedditFeedServiceTest` — four new cases covering relevance-sort fallback,
  rising-to-new search degradation, front-page endpoints, and query URL encoding.

## CI

The workflow now installs platform 37, uploads the release APK as a build
artifact, and always uploads the lint HTML report for inspection.

## Verification

```
./gradlew testDebugUnitTest lintDebug assembleRelease assembleDebug
BUILD SUCCESSFUL
29 tests passed, 0 lint findings
app-release.apk  3.38 MiB signed (3.54 MB decimal)
```

### On-device verification

The signed release APK was installed and exercised on an Android 14 emulator:

- App installs and launches with no `FATAL EXCEPTION` in logcat.
- The live Reddit feed loads and renders posts, flairs, vote bars, and thumbnails.
- The new overflow menu opens and shows all six secondary actions.
- Switching to Matrix Emerald repaints the dialog *and* the entire feed instantly
  (top bar, chips, sort bar, card accents), confirming the palette reactivity fix,
  and the choice persists across an app restart.

### Scope and remaining limits

- The audit covered the checked-in v2.2.0 implementation and its committed APK, then
  validated the rebuilt v2.3.0 release on an Android 14 emulator.
- It did not independently exercise Reddit account authentication, authenticated
  voting, media playback across every provider, or production API rate-limit behavior.
- The release APK committed to the repository is signed with a local release keystore;
  the keystore is intentionally ignored and is not published. Future updates must use
  the same signing key, or Android will treat them as a different app.
