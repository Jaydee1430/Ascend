package com.ascend.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.West
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val cards by viewModel.getCardsForSet(setId).collectAsState(initial = emptyList())
    val context = LocalContext.current
    val userDataStore = remember { UserDataStore(context) }
    val scope = rememberCoroutineScope()

    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var wrongCount by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val offsetX = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    if (cards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = primary)
        }
        return
    }

    if (currentIndex >= cards.size) {
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

    val currentCard = cards[currentIndex]

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
                text = "${currentIndex + 1} / ${cards.size}",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            IconButton(onClick = { }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(Icons.Outlined.VolumeUp, contentDescription = null, tint = Color.Gray)
                        Icon(Icons.Outlined.StarBorder, contentDescription = null, tint = Color.Gray)
                    }

                    Text(
                        text = if (isFlipped) currentCard.definition else currentCard.term,
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
