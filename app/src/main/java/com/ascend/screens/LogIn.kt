package com.ascend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.primary
import com.ascend.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ascend.ui.theme.AscendTheme
import com.ascend.ui.theme.panel


@Composable
fun Login(navController: NavController){
    var username by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }

    val annotatedText = buildAnnotatedString {
        append("Don't have an account yet? ")

        // Make "Sign Up" clickable
        pushStringAnnotation(tag = "SIGN_UP", annotation = "sign_up")
        withStyle(style = SpanStyle(
            color = primary,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline
        )
        ) {
            append("Sign Up")
        }
        pop()
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF171131), shape = RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFF1E2C4F), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ascend_logo),
                    contentDescription = "Sword Icon",
                    modifier = Modifier.size(40.dp),
                    tint = primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false
                    )
                    .background(
                        Color(0xFF171131),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(25.dp)

            ){
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "EMAIL",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(0.dp, 0.dp, 200.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
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
                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text = "PASSSWORD",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(0.dp, 0.dp, 160.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = pw,
                    onValueChange = { pw = it },
                    label = { Text("Password") },
                    modifier = Modifier.width(250.dp),
                    singleLine = true,  // makes it compact
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

                Button(
                    onClick = { /* Handle Navigation */ },
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
                ){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Log In",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                ClickableText(
                    text = annotatedText,
                    onClick = { navController.navigate("signup")
                    },
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        color = Color.White
                    )
                )
            }

        }
    }
}
@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    AscendTheme {
        val navController = rememberNavController()
        Login(navController = navController)
    }
}
