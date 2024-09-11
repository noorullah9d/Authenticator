package com.example.my.project.authenticator.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardSelectionViewModel @Inject constructor() : ViewModel() {


    private val _selectedCardIndex = MutableLiveData<Int?>()
    val selectedCardIndex: LiveData<Int?> = _selectedCardIndex

    fun toggleCardSelection(index: Int) {
        if (_selectedCardIndex.value == index) {
            _selectedCardIndex.value = null
        } else {
            _selectedCardIndex.value = index
        }
    }

    fun getSelectedCardIndex(): Int? {
        return _selectedCardIndex.value
    }


}