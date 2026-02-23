package com.ascend.screens

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ascend.R
import com.ascend.data.UserDataStore
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.panel
import com.ascend.ui.theme.primary
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
                text = "Warning: Character name cannot be changed later.",
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
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val userDataStore = remember { UserDataStore(context) }
    val username by userDataStore.username.collectAsState(initial = "Hunter")
    val rank by userDataStore.rank.collectAsState(initial = "E-RANK")
    val exp by userDataStore.exp.collectAsState(initial = 0)
    
    val rankInfo = userDataStore.getNextRank(exp)
    val nextRankExp = rankInfo.second

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
                    ProfileMenuItem(icon = Icons.Default.Edit, label = "Edit Character")
                    ProfileMenuItem(icon = Icons.Default.Settings, label = "System Settings")
                    ProfileMenuItem(icon = Icons.Default.Info, label = "Hunter Information")
                    ProfileMenuItem(icon = Icons.Default.Logout, label = "Logout", color = Color(0xFFE91E63))
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
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
fun ProfileMenuItem(icon: ImageVector, label: String, color: Color = Color.White) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp).clickable { },
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
