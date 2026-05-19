<p align="center">
  <img src="https://raw.githubusercontent.com/catppuccin/catppuccin/main/assets/misc/transparent.png" height="30" width="0px"/>
  <h1 align="center">📺 IPTV Coco</h1>
  <p align="center">A modern, fast Android IPTV client for <b>Phones, Tablets & Android TV</b></p>
  <p align="center">
    <img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white" />
    <img src="https://img.shields.io/badge/Android-24%2B-3DDC84?logo=android&logoColor=white" />
    <img src="https://img.shields.io/badge/Android%20TV-Optimized-FF6F00?logo=androidtv&logoColor=white" />
    <img src="https://img.shields.io/badge/Media3-ExoPlayer-orange?logo=google&logoColor=white" />
    <img src="https://img.shields.io/badge/Lint-0%20Errors-brightgreen?logo=androidstudio&logoColor=white" />
    <img src="https://img.shields.io/badge/License-MIT-blue.svg" />
  </p>
</p>

---

## 🎯 What is IPTV Coco?

**IPTV Coco** is a lightweight, high-performance IPTV streaming app built for Android. It supports both **M3U playlists** and **Xtream Codes API**, with full support for **Live TV**, **Movies**, and **TV Series**. The app is uniquely optimized for both **touch devices** (phones/tablets) and **Android TV** — automatically adapting its UI and navigation to the device you're on.

The app features a **premium Apple TV-inspired design** with deep blacks, subtle blue accents, and generous rounded corners for a cinematic feel.

---

## ✨ Features

### 📱 Phone & Tablet
| Feature | Description |
|---------|-------------|
| 🎬 **Live TV** | Grid-based channel browser with category filtering and real-time EPG |
| 🍿 **Movies** | On-demand library with poster grids, detail screens, and resume playback |
| 📺 **TV Series** | Season & episode grouping with on-demand episode loading |
| ⭐ **Favorites** | Favorite any channel, movie, or series from the detail screen — accessible from a dedicated tab |
| 🔍 **Real-time Search** | Instant debounced search across all content with 400ms typing delay |
| 🔐 **Dual Login** | Connect via **M3U URL** or **Xtream Codes API** (username/password) |
| 🎮 **Custom Player** | Fullscreen ExoPlayer with gesture controls, seek bar, and auto-hide UI |

### 📺 Android TV
| Feature | Description |
|---------|-------------|
| 🎯 **D-Pad Navigation** | Full remote control support — navigate every screen with directional pad |
| 🔦 **Crystal-Clear Focus** | Grid items show a **hollow white border** when focused; sidebar buttons get a **soft blue glow**; selected tabs show a **blue indicator** |
| 🔎 **Scale Animation** | Focused items subtly scale up for unmistakable visual feedback |
| ⏯️ **Remote Player Controls** | D-Pad Left/Right seeks, Up/Down changes channels, Center toggles play/pause |
| 🔗 **Predictable Focus Chains** | Every control has explicit `nextFocus` attributes — D-pad never gets lost |
| ♿ **Accessibility Compliant** | All controls have `contentDescription`, every touch target is ≥48dp |
| 🏠 **Leanback Launcher** | Appears directly on your Android TV home screen with a custom banner |

### ⚡ Performance
| Feature | Description |
|---------|-------------|
| 🚀 **Instant Restart** | Xtream playlists cached as JSON — app restarts load instantly, no waiting |
| ⚡ **Instant Detail Screens** | Series/Movie info shows **immediately** from cache; episodes load in background |
| 🖼️ **Smart Image Caching** | Glide with `DiskCacheStrategy.ALL` + thumbnails — images load instantly on revisit |
| 🧠 **Memory Optimized** | `largeHeap`, Glide cache clearing on low memory, conditional view cache (20 TV / 8 phone) |
| 🧹 **Auto Cache Cleanup** | Old cache files (>7 days or >100MB) are purged automatically on startup |
| 📦 **Streaming Parser** | Memory-safe JSON parsing with 100k item cap — no OOM crashes on massive playlists |
| ⚡ **DiffUtil Everywhere** | All 6 adapters use `ListAdapter` + `DiffUtil` for smooth, efficient updates |

---

## 🚀 Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM (ViewModel + LiveData)
- **Async:** Kotlin Coroutines
- **Video Playback:** [AndroidX Media3 ExoPlayer](https://developer.android.com/media/media3/exoplayer)
- **Image Loading:** [Glide](https://bumptech.github.io/glide/)
- **UI:** ViewBinding · ConstraintLayout · RecyclerView · Material Components · NestedScrollView · Material3
- **Build:** R8 Full Mode · ProGuard · Resource Shrinking · Signed Release APK
- **Serialization:** Gson
- **TV Detection:** `Configuration.UI_MODE_TYPE_TELEVISION` + `PackageManager.FEATURE_LEANBACK`

---

## 📸 Screenshots

> 🚧 *Screenshots coming soon — feel free to contribute if you try the app!*
>
> **Expected screens:**
> - Login screen (M3U / Xtream toggle)
> - Live TV grid with category chips and preview pane
> - Movie/Series poster grids with clean white focus borders
> - Detail screen with banner, plot, and episode selector
> - Fullscreen player with controls overlay
> - **Android TV:** Left sidebar navigation with blue selected indicator and white focus borders

---

## 🛠️ Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- JDK 17

### Build & Run

```bash
# Clone the repository
git clone https://github.com/Moussa/IPTV-Coco.git
cd IPTV-Coco

# Build debug APK
./gradlew assembleDebug

# Build signed release APK (R8 + resource shrinking enabled)
./gradlew assembleRelease

# Run lint
./gradlew lintDebug
```

The debug APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📝 Usage

### On Phone / Tablet (Touch)

1. **Launch the app** — Works in both portrait and landscape.
2. **Log in** — Choose M3U URL or Xtream Codes, enter your credentials.
3. **Browse** — Use bottom navigation to switch between Live TV, Movies, Series, and Favorites.
4. **Search** — Type in any search field; results update as you type.
5. **Tap to play** — Select any item to launch the built-in player.
6. **Favorite** — Open a movie, series, or channel to add it to Favorites.
7. **Player gestures:**
   - **Tap** — Show/hide controls
   - **Swipe** — Adjust volume & brightness
   - **Seek bar** — Scrub through VOD content

### On Android TV (Remote / D-Pad)

1. **Install** via sideload, Google Play Store, or Android TV app store.
2. **Log in** — Navigate fields with D-Pad, use on-screen keyboard or remote app.
3. **Browse** — D-Pad navigates between the left sidebar, categories, and content grids.
4. **Always see your selection** — A **hollow white border** appears around focused grid items; sidebar buttons show a **soft blue glow**; the selected tab shows a **blue vertical indicator**.
5. **Favorite** — Open a movie, series, or channel to add it to Favorites.
6. **Player remote controls:**
   - **D-Pad Center / Enter** — Toggle play/pause or show controls
   - **D-Pad Left / Right** — Seek backward/forward (VOD)
   - **D-Pad Up / Down** — Change channel (Live TV)
   - **Back** — Exit player or go back

---

## 📂 Project Structure

```
app/src/main/java/com/iptvcoco/app/
├── adapter/          # RecyclerView adapters (Movie, Series, Channel, Category, Episode, EPG)
├── model/            # Data classes (Channel, Movie, Series, Episode, EPGEntry, etc.)
├── parser/           # M3UParser & XtreamParser (streaming JSON, memory-safe)
├── repository/       # IPTVRepository — playlist cache, favorites, resume positions
├── ui/
│   ├── login/        # LoginActivity (M3U / Xtream)
│   ├── main/         # MainActivity with sidebar/bottom nav & fragment state preservation
│   ├── live/         # LiveTVFragment — channel grid + preview + EPG
│   ├── movies/       # MoviesFragment — poster grid + search
│   ├── series/       # SeriesFragment — poster grid + search
│   ├── favorites/    # FavoritesFragment — combined horizontal lists
│   ├── detail/       # MovieDetailActivity & SeriesDetailActivity
│   └── player/       # PlayerActivity — ExoPlayer + custom controls + D-pad handling
├── util/             # DeviceUtils (TV detection), TvFocusHelper, AppLogger
├── viewmodel/        # MVVM ViewModels for each tab
└── IPTVCocoApplication.kt  # Application singleton, crash handler, memory pressure
```

---

## 🏗️ Architecture Highlights

| Component | Implementation |
|-----------|---------------|
| **TV Detection** | `DeviceUtils.isTv()` checks `UI_MODE_TYPE_TELEVISION` + `FEATURE_LEANBACK` |
| **Focus Animation** | `TvFocusHelper.apply()` scales views up on focus — **disabled on phones** |
| **Focus Borders** | `bg_item_focused.xml` — hollow white border so content stays visible; `bg_tv_button_focused.xml` — soft blue glow for sidebar buttons |
| **Focus Chains** | Explicit `nextFocusLeft/Right/Up/Down` on all player controls for predictable D-pad navigation |
| **DiffUtil** | All 6 adapters extend `ListAdapter` with `DiffUtil.ItemCallback` — smooth, efficient updates |
| **Release Build** | R8 minification + resource shrinking; comprehensive ProGuard rules for Gson, ExoPlayer, Glide |
| **JSON Cache** | Xtream playlists saved to `playlist.json` on disk for instant cold starts |
| **Cache Cleanup** | `cleanupOldCaches()` deletes files >7 days or >100MB on every init |
| **View Cache** | `setItemViewCacheSize(20)` on TV, `8` on phones — balances memory vs. scroll performance |
| **Streaming Parser** | `JsonReader` streaming with per-item try/catch — malformed items skipped, not fatal |
| **Series Episodes** | Fetched on-demand via `get_series_info` endpoint; basic info shows instantly |
| **Fragment State** | `add()`/`hide()`/`show()` with tag cache — scroll position survives tab switches |

---

## ♿ Accessibility & Code Quality

- **Lint Clean** — `lintDebug` passes with **0 errors**
- **No Hardcoded Colors** — All drawable XMLs reference `@color/` resources
- **Minimum Touch Targets** — Every interactive view is at least **48dp** (Android TV accessibility requirement)
- **Content Descriptions** — All `ImageView`s, `ImageButton`s, and icon-only controls have `contentDescription`
- **Autofill Ready** — Login fields declare proper `autofillHints`; search fields opt out with `importantForAutofill="no"`
- **Input Types** — Every `EditText` specifies an appropriate `inputType`
- **Text Legibility** — No text smaller than **12sp** on any screen

---

## 🤝 Contributing

Contributions are welcome! Whether it's bug fixes, new features, translations, screenshots, or documentation improvements — feel free to open an issue or submit a pull request.

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<p align="center">
  Made with ❤️ for stream lovers.
</p>
