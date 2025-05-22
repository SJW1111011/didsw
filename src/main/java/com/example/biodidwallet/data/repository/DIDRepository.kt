package com.example.biodidwallet.data.repository

import android.content.Context
import com.example.biodidwallet.security.BioDIDManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

class DIDRepository(private val context: Context) {
    private val bioDIDManager = BioDIDManager(context)
    private val preferences = context.getSharedPreferences("did_prefs", Context.MODE_PRIVATE)
    
    suspend fun createNewDID(name: String): DIDInfo = withContext(Dispatchers.IO) {
        val keyAlias = "did_key_${System.currentTimeMillis()}"
        bioDIDManager.generateBioBoundKey(keyAlias)
        val publicKey = bioDIDManager.getDIDPublicKey(keyAlias)
        
        // 生成DID标识符（实际应用中可能使用更复杂的算法）
        val didId = "did:bio:${publicKey.hashCode().absoluteValue}"
        
        // 保存DID信息
        preferences.edit()
            .putString("$didId:name", name)
            .putString("$didId:keyAlias", keyAlias)
            .putString("$didId:publicKey", publicKey)
            .putLong("$didId:created", System.currentTimeMillis())
            .apply()
        
        DIDInfo(didId, name, keyAlias, System.currentTimeMillis())
    }
    
    suspend fun getAllDIDs(): List<DIDInfo> = withContext(Dispatchers.IO) {
        val dids = mutableListOf<DIDInfo>()
        val allPrefs = preferences.all
        
        // 从键名模式找到所有DID
        val didPattern = Regex("(did:bio:[^:]+):name")
        for ((key, _) in allPrefs) {
            val match = didPattern.find(key)
            if (match != null) {
                val didId = match.groupValues[1]
                val name = preferences.getString("$didId:name", "") ?: ""
                val keyAlias = preferences.getString("$didId:keyAlias", "") ?: ""
                val created = preferences.getLong("$didId:created", 0)
                
                dids.add(DIDInfo(didId, name, keyAlias, created))
            }
        }
        
        dids
    }
    
    suspend fun getSelectedDID(): DIDInfo? = withContext(Dispatchers.IO) {
        val selectedDid = preferences.getString("selected_did", null) ?: return@withContext null
        
        val name = preferences.getString("$selectedDid:name", "") ?: ""
        val keyAlias = preferences.getString("$selectedDid:keyAlias", "") ?: ""
        val created = preferences.getLong("$selectedDid:created", 0)
        
        DIDInfo(selectedDid, name, keyAlias, created)
    }
    
    suspend fun selectDID(didId: String) = withContext(Dispatchers.IO) {
        preferences.edit().putString("selected_did", didId).apply()
    }
    
    suspend fun deleteDID(didId: String) = withContext(Dispatchers.IO) {
        val keyAlias = preferences.getString("$didId:keyAlias", null) ?: return@withContext false
        
        // 删除密钥库中的密钥
        bioDIDManager.deleteKey(keyAlias)
        
        // 删除首选项中的DID信息
        preferences.edit()
            .remove("$didId:name")
            .remove("$didId:keyAlias")
            .remove("$didId:publicKey")
            .remove("$didId:created")
            .apply()
        
        // 如果删除的是当前选中的DID，清除选择
        if (preferences.getString("selected_did", "") == didId) {
            preferences.edit().remove("selected_did").apply()
        }
        
        true
    }
}

data class DIDInfo(
    val id: String,
    val name: String,
    val keyAlias: String,
    val createdTimestamp: Long
) 