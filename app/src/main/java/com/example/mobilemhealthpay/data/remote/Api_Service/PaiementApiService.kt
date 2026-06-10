package com.example.mobilemhealthpay.data.remote.Api_Service

import com.example.mobilemhealthpay.data.remote.dto.ApiResponse
import com.example.mobilemhealthpay.data.remote.dto.PaiementDto
import com.example.mobilemhealthpay.data.remote.dto.RefundDto
import com.example.mobilemhealthpay.data.remote.dto.TransactionResponseDto
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface PaiementApiService {
    @GET("/api/paiement/mobile/programmes")
    suspend fun getPaiements(
        @Header("Authorization") token: String,
        @Query("tolerance_minutes")tolerance_minute:Int
    ): ApiResponse<List<PaiementDto>>

    @GET("external/retrait/list")
    suspend fun getTransactionInformation(
        @Query("numberOfRequesct")numberOfRequest:Int,
        @Header("Authorization") bearer:String): TransactionResponseDto

    @FormUrlEncoded
    @POST("external/retrait/validation")
    suspend fun validateTransaction(
        @Field("transaction_id")transactionId:String,
        @Field("message")message:String,
        @Field("hash")hash:String,
        @Header("Authorization") bearer:String): TransactionResponseDto

    //New lagfo version
    @GET("/refunds/")
    suspend fun getRefunds(
        @Header("X-Secret_Key") secretKey: String,
        @Query("refund_satatus") refundStatus: String
    ): RefundDto

}
