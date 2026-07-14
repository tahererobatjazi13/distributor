package ir.kitgroup.distributor.feature.order.model

import com.google.gson.annotations.SerializedName

data class OrderDistribution(
    @SerializedName("NoOrder") val noOrder: String?,
    @SerializedName("HdrGID") val hdrGid: String?,
    @SerializedName("ACID") val acid: String?,
    @SerializedName("AccountName") val accountName: String?,
    @SerializedName("Address") val address: String?,
    @SerializedName("Mobile") val mobile: String?,
    @SerializedName("Orderer") val orderer: String?,
    @SerializedName("OrdDate") val ordDate: String?,
    @SerializedName("StatusOrder") val statusOrder: Int?,
    @SerializedName("StatusOrderName") val statusOrderName: String?,
    @SerializedName("SettlementDATE") val settlementDate: String?,
    @SerializedName("SettlementDAY") val settlementDay: Int?,
    @SerializedName("SettlementTypeName") val settlementTypeName: String?,
    @SerializedName("SumPriceOrd") val sumPriceOrd: Double?,
    @SerializedName("SumDiscountDtlPrice") val sumDiscountDtlPrice: Double?,
    @SerializedName("SumDiscountOrdPrice") val sumDiscountOrdPrice: Double?,
    @SerializedName("SumTaxPrice") val sumTaxPrice: Double?,
    @SerializedName("SumNetPrice") val sumNetPrice: Double?,
    @SerializedName("StatusOfficial") val statusOfficial: Int?,
    @SerializedName("DESCDelivery") val descDelivery: String?,
    @SerializedName("DescOther") val descOther: String?,
    @SerializedName("SourceCrt") val sourceCrt: Int?,
    @SerializedName("SaleManagerConfirm") val saleManagerConfirm: Boolean?,
    @SerializedName("PrcDiscount1Hdr") val prcDiscount1Hdr: Double?,
    @SerializedName("DistributorDeliveryStatus") val distributorDeliveryStatus: Boolean?
)
