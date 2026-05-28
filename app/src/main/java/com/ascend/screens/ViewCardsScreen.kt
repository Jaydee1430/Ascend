package com.ascend.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ascend.data.FlashcardItem
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.panel
import com.ascend.ui.theme.primary
import com.ascend.viewModel.FlashcardViewModel
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun ViewCardsScreen(navController: NavController, viewModel: FlashcardViewModel, setId: Int, title: String) {
    val cardsState by viewModel.getCardsForSet(setId).collectAsState(initial = null)
    val cards = cardsState.orEmpty()
    val pagerState = rememberPagerState(pageCount = { cards.size })
    val context = LocalContext.current
    var showDeleteSetDialog by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<FlashcardItem?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { 
            saveSetToFile(context, it, title, cards)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Image(
            painter = painterResource(id = R.drawable.ascend_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row {
                        IconButton(onClick = { navController.navigate("edit_set/$setId/${Uri.encode(title)}") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit set", tint = Color.White)
                        }
                    }
                }
            }

            item {
                if (cards.isNotEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth().height(260.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp),
                            pageSpacing = 16.dp
                        ) { page ->
                            FlashcardPreviewItem(cards[page])
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.height(8.dp), horizontalArrangement = Arrangement.Center) {
                            repeat(cards.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.White else Color.Gray.copy(alpha = 0.5f)
                                Box(modifier = Modifier.padding(horizontal = 4.dp).clip(CircleShape).background(color).size(6.dp))
                            }
                        }
                    }
                } else if (cardsState == null) {
                    Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primary)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                        Text("No cards in this set.", color = Color.Gray)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { 
                            createDocumentLauncher.launch("${title.replace(" ", "_")}.json")
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                        }
                    }
                    Text(text = "${cards.size} terms", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionMenuItem(
                        icon = Icons.Outlined.MenuBook, 
                        label = "Flashcards", 
                        color = Color(0xFF4285F4),
                        onClick = { navController.navigate("flashcard_game/$setId") }
                    )
                    ActionMenuItem(
                        icon = Icons.Outlined.Quiz, 
                        label = "Test", 
                        color = Color(0xFF3F51B5),
                        onClick = { navController.navigate("test_screen/$setId") }
                    )
                    ActionMenuItem(
                        icon = Icons.Default.Delete,
                        label = "Delete Set",
                        color = Color(0xFFFF6B8A),
                        onClick = { showDeleteSetDialog = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Cards in this set", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(cards) { card ->
                TermDefinitionReviewItem(
                    card = card,
                    onDeleteClick = { cardToDelete = card }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showDeleteSetDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSetDialog = false },
            title = {
                Text("Delete set?", color = Color.White)
            },
            text = {
                Text(
                    text = "This will delete \"$title\" and all ${cards.size} cards in it. This cannot be undone.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSet(setId, title) {
                            showDeleteSetDialog = false
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF6B8A))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSetDialog = false }) {
                    Text("Cancel", color = primary)
                }
            },
            containerColor = panel
        )
    }

    cardToDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = {
                Text("Delete flashcard?", color = Color.White)
            },
            text = {
                Text(
                    text = "This will remove \"${card.term}\" from this set.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFlashcard(card)
                        cardToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF6B8A))
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Cancel", color = primary)
                }
            },
            containerColor = panel
        )
    }
}

private fun saveSetToFile(context: Context, uri: Uri, title: String, cards: List<FlashcardItem>) {
    try {
        val jsonObject = JSONObject().apply {
            put("title", title)
            val cardsArray = JSONArray()
            cards.forEach { card ->
                val cardObject = JSONObject().apply {
                    put("term", card.term)
                    put("definition", card.definition)
                }
                cardsArray.put(cardObject)
            }
            put("cards", cardsArray)
        }

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(jsonObject.toString(4).toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun TermDefinitionReviewItem(card: FlashcardItem, onDeleteClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = panel.copy(alpha = 0.8f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.term,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete flashcard",
                        tint = Color(0xFFFF6B8A).copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = card.definition, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun FlashcardPreviewItem(card: FlashcardItem) {
    var flipped by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxSize().clickable { flipped = !flipped },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = panel.copy(alpha = 0.9f))
    ) {
        Box(modifier = Modifier.fillMaxSize().border(1.dp, Color(0xFF2E2A5B), RoundedCornerShape(24.dp)).padding(24.dp)) {
            Text(
                text = if (flipped) card.definition else card.term,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.BottomEnd).size(24.dp))
        }
    }
}

@Composable
fun ActionMenuItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(64.dp).border(1.dp, Color(0xFF2E2A5B), RoundedCornerShape(12.dp)).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = panel.copy(alpha = 0.8f)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
