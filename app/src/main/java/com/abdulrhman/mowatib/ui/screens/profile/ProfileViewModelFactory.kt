package com.abdulrhman.mowatib.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abdulrhman.mowatib.data.preferences.UserPreferencesRepository
import com.abdulrhman.mowatib.data.repository.ShieldRepository

class ProfileViewModelFactory(
    private val context: Context,
    private val shieldRepository: ShieldRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(context, shieldRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
