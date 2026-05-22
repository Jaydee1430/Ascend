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
import com.ascend.data.FlashcardItem
import com.ascend.ui.theme.bgColor
import com.ascend.ui.theme.primary
import com.ascend.viewModel.FlashcardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSetScreen(
    navController: NavController,
    viewModel: FlashcardViewModel,
    setId: Int,
    title: String
) {
    val existingCards by viewModel.getCardsForSet(setId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    val editableCards = remember { mutableStateListOf<FlashcardItem>() }
    
    LaunchedEffect(existingCards) {
        if (editableCards.isEmpty() && existingCards.isNotEmpty()) {
            editableCards.addAll(existingCards)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit: $title", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                existingCards.forEach { viewModel.deleteFlashcard(it) }
                                editableCards.filter { it.term.isNotBlank() && it.definition.isNotBlank() }.forEach {
                                    viewModel.addFlashcard(it.copy(id = 0))
                                }
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Text("Save", color = primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editableCards.add(FlashcardItem(setId = setId, term = "", definition = "")) },
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
            itemsIndexed(editableCards) { index, card ->
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
                            IconButton(onClick = { editableCards.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray.copy(alpha = 0.6f))
                            }
                        }
                        
                        OutlinedTextField(
                            value = card.term,
                            onValueChange = { editableCards[index] = editableCards[index].copy(term = it) },
                            label = { Text("Term") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = card.definition,
                            onValueChange = { editableCards[index] = editableCards[index].copy(definition = it) },
                            label = { Text("Definition") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                }
            }
        }
    }
}
