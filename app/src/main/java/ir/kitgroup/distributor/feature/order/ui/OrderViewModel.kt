package ir.kitgroup.distributor.feature.order.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kitgroup.distributor.core.network.NetworkResult
import ir.kitgroup.distributor.feature.order.model.OrderConfirmRequest
import ir.kitgroup.distributor.feature.order.repository.OrderRepository
import ir.kitgroup.distributor.feature.order.model.OrderDistribution
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    private val _orderList = MutableLiveData<NetworkResult<List<OrderDistribution>>>()
    val orderList: LiveData<NetworkResult<List<OrderDistribution>>> = _orderList

    private val _orderByNo = MutableLiveData<NetworkResult<OrderDistribution>>()
    val orderByNo: LiveData<NetworkResult<OrderDistribution>> = _orderByNo

    private val _confirmOrderState = MutableLiveData<NetworkResult<String>?>()
    val confirmOrderState: LiveData<NetworkResult<String>?> = _confirmOrderState

    private val _confirmWarehouseDeliveryState = MutableLiveData<NetworkResult<String>?>()
    val confirmWarehouseDeliveryState: LiveData<NetworkResult<String>?> =
        _confirmWarehouseDeliveryState

    fun fetchOrdersDistributionPlan(disPlanGuid: String, token: String) = viewModelScope.launch {
        _orderList.value = NetworkResult.Loading
        _orderList.value = repository.getOrdersDistributionPlan(disPlanGuid, token)
    }

    fun fetchOrderByNo(orderNo: String, token: String) = viewModelScope.launch {
        _orderByNo.value = NetworkResult.Loading
        _orderByNo.value = repository.getOrderByNo(orderNo, token)
    }

    fun confirmOrderDistribution(token: String, request: OrderConfirmRequest) =
        viewModelScope.launch {
            _confirmOrderState.value = NetworkResult.Loading
            _confirmOrderState.value = repository.confirmOrderDistribution(token, request)
        }

    fun setConfirmOrderDelivery(token: String, orderId: String, prsGuid: String) =
        viewModelScope.launch {
            _confirmWarehouseDeliveryState.value = NetworkResult.Loading
            _confirmWarehouseDeliveryState.value =
                repository.setConfirmOrderDelivery(token, orderId, prsGuid)
        }

    fun resetConfirmOrderState() {
        _confirmOrderState.value = null
    }

    fun resetConfirmWarehouseDeliveryState() {
        _confirmWarehouseDeliveryState.value = null
    }
}
