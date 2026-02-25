package com.ascend.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ascend.R
import com.ascend.data.UserDataStore
import com.ascend.ui.theme.AscendTheme
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.panel
import com.ascend.ui.theme.primary
import com.ascend.viewModel.FlashcardViewModel
import kotlinx.coroutines.launch

@Composable
fun SetupProfileScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current
    val userDataStore = remember { UserDataStore(context) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ascend_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SYSTEM REGISTRATION",
                color = primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "IDENTIFY YOURSELF",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { if (it.length <= 15) username = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF171131).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1E2C4F), RoundedCornerShape(16.dp)),
                placeholder = {
                    Text("Enter Hunter Name...", color = Color.Gray, fontSize = 14.sp)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = primary,
                    focusedBorderColor = primary,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "You can change your Username later.",
                color = Color.Gray.copy(alpha = 0.7f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = {
                    scope.launch {
                        userDataStore.saveUsername(username)
                        userDataStore.saveRank("E-RANK")
                        userDataStore.completeSetup() // This ensures setup is marked as complete
                        navController.navigate("home") {
                            popUpTo("setup_profile") { inclusive = true }
                        }
                    }
                },
                enabled = username.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    disabledContainerColor = Color(0xFF1E2C4F)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "CONFIRM SELECTION",
                    color = if (username.isNotBlank()) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController, viewModel: FlashcardViewModel) {
    val context = LocalContext.current
    val userDataStore = remember { UserDataStore(context) }
    val username by userDataStore.username.collectAsState(initial = "Hunter")
    val rank by userDataStore.rank.collectAsState(initial = "E-RANK")
    val exp by userDataStore.exp.collectAsState(initial = 0)
    val notificationsEnabled by userDataStore.notificationsEnabled.collectAsState(initial = true)
    val darkMode by userDataStore.darkMode.collectAsState(initial = true)
    val flashcardSets by viewModel.allSets.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    val rankInfo = userDataStore.getNextRank(exp)
    val nextRankExp = rankInfo.second

    var showChangeUsernameDialog by remember { mutableStateOf(false) }
    var showHunterInfo by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Image(
            painter = painterResource(id = R.drawable.ascend_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(64.dp)) }

            // --- Profile Header ---
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.2f))
                            .border(2.dp, primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = username?.take(1)?.uppercase() ?: "H", color = primary, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = username?.uppercase() ?: "HUNTER", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Text(text = rank, color = primary, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                }
            }

            // --- Stats Section ---
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(modifier = Modifier.weight(1f), label = "TOTAL EXP", value = exp.toString(), icon = Icons.Default.TrendingUp)
                    StatCard(modifier = Modifier.weight(1f), label = "RANK", value = rank.split("-")[0], icon = Icons.Default.MilitaryTech)
                }
            }

            // --- Progress Section ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = panel.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, Color(0xFF2E2A5B))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "PROGRESS TO NEXT RANK", color = primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "EXP", color = Color.Gray, fontSize = 12.sp)
                            Text(text = "$exp / $nextRankExp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val progress = if (nextRankExp > 0) exp.toFloat() / nextRankExp else 1f
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = primary,
                            trackColor = Color.Black.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // --- Menu Options ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileMenuItem(
                        icon = Icons.Default.Edit, 
                        label = "Change Username",
                        onClick = { showChangeUsernameDialog = true }
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Settings, 
                        label = "System Settings",
                        onClick = { showSettings = true }
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Info, 
                        label = "Hunter Information",
                        onClick = { showHunterInfo = true }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        if (showChangeUsernameDialog) {
            ChangeUsernameDialog(
                currentUsername = username ?: "",
                onDismiss = { showChangeUsernameDialog = false },
                onConfirm = { newUsername ->
                    scope.launch {
                        userDataStore.saveUsername(newUsername)
                        showChangeUsernameDialog = false
                    }
                }
            )
        }

        // Hunter Info Status Window Overlay
        AnimatedVisibility(
            visible = showHunterInfo,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            HunterInfoStatusWindow(
                username = username ?: "Hunter",
                rank = rank,
                exp = exp,
                nextRankExp = nextRankExp,
                setsCount = flashcardSets.size,
                onClose = { showHunterInfo = false }
            )
        }

        // System Settings Window Overlay
        AnimatedVisibility(
            visible = showSettings,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            SystemSettingsWindow(
                notificationsEnabled = notificationsEnabled,
                darkMode = darkMode,
                onToggleNotifications = { scope.launch { userDataStore.setNotificationsEnabled(it) } },
                onToggleDarkMode = { scope.launch { userDataStore.setDarkMode(it) } },
                onResetProgress = { 
                    scope.launch { 
                        userDataStore.clearData()
                        navController.navigate("startscreen") {
                            popUpTo(0) { inclusive = true }
                        }
                    } 
                },
                onClose = { showSettings = false }
            )
        }
    }
}

@Composable
fun SystemSettingsWindow(
    notificationsEnabled: Boolean,
    darkMode: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onResetProgress: () -> Unit,
    onClose: () -> Unit
) {
    var showResetConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = true, onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .border(2.dp, primary, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B21))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SYSTEM SETTINGS",
                        color = primary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = primary.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Settings Rows
                SettingSwitchRow(
                    label = "SYSTEM ALERTS",
                    description = "Enable notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = onToggleNotifications,
                    icon = Icons.Default.Notifications
                )


                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Danger Zone
                Text(
                    text = "DANGER ZONE",
                    color = Color(0xFFE91E63),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { showResetConfirmation = true },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE91E63).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFFE91E63))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "RESET ALL PROGRESS", color = Color(0xFFE91E63), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(primary, RoundedCornerShape(8.dp))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "CLOSE", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
        }

        if (showResetConfirmation) {
            AlertDialog(
                onDismissRequest = { showResetConfirmation = false },
                containerColor = Color(0xFF0F0B21),
                title = { Text("WARNING", color = Color(0xFFE91E63), fontWeight = FontWeight.ExtraBold) },
                text = { Text("This will permanently delete your hunter profile, EXP, rank, and all flashcard sets. This action cannot be undone.", color = Color.White) },
                confirmButton = {
                    Button(
                        onClick = onResetProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        Text("RESET")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirmation = false }) {
                        Text("CANCEL", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingSwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = description, color = Color.Gray, fontSize = 11.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primary,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Black.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun HunterInfoStatusWindow(
    username: String,
    rank: String,
    exp: Int,
    nextRankExp: Int,
    setsCount: Int,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = true, onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .border(2.dp, primary, RoundedCornerShape(16.dp))
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B21))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STATUS",
                        color = primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = primary.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(24.dp))

                StatusRow(label = "NAME", value = username.uppercase())
                StatusRow(label = "RANK", value = rank)
                StatusRow(label = "LEVEL", value = (exp / 1000 + 1).toString())
                StatusRow(label = "EXP", value = "$exp / $nextRankExp")
                StatusRow(label = "COLLECTIONS", value = setsCount.toString())
                StatusRow(label = "CLASS", value = "FLASHCARD HUNTER")

                Spacer(modifier = Modifier.height(32.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(primary, RoundedCornerShape(8.dp))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CONFIRM",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun ChangeUsernameDialog(currentUsername: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newUsername by remember { mutableStateOf(currentUsername) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Hunter Name", color = Color.White) },
        containerColor = panel,
        text = {
            Column {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { if (it.length <= 15) newUsername = it },
                    label = { Text("New Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = primary,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (newUsername.isNotBlank()) onConfirm(newUsername) },
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                enabled = newUsername.isNotBlank() && newUsername != currentUsername
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = panel.copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, Color(0xFF2E2A5B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, color: Color = Color.White, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = panel.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = if (color == Color.White) primary else color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
