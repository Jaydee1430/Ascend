package com.ascend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import com.ascend.R
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.primary
import com.ascend.ui.theme.AscendTheme

@Composable
fun SignUp() {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        // Back Button in Top Left
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(
                onClick = { /* Handle Navigation */ },
                modifier = Modifier
                    .background(Color(0xFF171131), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1E2C4F), RoundedCornerShape(12.dp))
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = primary
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF171131), shape = RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFF1E2C4F), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ascend_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(40.dp),
                    tint = primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create Account",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Form card
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false
                    )
                    .background(Color(0xFF171131), shape = RoundedCornerShape(24.dp))
                    .padding(25.dp)
            ) {
                // Username
                Text(
                    text = "USERNAME",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 6.dp, end = 140.dp)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.width(250.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF171131),
                        unfocusedContainerColor = Color(0xFF171131),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Email
                Text(
                    text = "EMAIL",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 6.dp, end = 180.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.width(250.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF171131),
                        unfocusedContainerColor = Color(0xFF171131),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Password
                Text(
                    text = "PASSWORD",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 6.dp, end = 160.dp)
                )
                OutlinedTextField(
                    value = pw,
                    onValueChange = { pw = it },
                    label = { Text("Password") },
                    modifier = Modifier.width(250.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF171131),
                        unfocusedContainerColor = Color(0xFF171131),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Sign Up Button
                Button(
                    onClick = { /* Handle Sign Up */ },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp)
                        .border(1.dp, Color(0xFF1E2C4F), RoundedCornerShape(18.dp))
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(18.dp),
                            clip = false
                        ),
                    colors = ButtonDefaults.buttonColors(primary),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Sign Up",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpPreview() {
    AscendTheme {
        SignUp()
    }
}
