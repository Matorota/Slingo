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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import kotlinx.coroutines.flow.first
import lt.viko.eif.mtrimaitis.Slingo.data.models.FriendRequest
import lt.viko.eif.mtrimaitis.Slingo.data.models.SharedPlaylist
import lt.viko.eif.mtrimaitis.Slingo.data.models.User
import lt.viko.eif.mtrimaitis.Slingo.viewmodel.AuthViewModel
import lt.viko.eif.mtrimaitis.Slingo.viewmodel.FavoriteViewModel
import lt.viko.eif.mtrimaitis.Slingo.viewmodel.FriendViewModel
import lt.viko.eif.mtrimaitis.Slingo.viewmodel.PlaylistViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    playlistViewModel: PlaylistViewModel,
    favoriteViewModel: FavoriteViewModel,
    friendViewModel: FriendViewModel,
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
    var showLogoutDialog by remember { mutableStateOf(false) }

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

                ProfileTab.Friends -> FriendsSection(
                    friendViewModel = friendViewModel,
                    playlistViewModel = playlistViewModel
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
            onClick = { showLogoutDialog = true },
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

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                authViewModel.logout()
                onLogout()
                showLogoutDialog = false
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F1D2B),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text("Confirm Logout", style = MaterialTheme.typography.titleMedium, color = Color.White)
        },
        text = {
            Text(
                "Are you sure you want to log out?",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                )
            ) {
                Text("Log out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
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

    // Database Info Card
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Database Information", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            val context = androidx.compose.ui.platform.LocalContext.current
            val dbPath = context.getDatabasePath("slingo_database").absolutePath
            InfoRow(
                icon = Icons.Filled.Lock,
                label = "Database Location",
                value = dbPath
            )
            Divider(color = Color.White.copy(alpha = 0.1f))
            Text(
                "To view database data:\n1. Enable USB debugging\n2. Use Android Studio Database Inspector\n3. Or use adb: adb shell run-as lt.viko.eif.mtrimaitis.Slingo cat databases/slingo_database",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
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

@Composable
private fun FriendsSection(
    friendViewModel: FriendViewModel,
    playlistViewModel: PlaylistViewModel
) {
    val friendUiState by friendViewModel.uiState.collectAsState()
    val playlistUiState by playlistViewModel.uiState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showSharePlaylistDialog by remember { mutableStateOf(false) }
    var showFriendPlaylistsDialog by remember { mutableStateOf(false) }
    var selectedFriendId by remember { mutableLongStateOf(-1L) }
    var selectedPlaylistId by remember { mutableLongStateOf(-1L) }
    var selectedUserId by remember { mutableLongStateOf(-1L) }
    var friendPlaylists by remember { mutableStateOf<List<lt.viko.eif.mtrimaitis.Slingo.data.models.Playlist>>(emptyList()) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() || searchQuery.isEmpty()) {
            friendViewModel.searchUsers(searchQuery)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Search Users
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Search Users", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by username or email", color = Color.White.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (friendUiState.searchResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(friendUiState.searchResults.size) { index ->
                            val user = friendUiState.searchResults[index]
                            UserSearchResultRow(
                                user = user,
                                onAddFriend = { friendViewModel.sendFriendRequest(user.id) },
                                onSharePlaylist = {
                                    selectedUserId = user.id
                                    showSharePlaylistDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Pending Friend Requests
        if (friendUiState.pendingRequests.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Friend Requests", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    friendUiState.pendingRequests.forEach { request ->
                        FriendRequestRow(
                            request = request,
                            friendViewModel = friendViewModel,
                            onAccept = { friendViewModel.acceptFriendRequest(request.id) },
                            onDecline = { friendViewModel.declineFriendRequest(request.id) }
                        )
                        if (request != friendUiState.pendingRequests.last()) {
                            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Pending Shared Playlists
        if (friendUiState.pendingSharedPlaylists.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Shared Playlists", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    friendUiState.pendingSharedPlaylists.forEach { sharedPlaylist ->
                        SharedPlaylistRequestRow(
                            sharedPlaylist = sharedPlaylist,
                            playlistViewModel = playlistViewModel,
                            onAccept = { 
                                friendViewModel.acceptSharedPlaylist(sharedPlaylist.id) {
                                    playlistViewModel.loadPlaylists()
                                }
                            },
                            onDecline = { friendViewModel.declineSharedPlaylist(sharedPlaylist.id) }
                        )
                        if (sharedPlaylist != friendUiState.pendingSharedPlaylists.last()) {
                            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Friends List
        if (friendUiState.friends.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Your Friends", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    friendUiState.friends.forEach { friend ->
                        FriendRow(
                            friend = friend,
                            onViewPlaylists = {
                                selectedFriendId = friend.id
                                showFriendPlaylistsDialog = true
                            },
                            onSharePlaylist = {
                                selectedUserId = friend.id
                                showSharePlaylistDialog = true
                            }
                        )
                        if (friend != friendUiState.friends.last()) {
                            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        if (friendUiState.errorMessage != null) {
            Surface(
                color = Color(0xFFFFB4A9).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = friendUiState.errorMessage ?: "",
                    color = Color(0xFFFFB4A9),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }

    // Share Playlist Dialog
    if (showSharePlaylistDialog) {
        SharePlaylistDialog(
            playlists = playlistUiState.playlists,
            onDismiss = { showSharePlaylistDialog = false },
            onShare = { playlistId ->
                friendViewModel.sharePlaylist(playlistId, selectedUserId)
                showSharePlaylistDialog = false
            }
        )
    }

    // Friend Playlists Dialog
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(selectedFriendId) {
        if (selectedFriendId > 0 && showFriendPlaylistsDialog) {
            withContext(Dispatchers.IO) {
                val db = lt.viko.eif.mtrimaitis.Slingo.data.database.DatabaseProvider.getDatabase(context)
                friendPlaylists = db.playlistDao().getPlaylistsByUser(selectedFriendId).first()
            }
        }
    }

    if (showFriendPlaylistsDialog) {
        FriendPlaylistsDialog(
            friendName = friendUiState.friends.find { it.id == selectedFriendId }?.username ?: "Friend",
            playlists = friendPlaylists,
            onDismiss = { showFriendPlaylistsDialog = false }
        )
    }
}

@Composable
private fun UserSearchResultRow(
    user: User,
    onAddFriend: () -> Unit,
    onSharePlaylist: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.username, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Text(user.email, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onAddFriend) {
                Icon(Icons.Filled.Add, contentDescription = "Add Friend", tint = Color.White)
            }
            IconButton(onClick = onSharePlaylist) {
                Icon(Icons.Filled.Share, contentDescription = "Share Playlist", tint = Color.White)
            }
        }
    }
}

@Composable
private fun FriendRequestRow(
    request: FriendRequest,
    friendViewModel: FriendViewModel,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var fromUser by remember { mutableStateOf<User?>(null) }
    
    LaunchedEffect(request.fromUserId) {
        withContext(Dispatchers.IO) {
            fromUser = friendViewModel.getUserById(request.fromUserId)
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Group,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Friend request", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(
                fromUser?.username ?: "User #${request.fromUserId}",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onAccept) {
            Icon(Icons.Filled.Check, contentDescription = "Accept", tint = Color(0xFF4CAF50))
        }
        IconButton(onClick = onDecline) {
            Icon(Icons.Filled.Close, contentDescription = "Decline", tint = Color(0xFFE53935))
        }
    }
}

@Composable
private fun SharedPlaylistRequestRow(
    sharedPlaylist: SharedPlaylist,
    playlistViewModel: PlaylistViewModel,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val playlistUiState by playlistViewModel.uiState.collectAsState()
    val playlist = playlistUiState.playlists.find { it.id == sharedPlaylist.playlistId }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.PlaylistPlay,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Shared playlist", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(
                playlist?.name ?: "Playlist #${sharedPlaylist.playlistId}",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onAccept) {
            Icon(Icons.Filled.Check, contentDescription = "Accept", tint = Color(0xFF4CAF50))
        }
        IconButton(onClick = onDecline) {
            Icon(Icons.Filled.Close, contentDescription = "Decline", tint = Color(0xFFE53935))
        }
    }
}

@Composable
private fun FriendRow(
    friend: User,
    onSharePlaylist: () -> Unit,
    onViewPlaylists: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Person,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(friend.username, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(friend.email, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onViewPlaylists) {
            Icon(Icons.Filled.PlaylistPlay, contentDescription = "View Playlists", tint = Color.White)
        }
        IconButton(onClick = onSharePlaylist) {
            Icon(Icons.Filled.Share, contentDescription = "Share Playlist", tint = Color.White)
        }
    }
}

@Composable
private fun SharePlaylistDialog(
    playlists: List<lt.viko.eif.mtrimaitis.Slingo.data.models.Playlist>,
    onDismiss: () -> Unit,
    onShare: (Long) -> Unit
) {
    var selectedPlaylistId by remember { mutableLongStateOf(-1L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F1D2B),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text("Share Playlist", style = MaterialTheme.typography.titleMedium, color = Color.White)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (playlists.isEmpty()) {
                    Text("No playlists available", color = Color.White.copy(alpha = 0.7f))
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playlists.size) { index ->
                            val playlist = playlists[index]
                            Surface(
                                color = if (selectedPlaylistId == playlist.id) 
                                    Color.White.copy(alpha = 0.15f) 
                                else 
                                    Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPlaylistId = playlist.id }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.PlaylistPlay,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(playlist.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (selectedPlaylistId > 0) onShare(selectedPlaylistId) },
                enabled = selectedPlaylistId > 0,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private enum class ProfileTab(val title: String) {
    Overview("Overview"),
    Friends("Friends"),
    Settings("Settings")
}

@Composable
private fun FriendPlaylistsDialog(
    friendName: String,
    playlists: List<lt.viko.eif.mtrimaitis.Slingo.data.models.Playlist>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1F1D2B),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text("$friendName's Playlists", style = MaterialTheme.typography.titleMedium, color = Color.White)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (playlists.isEmpty()) {
                    Text("No playlists available", color = Color.White.copy(alpha = 0.7f))
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playlists.size) { index ->
                            val playlist = playlists[index]
                            Surface(
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.PlaylistPlay,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(playlist.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                        Text("${playlist.id} songs", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Close")
            }
        }
    )
}

private enum class PasswordChangeStep {
    VerifyCurrent,
    EnterNew
}

