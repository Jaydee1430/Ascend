package com.ascend.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatQuote
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ascend.R
import com.ascend.data.FlashcardSet
import com.ascend.data.UserDataStore
import com.ascend.screens.ui.theme.CustomFloatingNavBar
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.panel
import com.ascend.ui.theme.primary
import com.ascend.viewModel.FlashcardItemInternal
import com.ascend.viewModel.FlashcardViewModel
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController, viewModel: FlashcardViewModel) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showManualCreate by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    val context = LocalContext.current
    val userDataStore = remember { UserDataStore(context) }
    val username by userDataStore.username.collectAsState(initial = "Hunter")
    val currentExp by userDataStore.exp.collectAsState(initial = 0)
    
    val rankInfo = userDataStore.getNextRank(currentExp)
    val rank = rankInfo.first
    val nextRankExp = rankInfo.second

    val flashcardSets by viewModel.allSets.collectAsState(initial = emptyList())

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cardCount by remember { mutableStateOf("5") }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            importSetFromFile(context, it, viewModel) {
                showBottomSheet = false
            }
        }
    }

    Scaffold(
        containerColor = bgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ascend_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )
            when (selectedIndex) {
                0 -> MainDashboard(
                    sets = flashcardSets,
                    username = username ?: "Hunter",
                    rank = rank,
                    exp = currentExp,
                    nextRankExp = nextRankExp,
                    onCardClick = { set ->
                        navController.navigate("view_cards/${set.id}/${set.title}")
                    },
                    onViewAllClick = { selectedIndex = 2 }
                )
                2 -> FlashcardListScreen(
                    sets = flashcardSets,
                    onCardClick = { set ->
                        navController.navigate("view_cards/${set.id}/${set.title}")
                    }
                )
                3 -> ProfileScreen(navController = navController, viewModel = viewModel)
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
                            showManualCreate = false
                        } else {
                            selectedIndex = index
                        }
                    }
                )
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showBottomSheet = false
                    showManualCreate = false
                },
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
                    if (!showManualCreate) {
                        Text(text = "Create Flashcard Set", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        ImportOptionItem(
                            title = "Import from File",
                            subtitle = "Use a JSON file shared by others",
                            icon = Icons.Default.FileUpload,
                            onClick = { importLauncher.launch("application/json") }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ImportOptionItem(
                            title = "Create Flashcard Set",
                            subtitle = "Build your set from scratch",
                            icon = Icons.Default.Add,
                            onClick = { showManualCreate = true }
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    } else {
                        Text(text = "Manual Configuration", style = MaterialTheme.typography.headlineSmall, color = Color.White)
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
                                showManualCreate = false
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
}

private fun importSetFromFile(context: Context, uri: Uri, viewModel: FlashcardViewModel, onComplete: () -> Unit) {
    try {
        val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (jsonString != null) {
            val jsonObject = JSONObject(jsonString)
            val title = jsonObject.getString("title")
            val cardsArray = jsonObject.getJSONArray("cards")
            val cards = mutableListOf<FlashcardItemInternal>()
            
            for (i in 0 until cardsArray.length()) {
                val cardObj = cardsArray.getJSONObject(i)
                cards.add(FlashcardItemInternal(
                    term = cardObj.getString("term"),
                    definition = cardObj.getString("definition")
                ))
            }
            
            viewModel.saveSetWithCards(title, "Imported Set", cards) {
                onComplete()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun ImportOptionItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MainDashboard(
    sets: List<FlashcardSet>, 
    username: String, 
    rank: String, 
    exp: Int, 
    nextRankExp: Int, 
    onCardClick: (FlashcardSet) -> Unit,
    onViewAllClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(32.dp)) }

        item {
            UserProfileSection(username = username, exp = exp, nextRankExp = nextRankExp, rank = rank)
        }

        item {
            QuoteWidget()
        }

        item {
            YourSetsWidget(count = sets.size, onClick = onViewAllClick)
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
fun QuoteWidget() {
    val quotes = listOf(
        "It doesn't matter how slow you go as long as you do not stop.",
        "Your grades don’t define your limits—they show where you start leveling.",
        "Consistency is your daily quest—miss it, and you lose experience.",
        "Don’t run from difficult topics—those are your boss fights.",
        "You won’t understand everything at first. Keep grinding until it becomes your power."
    )
    val randomQuote = remember { quotes.random() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = panel.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(32.dp).align(Alignment.Top)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = randomQuote,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "— SYSTEM MESSAGE",
                    color = primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourSetsWidget(count: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = panel),
        border = BorderStroke(1.dp, Color(0xFF2E2A5B))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CollectionsBookmark, contentDescription = null, tint = primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "YOUR SETS", color = primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(text = "$count Collections", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun UserProfileSection(username: String, exp: Int, nextRankExp: Int, rank: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF1E1B4B), panel)))
            .border(1.dp, Color(0xFF2E2A5B), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "EXP", color = Color.Gray, fontSize = 10.sp)
                        Text(text = "$exp / $nextRankExp", color = Color.Gray, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        val progress = if (nextRankExp > 0) exp.toFloat() / nextRankExp else 1f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
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
