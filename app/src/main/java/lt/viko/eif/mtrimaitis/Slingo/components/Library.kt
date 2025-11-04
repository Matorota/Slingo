package lt.viko.eif.mtrimaitis.Slingo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun LibraryScreen(
    navController: NavHostController,
    playlistViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.PlaylistViewModel,
    musicPlayerViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.MusicPlayerViewModel,
    onNavigateToPlaying: () -> Unit = {}
) {
    val uiState by playlistViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var selectedPlaylistId by remember { mutableLongStateOf(-1L) }
    
    LaunchedEffect(selectedPlaylistId) {
        if (selectedPlaylistId > 0) {
            playlistViewModel.loadPlaylistSongs(selectedPlaylistId)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            "Your Library",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search your library") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f),
                disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Your Playlists",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            IconButton(onClick = { showCreatePlaylistDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Create Playlist", tint = Color.White)
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(uiState.playlists.size) { index ->
                val playlist = uiState.playlists[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Toggle playlist: if clicking same playlist, close it
                            if (selectedPlaylistId == playlist.id) {
                                selectedPlaylistId = -1L
                            } else {
                                selectedPlaylistId = playlist.id
                                playlistViewModel.loadPlaylistSongs(playlist.id)
                            }
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
                            contentDescription = "Playlist",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(playlist.name, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Text("Playlist", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { 
                        playlistViewModel.loadPlaylistSongs(playlist.id)
                    }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "View Playlist", tint = Color.White)
                    }
                }
            }
            
            // Show playlist songs if a playlist is selected
            if (selectedPlaylistId > 0 && uiState.currentPlaylistSongs.isNotEmpty()) {
                item {
                    Text(
                        "Songs in Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(uiState.currentPlaylistSongs.size) { index ->
                    val song = uiState.currentPlaylistSongs[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    musicPlayerViewModel.setPlaylist(uiState.currentPlaylistSongs, index, autoPlay = true)
                                    onNavigateToPlaying()
                                }
                        ) {
                            Text(song.name, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                            Text(song.artist, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(
                            onClick = {
                                musicPlayerViewModel.setPlaylist(uiState.currentPlaylistSongs, index, autoPlay = true)
                                onNavigateToPlaying()
                            }
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White)
                        }
                        IconButton(
                            onClick = {
                                playlistViewModel.removeSongFromPlaylist(selectedPlaylistId, song.id)
                            }
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
    
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Create New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            playlistViewModel.createPlaylist(newPlaylistName) {
                                newPlaylistName = ""
                                showCreatePlaylistDialog = false
                            }
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}