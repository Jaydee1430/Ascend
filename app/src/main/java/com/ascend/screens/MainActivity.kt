package com.ascend.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ascend.data.UserDataStore
import com.ascend.ui.theme.AscendTheme
import com.ascend.ui.theme.primary
import com.ascend.viewModel.FlashcardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val viewModel: FlashcardViewModel by viewModels()
            val context = LocalContext.current
            val userDataStore = remember { UserDataStore(context) }
            val setupComplete by userDataStore.setupComplete.collectAsState(initial = null)

            AscendTheme {
                if (setupComplete != null) {
                    val navController = rememberNavController()
                    
                    val startDestination = if (setupComplete == true) "home" else "startscreen"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("startscreen") {
                            AscendSplashScreen(navController)
                        }
                        
                        composable("setup_profile") {
                            SetupProfileScreen(navController)
                        }

                        composable("home") {
                            Home(navController, viewModel)
                        }

                        composable(
                            "create_cards/{title}/{description}/{count}",
                            arguments = listOf(
                                navArgument("title") { type = NavType.StringType },
                                navArgument("description") { type = NavType.StringType },
                                navArgument("count") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val title = backStackEntry.arguments?.getString("title") ?: ""
                            val description = backStackEntry.arguments?.getString("description") ?: ""
                            val count = backStackEntry.arguments?.getInt("count") ?: 5
                            CreateCardsScreen(navController, viewModel, title, description, count)
                        }

                        composable(
                            "view_cards/{setId}/{title}",
                            arguments = listOf(
                                navArgument("setId") { type = NavType.IntType },
                                navArgument("title") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val setId = backStackEntry.arguments?.getInt("setId") ?: 0
                            val title = backStackEntry.arguments?.getString("title") ?: ""
                            ViewCardsScreen(navController, viewModel, setId, title)
                        }

                        composable(
                            "flashcard_game/{setId}",
                            arguments = listOf(
                                navArgument("setId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val setId = backStackEntry.arguments?.getInt("setId") ?: 0
                            FlashcardGameScreen(navController, viewModel, setId)
                        }

                        composable(
                            "edit_set/{setId}/{title}",
                            arguments = listOf(
                                navArgument("setId") { type = NavType.IntType },
                                navArgument("title") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val setId = backStackEntry.arguments?.getInt("setId") ?: 0
                            val title = backStackEntry.arguments?.getString("title") ?: ""
                            EditSetScreen(navController, viewModel, setId, title)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primary)
                    }
                }
            }
        }
    }
}
