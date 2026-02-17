package com.ascend.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Insert
    suspend fun insertSet(set: FlashcardSet): Long

    @Insert
    suspend fun insertFlashcards(cards: List<FlashcardItem>)

    @Transaction
    @Query("SELECT * FROM flashcard_sets ORDER BY createdAt DESC")
    fun getAllSets(): Flow<List<FlashcardSet>>

    @Query("SELECT * FROM flashcards WHERE setId = :setId")
    fun getCardsForSet(setId: Int): Flow<List<FlashcardItem>>

    @Update
    suspend fun updateFlashcard(card: FlashcardItem)

    @Delete
    suspend fun deleteSet(set: FlashcardSet)
}
