package ir.kitgroup.distributor.feature.order.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.network.ApiService
import ir.kitgroup.distributor.core.network.NetworkResult
import ir.kitgroup.distributor.core.utils.ErrorHandler
import ir.kitgroup.distributor.core.utils.ErrorHandler.getExceptionMessage
import ir.kitgroup.distributor.feature.order.model.OrderConfirmRequest
import ir.kitgroup.distributor.feature.order.model.OrderDistribution
import javax.inject.Inject

class OrderRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService
) {

    private val gson = Gson()

    suspend fun getOrdersDistributionPlan(
        disPlanGuid: String,
        token: String
    ): NetworkResult<List<OrderDistribution>> {
        return try {
            val response = api.getOrdersDistributionPlan(disPlanGuid, token)

            if (!response.isSuccessful) {
                return NetworkResult.Error(
                    ErrorHandler.getHttpErrorMessage(
                        context,
                        response.code(),
                        response.message()
                    )
                )
            }

            val body = response.body()
                ?: return NetworkResult.Error(
                    context.getString(R.string.error_server_empty_response)
                )

            when (body.StatusApi) {
                4 -> {

                    val resultString = body.Result
                    val listType = object : TypeToken<List<OrderDistribution>>() {}.type

                    val orders: List<OrderDistribution> =
                        if (resultString.isNullOrBlank()) {
                            emptyList()
                        } else {
                            gson.fromJson(resultString, listType) ?: emptyList()
                        }

                    NetworkResult.Success(orders)
                }

                3 -> {
                    val message = body.Description?.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.msg_no_order_distribution_plan)

                    NetworkResult.Empty(message)
                }

                else -> {
                    val message = body.Description?.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.error_order_distribution_status)

                    NetworkResult.Error(message)
                }
            }
        } catch (ex: Exception) {
            NetworkResult.Error(getExceptionMessage(context, ex))
        }
    }

    suspend fun getOrderByNo(
        orderNo: String,
        token: String
    ): NetworkResult<OrderDistribution> {
        return try {
            val response = api.getOrderByNo(orderNo, token)

            if (!response.isSuccessful) {
                return NetworkResult.Error(
                    ErrorHandler.getHttpErrorMessage(
                        context,
                        response.code(),
                        response.message()
                    )
                )
            }

            val body = response.body()
                ?: return NetworkResult.Error(
                    context.getString(R.string.error_server_empty_response)
                )

            when (body.StatusApi) {
                4 -> {
                    val resultString = body.Result?.takeIf { it.isNotBlank() }
                        ?: return NetworkResult.Error(
                            context.getString(R.string.error_order_data_empty)
                        )

                    val order = gson.fromJson(resultString, OrderDistribution::class.java)
                    NetworkResult.Success(order)
                }

                3 -> {
                    val message = body.Description?.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.error_order_not_found)

                    NetworkResult.Empty(message)
                }

                else -> {
                    val message = body.Description?.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.error_fetch_order_by_no)

                    NetworkResult.Error(message)
                }
            }
        } catch (ex: Exception) {
            NetworkResult.Error(getExceptionMessage(context, ex))
        }
    }

    suspend fun confirmOrderDistribution(
        token: String,
        request: OrderConfirmRequest
    ): NetworkResult<String> {
        return try {
            val response = api.setOrderDistributionConfirm(token, request)

            if (!response.isSuccessful) {
                return NetworkResult.Error(
                    ErrorHandler.getHttpErrorMessage(
                        context,
                        response.code(),
                        response.message()
                    )
                )
            }

            val body = response.body()
                ?: return NetworkResult.Error(
                    context.getString(R.string.error_server_empty_response)
                )

            val serverDesc = body.Description?.takeIf { it.isNotBlank() }

            when (body.StatusApi) {
                4 -> {
                    NetworkResult.Success(
                        serverDesc ?: context.getString(R.string.msg_order_confirmation_success)
                    )
                }

                3 -> {
                    NetworkResult.Empty(
                        serverDesc ?: context.getString(R.string.error_order_confirmation_no_data)
                    )
                }

                else -> {
                    NetworkResult.Error(
                        serverDesc ?: context.getString(R.string.error_order_confirmation_failed)
                    )
                }
            }
        } catch (ex: Exception) {
            NetworkResult.Error(getExceptionMessage(context, ex))
        }
    }

    suspend fun setConfirmOrderDelivery(
        token: String,
        orderId: String,
        prsGuid: String
    ): NetworkResult<String> {
        return try {
            val response = api.setConfirmOrderDelivery(
                token = token,
                orderId = orderId,
                prsGuid = prsGuid
            )

            if (!response.isSuccessful) {
                return NetworkResult.Error(
                    ErrorHandler.getHttpErrorMessage(
                        context,
                        response.code(),
                        response.message()
                    )
                )
            }

            val body = response.body()
                ?: return NetworkResult.Error(
                    context.getString(R.string.error_server_empty_response)
                )

            val serverDesc = body.Description?.takeIf { it.isNotBlank() }

            when (body.StatusApi) {
                4 -> {
                    NetworkResult.Success(
                        serverDesc ?: context.getString(R.string.msg_warehouse_delivery_confirmed_successfully)
                    )
                }

                3 -> {
                    NetworkResult.Empty(
                        serverDesc ?: context.getString(R.string.error_invalid_order_info)
                    )
                }

                else -> {
                    NetworkResult.Error(
                        serverDesc ?: context.getString(R.string.error_confirm_warehouse_delivery_failed)
                    )
                }
            }
        } catch (ex: Exception) {
            NetworkResult.Error(getExceptionMessage(context, ex))
        }
    }
}
