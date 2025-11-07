package lt.viko.eif.mtrimaitis.Slingo.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import lt.viko.eif.mtrimaitis.Slingo.viewmodel.AuthViewModel
import lt.viko.eif.mtrimaitis.Slingo.viewmodel.FavoriteViewModel
import lt.viko.eif.mtrimaitis.Slingo.viewmodel.PlaylistViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    playlistViewModel: PlaylistViewModel,
    favoriteViewModel: FavoriteViewModel,
    onLogout: () -> Unit
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val playlistUiState by playlistViewModel.uiState.collectAsState()
    val favoriteUiState by favoriteViewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(ProfileTab.Overview) }
    val scrollState = rememberScrollState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordStep by remember { mutableStateOf(PasswordChangeStep.VerifyCurrent) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var verifiedPassword by remember { mutableStateOf<String?>(null) }

    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var darkModeEnabled by rememberSaveable { mutableStateOf(true) }
    var dataSaverEnabled by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        ProfileHeader(
            username = authUiState.currentUser?.username ?: "Guest",
            email = authUiState.currentUser?.email ?: "Add an email",
            playlistCount = playlistUiState.playlists.size,
            favoritesCount = favoriteUiState.favoriteSongs.size
        )

        Spacer(modifier = Modifier.height(24.dp))

        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.White.copy(alpha = 0.05f),
            contentColor = Color.White,
            indicator = {}
        ) {
            ProfileTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        AnimatedContent(
            targetState = selectedTab,
            label = "profileSection"
        ) { tab ->
            when (tab) {
                ProfileTab.Overview -> ProfileOverviewSection(
                    username = authUiState.currentUser?.username ?: "Guest",
                    email = authUiState.currentUser?.email ?: "Add an email",
                    playlistCount = playlistUiState.playlists.size,
                    favoritesCount = favoriteUiState.favoriteSongs.size
                )

                ProfileTab.Settings -> SettingsSection(
                    notificationsEnabled = notificationsEnabled,
                    darkModeEnabled = darkModeEnabled,
                    dataSaverEnabled = dataSaverEnabled,
                    onNotificationsChanged = { notificationsEnabled = it },
                    onDarkModeChanged = { darkModeEnabled = it },
                    onDataSaverChanged = { dataSaverEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                passwordStep = PasswordChangeStep.VerifyCurrent
                currentPassword = ""
                newPassword = ""
                confirmNewPassword = ""
                verifiedPassword = null
                authViewModel.clearError()
                showPasswordDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Change password")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Log out")
        }
    }

    if (showPasswordDialog) {
        PasswordChangeDialog(
            step = passwordStep,
            isLoading = authUiState.isLoading,
            errorMessage = authUiState.errorMessage,
            currentPassword = currentPassword,
            newPassword = newPassword,
            confirmPassword = confirmNewPassword,
            onDismiss = {
                showPasswordDialog = false
                passwordStep = PasswordChangeStep.VerifyCurrent
                currentPassword = ""
                newPassword = ""
                confirmNewPassword = ""
                verifiedPassword = null
                authViewModel.clearError()
            },
            onBack = {
                passwordStep = PasswordChangeStep.VerifyCurrent
                newPassword = ""
                confirmNewPassword = ""
                authViewModel.clearError()
            },
            onCurrentPasswordChange = { currentPassword = it },
            onNewPasswordChange = { newPassword = it },
            onConfirmPasswordChange = { confirmNewPassword = it },
            onVerifyCurrent = {
                authViewModel.verifyCurrentPassword(currentPassword) { verified ->
                    if (verified) {
                        verifiedPassword = currentPassword
                        passwordStep = PasswordChangeStep.EnterNew
                        currentPassword = ""
                        authViewModel.clearError()
                    }
                }
            },
            onSubmitNewPassword = {
                val previousPassword = verifiedPassword ?: return@PasswordChangeDialog
                authViewModel.changePassword(previousPassword, newPassword) {
                    showPasswordDialog = false
                    passwordStep = PasswordChangeStep.VerifyCurrent
                    currentPassword = ""
                    newPassword = ""
                    confirmNewPassword = ""
                    verifiedPassword = null
                    authViewModel.clearError()
                }
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    username: String,
    email: String,
    playlistCount: Int,
    favoritesCount: Int
) {
    val gradient = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = username,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatChip(
                        icon = Icons.Filled.PlaylistPlay,
                        label = "Playlists",
                        value = playlistCount.toString()
                    )
                    StatChip(
                        icon = Icons.Filled.Favorite,
                        label = "Favorites",
                        value = favoritesCount.toString()
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .weight(1f)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = value, color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ProfileOverviewSection(
    username: String,
    email: String,
    playlistCount: Int,
    favoritesCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Account summary", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(icon = Icons.Filled.Person, label = "Username", value = username)
                Divider(color = Color.White.copy(alpha = 0.1f))
                InfoRow(icon = Icons.Filled.Email, label = "Email", value = email)
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Listening stats", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow(icon = Icons.Filled.PlaylistPlay, label = "Playlists", value = "$playlistCount created")
                Divider(color = Color.White.copy(alpha = 0.1f))
                InfoRow(icon = Icons.Filled.Favorite, label = "Favorites", value = "$favoritesCount saved")
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        }
    }
}

@Composable
private fun SettingsSection(
    notificationsEnabled: Boolean,
    darkModeEnabled: Boolean,
    dataSaverEnabled: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onDataSaverChanged: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("App preferences", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            SettingToggleRow(
                icon = Icons.Filled.Notifications,
                title = "Push notifications",
                description = "Get alerts for new releases and updates",
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsChanged
            )
            Divider(color = Color.White.copy(alpha = 0.1f))
            SettingToggleRow(
                icon = Icons.Filled.DarkMode,
                title = "Dynamic theme",
                description = "Follow system color palette",
                checked = darkModeEnabled,
                onCheckedChange = onDarkModeChanged
            )
            Divider(color = Color.White.copy(alpha = 0.1f))
            SettingToggleRow(
                icon = Icons.Filled.Lock,
                title = "Data saver mode",
                description = "Limit streaming to previews when on mobile data",
                checked = dataSaverEnabled,
                onCheckedChange = onDataSaverChanged
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = title, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(description, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PasswordChangeDialog(
    step: PasswordChangeStep,
    isLoading: Boolean,
    errorMessage: String?,
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onVerifyCurrent: () -> Unit,
    onSubmitNewPassword: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F1D2B),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (step == PasswordChangeStep.VerifyCurrent) "Verify your password" else "Create a new password",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val helperText = if (step == PasswordChangeStep.VerifyCurrent) {
                            "Confirm your current password to continue."
                        } else {
                            "Choose a new password. It must be at least 6 characters long."
                        }
                        Text(
                            helperText,
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (step == PasswordChangeStep.VerifyCurrent) {
                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = onCurrentPasswordChange,
                                label = { Text("Current password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = onNewPasswordChange,
                                label = { Text("New password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = onConfirmPasswordChange,
                                label = { Text("Confirm new password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword) {
                                Text(
                                    "Passwords do not match",
                                    color = Color(0xFFFFB4A9),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFFB4A9),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == PasswordChangeStep.VerifyCurrent) {
                        onVerifyCurrent()
                    } else {
                        onSubmitNewPassword()
                    }
                },
                enabled = if (step == PasswordChangeStep.VerifyCurrent) {
                    !isLoading && currentPassword.isNotBlank()
                } else {
                    !isLoading &&
                        newPassword.isNotBlank() &&
                        confirmPassword.isNotBlank() &&
                        newPassword.length >= 6 &&
                        newPassword == confirmPassword
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (step == PasswordChangeStep.VerifyCurrent) "Continue" else "Save")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (step == PasswordChangeStep.EnterNew) {
                        onBack()
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (step == PasswordChangeStep.EnterNew) "Back" else "Cancel")
            }
        }
    )
}

private enum class ProfileTab(val title: String) {
    Overview("Overview"),
    Settings("Settings")
}

private enum class PasswordChangeStep {
    VerifyCurrent,
    EnterNew
}

