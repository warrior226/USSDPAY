package com.example.mobilemhealthpay.data.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mobilemhealthpay.data.entity.RefundInfoTable

@Dao
interface RefundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(refund: RefundInfoTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(refunds: List<RefundInfoTable>)

    @Update
    suspend fun update(refund: RefundInfoTable)

    @Query("SELECT * FROM refund_info WHERE status = 0")
    fun getPendingRefunds(): LiveData<List<RefundInfoTable>>

    @Query("SELECT * FROM refund_info WHERE status = 1")
    fun getSuccessfulRefunds(): LiveData<List<RefundInfoTable>>

    @Query("SELECT * FROM refund_info WHERE status = 2")
    fun getFailedRefunds(): LiveData<List<RefundInfoTable>>

    @Query("SELECT * FROM refund_info WHERE refund_id = :refundId")
    suspend fun getRefundById(refundId: String): RefundInfoTable?
}
