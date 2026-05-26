package com.ascend.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ascend.data.FlashcardDao
import com.ascend.data.FlashcardDatabase
import com.ascend.data.FlashcardItem
import com.ascend.data.FlashcardSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FlashcardViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: FlashcardDao = FlashcardDatabase.getDatabase(application).flashcardDao()
    val allSets: Flow<List<FlashcardSet>> = dao.getAllSets()

    fun getCardsForSet(setId: Int): Flow<List<FlashcardItem>> {
        return dao.getCardsForSet(setId)
    }

    fun saveSetWithCards(title: String, description: String, cards: List<FlashcardItemInternal>, onComplete: () -> Unit) {
        viewModelScope.launch {
            val setId = dao.insertSet(FlashcardSet(title = title, description = description)).toInt()
            val flashcardItems = cards.map { 
                FlashcardItem(setId = setId, term = it.term, definition = it.definition) 
            }
            dao.insertFlashcards(flashcardItems)
            onComplete()
        }
    }

    fun updateFlashcard(card: FlashcardItem) {
        viewModelScope.launch {
            dao.updateFlashcard(card)
        }
    }

    fun addFlashcard(card: FlashcardItem) {
        viewModelScope.launch {
            dao.insertFlashcard(card)
        }
    }

    fun deleteFlashcard(card: FlashcardItem) {
        viewModelScope.launch {
            dao.deleteFlashcard(card)
        }
    }

    fun deleteSet(setId: Int, title: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            dao.deleteSet(FlashcardSet(id = setId, title = title, description = ""))
            onComplete()
        }
    }
}

data class FlashcardItemInternal(val term: String, val definition: String)
