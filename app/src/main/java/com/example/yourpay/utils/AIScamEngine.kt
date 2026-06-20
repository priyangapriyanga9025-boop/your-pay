package com.example.yourpay.utils

import com.example.yourpay.data.entity.TransactionInfo

object AIScamEngine {

    enum class RiskLevel {
        LOW, MODERATE, HIGH
    }

    data class ScamResult(
        val isSuspicious: Boolean,
        val riskLevel: RiskLevel,
        val reason: String
    )

    fun evaluateTransaction(
        targetUpi: String,
        amountText: String,
        history: List<TransactionInfo>
    ): ScamResult {
        val amount = amountText.toDoubleOrNull() ?: 0.0
        val targetLower = targetUpi.lowercase()

        // 1. Basic Heuristics
        if (targetLower.contains("scam") || targetLower.contains("fraud") || targetLower.contains("unknown")) {
            return ScamResult(true, RiskLevel.HIGH, "The recipient ID mimics a known fraudulent pattern.")
        }
        
        if (amount > 50000.0) {
            return ScamResult(true, RiskLevel.HIGH, "Transactions above ₹50,000 to untrusted IDs are extremely risky offline.")
        }

        // 2. Velocity Check (Rapid transactions in a short time)
        val now = System.currentTimeMillis()
        val recentTransactions = history.filter { (now - it.timestamp) < 5 * 60 * 1000 } // Last 5 minutes
        if (recentTransactions.size >= 3) {
            return ScamResult(true, RiskLevel.HIGH, "Unusual rapid transaction frequency detected. 3+ transfers within 5 minutes.")
        }

        // 3. Known Receiver Check
        val previousTransactionsWithTarget = history.filter { it.upiId.lowercase() == targetLower && it.status == "SUCCESS" }
        if (previousTransactionsWithTarget.isEmpty() && amount > 10000.0) {
            return ScamResult(true, RiskLevel.MODERATE, "You have never transacted with this ID before, and the amount (₹$amount) is large. Please verify the recipient.")
        }

        // 4. Anomaly Amount Check (Is this 3x higher than their average transaction?)
        if (history.isNotEmpty()) {
            val validAmounts = history.mapNotNull { it.amount.toDoubleOrNull() }
            if (validAmounts.isNotEmpty()) {
                val average = validAmounts.sum() / validAmounts.size
                if (average > 0 && amount > (average * 4) && previousTransactionsWithTarget.isEmpty()) {
                    return ScamResult(true, RiskLevel.MODERATE, "This amount is significantly higher than your typical outgoing transfers to a new receiver.")
                }
            }
        }

        return ScamResult(false, RiskLevel.LOW, "Safe")
    }
}
