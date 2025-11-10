package lt.viko.eif.mtrimaitis.Slingo.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    discoverViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.DiscoverViewModel,
    musicPlayerViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.MusicPlayerViewModel,
    playlistViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.PlaylistViewModel,
    favoriteViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.FavoriteViewModel,
    onNavigateToPlaying: () -> Unit
) {
    val uiState by discoverViewModel.uiState.collectAsState()
    val playlistUiState by playlistViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }
    var searchScope by remember { mutableStateOf(SearchScope.Songs) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<lt.viko.eif.mtrimaitis.Slingo.data.models.Song?>(null) }
    var selectedArtist by remember { mutableStateOf<ArtistResult?>(null) }

    val filterPresets = remember {
        listOf(
            "Top hits",
            "New releases",
            "Chill",
            "Workout",
            "Indie vibes",
            "Focus"
        )
    }

    LaunchedEffect(uiState.searchQuery) {
        if (searchQuery != uiState.searchQuery) {
            searchQuery = uiState.searchQuery
        }
    }

    val hasActiveQuery = uiState.searchQuery.isNotBlank()
    val isLoading = if (hasActiveQuery) uiState.isLoading else uiState.isLoadingRecommendations
    val songsSource = if (hasActiveQuery) uiState.searchResults else uiState.recommendations

    val albumResults = remember(songsSource) {
        songsSource
            .groupBy { it.album }
            .map { (albumName, songs) ->
                AlbumResult(
                    albumName = albumName,
                    artist = songs.firstOrNull()?.artist.orEmpty(),
                    imageUrl = songs.firstOrNull()?.imageUrl.orEmpty(),
                    songs = songs
                )
            }
    }
    val artistResults = remember(songsSource) {
        songsSource
            .flatMap { song ->
                song.artist.split(",").map { artistName ->
                    artistName.trim() to song
                }
            }
            .groupBy({ it.first }, { it.second })
            .map { (artistName, songs) ->
                ArtistResult(
                    artistName = artistName,
                    imageUrl = songs.firstOrNull()?.imageUrl.orEmpty(),
                    songs = songs.distinctBy { it.id }
                )
            }
    }

    fun executeSearch(query: String) {
        val trimmed = query.trim()
        selectedArtist = null
        if (trimmed.isEmpty()) {
            discoverViewModel.searchTracks("")
        } else {
            discoverViewModel.searchTracksImmediate(trimmed)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        TabRow(
            selectedTabIndex = searchScope.ordinal,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = {}
        ) {
            SearchScope.values().forEach { scope ->
                Tab(
                    selected = searchScope == scope,
                    onClick = {
                        searchScope = scope
                        selectedArtist = null
                    },
                    text = {
                        Text(
                            scope.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (searchScope == scope) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    executeSearch(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search songs or artists", color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                },
                singleLine = true,
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    executeSearch(searchQuery)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedBorderColor = Color.White.copy(alpha = 0.7f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (hasActiveQuery) "Results" else "Trending now",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelLarge
            )
            TextButton(onClick = { showFilters = true }) {
                Text("Filters", color = Color.White)
            }
        }

        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }

            selectedArtist != null -> AnimatedContent(
                targetState = selectedArtist,
                label = "artistDetail"
            ) { artist ->
                if (artist == null) return@AnimatedContent
                ArtistDetailView(
                    artist = artist,
                    favoriteViewModel = favoriteViewModel,
                    musicPlayerViewModel = musicPlayerViewModel,
                    onNavigateToPlaying = onNavigateToPlaying,
                    onAddToPlaylist = {
                        selectedSong = it
                        showAddToPlaylistDialog = true
                    },
                    onBack = { selectedArtist = null }
                )
            }

            songsSource.isEmpty() && hasActiveQuery -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matches for \"${searchQuery.trim()}\". Try another keyword.",
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(24.dp)
                )
            }

            songsSource.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Start typing to explore music instantly.",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> when (searchScope) {
                SearchScope.Songs -> SongList(
                    songs = songsSource,
                    favoriteViewModel = favoriteViewModel,
                    musicPlayerViewModel = musicPlayerViewModel,
                    onNavigateToPlaying = onNavigateToPlaying,
                    onAddToPlaylist = {
                        selectedSong = it
                        showAddToPlaylistDialog = true
                    }
                )

                SearchScope.Albums -> AlbumList(
                    albums = albumResults,
                    onPlay = { songs, index ->
                        musicPlayerViewModel.setPlaylist(songs, index, autoPlay = true)
                        onNavigateToPlaying()
                    },
                    onAddToPlaylist = {
                        selectedSong = it
                        showAddToPlaylistDialog = true
                    }
                )

                SearchScope.Artists -> ArtistList(
                    artists = artistResults,
                    onArtistSelected = { artist ->
                        selectedArtist = artist
                    }
                )
            }
        }
    }

    if (showAddToPlaylistDialog && selectedSong != null) {
        AlertDialog(
            onDismissRequest = {
                showAddToPlaylistDialog = false
                selectedSong = null
            },
            containerColor = Color(0xFF1F1D2B),
            tonalElevation = 8.dp,
            title = { Text("Add to playlist") },
            text = {
                if (playlistUiState.playlists.isEmpty()) {
                    Text("No playlists yet. Create one from the Library tab.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        playlistUiState.playlists.forEach { playlist ->
                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        playlistViewModel.addSongToPlaylist(
                                            playlist.id,
                                            selectedSong!!.id
                                        )
                                        showAddToPlaylistDialog = false
                                        selectedSong = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.14f),
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
                                        Text(playlist.name, color = Color.White)
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
                    selectedSong = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFilters) {
        AlertDialog(
            onDismissRequest = { showFilters = false },
            containerColor = Color(0xFF1F1D2B),
            tonalElevation = 6.dp,
            title = { Text("Search filters", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Quick picks",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterPresets.forEach { filter ->
                            SuggestionChip(
                                onClick = {
                                    searchQuery = filter
                                    focusManager.clearFocus()
                                    executeSearch(filter)
                                    showFilters = false
                                },
                                label = { Text(filter) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color.White.copy(alpha = 0.12f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilters = false }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun SongResultRow(
    song: lt.viko.eif.mtrimaitis.Slingo.data.models.Song,
    favoriteViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.FavoriteViewModel,
    musicPlayerViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.MusicPlayerViewModel,
    playlist: List<lt.viko.eif.mtrimaitis.Slingo.data.models.Song>,
    currentIndex: Int,
    onNavigateToPlaying: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    var isFavorite by remember(song.id) { mutableStateOf(false) }

    LaunchedEffect(song.id) {
        withContext(Dispatchers.IO) {
            isFavorite = favoriteViewModel.isFavorite(song.id)
        }
    }

    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                musicPlayerViewModel.setPlaylist(
                    playlist = playlist,
                    startIndex = currentIndex,
                    autoPlay = song.previewUrl.isNotBlank()
                )
                onNavigateToPlaying()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                if (song.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = song.imageUrl,
                        contentDescription = song.name,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.name,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2
                )
                Text(
                    text = song.artist,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }

            IconButton(onClick = onAddToPlaylist) {
                Icon(Icons.Filled.Add, contentDescription = "Add to playlist", tint = Color.White)
            }

            IconButton(
                onClick = {
                    favoriteViewModel.toggleFavorite(song.id) { updated ->
                        isFavorite = updated
                    }
                }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFEF5350) else Color.White
                )
            }
        }
    }
}

@Composable
private fun SongList(
    songs: List<lt.viko.eif.mtrimaitis.Slingo.data.models.Song>,
    favoriteViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.FavoriteViewModel,
    musicPlayerViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.MusicPlayerViewModel,
    onNavigateToPlaying: () -> Unit,
    onAddToPlaylist: (lt.viko.eif.mtrimaitis.Slingo.data.models.Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
    ) {
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
            SongResultRow(
                song = song,
                favoriteViewModel = favoriteViewModel,
                musicPlayerViewModel = musicPlayerViewModel,
                playlist = songs,
                currentIndex = index,
                onNavigateToPlaying = onNavigateToPlaying,
                onAddToPlaylist = { onAddToPlaylist(song) }
            )
        }
    }
}

@Composable
private fun AlbumList(
    albums: List<AlbumResult>,
    onPlay: (List<lt.viko.eif.mtrimaitis.Slingo.data.models.Song>, Int) -> Unit,
    onAddToPlaylist: (lt.viko.eif.mtrimaitis.Slingo.data.models.Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
    ) {
        itemsIndexed(albums, key = { _, album -> "${album.albumName}-${album.artist}" }) { _, album ->
            AlbumRow(
                album = album,
                onPlay = { onPlay(album.songs, 0) },
                onAddToPlaylist = {
                    album.songs.firstOrNull()?.let(onAddToPlaylist)
                }
            )
        }
    }
}

@Composable
private fun ArtistList(
    artists: List<ArtistResult>,
    onArtistSelected: (ArtistResult) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
    ) {
        itemsIndexed(artists, key = { _, artist -> artist.artistName }) { _, artist ->
            ArtistRow(
                artist = artist,
                onClick = { onArtistSelected(artist) }
            )
        }
    }
}

@Composable
private fun AlbumRow(
    album: AlbumResult,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                if (album.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = album.imageUrl,
                        contentDescription = album.albumName,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Album,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.albumName,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2
                )
                Text(
                    text = album.artist,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${album.songs.size} track${if (album.songs.size == 1) "" else "s"}",
                    color = Color.White.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            IconButton(onClick = onAddToPlaylist) {
                Icon(Icons.Filled.Add, contentDescription = "Add album", tint = Color.White)
            }

            IconButton(onClick = onPlay) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play album", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ArtistRow(
    artist: ArtistResult,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.08f)
            ) {
                if (artist.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = artist.imageUrl,
                        contentDescription = artist.artistName,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.artistName,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = "${artist.songs.size} track${if (artist.songs.size == 1) "" else "s"} available",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun ArtistDetailView(
    artist: ArtistResult,
    favoriteViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.FavoriteViewModel,
    musicPlayerViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.MusicPlayerViewModel,
    onNavigateToPlaying: () -> Unit,
    onAddToPlaylist: (lt.viko.eif.mtrimaitis.Slingo.data.models.Song) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = artist.artistName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.06f)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    if (artist.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = artist.imageUrl,
                            contentDescription = artist.artistName,
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
                Column {
                    Text(
                        text = artist.artistName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text(
                        text = "${artist.songs.size} available track${if (artist.songs.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        SongList(
            songs = artist.songs,
            favoriteViewModel = favoriteViewModel,
            musicPlayerViewModel = musicPlayerViewModel,
            onNavigateToPlaying = onNavigateToPlaying,
            onAddToPlaylist = onAddToPlaylist
        )
    }
}

private enum class SearchScope(val label: String) {
    Songs("Songs"),
    Albums("Albums"),
    Artists("Singers")
}

private data class AlbumResult(
    val albumName: String,
    val artist: String,
    val imageUrl: String,
    val songs: List<lt.viko.eif.mtrimaitis.Slingo.data.models.Song>
)

private data class ArtistResult(
    val artistName: String,
    val imageUrl: String,
    val songs: List<lt.viko.eif.mtrimaitis.Slingo.data.models.Song>
)