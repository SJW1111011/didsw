package com.example.biodidwallet.ui.credentials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.biodidwallet.data.local.VerifiableCredential
import com.example.biodidwallet.data.repository.CredentialRepository
import kotlinx.coroutines.launch

class CredentialViewModel(application: Application) : AndroidViewModel(application) {
    
    private val credentialRepository = CredentialRepository(application)
    
    private val _credentials = MutableLiveData<List<VerifiableCredential>>()
    val credentials: LiveData<List<VerifiableCredential>> = _credentials
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadCredentialsForDID(didId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val credentialList = credentialRepository.getCredentialsForDID(didId)
                _credentials.value = credentialList
            } catch (e: Exception) {
                // 处理错误
                _credentials.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createDemoCredential(ownerDid: String) {
        viewModelScope.launch {
            try {
                credentialRepository.createDemoCredential(ownerDid)
                // 重新加载凭证列表
                loadCredentialsForDID(ownerDid)
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
    
    fun verifyCredential(credential: VerifiableCredential) {
        viewModelScope.launch {
            val isValid = credentialRepository.verifyCredential(credential)
            // TODO: 处理验证结果
        }
    }
    
    fun deleteCredential(id: String, ownerDid: String) {
        viewModelScope.launch {
            credentialRepository.deleteCredential(id)
            // 重新加载凭证列表
            loadCredentialsForDID(ownerDid)
        }
    }
} 