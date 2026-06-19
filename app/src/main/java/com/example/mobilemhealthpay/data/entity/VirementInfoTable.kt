package com.example.mobilemhealthpay.data.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "virement_info")
@Keep
data class VirementInfoTable(
    @PrimaryKey
    @ColumnInfo(name = "virement_id")
    val virementId: String,
    
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    
    @ColumnInfo(name = "amount")
    val amount: Int,
    
    @ColumnInfo(name = "bank_name")
    val bankName: String,
    
    @ColumnInfo(name = "virement_status")
    val virementStatus: String,
    
    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "status")
    val status: Int = 0, // 0: Pending, 1: Success, 2: Failed

    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long = 0L
)
