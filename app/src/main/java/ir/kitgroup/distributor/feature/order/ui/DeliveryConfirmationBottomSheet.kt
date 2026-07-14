package ir.kitgroup.distributor.feature.order.ui

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.network.NetworkResult
import ir.kitgroup.distributor.core.utils.ApiConfig.TOKEN
import ir.kitgroup.distributor.core.utils.SnackBarType
import ir.kitgroup.distributor.core.utils.componenet.CustomSnackBar
import ir.kitgroup.distributor.databinding.BottomSheetDeliveryConfirmationBinding
import ir.kitgroup.distributor.feature.order.model.OrderConfirmRequest
import ir.kitgroup.distributor.feature.order.model.OrderDistribution

class DeliveryConfirmationBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDeliveryConfirmationBinding? = null
    private val binding get() = _binding!!

    private var orderId: String = ""
    private var accountName: String = ""
    private var orderNo: String = ""
    private var spinnerTypeface: Typeface? = null

    private val viewModel: OrderViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            orderId = it.getString(ARG_ORDER_ID).orEmpty()
            accountName = it.getString(ARG_ACCOUNT_NAME).orEmpty()
            orderNo = it.getString(ARG_ORDER_NO).orEmpty()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDeliveryConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetConfirmOrderState()
        spinnerTypeface = ResourcesCompat.getFont(requireContext(), R.font.iran_sans)

        displayOrderDetails()
        setupPaymentSpinner()
        setupClicks()
        setupObservers()
    }

    @SuppressLint("SetTextI18n")
    private fun displayOrderDetails() {
        binding.tvAccountName.text = accountName
        binding.tvOrderNumber.text = orderNo
    }

    private fun setupPaymentSpinner() {
        val paymentItems = listOf(
            getString(R.string.payment_cash_settlement),
            getString(R.string.payment_full_credit),
            getString(R.string.payment_cash_and_credit),
            getString(R.string.payment_postdated_check)
        )

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            paymentItems
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as? TextView)?.apply {
                    typeface = spinnerTypeface
                    textDirection = View.TEXT_DIRECTION_RTL
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                }
                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as? TextView)?.apply {
                    typeface = spinnerTypeface
                    textDirection = View.TEXT_DIRECTION_RTL
                    textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                }
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.item_spinner)
        binding.spPaymentType.adapter = adapter
    }

    private fun setupClicks() {
        binding.bmbSubmit.setOnClickBtnOneListener {
            val request = OrderConfirmRequest(
                orderId = orderId,
                distributionStatus = when (binding.rgDeliveryStatus.checkedRadioButtonId) {
                    R.id.rbFullDelivery -> 1
                    R.id.rbPartialDelivery -> 2
                    R.id.rbFullReturn -> 3
                    else -> 1
                },
                disConfirm = true,
                paymentType = when (binding.spPaymentType.selectedItemPosition) {
                    0 -> getString(R.string.payment_cash_settlement)
                    1 -> getString(R.string.payment_full_credit)
                    2 -> getString(R.string.payment_cash_and_credit)
                    3 -> getString(R.string.payment_postdated_check)
                    else -> getString(R.string.payment_cash_settlement)
                },
                descOrd = binding.etDescription.text?.toString()?.trim().orEmpty()
            )

            viewModel.confirmOrderDistribution(TOKEN, request)
        }
    }

    private fun setupObservers() {
        viewModel.confirmOrderState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.bmbSubmit.checkShowPbOne(true)
                }

                is NetworkResult.Success -> {
                    binding.bmbSubmit.checkShowPbOne(false)

                    parentFragmentManager.setFragmentResult(
                        REQ_REFRESH_ORDER_LIST,
                        bundleOf(KEY_REFRESH_ORDER_LIST to true)
                    )

                    viewModel.resetConfirmOrderState()
                    dismiss()

                    requireParentFragment().view?.let { parentView ->
                        CustomSnackBar.make(
                            parentView,
                            result.data,
                            SnackBarType.Success.value
                        )?.show()
                    }
                }

                is NetworkResult.Empty -> {
                    binding.bmbSubmit.checkShowPbOne(false)
                    CustomSnackBar.make(
                        binding.root,
                        result.message,
                        SnackBarType.Warning.value
                    )?.show()
                    viewModel.resetConfirmOrderState()
                }

                is NetworkResult.Error -> {
                    binding.bmbSubmit.checkShowPbOne(false)
                    CustomSnackBar.make(
                        binding.root,
                        result.message,
                        SnackBarType.Error.value
                    )?.show()
                    viewModel.resetConfirmOrderState()
                }

                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DeliveryConfirmationBottomSheet"
        const val REQ_REFRESH_ORDER_LIST = "req_refresh_order_list"
        const val KEY_REFRESH_ORDER_LIST = "key_refresh_order_list"

        private const val ARG_ORDER_ID = "arg_order_id"
        private const val ARG_ACCOUNT_NAME = "arg_account_name"
        private const val ARG_ORDER_NO = "arg_order_no"

        fun newInstance(item: OrderDistribution?) = DeliveryConfirmationBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_ORDER_ID, item?.hdrGid.orEmpty())
                putString(ARG_ACCOUNT_NAME, item?.accountName.orEmpty())
                putString(ARG_ORDER_NO, item?.noOrder.orEmpty())
            }
        }
    }
}
