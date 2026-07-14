package ir.kitgroup.distributor.core.network

import ir.kitgroup.distributor.feature.login.model.LoginDistributorResponse
import ir.kitgroup.distributor.feature.order.model.OrderConfirmRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("LoginDistributor")
    suspend fun loginDistributor(
        @Query("CodePrs") userName: String,
        @Query("Pass") password: String,
        @Query("Token") token: String
    ): Response<LoginDistributorResponse>

    @GET("GetDistributionPlans")
    suspend fun getDistributionPlans(
        @Query("PrsGuid") prsGuid: String,
        @Query("Token") token: String
    ): Response<ApiResponse>

    @GET("GetOrdersDistributionPlan")
    suspend fun getOrdersDistributionPlan(
        @Query("DisPlanGuid") disPlanGuid: String,
        @Query("Token") token: String
    ): Response<ApiResponse>

    @GET("GetOrderByNo")
    suspend fun getOrderByNo(
        @Query("OrderNo") orderNo: String,
        @Query("Token") token: String
    ): Response<ApiResponse>

    @POST("SetOrderDistributionConfirm")
    suspend fun setOrderDistributionConfirm(
        @Query("Token") token: String,
        @Body request: OrderConfirmRequest
    ): Response<ApiResponse>

    @POST("SetConfirmOrderDelivery")
    suspend fun setConfirmOrderDelivery(
        @Query("Token") token: String,
        @Query("OrderId") orderId: String,
        @Query("PrsGuid") prsGuid: String
    ): Response<ApiResponse>

}
