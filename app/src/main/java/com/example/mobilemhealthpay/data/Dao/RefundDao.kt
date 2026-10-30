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

    @Query("SELECT * FROM refund_info WHERE status = 0 AND is_refund = :isRefund")
    fun getPendingRefunds(isRefund: Int): LiveData<List<RefundInfoTable>>

    @Query("SELECT * FROM refund_info WHERE status = 1 AND is_refund = :isRefund")
    fun getSuccessfulRefunds(isRefund: Int): LiveData<List<RefundInfoTable>>

    @Query("SELECT * FROM refund_info WHERE status = 2 AND is_refund = :isRefund")
    fun getFailedRefunds(isRefund: Int): LiveData<List<RefundInfoTable>>

    @Query("SELECT * FROM refund_info WHERE refund_id = :refundId")
    suspend fun getRefundById(refundId: String): RefundInfoTable?

    @Query("SELECT * FROM refund_info WHERE status = :status AND is_refund = :isRefund AND strftime('%d', created_at) = :day AND strftime('%m', created_at) = :month AND strftime('%Y', created_at) = :year")
    fun getRefundsByDate(status: Int, isRefund: Int, day: String, month: String, year: String): LiveData<List<RefundInfoTable>>

    @Query("SELECT * FROM refund_info WHERE status = :status AND is_refund = :isRefund AND strftime('%d', created_at) = :day")
    fun getRefundsByDay(status: Int, isRefund: Int, day: String): LiveData<List<RefundInfoTable>>

    @Query("SELECT * FROM refund_info WHERE status = :status AND is_refund = :isRefund AND strftime('%m', created_at) = :month")
    fun getRefundsByMonth(status: Int, isRefund: Int, month: String): LiveData<List<RefundInfoTable>>

    @Query("SELECT * FROM refund_info WHERE status = :status AND is_refund = :isRefund AND strftime('%Y', created_at) = :year")
    fun getRefundsByYear(status: Int, isRefund: Int, year: String): LiveData<List<RefundInfoTable>>
}
