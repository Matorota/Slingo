# Spotify API Setup Instructions

## Required Configuration

### 1. Get Your Client Secret

1. Go to your Spotify Developer Dashboard: https://developer.spotify.com/dashboard
2. Click on your "Slingo" app
3. Click "View client secret" to reveal your Client Secret
4. Copy the Client Secret

### 2. Update RetrofitClient.kt

Open `app/src/main/java/lt/viko/eif/mtrimaitis/Slingo/data/api/RetrofitClient.kt`

Replace this line:
```kotlin
private const val SPOTIFY_CLIENT_SECRET = "YOUR_CLIENT_SECRET_HERE"
```

With your actual Client Secret:
```kotlin
private const val SPOTIFY_CLIENT_SECRET = "your_actual_client_secret_here"
```

### 3. Verify Spotify Dashboard Settings

Your Spotify app should have these settings:

- **Client ID:** `dcea1cbdd55d4c46b738d00caddac5b5` ✅ (Already configured)
- **Redirect URI:** `slingo://callback` ✅ (Already configured in AndroidManifest.xml)
- **APIs Used:** 
  - Web API ✅
  - Android ✅

### 4. What the App Can Do Now

Once you add your Client Secret:

1. **Search Songs:** Search for any song worldwide (Taylor Swift, Sabaton, etc.)
2. **Play Music:** Play 30-second previews from Spotify
3. **Add to Playlists:** Create playlists and add songs
4. **Favorites:** Mark songs as favorites with heart icon
5. **Remove from Playlist:** Delete button on playlist songs

### 5. Important Notes

- The app uses **Client Credentials Flow** for search (no user login required)
- This allows searching and playing previews
- For full playback, you'd need to implement OAuth with user login
- Preview URLs are 30-second samples from Spotify

### 6. Testing

After adding your Client Secret:
1. Build and run the app
2. Search for "Taylor Swift" or "Sabaton"
3. Tap songs to play them
4. Tap heart icon to add to favorites
5. Tap + icon to add to playlist
6. View favorites in the Favorites tab
7. View playlists in the Library tab

