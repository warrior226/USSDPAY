package com.example.mobilemhealthpay.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName="beneficiaires")
data class BeneficiaireEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "nom_prenom")
    val nomPrenom: String,
    @ColumnInfo(name = "telephone")
    val telephone: String,
    @ColumnInfo(name = "code")
    val code: String,
    @ColumnInfo(name = "date_creation")
    val createdAt: Long = System.currentTimeMillis()
)