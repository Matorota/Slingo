package lt.viko.eif.mtrimaitis.Slingo.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContentScreen(navController: NavHostController) {
    var selectedItem by remember { mutableIntStateOf(0) }

    val items = listOf(
        Icons.Filled.Home to "Library",
        Icons.Filled.PlayArrow to "Playing",
        Icons.Filled.LibraryMusic to "Discover",
        Icons.Filled.Favorite to "Favorites",
        Icons.Filled.Person to "Profile"
    )

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Black.copy(alpha = 0.3f), // Subtle scrim background
                    tonalElevation = 0.dp // No shadow
                ) {
                    items.forEachIndexed { index, (icon, label) ->
                        NavigationBarItem(
                            selected = selectedItem == index,
                            onClick = { selectedItem = index },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = Color.Transparent, // No background indicator
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedItem) {
                    0 -> LibraryScreen(navController)
                    1 -> NowPlayingScreen(navController)
                    2 -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Discover Music", style = MaterialTheme.typography.titleLarge, color = Color.White, textAlign = TextAlign.Center)
                    }
                    3 -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Your Favorites", style = MaterialTheme.typography.titleLarge, color = Color.White, textAlign = TextAlign.Center)
                    }
                    4 -> ProfileScreen(navController)
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Your Library", style = MaterialTheme.typography.titleLarge, color = Color.White, textAlign = TextAlign.Center)
    }
}

@Composable
fun NowPlayingScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Playing", style = MaterialTheme.typography.titleLarge, color = Color.White, textAlign = TextAlign.Center)
    }
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Profile", style = MaterialTheme.typography.titleLarge, color = Color.White, textAlign = TextAlign.Center)
    }
}
