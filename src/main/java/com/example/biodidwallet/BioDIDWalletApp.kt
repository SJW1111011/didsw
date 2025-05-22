package com.example.biodidwallet

import android.app.Application
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators

class BioDIDWalletApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 检查生物识别可用性
        checkBiometricAvailability()
    }
    
    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                // 生物识别可用
                println("生物识别可用")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                // 设备不支持生物识别
                println("此设备不支持生物识别")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                // 生物识别暂时不可用
                println("生物识别暂时不可用")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                // 用户没有注册生物特征
                println("用户未注册生物特征")
            else -> 
                // 生物识别不可用
                println("生物识别不可用")
        }
    }
} 