package com.ascend.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

class UserDataStore(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        val USERNAME_KEY = stringPreferencesKey("username")
        val RANK_KEY = stringPreferencesKey("rank")
        val EXP_KEY = intPreferencesKey("exp")
        val SETUP_COMPLETE_KEY = booleanPreferencesKey("setup_complete")
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

        val RANK_REQUIREMENTS = listOf(
            "E-RANK" to 0,
            "D-RANK" to 1000,
            "C-RANK" to 3000,
            "B-RANK" to 7000,
            "A-RANK" to 15000,
            "S-RANK" to 30000,
            "NATIONAL LEVEL" to 60000
        )
    }

    val username: Flow<String?>
        get() = appContext.dataStore.data.map { it[USERNAME_KEY] }

    val setupComplete: Flow<Boolean>
        get() = appContext.dataStore.data.map { it[SETUP_COMPLETE_KEY] ?: false }
    
    val rank: Flow<String>
        get() = appContext.dataStore.data.map { it[RANK_KEY] ?: "E-RANK" }

    val exp: Flow<Int>
        get() = appContext.dataStore.data.map { it[EXP_KEY] ?: 0 }

    val notificationsEnabled: Flow<Boolean>
        get() = appContext.dataStore.data.map { it[NOTIFICATIONS_ENABLED_KEY] ?: true }

    val darkMode: Flow<Boolean>
        get() = appContext.dataStore.data.map { it[DARK_MODE_KEY] ?: true }

    suspend fun saveUsername(name: String) {
        appContext.dataStore.edit { it[USERNAME_KEY] = name }
    }
    
    suspend fun saveRank(rank: String) {
        appContext.dataStore.edit { it[RANK_KEY] = rank }
    }

    suspend fun completeSetup() {
        appContext.dataStore.edit { it[SETUP_COMPLETE_KEY] = true }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        appContext.dataStore.edit { it[NOTIFICATIONS_ENABLED_KEY] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        appContext.dataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    suspend fun clearData() {
        appContext.dataStore.edit { it.clear() }
    }

    suspend fun addExp(amount: Int) {
        appContext.dataStore.edit { preferences ->
            val currentExp = (preferences[EXP_KEY] ?: 0) + amount
            preferences[EXP_KEY] = currentExp
            
            val currentRank = preferences[RANK_KEY] ?: "E-RANK"
            val nextRankInfo = getNextRank(currentExp)
            if (nextRankInfo.first != currentRank) {
                preferences[RANK_KEY] = nextRankInfo.first
            }
        }
    }

    fun getNextRank(currentTotalExp: Int): Pair<String, Int> {
        var currentRank = "E-RANK"
        var nextRankReq = 1000
        
        for (i in RANK_REQUIREMENTS.indices) {
            if (currentTotalExp >= RANK_REQUIREMENTS[i].second) {
                currentRank = RANK_REQUIREMENTS[i].first
                nextRankReq = if (i + 1 < RANK_REQUIREMENTS.size) {
                    RANK_REQUIREMENTS[i + 1].second
                } else {
                    RANK_REQUIREMENTS[i].second
                }
            } else {
                break
            }
        }
        return currentRank to nextRankReq
    }
}
