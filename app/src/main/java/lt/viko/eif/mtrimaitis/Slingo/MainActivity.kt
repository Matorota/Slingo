package lt.viko.eif.mtrimaitis.Slingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import lt.viko.eif.mtrimaitis.Slingo.data.LoginRepository
import lt.viko.eif.mtrimaitis.Slingo.data.PlaylistRepository
import lt.viko.eif.mtrimaitis.Slingo.data.SongRepository
import lt.viko.eif.mtrimaitis.Slingo.data.database.DatabaseProvider
import lt.viko.eif.mtrimaitis.Slingo.viewmodel.AuthViewModel
import lt.viko.eif.mtrimaitis.Slingo.views.LoginScreen
import lt.viko.eif.mtrimaitis.Slingo.views.MainContentScreen
import lt.viko.eif.mtrimaitis.Slingo.views.RegistrationScreen
import lt.viko.eif.mtrimaitis.Slingo.views.WelcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SlingoApp()
        }
    }
}

@Composable
fun SlingoApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val database = remember { DatabaseProvider.getDatabase(context) }
    val loginRepository = remember { LoginRepository(database.userDao()) }
    val songRepository = remember { SongRepository(database.songDao()) }
    val playlistRepository = remember { PlaylistRepository(database.playlistDao()) }
    
    val authViewModel: AuthViewModel = viewModel { 
        AuthViewModel(loginRepository)
    }
    
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            NavHost(
                navController = navController,
                startDestination = "welcome"
            ) {
                composable("welcome") { WelcomeScreen(navController) }
                composable("login") { 
                    LoginScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    ) 
                }
                composable("register") { 
                    RegistrationScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    ) 
                }
                composable("main") { 
                    MainContentScreen(
                        navController = navController,
                        songRepository = songRepository,
                        playlistRepository = playlistRepository,
                        currentUserId = authViewModel.uiState.value.currentUser?.id ?: 0L
                    ) 
                }
            }
        }
    }
}
