package com.abdulrhman.mowatib.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abdulrhman.mowatib.data.repository.ShieldRepository
import com.abdulrhman.mowatib.data.preferences.UserPreferencesRepository

class HomeViewModelFactory(
    private val context: Context,
    private val shieldRepository: ShieldRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(context, shieldRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
