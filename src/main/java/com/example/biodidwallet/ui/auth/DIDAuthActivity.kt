package com.example.biodidwallet.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import java.security.KeyStore
import java.security.Signature
import java.security.SecureRandom
import java.util.concurrent.Executors
import kotlin.math.absoluteValue

class DIDAuthActivity : AppCompatActivity() {
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var cryptoObject: BiometricPrompt.CryptoObject
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    
    companion object {
        const val EXTRA_KEY_ALIAS = "key_alias"
        const val EXTRA_AUTH_PURPOSE = "auth_purpose"
        const val EXTRA_CALLBACK_DATA = "callback_data"
        const val PURPOSE_SIGN_CREDENTIAL = 1
        const val PURPOSE_AUTHENTICATE = 2
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_did_auth)
        
        // 获取传入的密钥别名
        val keyAlias = intent.getStringExtra(EXTRA_KEY_ALIAS) ?: run {
            showToast("密钥别名未提供")
            finish()
            return
        }
        
        val authPurpose = intent.getIntExtra(EXTRA_AUTH_PURPOSE, PURPOSE_AUTHENTICATE)
        
        // 初始化生物识别提示
        val executor = Executors.newSingleThreadExecutor()
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // 使用解锁的私钥签名
                val signature = result.cryptoObject?.signature
                val didMessage = generateSecureMessage()
                signature?.update(didMessage)
                val sigBytes = signature?.sign() // 此签名可用于生成可验证凭证
                
                when (authPurpose) {
                    PURPOSE_SIGN_CREDENTIAL -> {
                        // 处理凭证签名
                        val callbackData = intent.getStringExtra(EXTRA_CALLBACK_DATA)
                        handleCredentialSigning(sigBytes, callbackData)
                    }
                    PURPOSE_AUTHENTICATE -> {
                        // 处理普通认证
                        showToast("身份验证成功")
                        setResult(RESULT_OK)
                    }
                }
                
                finish()
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                showToast("认证错误: $errString")
                setResult(RESULT_CANCELED)
                finish()
            }
            
            override fun onAuthenticationFailed() {
                showToast("认证失败")
            }
        })
        
        // 准备签名对象
        try {
            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initSign((keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry).privateKey)
            cryptoObject = BiometricPrompt.CryptoObject(signature)
            
            // 自动启动验证
            triggerBioAuth()
        } catch (e: Exception) {
            showToast("签名初始化错误: ${e.message}")
            finish()
        }
    }

    private fun triggerBioAuth() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("DID身份验证")
            .setSubtitle("请验证指纹以解锁您的去中心化身份")
            .setNegativeButtonText("取消")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        
        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    private fun handleCredentialSigning(signatureBytes: ByteArray?, callbackData: String?) {
        // 处理签名数据，可以发送到服务器或本地存储
        val signatureHex = signatureBytes?.toHex() ?: "签名失败"
        showToast("凭证签名成功: $signatureHex")
        // TODO: 处理回调数据和签名结果
    }

    private fun showToast(text: String) {
        runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
    }
    
    // 字节数组转十六进制字符串
    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
    
    // 生成包含时间戳和随机数的安全消息
    private fun generateSecureMessage(): ByteArray {
        val timestamp = System.currentTimeMillis()
        val nonce = SecureRandom().nextLong()
        return "VC:$timestamp:$nonce".toByteArray()
    }
} 