# 📺 IPTV Coco

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Android-24%2B-3DDC84?logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Media3-ExoPlayer-orange?logo=google&logoColor=white" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" />
</p>

<p align="center">
  <b>A modern, lightweight Android IPTV client for streaming Live TV, Movies, and Series from M3U playlists.</b>
</p>

---

## ✨ Features

| Category | Features |
|----------|----------|
| 🎬 **Live TV** | Browse channels by category, channel grid with preview pane, channel up/down switching |
| 🍿 **Movies** | On-demand movie library with detail screens and resume playback support |
| 📺 **TV Series** | Season & episode grouping with detail screens and progress tracking |
| ⭐ **Favorites** | Favorite any channel, movie, or episode with a dedicated Favorites tab |
| 🔍 **Search** | Quick search across categories and content lists |
| 🔐 **Flexible Login** | Connect via M3U URL with optional `USERNAME` / `PASSWORD` placeholders |
| 🎮 **Custom Player** | Fullscreen landscape player built on ExoPlayer with gestures and auto-hide controls |

---

## 🚀 Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM (ViewModel + LiveData)
- **Async:** Kotlin Coroutines
- **Video Playback:** [AndroidX Media3 ExoPlayer](https://developer.android.com/media/media3/exoplayer)
- **Image Loading:** [Glide](https://bumptech.github.io/glide/)
- **UI:** ViewBinding · ConstraintLayout · RecyclerView · Material Components
- **Serialization:** Gson

---

## 📱 Screenshots

> 🚧 *Screenshots coming soon.*
>
> Feel free to contribute screenshots if you try the app!

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

# Or open in Android Studio and run via the IDE
```

The debug APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📝 Usage

1. **Launch the app** — The app opens in landscape mode optimized for media consumption.
2. **Enter your M3U Playlist URL** — Provide the playlist URL. You can use `USERNAME` and `PASSWORD` placeholders if your provider requires authentication.
3. **Browse content** — Use the bottom navigation to switch between Live TV, Movies, Series, and Favorites.
4. **Tap to play** — Select any channel or title to start playback in the built-in player.
5. **Gestures in player:**
   - **Tap** — Show/hide controls
   - **Swipe** — Adjust volume & brightness
   - **Seek bar** — Scrub through VOD content
   - **Channel buttons** — Switch live channels

---

## 📂 Project Structure

```
app/src/main/java/com/example/iptvcoco/
├── data/          # M3U parser, repository, local caching
├── ui/            # Activities, Fragments, Adapters
├── viewmodel/     # MVVM ViewModels
└── player/        # ExoPlayer wrapper & custom controls
```

---

## 🤝 Contributing

Contributions are welcome! Whether it's bug fixes, new features, translations, or documentation improvements — feel free to open an issue or submit a pull request.

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
