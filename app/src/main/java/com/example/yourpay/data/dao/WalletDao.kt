package com.example.yourpay.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.yourpay.data.entity.WalletInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet WHERE id = 1")
    fun getWalletFlow(): Flow<WalletInfo?>

    @Query("SELECT * FROM wallet WHERE id = 1")
    suspend fun getWalletNow(): WalletInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setWallet(wallet: WalletInfo)

    @Query("UPDATE wallet SET balance = balance + :amount WHERE id = 1")
    suspend fun addBalance(amount: Double)

    @Query("UPDATE wallet SET balance = balance - :amount WHERE id = 1 AND balance >= :amount")
    suspend fun deductBalance(amount: Double): Int // returns number of rows updated (0 if insufficient)
}
