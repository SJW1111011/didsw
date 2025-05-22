// DIDAuthActivity.kt
import androidx.biometric.BiometricPrompt
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.security.Signature
import java.util.concurrent.Executors

class DIDAuthActivity : AppCompatActivity() {
    private lateinit var biometricPrompt: BiometricPrompt
    private val cryptoObject: BiometricPrompt.CryptoObject?
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化生物识别提示
        val executor = Executors.newSingleThreadExecutor()
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // 使用解锁的私钥签名
                val signature = result.cryptoObject?.signature
                val didMessage = "VC:${System.currentTimeMillis()}".toByteArray()
                signature?.update(didMessage)
                val sigBytes = signature?.sign() // 此签名可用于生成可验证凭证
                showToast("DID签名成功: ${sigBytes?.toHex()}")
            }
        })
        
        // 准备签名对象
        val keyAlias = "user_did_key"
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign((keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry).privateKey)
        cryptoObject = BiometricPrompt.CryptoObject(signature)
    }

    fun triggerBioAuth() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("DID身份验证")
            .setSubtitle("请验证指纹以解锁您的去中心化身份")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        
        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    private fun showToast(text: String) {
        runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
    }
}

// 字节数组转十六进制字符串
fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }

// 验证密钥是否受生物特征保护
fun isKeyBioBound(alias: String): Boolean {
    val entry = keyStore.getEntry(alias, null) as? KeyStore.PrivateKeyEntry
    return entry?.privateKey?.let { key ->
        key is AndroidKeyStorePrivateKey &&
        key.algorithm == "EC" &&
        key.getSecurityLevel() == KeyProperties.SECURITY_LEVEL_STRONGBOX
    } ?: false
}

// 在签名中包含时间戳和随机数
fun generateSecureMessage(): ByteArray {
    val timestamp = System.currentTimeMillis()
    val nonce = SecureRandom().nextLong()
    return "VC:$timestamp:$nonce".toByteArray()
}