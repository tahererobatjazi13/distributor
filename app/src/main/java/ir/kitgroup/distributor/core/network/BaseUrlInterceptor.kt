package ir.kitgroup.distributor.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class BaseUrlInterceptor @Inject constructor(
    private val baseUrlProvider: BaseUrlProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()
        val baseUrl = baseUrlProvider.getBaseUrl()

        val basePath = baseUrl.encodedPath
        val originalPath = originalRequest.url.encodedPath

        val newPath = if (basePath.endsWith("/")) {
            basePath + originalPath.removePrefix("/")
        } else {
            "$basePath/$originalPath"
        }

        val newUrl = originalRequest.url.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .encodedPath(newPath)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}