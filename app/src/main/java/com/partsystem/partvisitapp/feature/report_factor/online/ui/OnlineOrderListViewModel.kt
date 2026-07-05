package com.partsystem.partvisitapp.feature.report_factor.online.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.feature.report_factor.online.model.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.extensions.toEnglishDigits
import com.partsystem.partvisitapp.feature.main.home.repository.AppRepository
import com.partsystem.partvisitapp.feature.report_factor.online.repository.OnlineOrderListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineOrderListViewModel @Inject constructor(
    private val onlineOrderListRepository: OnlineOrderListRepository,
    private val appRepository: AppRepository
) : ViewModel() {


    private val _visitorList = MutableLiveData<NetworkResult<List<ReportFactorDto>>>()
    val reportFactorVisitorList: LiveData<NetworkResult<List<ReportFactorDto>>> = _visitorList

    private val _customerList = MutableLiveData<NetworkResult<List<ReportFactorDto>>>()
    val reportFactorCustomerList: LiveData<NetworkResult<List<ReportFactorDto>>> = _customerList

    // نگهداری وضعیت فعلی برای اینکه در اسکرول بعدی هم اعمال شوند
    private var lastCondition = ""
    private var lastQuery = ""

    private var originalVisitor = emptyList<ReportFactorDto>()
    private var originalCustomer = emptyList<ReportFactorDto>()


    fun fetchReportFactorVisitorList(
        type: Int,
        visitorId: Int,
        baseCondition: String,
        query: String,
        pageNumber: Int,
        pageSize: Int
    ) = viewModelScope.launch {

        // همیشه Loading بفرست
        _visitorList.value = NetworkResult.Loading

        lastCondition = baseCondition
        lastQuery = query

        val finalCondition =
            if (query.isBlank()) baseCondition
            else "$baseCondition AND CustomerName LIKE N'%$query%'"

        val result = onlineOrderListRepository.getReportFactorVisitor(
            type,
            visitorId,
            finalCondition,
            pageNumber,
            pageSize
        )

        _visitorList.value = result
    }

    fun fetchReportFactorCustomerList(
        type: Int,
        customerId: Int,
        query: String,
        pageNumber: Int,
        pageSize: Int
    ) = viewModelScope.launch {

        _customerList.value = NetworkResult.Loading

        lastQuery = query

        val finalCondition =
            if (query.isBlank()) ""
            else "AND CustomerName LIKE N'%$query%'"

        val result = onlineOrderListRepository.getReportFactorCustomer(
            type,
            customerId,
            pageNumber,
            pageSize
        )

        _customerList.value = result
    }

/*

    fun fetchReportFactorVisitorList(type: Int, visitorId: Int, condition: String, pageNumber: Int, pageSize: Int) =
        viewModelScope.launch {
            _visitorList.value = NetworkResult.Loading
            when (val result = onlineOrderListRepository.getReportFactorVisitor(type, visitorId, condition, pageNumber, pageSize)) {
                is NetworkResult.Success -> {
                    originalVisitor = result.data
                    _visitorList.value = result
                }
                else -> _visitorList.value = result
            }
        }

    fun fetchReportFactorCustomerList(type: Int, customerId: Int, pageNumber: Int, pageSize: Int) = viewModelScope.launch {
        _customerList.value = NetworkResult.Loading
        when (val result = onlineOrderListRepository.getReportFactorCustomer(type, customerId, pageNumber, pageSize)) {
            is NetworkResult.Success -> {
                originalCustomer = result.data
                _customerList.value = result
            }

            else -> _customerList.value = result
        }
    }
*/

    fun searchVisitorList(query: String) {
        val q = query.trim().toEnglishDigits()
        if (q.isBlank()) {
            _visitorList.value = NetworkResult.Success(originalVisitor)
            return
        }
        _visitorList.value = NetworkResult.Success(
            originalVisitor.filter { it.matchesQuery(q) }
        )
    }

    fun searchCustomerList(query: String) {
        val q = query.trim().toEnglishDigits()
        if (q.isBlank()) {
            _customerList.value = NetworkResult.Success(originalCustomer)
            return
        }
        _customerList.value = NetworkResult.Success(
            originalCustomer.filter { it.matchesQuery(q) }
        )

    }

    private val _reportFactorDetail = MutableLiveData<NetworkResult<List<ReportFactorDto>>>()
    val reportFactorDetail: LiveData<NetworkResult<List<ReportFactorDto>>> = _reportFactorDetail

    private var currentDetailList = mutableListOf<ReportFactorDto>()

    fun fetchReportFactorDetail(type: Int, factorId: Int, pageNumber: Int, pageSize: Int) = viewModelScope.launch {
        if (pageNumber == 1) _reportFactorDetail.value = NetworkResult.Loading

        val result = onlineOrderListRepository.getReportFactorDetail(type, factorId, pageNumber, pageSize)

        if (result is NetworkResult.Success) {
            if (pageNumber == 1) currentDetailList.clear()
            currentDetailList.addAll(result.data)
            _reportFactorDetail.value = NetworkResult.Success(currentDetailList.toList())
        } else {
            _reportFactorDetail.value = result
        }
    }

    // یک تابع اکستنشن برای فیلتر تمیز
    private fun ReportFactorDto.matchesQuery(q: String): Boolean =
        customerName.contains(q, ignoreCase = true) || id.toString().contains(q)

    fun checkDatabase(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = appRepository.isDatabaseReady()
            onResult(result)
        }
    }
}