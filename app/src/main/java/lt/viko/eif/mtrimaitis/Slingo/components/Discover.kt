package lt.viko.eif.mtrimaitis.Slingo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun DiscoverScreen(
    navController: NavHostController,
    discoverViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.DiscoverViewModel,
    musicPlayerViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.MusicPlayerViewModel,
    playlistViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.PlaylistViewModel,
    favoriteViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.FavoriteViewModel,
    onNavigateToPlaying: () -> Unit
) {
    val uiState by discoverViewModel.uiState.collectAsState()
    val playlistUiState by playlistViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var selectedSongForPlaylist by remember { mutableStateOf<lt.viko.eif.mtrimaitis.Slingo.data.models.Song?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(infoMessage) {
        if (infoMessage != null) {
            delay(2500)
            infoMessage = null
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
            onValueChange = {
                searchQuery = it
                discoverViewModel.searchTracks(it)
            },
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

        infoMessage?.let { message ->
            InfoBanner(message)
        }

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
                if (uiState.searchResults.isEmpty() && uiState.searchQuery.isNotBlank()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "No playable previews found for \"${uiState.searchQuery}\".\nTry another song or artist.",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        )
                    }
                }
                itemsIndexed(uiState.searchResults, key = { _, song -> song.id }) { index, song ->
                    var isFavorite by remember { mutableStateOf(false) }
                    LaunchedEffect(song.id) {
                        withContext(Dispatchers.IO) {
                            isFavorite = favoriteViewModel.isFavorite(song.id)
                        }
                    }

                    DiscoverResultCard(
                        song = song,
                        isFavorite = isFavorite,
                        onPlay = {
                            if (song.previewUrl.isBlank()) {
                                infoMessage = "Spotify doesn't provide a preview for \"${song.name}\"."
                            } else {
                                musicPlayerViewModel.setPlaylist(uiState.searchResults, index, autoPlay = true)
                                onNavigateToPlaying()
                            }
                        },
                        onToggleFavorite = {
                            favoriteViewModel.toggleFavorite(song.id) { updated ->
                                isFavorite = updated
                            }
                        },
                        onAddToPlaylist = {
                            selectedSongForPlaylist = song
                            showAddToPlaylistDialog = true
                        }
                    )
                }
            }
        }

        if (showAddToPlaylistDialog && selectedSongForPlaylist != null) {
            AlertDialog(
                onDismissRequest = {
                    showAddToPlaylistDialog = false
                    selectedSongForPlaylist = null
                },
                containerColor = Color(0xFF1F1D2B),
                tonalElevation = 8.dp,
                title = { Text("Add to playlist") },
                text = {
                    if (playlistUiState.playlists.isEmpty()) {
                        Text("No playlists available. Create one from the Library tab.")
                    } else {
                        Column {
                            playlistUiState.playlists.forEach { playlist ->
                                Surface(
                                    color = Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            playlistViewModel.addSongToPlaylist(
                                                playlist.id,
                                                selectedSongForPlaylist!!.id
                                            )
                                            showAddToPlaylistDialog = false
                                            selectedSongForPlaylist = null
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color(0xFF3B2A7A),
                                            shape = CircleShape,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Filled.MusicNote,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(playlist.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                "Tap to add",
                                                color = Color.White.copy(alpha = 0.6f),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
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
    }
}

@Composable
private fun DiscoverResultCard(
    song: lt.viko.eif.mtrimaitis.Slingo.data.models.Song,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
    ) {
        Box {
            if (song.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = song.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.35f), shape = RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFEF5350) else Color.White
                )
            }

            IconButton(
                onClick = onAddToPlaylist,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.35f), shape = RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add to playlist",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = song.name,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2
                )
                Text(
                    text = song.artist,
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )

                if (song.previewUrl.isBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Preview not provided",
                        color = Color.White.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoBanner(message: String) {
    Surface(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}