package com.ascend.screens

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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ascend.data.FlashcardItem
import com.ascend.data.UserDataStore
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.panel
import com.ascend.ui.theme.primary
import com.ascend.viewModel.FlashcardViewModel

@Composable
fun ViewCardsScreen(navController: NavController, viewModel: FlashcardViewModel, setId: Int, title: String) {
    val cards by viewModel.getCardsForSet(setId).collectAsState(initial = emptyList())
    val context = LocalContext.current
    val userDataStore = remember { UserDataStore(context) }
    val username by userDataStore.username.collectAsState(initial = "Hunter")

    val pagerState = rememberPagerState(pageCount = { cards.size })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
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

                Surface(
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(32.dp)
                ) {

                }

                Row {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save", tint = Color.White)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = Color.White)
                    }
                }
            }
        }

        // --- Flashcard Carousel (Pager) ---
        item {
            if (cards.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        FlashcardPreviewItem(cards[page])
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.height(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(cards.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color.White else Color.Gray.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(6.dp)
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primary)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        // --- Set Info & User Profile ---
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(username?.take(1)?.uppercase() ?: "H", color = Color.White, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = username ?: "Hunter", color = Color.White, fontSize = 14.sp)

                    Spacer(modifier = Modifier.width(12.dp))
                    VerticalDivider(modifier = Modifier.height(14.dp), color = Color.Gray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(text = "${cards.size} terms", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // --- Action List ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionMenuItem(icon = Icons.Outlined.MenuBook, label = "Flashcards", color = Color(0xFF4285F4))
                ActionMenuItem(icon = Icons.Outlined.Quiz, label = "Test", color = Color(0xFF3F51B5))
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Cards in this set",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- List of Terms & Definitions ---
        items(cards) { card ->
            TermDefinitionItem(card)
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun TermDefinitionItem(card: FlashcardItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),

        shape = RoundedCornerShape(12.dp),
        color = panel
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = card.term,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = card.definition,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun FlashcardPreviewItem(card: FlashcardItem) {
    var flipped by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable { flipped = !flipped },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = panel)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color(0xFF2E2A5B), RoundedCornerShape(24.dp))
            .padding(24.dp)) {
            Text(
                text = if (flipped) card.definition else card.term,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )

            Icon(
                Icons.Default.Fullscreen,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
            )
        }
    }
}

@Composable
fun ActionMenuItem(icon: ImageVector, label: String, color: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .border(1.dp, Color(0xFF2E2A5B), RoundedCornerShape(24.dp))
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        color = panel
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
