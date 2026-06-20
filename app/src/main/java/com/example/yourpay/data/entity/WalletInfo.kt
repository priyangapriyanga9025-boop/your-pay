package com.example.yourpay.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet")
data class WalletInfo(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 0.0
)
