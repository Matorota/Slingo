# Slingo 

> A Kotlin-based Android music app powered by the **Spotify Web API**.

---

##  Overview

Slingo is an Android app that connects to the **Spotify API** to let users browse, play, and manage songs, playlists, and favorites. Built with **Jetpack Compose**, **MVVM**, **Room**, and **Retrofit**, it offers a clean architecture and smooth UI for exploring music.

---

##  Tech Stack

* **Language:** Kotlin
* **Architecture:** MVVM (Model–View–ViewModel)
* **UI:** Jetpack Compose
* **Network:** Retrofit (Spotify API integration)
* **Database:**
* **Dependency Management:** Gradle (KTS)

---

## Current Progress

Spotify API connection established via `RetrofitClient` and `SpotifyApi.kt`
Models created for `Song`, `Playlist`, `User`, `SpotifyResponse`
Local storage implemented with Room (`AppDatabase`, `Dao` classes)
MVVM architecture set up with `Repository` and `ViewModel` layers
UI screens built with Compose: `Discover`, `Favorites`, `Library`, `Profile`, `NowPlaying`
Navigation system configured between main views
Authentication and advanced playback features in progress
Unit and UI testing planned next phase

---

## Launching the App

1. Clone the repo:

   ```bash
   git clone https://github.com/Matorota/Slingo.git
   ```
2. Open in **Android Studio** (latest version).
3. Sync Gradle and build the project.
4. Add your **Spotify API credentials** to `SpotifyApi.kt`.
5. Run on emulator or Android device (min SDK 26+).

---

## Next Steps

* Add Spotify OAuth login flow
* Implement real playback via Spotify SDK
* Polish animations and transitions
* Add tests and CI/CD integration

---



*Developed by Matas Štrimaitis* 🎧
