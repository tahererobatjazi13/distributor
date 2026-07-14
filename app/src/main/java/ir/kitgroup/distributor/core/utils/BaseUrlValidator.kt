package ir.kitgroup.distributor.core.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object BaseUrlValidator {

    suspend fun buildBaseUrl(input: String): String? {
        val clean = input.trim().removeSuffix("/")
        val baseUrl = "http://$clean/api/v2/insku/"

        if (isReachable(baseUrl)) return baseUrl

        val httpsUrl = "https://$clean/api/v2/insku/"
        if (isReachable(httpsUrl)) return httpsUrl

        return null
    }

    private suspend fun isReachable(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).get().build()
            val response = OkHttpClient().newCall(request).execute()
            true
        } catch (e: Exception) {
            false
        }
    }
}