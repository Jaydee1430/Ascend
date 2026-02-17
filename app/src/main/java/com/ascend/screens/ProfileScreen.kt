package com.ascend.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ascend.R
import com.ascend.data.UserDataStore
import com.ascend.ui.theme.AscendTheme
import com.ascend.ui.theme.bgColor
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
                        userDataStore.saveRank("E-RANK") // Default rank
                        navController.navigate("home") {
                            popUpTo("setup_profile") { inclusive = true }
                        }
                    }
                },
                enabled = username.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .shadow(
                        elevation = if (username.isNotBlank()) 15.dp else 0.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = primary
                    ),
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
