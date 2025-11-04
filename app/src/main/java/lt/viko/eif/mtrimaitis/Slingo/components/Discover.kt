package lt.viko.eif.mtrimaitis.Slingo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun DiscoverScreen(
    navController: NavHostController,
    discoverViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.DiscoverViewModel,
    musicPlayerViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.MusicPlayerViewModel,
    playlistViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.PlaylistViewModel
) {
    val uiState by discoverViewModel.uiState.collectAsState()
    val playlistUiState by playlistViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var selectedSongForPlaylist by remember { mutableStateOf<lt.viko.eif.mtrimaitis.Slingo.data.models.Song?>(null) }
    
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(500) // Debounce
            discoverViewModel.searchTracks(searchQuery)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Discover New Music",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search for songs...", color = Color.White.copy(alpha = 0.7f)) },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            },
            keyboardActions = KeyboardActions(onSearch = {
                discoverViewModel.searchTracks(searchQuery)
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage!!, color = Color.Red)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.searchResults.size) { index ->
                    val song = uiState.searchResults[index]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    musicPlayerViewModel.loadSong(song)
                                    musicPlayerViewModel.setPlaylist(uiState.searchResults, index)
                                    musicPlayerViewModel.playPause()
                                }
                        ) {
                            if (song.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = song.imageUrl,
                                    contentDescription = song.name,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .fillMaxWidth(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .fillMaxWidth()
                                        .background(
                                            Color.Gray.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Album,
                                        contentDescription = "Album",
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    song.name,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2
                                )
                                Text(
                                    song.artist,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                            IconButton(
                                onClick = {
                                    selectedSongForPlaylist = song
                                    showAddToPlaylistDialog = true
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Add to Playlist",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddToPlaylistDialog && selectedSongForPlaylist != null) {
            AlertDialog(
                onDismissRequest = {
                    showAddToPlaylistDialog = false
                    selectedSongForPlaylist = null
                },
                title = { Text("Add to Playlist") },
                text = {
                    if (playlistUiState.playlists.isEmpty()) {
                        Text("No playlists available. Create one from the Library tab.")
                    } else {
                        Column {
                            playlistUiState.playlists.forEach { playlist ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            playlistViewModel.addSongToPlaylist(
                                                playlist.id,
                                                selectedSongForPlaylist!!.id
                                            )
                                            showAddToPlaylistDialog = false
                                            selectedSongForPlaylist = null
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.MusicNote,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(playlist.name, color = Color.White)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showAddToPlaylistDialog = false
                        selectedSongForPlaylist = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }}