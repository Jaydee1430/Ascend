package com.ascend.screens

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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.BookmarkBorder
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

@Composable
fun ViewCardsScreen(navController: NavController, viewModel: FlashcardViewModel, setId: Int, title: String) {
    val cards by viewModel.getCardsForSet(setId).collectAsState(initial = emptyList())
    val pagerState = rememberPagerState(pageCount = { cards.size })

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        // --- Background Image ---
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

            // --- Custom Top Bar ---
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
                        IconButton(onClick = { }) {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save", tint = Color.White)
                        }
                        // This button now navigates to the dedicated Edit Set screen
                        IconButton(onClick = { navController.navigate("edit_set/$setId/$title") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit set", tint = Color.White)
                        }
                    }
                }
            }

            // --- Flashcard Carousel ---
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
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primary)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            // --- Set Info Section ---
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                        }
                    }
                    Text(text = "${cards.size} terms", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // --- Action Menu ---
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
                        onClick = { /* Future feature */ }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Cards in this set", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Scrollable Review List ---
            items(cards) { card ->
                TermDefinitionReviewItem(card)
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun TermDefinitionReviewItem(card: FlashcardItem) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = panel.copy(alpha = 0.8f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = card.term, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
