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


---

## v2.4.0 addendum

A second pass over the v2.3.1 tree, concentrating on UI defects and data-layer bugs.
This environment had no Android SDK access, so every change below was verified by code
review plus the CI workflow (`./gradlew testDebugUnitTest lintDebug assembleRelease`)
rather than on a device; see the commit's CI run for results.

### UI / layout
| # | Bug | Fix |
|---|-----|-----|
| U1 | Window theme inherited `Material.Light`: white flash at launch, and `enableEdgeToEdge()` picked dark status/nav icons on light-mode phones (invisible on the dark UI) | Dark window theme + explicit `SystemBarStyle.dark` |
| U2 | Phone header did not consume status-bar insets, so it drew under the status bar | `windowInsetsPadding(statusBars)` on the header column |
| U3 | Phones in landscape (>760dp wide, ~400dp tall) got the two-pane tablet layout | Tablet mode now also requires height >= 480dp |
| U4 | Scroll-to-top, scroll reset on feed change and the FAB only knew the list state; Gallery and tablet grid views never scrolled | Grid states hoisted; all three reset / the active one animates to top |
| U5 | `AnimatedContent` kept two lazy layouts alive on one scroll state when switching styles | Plain branch |
| U6 | Edge-swipe overlay rails sat above the feed as siblings and could swallow touches for ~72dp on each side | Gesture observed on the feed container (Initial pass, never consumes) |
| U7 | Hard-coded "Xiaomi Pad 7" badge / "Command Center" copy shown on every tablet | Removed |
| U8 | Card action bar (vote + comments + 6 icons) overflowed on 360dp phones | Download / open-link moved to the existing quick-actions sheet |
| U9 | Gallery tile stats clipped on narrow tiles | Ellipsis + non-wrapping stats |
| U10 | Relay card accent bar never rendered (`fillMaxSize` in an unbounded Row) | Drawn with `drawBehind` |
| U11 | Lightbox pan limits were fixed pixel values | Derived from the viewport |
| U12 | Double-tap seek on video left it paused (first tap had toggled playback) | Playback state restored |
| U13 | Comments WebView reloaded the original URL on every recomposition | `update` block removed |

### Data / network
| # | Bug | Fix |
|---|-----|-----|
| D1 | Pagination failure re-triggered the auto-loader immediately and forever | `loadMoreFailed` state + retry button |
| D2 | A failed next-page fetch fell through to the search endpoint and appended unrelated posts | No search fallback while paging |
| D3 | Atom `<category label="r/sub">` was surfaced as every post's flair | Subreddit labels ignored |
| D4 | Atom `<content>` (thumbnail + "submitted by /u/x [link] [comments]") was used as post body, making every post a "text" post and polluting keyword filters | Only the `SC_OFF/SC_ON` markdown block is used |
| D5 | RSS posts showed `0` score/comments | `hasMetrics=false` renders `–` |
| D6 | OkHttp's bridge interceptor replaces an explicit `Cookie` header with the jar contents, dropping `reddit_session` | Session requests use a jar-less client |
| D7 | Titles containing `<...>` lost text (HTML-stripped) | Entity decode only |
| D8 | Votes on posts not in the current list (saved list, profile) were silently ignored | Falls back to the passed post |
| D9 | A modhash was fetched before every vote/save | Cached per session cookie |
| D10 | User profiles always failed signed-out (anonymous `.json` is 403) | RSS fallback and visible error |
| D11 | Multi-feed banner stayed active after selecting a normal subreddit | Derived from the target |
| D12 | Favourite subreddits stored as an unordered set | Ordered JSON (legacy set migrated) |
| D13 | One corrupt saved post discarded all following saved posts | Per-item parsing |
| D14 | Ad-hiding script removed every `[data-testid*="ad"]` element (header, thread, ...) | Anchored selectors |
| D15 | Every feed video created an ExoPlayer / JS WebView on composition | `Autoplay videos in feed` setting; embeds are tap-to-play |

### Second pass (same release)
| # | Bug | Fix |
|---|-----|-----|
| P1 | Default Cards view: `RelaySwipeablePostCard` added a second 10dp margin and painted an elevated slab behind every card at rest | Drawer and backdrop only exist while swiped; no extra margin |
| P2 | Feed cards never passed a gallery list, so the lightbox's multi-image navigation could never appear | ViewModel resolves the gallery from the post that owns the tapped URL |
| P3 | Text-to-speech silently failed on posts over the engine's 4000-char limit and could report "playing" when the engine rejected input | `SpeechChunker` queues chunks; state follows real callbacks; device language preferred |
| P4 | Search, Jump-to-subreddit, Account, Share, Multi-feed and Crosspost dialogs did not scroll (buttons unreachable in landscape / with the keyboard) | Scrollable content |
| P5 | Search dialog always opted into mature results regardless of the user's setting | Seeded from the setting |
| P6 | Invalid subreddit names were saved to "recent subreddits" and became the active feed before being rejected | Validated up front with a message |
| P7 | Multi-feed picker accepted names the feed layer silently dropped | Same validator |
| P9 | Downloads: RedGifs embeds / HLS saved as junk files; Android 8-9 lacked permission for public Downloads | Clear message; app-scoped folder pre-Android 10 |
| P10 | Read-later "open" could crash with no browser; share text showed `▲ – • 💬 –` | Guarded; metrics omitted when unknown |

### Video player pass
| # | Bug | Fix |
|---|-----|-----|
| V1 | Scrolling the feed with a finger on a video toggled play/pause on release and could trigger 2x speed | Gesture ignores movement/consumed events; cleanup in `finally` so 2x/scrub can't stick |
| V2 | No audio-focus handling: unmuting didn't pause the user's music, headphone unplug kept playing | Audio attributes + focus only while unmuted; pause on becoming noisy |
| V3 | Black box + spinner until first frame | Poster shown until the first frame renders (never for stream URLs) |
| V4 | Feed videos kept playing under the lightbox / modal reader | `LocalFeedVideosPaused`; resumes only those that were playing |
| V5 | RedGifs WebView kept running when the app was backgrounded | Paused/resumed with the lifecycle |
| V6 | Inline previews decoded full-resolution streams | Capped to 720p for feed items |
| V7 | Error text blamed "Reddit CDN" for any failure | Generic message |
| V8 | Saved-posts filter field had a fixed 50dp height (clipped text) | `heightIn(min = 56dp)` |
| V9 | Main screen collected flows while backgrounded | `collectAsStateWithLifecycle` |

### Added tests
`AtomContentTest`, `RedditPostMetricsTest`, `SpeechChunkerTest`, and an `AdBlockerTest` regression case.

### Release artifacts
The committed `redx*.apk` files are the previous v2.3.1 builds. They can only be
re-signed with the maintainer's keystore, so they are produced by CI (set the
`REDX_KEYSTORE_BASE64`, `REDX_KEYSTORE_PASSWORD`, `REDX_KEY_ALIAS`, `REDX_KEY_PASSWORD`
secrets) or locally via `keystore.properties`.


---

## v2.5.0 — features and third review pass

New in this release: Top time range, Hide post, Block subreddit, Recent searches and
Remember-last-feed (see README). Pure logic is covered by `RecentSearchesTest`,
`ContentFilterRulesTest` and new `RedditFeedServiceTest` cases (time-range URLs and
per-range cache keys).

### Tap-to-full-screen
Inline video in Big Tiles, Streamline and Social had no full-screen hook at all, and in the
other styles a tap only paused the video (and the touch also fell through to the card,
opening the post behind it). A tap on any inline feed video now opens the full-screen
player, the poster state does the same, and the tap is consumed so the card underneath does
not react. The ViewModel also treats any post's own video URL as video, so the lightbox opens
the player even when the URL has no telltale extension.

### Pre-release review (v2.5.0)
| Bug | Fix |
|---|---|
| *Block r/x* did nothing (but reported success) when the keyword/domain filter engine was switched off | Blocked subreddits apply independently of the engine toggle |
| A horizontal swipe starting at the screen edge switched feeds **and** triggered the card's own swipe (upvote/save) | The edge gesture claims (consumes) clearly-horizontal edge swipes in the Initial pass; taps and vertical scrolls are untouched |
| Filter banner said "hidden by keyword/domain filters" although it also counts blocked subreddits | "hidden by your filters" |
| Content-filter *Add* buttons were 50dp next to 56dp text fields | 56dp |
