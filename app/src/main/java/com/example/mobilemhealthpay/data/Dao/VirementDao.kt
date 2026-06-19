package com.example.mobilemhealthpay.data.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mobilemhealthpay.data.entity.VirementInfoTable

@Dao
interface VirementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(virement: VirementInfoTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(virements: List<VirementInfoTable>)

    @Update
    suspend fun update(virement: VirementInfoTable)

    @Query("SELECT * FROM virement_info WHERE status = 0")
    fun getPendingVirements(): LiveData<List<VirementInfoTable>>

    @Query("SELECT * FROM virement_info WHERE status = 1")
    fun getSuccessfulVirements(): LiveData<List<VirementInfoTable>>

    @Query("SELECT * FROM virement_info WHERE status = 2")
    fun getFailedVirements(): LiveData<List<VirementInfoTable>>

    @Query("SELECT * FROM virement_info WHERE virement_id = :virementId")
    suspend fun getVirementById(virementId: String): VirementInfoTable?
}
