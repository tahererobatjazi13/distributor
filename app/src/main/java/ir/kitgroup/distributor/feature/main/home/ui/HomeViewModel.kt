package ir.kitgroup.distributor.feature.main.home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kitgroup.distributor.core.network.NetworkResult
import ir.kitgroup.distributor.feature.main.home.model.DistributionPlan
import ir.kitgroup.distributor.feature.main.home.repository.HomeRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeListViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _distributionPlansList = MutableLiveData<NetworkResult<List<DistributionPlan>>>()
    val distributionPlansList: LiveData<NetworkResult<List<DistributionPlan>>> =
        _distributionPlansList

    fun fetchDistributionPlans(
        prsGuid: String,
        token: String
    ) = viewModelScope.launch {
        _distributionPlansList.value = NetworkResult.Loading
        _distributionPlansList.value = homeRepository.getDistributionPlans(prsGuid, token)
    }

}
