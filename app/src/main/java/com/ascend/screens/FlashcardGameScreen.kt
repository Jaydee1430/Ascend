package com.ascend.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ascend.data.FlashcardItem
import com.ascend.data.UserDataStore
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.panel
import com.ascend.ui.theme.primary
import com.ascend.viewModel.FlashcardViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FlashcardGameScreen(navController: NavController, viewModel: FlashcardViewModel, setId: Int) {
    val initialCardsState by viewModel.getCardsForSet(setId).collectAsState(initial = null)
    val initialCards = initialCardsState.orEmpty()
    var gameCards by remember { mutableStateOf<List<FlashcardItem>>(emptyList()) }
    
    val context = LocalContext.current
    val userDataStore = remember { UserDataStore(context) }
    val scope = rememberCoroutineScope()

    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var wrongCount by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    // Option to choose what to show in front (true = Term, false = Definition)
    // Defaulting to false (Definition) as requested
    var showTermOnFront by remember { mutableStateOf(false) }
    var settingsExpanded by remember { mutableStateOf(false) }

    val offsetX = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(initialCardsState) {
        if (gameCards.isEmpty() && initialCards.isNotEmpty()) {
            gameCards = initialCards
        }
    }

    if (initialCardsState == null) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = primary)
        }
        return
    }

    if (gameCards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No cards in this set.", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(primary)) {
                    Text("RETURN")
                }
            }
        }
        return
    }

    if (currentIndex >= gameCards.size) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SESSION COMPLETE", color = primary, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("Exp Gained: ${correctCount * 3}", color = Color.White, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(primary)) {
                    Text("RETURN")
                }
            }
        }
        return
    }

    val currentCard = gameCards[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            Text(
                text = "${currentIndex + 1} / ${gameCards.size}",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            Box {
                IconButton(onClick = { settingsExpanded = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
                DropdownMenu(
                    expanded = settingsExpanded,
                    onDismissRequest = { settingsExpanded = false },
                    modifier = Modifier.background(panel)
                ) {
                    DropdownMenuItem(
                        text = { Text("Show Term on Front", color = Color.White) },
                        trailingIcon = { if (showTermOnFront) Icon(Icons.Default.Done, null, tint = primary) },
                        onClick = {
                            showTermOnFront = true
                            settingsExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Show Definition on Front", color = Color.White) },
                        trailingIcon = { if (!showTermOnFront) Icon(Icons.Default.Done, null, tint = primary) },
                        onClick = {
                            showTermOnFront = false
                            settingsExpanded = false
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                    DropdownMenuItem(
                        text = { Text("Shuffle Cards", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color.White) },
                        onClick = {
                            gameCards = gameCards.shuffled()
                            currentIndex = 0
                            correctCount = 0
                            wrongCount = 0
                            isFlipped = false
                            settingsExpanded = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ScoreIndicator(count = wrongCount, color = Color(0xFFE91E63))
            ScoreIndicator(count = correctCount, color = Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.weight(0.1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .graphicsLayer { rotationZ = rotation.value }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                if (offsetX.value > 300) {
                                    scope.launch {
                                        correctCount++
                                        userDataStore.addExp(3)
                                        launch { rotation.animateTo(offsetX.value / 10f) }
                                        offsetX.animateTo(1200f, animationSpec = tween(300))
                                        
                                        currentIndex++
                                        offsetX.snapTo(0f)
                                        rotation.snapTo(0f)
                                        isFlipped = false
                                    }
                                } else if (offsetX.value < -300) {
                                    scope.launch {
                                        wrongCount++
                                        launch { rotation.animateTo(offsetX.value / 10f) }
                                        offsetX.animateTo(-1200f, animationSpec = tween(300))
                                        
                                        currentIndex++
                                        offsetX.snapTo(0f)
                                        rotation.snapTo(0f)
                                        isFlipped = false
                                    }
                                } else {
                                    scope.launch {
                                        launch { offsetX.animateTo(0f, spring(Spring.DampingRatioMediumBouncy)) }
                                        launch { rotation.animateTo(0f, spring(Spring.DampingRatioMediumBouncy)) }
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    rotation.snapTo(offsetX.value / 15f)
                                }
                            }
                        )
                    }
                    .clickable { isFlipped = !isFlipped },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = panel),
                border = BorderStroke(1.dp, Color(0xFF2E2A5B))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    val frontText = if (showTermOnFront) currentCard.term else currentCard.definition
                    val backText = if (showTermOnFront) currentCard.definition else currentCard.term

                    Text(
                        text = if (isFlipped) backText else frontText,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tap the card to flip it",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ScoreIndicator(count: Int, color: Color) {
    Surface(
        color = Color.Transparent,
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = count.toString(),
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            fontSize = 14.sp
        )
    }
}
