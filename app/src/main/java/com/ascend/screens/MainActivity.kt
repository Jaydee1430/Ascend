package com.ascend.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ascend.ui.theme.AscendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            AscendTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "startscreen") {
                    composable("startscreen") {
                        AscendSplashScreen(navController)
                    }

                    composable("home") {
                        Home()
                    }
                }
            }
        }
    }
}
