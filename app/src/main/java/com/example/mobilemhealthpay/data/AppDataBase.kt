package com.example.mobilemhealthpay.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mobilemhealthpay.data.Dao.BeneficiaireDao
import com.example.mobilemhealthpay.data.Dao.PaiementBeneficiaireDao
import com.example.mobilemhealthpay.data.Dao.PaiementDao
import com.example.mobilemhealthpay.data.Dao.RefundDao
import com.example.mobilemhealthpay.data.Dao.TransactionInfoDao
import com.example.mobilemhealthpay.data.entity.BeneficiaireEntity
import com.example.mobilemhealthpay.data.entity.PaiementBeneficiaireCrossRef
import com.example.mobilemhealthpay.data.entity.PaiementEntity
import com.example.mobilemhealthpay.data.entity.RefundInfoTable
import com.example.mobilemhealthpay.data.entity.TransactionInfoTable
import com.example.mobilemhealthpay.utils.Converters

@Database(
    entities = [
        PaiementEntity::class,
        BeneficiaireEntity::class,
        PaiementBeneficiaireCrossRef::class,
        TransactionInfoTable::class,
        RefundInfoTable::class
    ],
    version = 3
)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun paiementDao(): PaiementDao
    abstract fun beneficiaireDao(): BeneficiaireDao
    abstract fun paiementBeneficiaireDao(): PaiementBeneficiaireDao
    abstract fun transactionDao(): TransactionInfoDao
    abstract fun refundDao(): RefundDao

}
