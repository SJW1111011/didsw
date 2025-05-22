package com.example.biodidwallet.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.biodidwallet.data.repository.DIDInfo
import com.example.biodidwallet.data.repository.DIDRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val didRepository = DIDRepository(application)
    
    private val _selectedDID = MutableLiveData<DIDInfo?>()
    val selectedDID: LiveData<DIDInfo?> = _selectedDID
    
    init {
        // 初始化时加载选中的DID
        loadSelectedDID()
    }
    
    private fun loadSelectedDID() {
        viewModelScope.launch {
            val selected = didRepository.getSelectedDID()
            _selectedDID.value = selected
        }
    }
    
    fun selectDID(didId: String) {
        viewModelScope.launch {
            didRepository.selectDID(didId)
            loadSelectedDID()
        }
    }
    
    fun getActiveDIDKeyAlias(): String? {
        return selectedDID.value?.keyAlias
    }
} 