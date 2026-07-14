package ir.kitgroup.distributor.feature.login.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputLayout
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.base.BaseActivity
import ir.kitgroup.distributor.core.network.NetworkResult
import ir.kitgroup.distributor.core.utils.SnackBarType
import ir.kitgroup.distributor.core.utils.componenet.CustomSnackBar
import ir.kitgroup.distributor.core.utils.convertNumbersToEnglish
import ir.kitgroup.distributor.core.utils.datastore.MainPreferences
import ir.kitgroup.distributor.core.utils.fixPersianChars
import ir.kitgroup.distributor.core.utils.hideKeyboard
import ir.kitgroup.distributor.databinding.ActivityLoginBinding
import ir.kitgroup.distributor.feature.login.dialog.ServerAddressDialog
import ir.kitgroup.distributor.feature.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import ir.kitgroup.distributor.core.utils.ApiConfig.TOKEN
import ir.kitgroup.distributor.feature.login.model.LoginDistributorResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    @Inject
    lateinit var mainPreferences: MainPreferences

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupClicks()
        observeLogin()
        initPasswordToggle()
    }

    private fun setupClicks() {
        binding.bmbLogin.setOnClickBtnOneListener {
            if (validateInputs()) {
                doLogin()
            }
        }
        binding.bmbSetting.setOnClickBtnOneListener {
            val dialog = ServerAddressDialog()
            dialog.show(supportFragmentManager, "Setting")
        }
    }

    private fun doLogin() {
        lifecycleScope.launch {
            val baseUrl = mainPreferences.baseUrlFlow.firstOrNull()

            if (baseUrl.isNullOrEmpty()) {
                showSettingDialog()
                return@launch
            }

            var codePrs = binding.tieCodePrs.text.toString().trim()
            var passWord = binding.tiePassword.text.toString().trim()

            codePrs = convertNumbersToEnglish(fixPersianChars(codePrs))
            passWord = convertNumbersToEnglish(fixPersianChars(passWord))

            hideKeyboard(this@LoginActivity)

            loginViewModel.loginDistributor(codePrs, passWord, TOKEN)
        }
    }

    private fun showSettingDialog() {
        CustomSnackBar.make(
            findViewById(android.R.id.content),
            getString(R.string.error_configure_ip_domain_settings),
            SnackBarType.Error.value
        )?.show()

        ServerAddressDialog().show(supportFragmentManager, "Setting")
    }

    private fun observeLogin() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.loginDistributor.collectLatest { result ->
                    when (result) {
                        is NetworkResult.Idle -> {
                            binding.bmbLogin.checkShowPbOne(false)
                        }

                        is NetworkResult.Loading -> {
                            binding.bmbLogin.checkShowPbOne(true)
                        }

                        is NetworkResult.Success -> {
                            binding.bmbLogin.checkShowPbOne(false)
                            processLogin(result.data)
                            loginViewModel.resetState()
                        }

                        is NetworkResult.Empty -> {
                            binding.bmbLogin.checkShowPbOne(false)
                            processError(result.message)
                            loginViewModel.resetState()
                        }

                        is NetworkResult.Error -> {
                            binding.bmbLogin.checkShowPbOne(false)
                            processError(result.message)
                            loginViewModel.resetState()
                        }
                    }
                }
            }
        }
    }

    private fun processLogin(data: LoginDistributorResult) {
        if (data.prsGuid.isNullOrBlank()) {
            processError(getString(R.string.error_user_not_found))
            return
        }

        lifecycleScope.launch {
            mainPreferences.saveDistributorInfo(
                prsGuid = data.prsGuid,
                name = data.name.orEmpty()
            )
            Toast.makeText(this@LoginActivity, R.string.msg_success_login, Toast.LENGTH_SHORT)
                .show()
            navigateToHome()
        }
    }

    private fun processError(msg: String?) {
        hideKeyboard(this)
        CustomSnackBar.make(
            findViewById(android.R.id.content),
            msg ?: getString(R.string.error_unknown),
            SnackBarType.Error.value
        )?.show()
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun initPasswordToggle() {
        binding.tilPassword.endIconMode = TextInputLayout.END_ICON_NONE

        binding.tiePassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.root.post {
                    binding.root.smoothScrollTo(0, binding.tilPassword.bottom)
                }
            }
        }
        binding.tieCodePrs.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.root.post {
                    binding.root.smoothScrollTo(0, binding.tilCodePrs.bottom)
                }
            }
        }
        binding.tiePassword.addTextChangedListener {
            binding.tilPassword.endIconMode =
                if (!it.isNullOrEmpty()) TextInputLayout.END_ICON_PASSWORD_TOGGLE
                else TextInputLayout.END_ICON_NONE
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val username = binding.tieCodePrs.text.toString().trim()
        binding.tilCodePrs.error = if (username.isEmpty()) {
            isValid = false
            getString(R.string.error_enter_code_prs)
        } else null

        val password = binding.tiePassword.text.toString().trim()
        binding.tilPassword.error = if (password.isEmpty()) {
            isValid = false
            getString(R.string.error_enter_password)
        } else null

        return isValid
    }
}
