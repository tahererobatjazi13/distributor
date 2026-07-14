package ir.kitgroup.distributor.core.network

sealed class NetworkResult<out T> {
    data object Idle : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
    data class Empty(val message: String) : NetworkResult<Nothing>()
}
