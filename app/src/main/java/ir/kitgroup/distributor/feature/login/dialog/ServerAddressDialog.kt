package ir.kitgroup.distributor.feature.login.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.utils.BaseUrlValidator
import ir.kitgroup.distributor.core.utils.convertNumbersToEnglish
import ir.kitgroup.distributor.core.utils.datastore.MainPreferences
import ir.kitgroup.distributor.core.utils.fixPersianChars
import ir.kitgroup.distributor.databinding.DialogServerAddressBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject

@AndroidEntryPoint
class ServerAddressDialog : DialogFragment() {

    @Inject
    lateinit var mainPreferences: MainPreferences

    private lateinit var binding: DialogServerAddressBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogServerAddressBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_dialog)

        lifecycleScope.launch {
            //  مقدار واقعی
            val savedBaseUrl = mainPreferences.baseUrlFlow.firstOrNull()

            if (savedBaseUrl.isNullOrBlank()) {
                binding.tieServerAddress.setText("")
            } else {
                val displayAddress = extractHostAndPort(savedBaseUrl)
                binding.tieServerAddress.setText(displayAddress)
            }
        }

        binding.btnSave.setOnClickListener {

            var serverAddress = binding.tieServerAddress.text.toString().trim()
            serverAddress = convertNumbersToEnglish(fixPersianChars(serverAddress))

            if (serverAddress.isEmpty()) {
                binding.tilServerAddress.error = getString(R.string.error_enter_address_server)
                return@setOnClickListener
            }

            lifecycleScope.launch {
                binding.btnSave.isEnabled = false
                binding.tilServerAddress.error = null

                val baseUrl = BaseUrlValidator.buildBaseUrl(serverAddress)
                if (baseUrl == null) {
                    binding.tilServerAddress.error =
                        getString(R.string.error_could_not_connect_server)
                    binding.btnSave.isEnabled = true
                    return@launch
                }

                mainPreferences.saveBaseUrl(baseUrl)
                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    // استخراج IP و پورت از آدرس کامل
    private fun extractHostAndPort(fullUrl: String): String {
        return try {
            val uri = fullUrl.toHttpUrl()
            if (uri.port == 80 || uri.port == 443) {
                uri.host
            } else {
                "${uri.host}:${uri.port}"
            }
        } catch (e: Exception) {
            ""
        }
    }
}


