// core/model/OrderConfirmRequest.kt
package ir.kitgroup.distributor.feature.order.model

import com.google.gson.annotations.SerializedName

data class OrderConfirmRequest(
    @SerializedName("OrderId") val orderId: String,
    @SerializedName("distributionstatus") val distributionStatus: Int,
    @SerializedName("DisConfirm") val disConfirm: Boolean,
    @SerializedName("PaymentType") val paymentType: String,
    @SerializedName("descOrd") val descOrd: String
)
