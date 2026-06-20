package com.example.yourpay.utils

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoEngine {
    
    // For demonstration purposes, we are using a static symmetric key.
    // In a real-world scenario, this should be an asymmetric key generated via Android Keystore
    // and dynamically exchanged with the receiver/backend.
    private const val DEMO_SECRET_KEY = "YourPaySecretKey12345678" // Must be 16, 24, or 32 bytes

    private fun getSecretKey(): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(DEMO_SECRET_KEY.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes.copyOf(16), "AES")
    }

    fun generateOfflineToken(senderId: String, receiverId: String, amount: String): String {
        val timestamp = System.currentTimeMillis()
        val payload = "TXN|$senderId|$receiverId|$amount|$timestamp"
        
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val encryptedBytes = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            "ERROR_GENERATING_TOKEN"
        }
    }

    fun decryptOfflineToken(token: String): Map<String, String>? {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey())
            val decodedBytes = Base64.decode(token, Base64.NO_WRAP)
            val decryptedString = String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
            
            val parts = decryptedString.split("|")
            if (parts.size >= 5 && parts[0] == "TXN") {
                mapOf(
                    "sender" to parts[1],
                    "receiver" to parts[2],
                    "amount" to parts[3],
                    "timestamp" to parts[4]
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
