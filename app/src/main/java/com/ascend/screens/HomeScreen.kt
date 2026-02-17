package com.ascend.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ascend.data.FlashcardSet
import com.ascend.data.UserDataStore
import com.ascend.screens.ui.theme.CustomFloatingNavBar
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.panel
import com.ascend.ui.theme.primary
import com.ascend.viewModel.FlashcardViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.ascend.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController, viewModel: FlashcardViewModel) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    val context = LocalContext.current
    val userDataStore = remember { UserDataStore(context) }
    val username by userDataStore.username.collectAsState(initial = "Hunter")
    val rank by userDataStore.rank.collectAsState(initial = "E-RANK")

    val flashcardSets by viewModel.allSets.collectAsState(initial = emptyList())

    // Form states for BottomSheet
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cardCount by remember { mutableStateOf("5") }

    Scaffold(
        containerColor = bgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Switcher
            when (selectedIndex) {
                0 -> MainDashboard(
                    sets = flashcardSets,
                    username = username ?: "Hunter",
                    rank = rank ?: "E-RANK",
                    onCardClick = { set ->
                        navController.navigate("view_cards/${set.id}/${set.title}")
                    }
                )
                2 -> FlashcardListScreen(
                    sets = flashcardSets,
                    onCardClick = { set ->
                        navController.navigate("view_cards/${set.id}/${set.title}")
                    }
                )
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Screen $selectedIndex", color = Color.White.copy(alpha = 0.3f))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
            ) {
                CustomFloatingNavBar(
                    selectedIndex = selectedIndex,
                    onItemSelected = { index ->
                        if (index == 1) {
                            showBottomSheet = true
                        } else {
                            selectedIndex = index
                        }
                    }
                )
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = panel,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Create Flashcard Set", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cardCount,
                        onValueChange = { if (it.all { c -> c.isDigit() }) cardCount = it },
                        label = { Text("Number of Cards") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            showBottomSheet = false
                            val count = cardCount.toIntOrNull() ?: 5
                            val encodedDesc = if (description.isEmpty()) "none" else URLEncoder.encode(description, StandardCharsets.UTF_8.toString())
                            navController.navigate("create_cards/$title/$encodedDesc/$count")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(primary),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Continue")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun MainDashboard(sets: List<FlashcardSet>, username: String, rank: String, onCardClick: (FlashcardSet) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(32.dp)) }

        item {
            UserProfileSection(username = username, exp = 0, rank = rank)
        }

        item {
            Text(text = "RECENT FLASHCARDS", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
        }

        items(sets.take(3)) { set ->
            FlashcardSetCard(set, onClick = { onCardClick(set) })
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun UserProfileSection(username: String, exp: Int, rank: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF1E1B4B), panel)))
            .border(1.dp, Color(0xFF2E2A5B), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Profile Image Placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.2f))
                    .border(2.dp, primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = username.take(1).uppercase(), color = primary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = username.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 1.sp)
                Text(text = rank, color = primary, fontWeight = FontWeight.Black, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                // EXP Bar
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "EXP", color = Color.Gray, fontSize = 10.sp)
                        Text(text = "$exp / 1000", color = Color.Gray, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(exp / 1000f)
                                .fillMaxHeight()
                                .background(primary)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardSetCard(set: FlashcardSet, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = panel),
        border = BorderStroke(1.dp, Color(0xFF2E2A5B))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1.0f)) {
                Text(text = set.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (set.description.isNotEmpty()) {
                    Text(text = set.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
            }
            Icon(
                painter = painterResource(id = R.drawable.book),
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
