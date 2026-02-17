package com.ascend.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

class UserDataStore(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        val USERNAME_KEY = stringPreferencesKey("username")
        val RANK_KEY = stringPreferencesKey("rank")
    }

    val username: Flow<String?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[USERNAME_KEY]
        }
    
    val rank: Flow<String?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[RANK_KEY]
        }

    suspend fun saveUsername(name: String) {
        appContext.dataStore.edit {
            it[USERNAME_KEY] = name
        }
    }
    
    suspend fun saveRank(rank: String) {
        appContext.dataStore.edit {
            it[RANK_KEY] = rank
        }
    }
}
