package ir.kitgroup.distributor.feature.main.home.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.network.ApiService
import ir.kitgroup.distributor.core.network.NetworkResult
import ir.kitgroup.distributor.feature.main.home.model.DistributionPlan
import javax.inject.Inject

class HomeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService
) {

    private val gson = Gson()

    suspend fun getDistributionPlans(
        prsGuid: String,
        token: String
    ): NetworkResult<List<DistributionPlan>> {
        return try {
            val response = api.getDistributionPlans(prsGuid, token)

            if (response.isSuccessful) {
                val body = response.body()

                if (body == null) {
                    NetworkResult.Error(context.getString(R.string.error_empty_response))
                } else {
                    when (body.StatusApi) {
                        4 -> {
                            val resultString = body.Result
                            val listType = object : TypeToken<List<DistributionPlan>>() {}.type

                            val plans: List<DistributionPlan> =
                                if (resultString.isNullOrBlank()) {
                                    emptyList()
                                } else {
                                    gson.fromJson(resultString, listType)
                                }

                            NetworkResult.Success(plans)
                        }

                        3 -> {
                            val message = body.Description?.takeIf { it.isNotBlank() }
                                ?: context.getString(R.string.msg_no_distribution_plan)

                            NetworkResult.Empty(message)
                        }

                        else -> {
                            val message = body.Description?.takeIf { it.isNotBlank() }
                                ?: context.getString(R.string.error_fetch_distribution_plans)

                            NetworkResult.Error(message)
                        }
                    }
                }
            } else {
                NetworkResult.Error(
                    context.getString(R.string.error_server_code, response.code())
                )
            }
        } catch (e: Exception) {
            val errorMessage = e.message?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.error_unknown_server)

            NetworkResult.Error(errorMessage)
        }
    }
}
