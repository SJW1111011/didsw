// BioDIDManager.kt
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator

class BioDIDManager(context: Context) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    // 生成受生物特征保护的密钥
    fun generateBioBoundKey(alias: String) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

        val keySpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).apply {
            setDigests(KeyProperties.DIGEST_SHA256)
            setUserAuthenticationRequired(true)
            setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            setInvalidatedByBiometricEnrollment(true)
        }.build()

        keyGenerator.init(keySpec)
        keyGenerator.generateKey()
    }

    // 获取DID公钥（PEM格式）
    fun getDIDPublicKey(alias: String): String {
        val entry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        val publicKey = entry.certificate.publicKey
        val pem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP) +
                "\n-----END PUBLIC KEY-----"
        return pem
    }
}