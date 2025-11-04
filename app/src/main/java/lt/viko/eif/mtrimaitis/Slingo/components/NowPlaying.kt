package lt.viko.eif.mtrimaitis.Slingo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@Composable
fun NowPlayingScreen(
    navController: NavHostController,
    musicPlayerViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.MusicPlayerViewModel
) {
    val playerState by musicPlayerViewModel.playerState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val currentSong = playerState.currentSong
        if (currentSong == null) {
            Text("No song playing", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f))
        } else {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (currentSong.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = currentSong.imageUrl,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.MusicNote,
                        contentDescription = "Album Art",
                        modifier = Modifier.size(150.dp),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(currentSong.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(currentSong.artist, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(32.dp))
            
            val progress = if (playerState.duration > 0) {
                playerState.currentPosition.toFloat() / playerState.duration.toFloat()
            } else 0f
            
            Slider(
                value = progress,
                onValueChange = { newValue ->
                    val newPosition = (newValue * playerState.duration).toInt()
                    musicPlayerViewModel.seekTo(newPosition)
                },
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.5f)
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatTime(playerState.currentPosition),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    formatTime(playerState.duration),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { musicPlayerViewModel.playPrevious() }) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(48.dp), tint = Color.White)
                }
                IconButton(onClick = { musicPlayerViewModel.playPause() }) {
                    Icon(
                        if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayCircle,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(72.dp),
                        tint = Color.White
                    )
                }
                IconButton(onClick = { musicPlayerViewModel.playNext() }) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(48.dp), tint = Color.White)
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}