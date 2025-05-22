package com.example.biodidwallet.ui.identity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.biodidwallet.data.repository.DIDInfo
import com.example.biodidwallet.data.repository.DIDRepository
import kotlinx.coroutines.launch

class DIDViewModel(application: Application) : AndroidViewModel(application) {
    
    private val didRepository = DIDRepository(application)
    
    private val _didList = MutableLiveData<List<DIDInfo>>()
    val didList: LiveData<List<DIDInfo>> = _didList
    
    private val _isCreating = MutableLiveData<Boolean>()
    val isCreating: LiveData<Boolean> = _isCreating
    
    fun loadDIDs() {
        viewModelScope.launch {
            val dids = didRepository.getAllDIDs()
            _didList.value = dids
        }
    }
    
    fun createDID(name: String) {
        _isCreating.value = true
        viewModelScope.launch {
            try {
                val newDID = didRepository.createNewDID(name)
                // 重新加载DID列表
                loadDIDs()
            } catch (e: Exception) {
                // 处理错误
            } finally {
                _isCreating.value = false
            }
        }
    }
    
    fun deleteDID(didId: String) {
        viewModelScope.launch {
            val success = didRepository.deleteDID(didId)
            // 重新加载DID列表
            loadDIDs()
        }
    }
} 