package lt.viko.eif.mtrimaitis.Slingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    val navController = androidx.navigation.compose.rememberNavController()
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            NavHost(
                navController = navController,
                startDestination = "welcome"
            ) {
                composable("welcome") { WelcomeScreen(navController) }
                composable("login") { LoginScreen(navController) }
                composable("register") { RegistrationScreen(navController) }
                composable("main") { MainContentScreen(navController) }
            }
        }
    }
}
