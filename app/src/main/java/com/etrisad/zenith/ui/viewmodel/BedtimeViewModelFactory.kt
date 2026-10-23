package com.etrisad.zenith.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.etrisad.zenith.data.preferences.UserPreferencesRepository
import com.etrisad.zenith.data.repository.ShieldRepository

class BedtimeViewModelFactory(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val shieldRepository: ShieldRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BedtimeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BedtimeViewModel(context, userPreferencesRepository, shieldRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
