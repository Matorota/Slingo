package lt.viko.eif.mtrimaitis.Slingo.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import lt.viko.eif.mtrimaitis.Slingo.components.DiscoverScreen
import lt.viko.eif.mtrimaitis.Slingo.components.FavoritesScreen
import lt.viko.eif.mtrimaitis.Slingo.components.LibraryScreen
import lt.viko.eif.mtrimaitis.Slingo.components.NowPlayingScreen
import lt.viko.eif.mtrimaitis.Slingo.components.ProfileScreen

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
                    2 -> DiscoverScreen(navController)
                    3 -> FavoritesScreen(navController)
                    4 -> ProfileScreen(navController)
                }
            }
        }
    }
}