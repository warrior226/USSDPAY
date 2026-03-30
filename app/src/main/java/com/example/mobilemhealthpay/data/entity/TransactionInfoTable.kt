package com.example.mobilemhealthpay.data.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_info",
    indices = [Index(value = ["transaction_id"], unique = true)]
)
@Keep
data class TransactionInfoTable(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "user_id")
    var user_id:Int=0,
    @ColumnInfo(name = "montant")
    var montant:Int=0,
    @ColumnInfo(name = "transaction_id")
    var transactionId:String="",
    @ColumnInfo(name = "numero")
    var numero:String="",
    @ColumnInfo("operateur")
    var operateur:String="",
    @ColumnInfo("comment")
    var comment:String?=null,
    @ColumnInfo("date_creation")
    var date_creation:String="",
    @ColumnInfo("status")
    var status:Int=0,
    @ColumnInfo("tentative")
    val retryCount: Int= 0,
    @ColumnInfo("lastAttemptDate")
    val lastAttemptAt: Long= 0L
){
    // Secondary constructor without parameters
    constructor() : this(null, 0, 0, "", "", "", null, "",  0)
}
