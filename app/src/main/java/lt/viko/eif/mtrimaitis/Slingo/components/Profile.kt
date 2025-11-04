package lt.viko.eif.mtrimaitis.Slingo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: lt.viko.eif.mtrimaitis.Slingo.viewmodel.AuthViewModel,
    onLogout: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
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
        Text(
            uiState.currentUser?.username ?: "Guest",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        Text(
            uiState.currentUser?.email ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))
        Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            ProfileMenuItem(icon = Icons.Filled.Settings, text = "Settings") { }
            ProfileMenuItem(icon = Icons.Filled.AccountCircle, text = "Account") { }
            ProfileMenuItem(icon = Icons.Filled.Lock, text = "Change Password") {
                showPasswordChangeDialog = true
            }
            ProfileMenuItem(icon = Icons.Filled.Logout, text = "Log Out") {
                authViewModel.logout()
                onLogout()
            }
        }
    }
    
    if (showPasswordChangeDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPasswordChangeDialog = false
                oldPassword = ""
                newPassword = ""
                confirmNewPassword = ""
            },
            title = { Text("Change Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.errorMessage != null) {
                        Text(
                            uiState.errorMessage!!,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (newPassword.isNotBlank() && confirmNewPassword.isNotBlank() && newPassword != confirmNewPassword) {
                        Text(
                            "Passwords do not match",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPassword.isNotBlank() && newPassword == confirmNewPassword && newPassword.length >= 6) {
                            authViewModel.changePassword(oldPassword, newPassword) {
                                showPasswordChangeDialog = false
                                oldPassword = ""
                                newPassword = ""
                                confirmNewPassword = ""
                            }
                        }
                    },
                    enabled = !uiState.isLoading && 
                             oldPassword.isNotBlank() && 
                             newPassword.isNotBlank() && 
                             confirmNewPassword.isNotBlank() &&
                             newPassword == confirmNewPassword &&
                             newPassword.length >= 6
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Change")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPasswordChangeDialog = false
                    oldPassword = ""
                    newPassword = ""
                    confirmNewPassword = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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