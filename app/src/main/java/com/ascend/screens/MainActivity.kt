package com.ascend.screens

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ascend.StudyNotificationReceiver
import com.ascend.data.UserDataStore
import com.ascend.ui.theme.AscendTheme
import com.ascend.ui.theme.primary
import com.ascend.viewModel.FlashcardViewModel
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val viewModel: FlashcardViewModel by viewModels()
            val context = LocalContext.current
            val userDataStore = remember { UserDataStore(context) }
            val setupComplete by userDataStore.setupComplete.collectAsState(initial = null)

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    scheduleDailyReminder(context)
                }
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        scheduleDailyReminder(context)
                    }
                } else {
                    scheduleDailyReminder(context)
                }
            }

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

                        composable(
                            "test_screen/{setId}",
                            arguments = listOf(
                                navArgument("setId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val setId = backStackEntry.arguments?.getInt("setId") ?: 0
                            TestScreen(navController, viewModel, setId)
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

    private fun scheduleDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StudyNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
