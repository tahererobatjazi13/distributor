package ir.kitgroup.distributor.feature.login.model

import com.google.gson.annotations.SerializedName

data class LoginDistributorResponse(
    @SerializedName("TypeClass") val typeClass: String?,
    @SerializedName("Current") val current: String?,
    @SerializedName("StatusApi") val statusApi: Int?,
    @SerializedName("Description") val description: String?,
    @SerializedName("Result") val result: String?
)

data class LoginDistributorResult(
    @SerializedName("PrsGuid") val prsGuid: String?,
    @SerializedName("Name") val name: String?
)
