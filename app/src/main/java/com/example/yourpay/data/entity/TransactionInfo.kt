package com.example.yourpay.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val upiId: String,
    val amount: String,
    val timestamp: Long,
    val status: String // "SUCCESS", "OFFLINE", "REFUNDED"
)
