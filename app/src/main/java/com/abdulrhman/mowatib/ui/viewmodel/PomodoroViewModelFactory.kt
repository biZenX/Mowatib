package com.abdulrhman.mowatib.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abdulrhman.mowatib.data.preferences.UserPreferencesRepository
import com.abdulrhman.mowatib.data.repository.ShieldRepository

class PomodoroViewModelFactory(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val shieldRepository: ShieldRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
            return PomodoroViewModel(context, userPreferencesRepository, shieldRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
