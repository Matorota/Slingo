package lt.viko.eif.mtrimaitis.Slingo.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun MainContentScreen(navController: NavHostController) {
    var selectedItem by remember { mutableIntStateOf(0) }

    val items = listOf(
        Icons.Filled.Home to "Library",
        Icons.Filled.PlayArrow to "Now Playing",
        Icons.Filled.LibraryMusic to "Discover",
        Icons.Filled.Favorite to "Favorites",
        Icons.Filled.Person to "Profile"
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, (icon, label) ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = { Icon(icon, label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedItem) {
                0 -> LibraryScreen(navController)
                1 -> NowPlayingScreen(navController)
                2 -> Text("Discover Music")
                3 -> Text("Your Favorites")
                4 -> ProfileScreen(navController)
            }
        }
    }
}

@Composable
fun LibraryScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Your Library", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun NowPlayingScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Now Playing", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Profile", style = MaterialTheme.typography.titleLarge)
    }
}
