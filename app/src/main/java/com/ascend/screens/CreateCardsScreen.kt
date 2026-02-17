package com.ascend.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.primary
import com.ascend.viewModel.FlashcardItemInternal
import com.ascend.viewModel.FlashcardViewModel

data class Flashcard(var term: String = "", var definition: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardsScreen(
    navController: NavController,
    viewModel: FlashcardViewModel,
    title: String,
    description: String,
    initialCount: Int
) {
    val flashcards = remember { 
        mutableStateListOf<Flashcard>().apply {
            repeat(initialCount) { add(Flashcard()) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { flashcards.add(Flashcard()) },
                containerColor = primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(flashcards) { index, card ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171131)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Card ${index + 1}", color = primary, style = MaterialTheme.typography.labelLarge)
                            IconButton(onClick = { flashcards.removeAt(index) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Card",
                                    tint = Color.Gray.copy(alpha = 0.6f)
                                )
                            }
                        }
                        
                        OutlinedTextField(
                            value = card.term,
                            onValueChange = { 
                                flashcards[index] = flashcards[index].copy(term = it)
                            },
                            label = { Text("Term") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = card.definition,
                            onValueChange = { 
                                flashcards[index] = flashcards[index].copy(definition = it)
                            },
                            label = { Text("Definition") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            }
            
            item {
                Button(
                    onClick = { 
                        val items = flashcards.map { FlashcardItemInternal(it.term, it.definition) }
                        val descToSave = if (description == "none") "" else description
                        viewModel.saveSetWithCards(title, descToSave, items) {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(primary),
                    enabled = flashcards.isNotEmpty() && flashcards.all { it.term.isNotBlank() && it.definition.isNotBlank() }
                ) {
                    Text("Finish Set")
                }
            }
        }
    }
}
