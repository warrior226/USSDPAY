package com.example.mobilemhealthpay.data.remote.dto

import androidx.annotation.Keep
import com.example.mobilemhealthpay.data.entity.TransactionInfoEntity
import com.squareup.moshi.Json

@Keep
class TransactionInfoDto(
    @field:Json(name = "user_id")
    val user_id:Int,
    @field:Json(name = "montant")
    val montant:Int,
    @field:Json(name="transaction_id")
    var transaction_id:String,
    @field:Json(name="numero")
    val numero:String,
    @field:Json(name = "operateur")
    val operateur:String,
    @field:Json(name="comment")
    val comment:String?,
    @field:Json(name = "date_creation")
    val date_creation:String
) {
  fun toModel()= TransactionInfoEntity(
      user_id = user_id,
      montant = montant,
      transaction_id = transaction_id,
      numero = numero,
      operateur = operateur,
      comment = comment,
      date_creation = date_creation
  )
}
