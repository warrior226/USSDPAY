package com.example.mobilemhealthpay.data.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mobilemhealthpay.data.entity.PaiementEntity
import com.example.mobilemhealthpay.data.entity.TransactionInfoEntity
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable

@Dao
interface TransactionInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactionInfoTable: TransactionInfoTable)

    @Update
    fun update(transactionInfoTable: TransactionInfoTable)

    @Delete
    fun delete(transactionInfoTable: TransactionInfoTable)

    @Query("select comment from transaction_info where transaction_id=:transactionId")
    fun getComment(transactionId:String):LiveData<String>

    @Query("update  transaction_info SET comment=:newComment  where transaction_id=:transactionId")
    fun updateComment(transactionId:String,newComment:String):Int

    @Query("SELECT * FROM transaction_info where status=0")
    fun getTransactionEnCoursFromDb(): LiveData<List<TransactionInfoEntity>>

    @Query("SELECT * FROM transaction_info where status=1")
    fun getTransactionEffectueFromDb(): LiveData<List<TransactionInfoEntity>>

    @Query("SELECT * FROM transaction_info where status=2")
    fun getTransactionEchoueFromDb(): LiveData<List<TransactionInfoEntity>>

}