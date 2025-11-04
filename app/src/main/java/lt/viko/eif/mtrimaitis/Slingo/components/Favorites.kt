package lt.viko.eif.mtrimaitis.Slingo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun FavoritesScreen(
    navController: NavHostController,
    playlistViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.PlaylistViewModel,
    musicPlayerViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.MusicPlayerViewModel
) {
    val uiState by playlistViewModel.uiState.collectAsState()
    var selectedPlaylistId by remember { mutableLongStateOf(-1L) }
    
    LaunchedEffect(selectedPlaylistId) {
        if (selectedPlaylistId > 0) {
            playlistViewModel.loadPlaylistSongs(selectedPlaylistId)
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                "Your Favorites",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        items(uiState.currentPlaylistSongs.size) { index ->
            val song = uiState.currentPlaylistSongs[index]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        musicPlayerViewModel.loadSong(song)
                        musicPlayerViewModel.setPlaylist(uiState.currentPlaylistSongs, index)
                        musicPlayerViewModel.playPause()
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MusicNote,
                        contentDescription = "Song",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(song.name, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    Text(song.artist, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Favorite, contentDescription = "Favorite", tint = MaterialTheme.colorScheme.primaryContainer)
            }
        }
        
        if (uiState.currentPlaylistSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No favorites yet. Add songs to your playlists!",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}