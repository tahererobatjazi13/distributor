package ir.kitgroup.distributor.feature.login.repository

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.network.ApiService
import ir.kitgroup.distributor.core.network.NetworkResult
import ir.kitgroup.distributor.core.utils.ErrorHandler
import ir.kitgroup.distributor.core.utils.ReleaseLogger
import ir.kitgroup.distributor.feature.login.model.LoginDistributorResult
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {

    suspend fun loginDistributor(
        codePrs: String,
        pass: String,
        token: String
    ): NetworkResult<LoginDistributorResult> {
        return try {
            val response = apiService.loginDistributor(codePrs, pass, token)
            val body = response.body()

            if (!response.isSuccessful) {
                val errorMessage = ErrorHandler.getHttpErrorMessage(
                    context,
                    response.code(),
                    response.message()
                )
                return NetworkResult.Error(errorMessage)
            }

            if (body == null) {
                return NetworkResult.Error(
                    context.getString(R.string.error_empty_response)
                )
            }

            when (body.statusApi) {
                4 -> {
                    val resultJson = body.result?.takeIf { it.isNotBlank() }
                    if (resultJson.isNullOrEmpty()) {
                        NetworkResult.Empty(
                            body.description?.takeIf { it.isNotBlank() }
                                ?: context.getString(R.string.error_user_not_found)
                        )
                    } else {
                        val parsedResult =
                            Gson().fromJson(resultJson, LoginDistributorResult::class.java)

                        if (parsedResult?.prsGuid.isNullOrBlank()) {
                            NetworkResult.Empty(
                                body.description?.takeIf { it.isNotBlank() }
                                    ?: context.getString(R.string.error_user_not_found)
                            )
                        } else {
                            NetworkResult.Success(parsedResult)
                        }
                    }
                }

                3 -> {
                    NetworkResult.Error(
                        body.description?.takeIf { it.isNotBlank() }
                            ?: context.getString(R.string.error_invalid_username_password)
                    )
                }

                else -> {
                    NetworkResult.Error(
                        body.description?.takeIf { it.isNotBlank() }
                            ?: context.getString(R.string.error_login_failed)
                    )
                }
            }
        } catch (ex: Exception) {
            ReleaseLogger.e("LoginRepo", "Login Error: ${ex.message}", ex)
            val errorMsg = ErrorHandler.getExceptionMessage(context, ex)
            NetworkResult.Error(errorMsg)
        }
    }
}
