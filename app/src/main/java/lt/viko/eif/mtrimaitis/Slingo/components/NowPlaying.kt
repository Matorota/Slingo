package lt.viko.eif.mtrimaitis.Slingo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun NowPlayingScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = "Album Art",
                modifier = Modifier.size(150.dp),
                tint = Color.White.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Song Title", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Text("Artist Name", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(32.dp))
        Slider(
            value = 0.3f,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.5f)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(48.dp), tint = Color.White)
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.PlayCircle, contentDescription = "Play", modifier = Modifier.size(72.dp), tint = Color.White)
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(48.dp), tint = Color.White)
            }
        }
    }
}