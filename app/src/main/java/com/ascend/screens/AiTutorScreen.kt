package com.ascend.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ascend.R
import com.ascend.ai.AiTutorMessage
import com.ascend.ai.AiTutorRepository
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.panel
import com.ascend.ui.theme.primary
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private enum class ChatAuthor {
    User,
    Assistant
}

private data class ChatUiMessage(
    val id: Long,
    val text: String,
    val author: ChatAuthor,
    val isError: Boolean = false
)

@Composable
fun AiTutorScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { AiTutorRepository() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var input by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var nextMessageId by remember { mutableStateOf(0L) }
    var messages by remember { mutableStateOf<List<ChatUiMessage>>(emptyList()) }

    fun nextId(): Long {
        nextMessageId += 1
        return nextMessageId
    }

    fun sendMessage() {
        val question = input.trim()

        if (isLoading) return

        if (question.isBlank()) {
            inputError = "Type a study question first."
            return
        }

        if (!context.hasNetworkConnection()) {
            inputError = null
            messages = messages + ChatUiMessage(
                id = nextId(),
                text = "No internet connection. Please reconnect and try again.",
                author = ChatAuthor.Assistant,
                isError = true
            )
            return
        }

        input = ""
        inputError = null
        val userMessage = ChatUiMessage(
            id = nextId(),
            text = question,
            author = ChatAuthor.User
        )
        messages = messages + userMessage

        val requestMessages = messages
            .filterNot { it.isError }
            .takeLast(12)
            .map { message ->
                AiTutorMessage(
                    role = if (message.author == ChatAuthor.User) "user" else "model",
                    text = message.text
                )
            }

        isLoading = true
        coroutineScope.launch {
            val result = repository.askStudyQuestion(requestMessages)
            messages = messages + result.fold(
                onSuccess = { reply ->
                    ChatUiMessage(
                        id = nextId(),
                        text = reply,
                        author = ChatAuthor.Assistant
                    )
                },
                onFailure = { error ->
                    ChatUiMessage(
                        id = nextId(),
                        text = error.toFriendlyMessage(),
                        author = ChatAuthor.Assistant,
                        isError = true
                    )
                }
            )
            isLoading = false
        }
    }

    LaunchedEffect(messages.size, isLoading) {
        val lastIndex = messages.lastIndex + if (isLoading) 1 else 0
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

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
            alpha = 0.35f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            AiTutorHeader(onBack = { navController.popBackStack() })

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && !isLoading) {
                    EmptyChatState(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 12.dp,
                            bottom = 18.dp
                        )
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatMessageBubble(message = message)
                        }

                        if (isLoading) {
                            item(key = "loading") {
                                LoadingBubble()
                            }
                        }
                    }
                }
            }

            ChatInputBar(
                input = input,
                inputError = inputError,
                isLoading = isLoading,
                onInputChange = {
                    input = it
                    if (inputError != null) {
                        inputError = null
                    }
                },
                onSend = { sendMessage() }
            )
        }
    }
}

@Composable
private fun AiTutorHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = "Beru",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Shadow study guide",
                color = primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun EmptyChatState(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = panel.copy(alpha = 0.75f),
        tonalElevation = 2.dp
    ) {
        Text(
            text = "Ask a study question to begin.",
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChatMessageBubble(message: ChatUiMessage) {
    val isUser = message.author == ChatAuthor.User
    val bubbleColor = when {
        message.isError -> Color(0xFF4A1E2A)
        isUser -> primary
        else -> panel.copy(alpha = 0.9f)
    }
    val borderColor = when {
        message.isError -> Color(0xFFFF7A9A).copy(alpha = 0.45f)
        isUser -> primary.copy(alpha = 0.55f)
        else -> Color(0xFF2E2A5B)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .widthIn(max = maxWidth * 0.84f)
                .align(if (isUser) Alignment.CenterEnd else Alignment.CenterStart)
                .border(1.dp, borderColor, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = bubbleColor
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun LoadingBubble() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = panel.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2A5B))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Beru is thinking...",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    input: String,
    inputError: String?,
    isLoading: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor.copy(alpha = 0.96f),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (inputError != null) {
                Text(
                    text = inputError,
                    color = Color(0xFFFF9BB2),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask about a topic...", color = Color.Gray) },
                    singleLine = false,
                    minLines = 1,
                    maxLines = 4,
                    enabled = !isLoading,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White.copy(alpha = 0.45f),
                        cursorColor = primary,
                        focusedBorderColor = primary,
                        unfocusedBorderColor = Color(0xFF2E2A5B),
                        disabledBorderColor = Color(0xFF2E2A5B).copy(alpha = 0.5f),
                        focusedContainerColor = panel.copy(alpha = 0.9f),
                        unfocusedContainerColor = panel.copy(alpha = 0.9f),
                        disabledContainerColor = panel.copy(alpha = 0.55f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                IconButton(
                    onClick = onSend,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isLoading) primary.copy(alpha = 0.35f) else primary)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private fun Context.hasNetworkConnection(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        @Suppress("DEPRECATION")
        connectivityManager.activeNetworkInfo?.isConnected == true
    }
}

private fun Throwable.toFriendlyMessage(): String =
    when (this) {
        is IllegalStateException -> message ?: "Beru is not configured yet."
        is UnknownHostException -> "I couldn't reach the AI service. Check your internet connection and try again."
        is SocketTimeoutException -> "The AI service took too long to reply. Please try again."
        else -> "Something went wrong while asking Beru. Please try again."
    }
