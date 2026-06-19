package com.example.mobilemhealthpay.data.remote.Api_Service

import com.example.mobilemhealthpay.data.remote.dto.ApiResponse
import com.example.mobilemhealthpay.data.remote.dto.PaiementDto
import com.example.mobilemhealthpay.data.remote.dto.RefundCompleteDto
import com.example.mobilemhealthpay.data.remote.dto.RefundCompleteRequest
import com.example.mobilemhealthpay.data.remote.dto.RefundDto
import com.example.mobilemhealthpay.data.remote.dto.TransactionResponseDto
import com.example.mobilemhealthpay.data.remote.dto.TransactionAttemptRequest
import com.example.mobilemhealthpay.data.remote.dto.VirementDto
import com.example.mobilemhealthpay.data.remote.dto.VirementResponseDto
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
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
    @GET("refunds/")
    suspend fun getRefunds(
        @Header("X-Secret-Key") secretKey: String,
        @Query("refund_status") refundStatus: String,
        @Query("page_size") numberOfRequests : Int
    ): RefundDto
    
    @POST("refunds/{refund_id}/complete")
    suspend fun completeRefund(
        @Header("X-Secret-Key") secretKey: String,
        @Path("refund_id") refundId: String,
        @Body request: RefundCompleteRequest
    ): ApiResponse<RefundCompleteDto>

    @POST("refunds/{refund_id}/fail")
    suspend fun failRefund(
        @Header("X-Secret-Key") secretKey: String,
        @Path("refund_id") refundId: String,
        @Body request: com.example.mobilemhealthpay.data.remote.dto.RefundFailRequest
    ): ApiResponse<RefundCompleteDto>

    @GET("virements/")
    suspend fun getVirements(
        @Header("X-Secret-Key") secretKey: String,
        @Query("status") status: String,
        @Query("page_size") numberOfRequest: Int
    ): VirementResponseDto

    @POST("virements/{virement_id}/attempts")
    suspend fun reportTransactionAttempt(
        @Header("X-Secret-Key") secretKey: String,
        @Path("virement_id") virementId: String,
        @Body request: TransactionAttemptRequest
    ): ApiResponse<VirementDto>






}
