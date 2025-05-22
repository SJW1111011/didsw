package com.example.biodidwallet.data.repository

import android.content.Context
import com.example.biodidwallet.data.local.CredentialDatabase
import com.example.biodidwallet.data.local.VerifiableCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*

class CredentialRepository(context: Context) {
    private val database = CredentialDatabase.getInstance(context)
    private val dao = database.credentialDao()
    
    suspend fun saveCredential(credential: VerifiableCredential) = withContext(Dispatchers.IO) {
        dao.insert(credential)
    }
    
    suspend fun getCredentialsForDID(didId: String): List<VerifiableCredential> = withContext(Dispatchers.IO) {
        dao.getByDID(didId)
    }
    
    suspend fun getCredentialById(id: String): VerifiableCredential? = withContext(Dispatchers.IO) {
        dao.getById(id)
    }
    
    suspend fun deleteCredential(id: String) = withContext(Dispatchers.IO) {
        dao.delete(id)
    }
    
    suspend fun getValidCredentials(): List<VerifiableCredential> = withContext(Dispatchers.IO) {
        dao.getValidCredentials()
    }
    
    suspend fun verifyCredential(credential: VerifiableCredential): Boolean = withContext(Dispatchers.IO) {
        // 这里应该实现实际的凭证验证逻辑，通常包括检查签名
        // 这是一个简化的实现
        try {
            // 1. 检查凭证是否过期
            val currentTime = System.currentTimeMillis()
            if (credential.expirationDate != null && credential.expirationDate < currentTime) {
                return@withContext false
            }
            
            // 2. 检查凭证格式
            val credentialJson = JSONObject(credential.credentialData)
            if (!credentialJson.has("type") || !credentialJson.has("issuer")) {
                return@withContext false
            }
            
            // 3. 在实际应用中，这里应该验证签名
            // 为简化起见，我们假设所有签名有效
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // 创建一个测试凭证（仅用于演示）
    suspend fun createDemoCredential(ownerDid: String): VerifiableCredential {
        val currentTime = System.currentTimeMillis()
        val expirationTime = currentTime + 365 * 24 * 60 * 60 * 1000L // 一年后过期
        
        val credential = VerifiableCredential(
            id = UUID.randomUUID().toString(),
            type = "IdentityCredential",
            issuer = "did:example:issuer123",
            issuanceDate = currentTime,
            expirationDate = expirationTime,
            ownerDid = ownerDid,
            subject = "身份证明",
            credentialData = """
                {
                    "type": "IdentityCredential",
                    "issuer": "did:example:issuer123",
                    "issuanceDate": "$currentTime",
                    "claims": {
                        "name": "张三",
                        "age": "30",
                        "id": "330102199901011234"
                    }
                }
            """.trimIndent(),
            proof = "模拟签名数据"
        )
        
        saveCredential(credential)
        return credential
    }
} 