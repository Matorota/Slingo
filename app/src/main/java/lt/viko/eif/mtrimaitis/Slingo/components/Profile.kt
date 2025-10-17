package lt.viko.eif.mtrimaitis.Slingo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ProfileScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.Gray.copy(alpha = 0.5f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(72.dp),
                tint = Color.White.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your Name", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Text("@username", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))

        Spacer(modifier = Modifier.height(32.dp))
        Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            ProfileMenuItem(icon = Icons.Filled.Settings, text = "Settings")
            ProfileMenuItem(icon = Icons.Filled.AccountCircle, text = "Account")
            ProfileMenuItem(icon = Icons.Filled.Notifications, text = "Notifications")
            ProfileMenuItem(icon = Icons.Filled.Lock, text = "Privacy")
            ProfileMenuItem(icon = Icons.Filled.Logout, text = "Log Out")
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = Color.White.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
    }
}