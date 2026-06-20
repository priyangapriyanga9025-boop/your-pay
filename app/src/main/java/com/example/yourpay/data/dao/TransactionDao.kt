package com.example.yourpay.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.yourpay.data.entity.TransactionInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionInfo>>

    @Insert
    suspend fun insertTransaction(transaction: TransactionInfo): Long

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    suspend fun updateTransactionStatus(id: Long, status: String)
}
