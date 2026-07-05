package com.partsystem.partvisitapp.feature.report_factor.online.repository

import android.content.Context
import com.partsystem.partvisitapp.core.network.ApiService
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.feature.report_factor.online.model.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.ErrorHandler
import com.partsystem.partvisitapp.core.utils.ErrorHandler.getExceptionMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class OnlineOrderListRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService
) {

    suspend fun getReportFactorVisitor(
        type: Int,
        visitorId: Int,
        condition: String,
        pageNumber: Int,
        pageSize: Int
    ): NetworkResult<List<ReportFactorDto>> {
        return try {
            val response =
                api.getReportFactorVisitor(type, visitorId, condition, pageNumber, pageSize)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                val errorMessage =
                    ErrorHandler.getHttpErrorMessage(context, response.code(), response.message())
                NetworkResult.Error(errorMessage)
            }
        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }

    suspend fun getReportFactorCustomer(
        type: Int,
        customerId: Int,
        pageNumber: Int,
        pageSize: Int
    ): NetworkResult<List<ReportFactorDto>> {
        return try {
            val response = api.getReportFactorCustomer(type, customerId, pageNumber, pageSize)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                val errorMessage =
                    ErrorHandler.getHttpErrorMessage(context, response.code(), response.message())
                NetworkResult.Error(errorMessage)
            }
        } catch (ex: Exception) {
            val errorMsg = getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }

    suspend fun getReportFactorDetail(
        type: Int,
        factorId: Int,
        pageNumber: Int,
        pageSize: Int
    ): NetworkResult<List<ReportFactorDto>> {
        return try {
            val response = api.getReportFactorDetail(type, factorId, pageNumber, pageSize)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                val errorMessage =
                    ErrorHandler.getHttpErrorMessage(context, response.code(), response.message())
                NetworkResult.Error(errorMessage)
            }
        } catch (ex: Exception) {
            NetworkResult.Error(getExceptionMessage(context, ex))
        }
    }
}




