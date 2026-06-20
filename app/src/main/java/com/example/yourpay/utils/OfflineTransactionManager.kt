package com.example.yourpay.utils

import com.example.yourpay.data.dao.WalletDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class OfflineTransactionManager(
    private val walletDao: WalletDao
) {
    /**
     * Attempts to generate a secure offline payment token.
     * Deducts balance from the local wallet if successful.
     * Returns the encrypted token, or throws an Exception on failure.
     */
    suspend fun processOfflinePayment(
        senderId: String,
        receiverId: String,
        amount: Double
    ): String = withContext(Dispatchers.IO) {
        // Double check balance right before transaction lock
        val isDeducted = walletDao.deductBalance(amount)
        if (isDeducted == 0) {
            throw IllegalStateException("Insufficient Offline Balance")
        }

        val txId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        
        // Construct the raw payload
        val rawPayload = "TXNOP|$txId|$senderId|$receiverId|$amount|$timestamp"

        // Let CryptoEngine encrypt it (Using CryptoEngine's existing AES encryption,
        // although CryptoEngine uses slightly different format, we can adapt it or just use CryptoEngine.generateOfflineToken)
        // Since CryptoEngine.generateOfflineToken does not take txId, we'll use its logic or just pass amount.
        // Actually, the existing CryptoEngine generates: TXN|sender|receiver|amount|ts
        // Let's use the existing CryptoEngine method to stay compatible with success validation.
        val token = CryptoEngine.generateOfflineToken(senderId, receiverId, amount.toString())
        
        if (token == "ERROR_GENERATING_TOKEN") {
             // Rollback deduct due to cryptographic failure
             walletDao.addBalance(amount)
             throw IllegalStateException("Failed to sign securely.")
        }

        return@withContext token
    }
}
