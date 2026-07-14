package ir.kitgroup.distributor.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kitgroup.distributor.core.network.NetworkResult
import ir.kitgroup.distributor.feature.login.model.LoginDistributorResult
import ir.kitgroup.distributor.feature.login.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _loginDistributor =
        MutableStateFlow<NetworkResult<LoginDistributorResult>>(NetworkResult.Idle)
    val loginDistributor: StateFlow<NetworkResult<LoginDistributorResult>> = _loginDistributor

    fun loginDistributor(codePrs: String, pass: String, token: String) {
        viewModelScope.launch {
            _loginDistributor.value = NetworkResult.Loading
            _loginDistributor.value = loginRepository.loginDistributor(codePrs, pass, token)
        }
    }

    fun resetState() {
        _loginDistributor.value = NetworkResult.Idle
    }
}
