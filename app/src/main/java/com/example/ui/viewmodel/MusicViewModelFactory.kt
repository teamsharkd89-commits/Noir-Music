package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.NoirDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.MusicRepository

class MusicViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            val database = NoirDatabase.getInstance(application)
            val repository = MusicRepository(database.songDao(), database.playlistDao())
            val preferencesRepository = UserPreferencesRepository(application)
            return MusicViewModel(application, repository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
