package com.ascend.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch

data class Question(
    val term: String,
    val correctAnswer: String,
    val options: List<String>
)

@Composable
fun TestScreen(navController: NavController, viewModel: FlashcardViewModel, setId: Int) {
    val cards by viewModel.getCardsForSet(setId).collectAsState(initial = emptyList())
    val context = LocalContext.current
    val userDataStore = remember { UserDataStore(context) }
    val scope = rememberCoroutineScope()

    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(cards) {
        if (cards.size >= 2 && questions.isEmpty()) {
            questions = cards.shuffled().map { card ->
                val distractors = cards.filter { it.id != card.id }
                    .shuffled()
                    .take(3)
                    .map { it.definition }
                
                Question(
                    term = card.term,
                    correctAnswer = card.definition,
                    options = (distractors + card.definition).shuffled()
                )
            }
        }
    }

    if (cards.isEmpty() || questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            if (cards.isEmpty()) {
                Text("Loading cards...", color = Color.White)
            } else if (cards.size < 2) {
                Text("Need at least 2 cards for a test.", color = Color.White)
            } else {
                CircularProgressIndicator(color = primary)
            }
        }
        return
    }

    if (isFinished) {
        TestResultScreen(
            score = score,
            total = questions.size,
            onReturn = { navController.popBackStack() },
            onRestart = {
                currentIndex = 0
                score = 0
                selectedOption = null
                isFinished = false
                questions = cards.shuffled().map { card ->
                    val distractors = cards.filter { it.id != card.id }
                        .shuffled()
                        .take(3)
                        .map { it.definition }
                    Question(
                        term = card.term,
                        correctAnswer = card.definition,
                        options = (distractors + card.definition).shuffled()
                    )
                }
            }
        )
        return
    }

    val currentQuestion = questions[currentIndex]

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
                text = "TEST: ${currentIndex + 1} / ${questions.size}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size },
                    modifier = Modifier.fillMaxSize(),
                    color = primary,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    strokeWidth = 4.dp
                )
                Text(
                    text = "${((currentIndex + 1).toFloat() / questions.size * 100).roundToInt()}%",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(200.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = panel),
            border = BorderStroke(1.dp, Color(0xFF2E2A5B))
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = currentQuestion.term,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            currentQuestion.options.forEach { option ->
                val isCorrect = option == currentQuestion.correctAnswer
                val isSelected = option == selectedOption
                
                OptionButton(
                    text = option,
                    isSelected = isSelected,
                    onClick = {
                        if (selectedOption == null) {
                            selectedOption = option
                            if (isCorrect) {
                                score++
                                scope.launch { userDataStore.addExp(5) }
                            }
                            
                            scope.launch {
                                kotlinx.coroutines.delay(1000)
                                if (currentIndex < questions.size - 1) {
                                    currentIndex++
                                    selectedOption = null
                                } else {
                                    isFinished = true
                                }
                            }
                        }
                    },
                    status = when {
                        selectedOption == null -> OptionStatus.DEFAULT
                        isCorrect -> OptionStatus.CORRECT
                        isSelected && !isCorrect -> OptionStatus.WRONG
                        else -> OptionStatus.DEFAULT
                    }
                )
            }
        }
    }
}

enum class OptionStatus { DEFAULT, CORRECT, WRONG }

@Composable
fun OptionButton(
    text: String,
    isSelected: Boolean,
    status: OptionStatus,
    onClick: () -> Unit
) {
    val borderColor = when (status) {
        OptionStatus.DEFAULT -> if (isSelected) primary else Color(0xFF2E2A5B)
        OptionStatus.CORRECT -> Color(0xFF4CAF50)
        OptionStatus.WRONG -> Color(0xFFE91E63)
    }
    
    val containerColor = when (status) {
        OptionStatus.DEFAULT -> if (isSelected) primary.copy(alpha = 0.1f) else panel
        OptionStatus.CORRECT -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        OptionStatus.WRONG -> Color(0xFFE91E63).copy(alpha = 0.1f)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TestResultScreen(score: Int, total: Int, onReturn: () -> Unit, onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "TEST COMPLETE",
                color = primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "RANK EVALUATION",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { score.toFloat() / total },
                    modifier = Modifier.size(200.dp),
                    color = primary,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    strokeWidth = 12.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score / $total",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "CORRECT",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "EXP Gained: ${score * 5}",
                color = primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, primary)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RETRY", color = primary, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onReturn,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("FINISH", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun Float.roundToInt() = kotlin.math.round(this).toInt()
