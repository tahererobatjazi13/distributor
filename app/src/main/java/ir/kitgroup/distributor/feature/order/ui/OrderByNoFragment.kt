package ir.kitgroup.distributor.feature.order.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.network.NetworkResult
import ir.kitgroup.distributor.core.utils.ApiConfig.TOKEN
import ir.kitgroup.distributor.core.utils.SnackBarType
import ir.kitgroup.distributor.core.utils.componenet.CustomSnackBar
import ir.kitgroup.distributor.core.utils.datastore.MainPreferences
import ir.kitgroup.distributor.core.utils.extensions.gone
import ir.kitgroup.distributor.core.utils.extensions.show
import ir.kitgroup.distributor.databinding.FragmentOrderByNoBinding
import ir.kitgroup.distributor.feature.order.model.OrderDistribution
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class OrderByNoFragment : Fragment() {

    @Inject
    lateinit var mainPreferences: MainPreferences

    private var _binding: FragmentOrderByNoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderViewModel by viewModels()
    private val args: OrderByNoFragmentArgs by navArgs()
    private val formatter = DecimalFormat("#,###")
    private var currentOrder: OrderDistribution? = null
    private var currentOrderId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderByNoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        setupObserver()
        setupFragmentResult()
        viewModel.fetchOrderByNo(args.search, TOKEN)
    }

    private fun setupClicks() = binding.apply {
        tryAgain.setOnClickListener {
            viewModel.fetchOrderByNo(args.search, TOKEN)
        }

        hfOrderDetail.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }

        bmbDeliveryWarehouse.setOnClickBtnOneListener {
            val item = currentOrder ?: run {
                CustomSnackBar.make(
                    binding.root,
                    getString(R.string.msg_order_info_not_available),
                    SnackBarType.Error.value
                )?.show()
                return@setOnClickBtnOneListener
            }

            val orderId = item.hdrGid.orEmpty()
            if (orderId.isBlank()) {
                CustomSnackBar.make(
                    binding.root,
                    getString(R.string.msg_order_id_not_valid),
                    SnackBarType.Error.value
                )?.show()
                return@setOnClickBtnOneListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val prsGuid = mainPreferences.prsGuid.first().orEmpty()

                if (prsGuid.isBlank()) {
                    CustomSnackBar.make(
                        binding.root,
                        getString(R.string.msg_person_guid_not_valid),
                        SnackBarType.Error.value
                    )?.show()
                    return@launch
                }

                viewModel.setConfirmOrderDelivery(
                    token = TOKEN,
                    orderId = orderId,
                    prsGuid = prsGuid
                )
            }
        }

        bmbDeliveryConfirmation.setOnClickBtnOneListener {
            DeliveryConfirmationBottomSheet
                .newInstance(currentOrder)
                .show(childFragmentManager, DeliveryConfirmationBottomSheet.TAG)
        }
    }


    private fun setupFragmentResult() {
        childFragmentManager.setFragmentResultListener(
            DeliveryConfirmationBottomSheet.REQ_REFRESH_ORDER_LIST,
            viewLifecycleOwner
        ) { _, bundle ->
            findNavController().navigateUp()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObserver() {
        viewModel.orderByNo.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> showLoadingState()
                is NetworkResult.Success -> showOrderDetails(result.data)
                is NetworkResult.Empty -> showEmptyState(result.message)
                is NetworkResult.Error -> showErrorState(result.message)
                else -> Unit
            }
        }

        viewModel.confirmWarehouseDeliveryState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.bmbDeliveryWarehouse.checkShowPbOne(true)
                }

                is NetworkResult.Success -> {
                    binding.bmbDeliveryWarehouse.checkShowPbOne(false)

                    CustomSnackBar.make(
                        binding.root,
                        result.data,
                        SnackBarType.Success.value
                    )?.show()

                    binding.root.postDelayed({
                        if (isAdded) {
                            findNavController().navigateUp()
                        }
                    }, 100)
                }

                is NetworkResult.Empty -> {
                    binding.bmbDeliveryWarehouse.checkShowPbOne(false)

                    CustomSnackBar.make(
                        binding.root,
                        result.message,
                        SnackBarType.Warning.value
                    )?.show()
                }

                is NetworkResult.Error -> {
                    binding.bmbDeliveryWarehouse.checkShowPbOne(false)

                    CustomSnackBar.make(
                        binding.root,
                        result.message,
                        SnackBarType.Error.value
                    )?.show()
                }

                else -> Unit
            }
        }
    }

    private fun showLoadingState() = binding.apply {
        loading.show()
        tryAgain.gone()
        info.gone()
        cvContent.gone()
    }

    private fun showEmptyState(message: String) = binding.apply {
        loading.gone()
        tryAgain.gone()
        cvContent.gone()
        info.show()
        info.message(message)
    }

    private fun showErrorState(message: String) = binding.apply {
        loading.gone()
        info.gone()
        cvContent.gone()
        tryAgain.show()
        tryAgain.message = message
    }

    @SuppressLint("SetTextI18n")
    private fun showOrderDetails(item: OrderDistribution) = binding.apply {
        loading.gone()
        tryAgain.gone()
        info.gone()
        cvContent.show()

        currentOrder = item
        currentOrderId = item.hdrGid

        tvAccountName.text = item.accountName
        tvOrderNumber.text = item.noOrder
        tvAccountAddress.text = item.address
        tvAccountMobile.text = item.mobile
        tvOrderDate.text = item.ordDate

        if (item.distributorDeliveryStatus == true) {
            bmbDeliveryWarehouse.gone()
            tvDeliveryWarehouseStatus.show()
            bmbDeliveryConfirmation.show()

            tvDistributorDeliveryStatus.text = getString(R.string.label_yes)
            tvDistributorDeliveryStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.green_21BF73)
            )
        } else {
            bmbDeliveryWarehouse.show()
            tvDeliveryWarehouseStatus.gone()
            bmbDeliveryConfirmation.gone()

            tvDistributorDeliveryStatus.text = getString(R.string.label_no)
            tvDistributorDeliveryStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.colorError)
            )
        }

        if (item.descOther.isNullOrBlank() || item.descOther == "-") {
            clDescriptionOrder.gone()
        } else {
            clDescriptionOrder.show()
            tvDescriptionOrder.text = item.descOther
        }

        if (item.settlementTypeName.isNullOrBlank() || item.settlementTypeName == "-") {
            clSettlementTypeName.gone()
        } else {
            clSettlementTypeName.show()
            tvSettlementTypeName.text = item.settlementTypeName
        }

        val netPrice = item.sumNetPrice ?: 0.0
        tvSumNetPrice.text = "${formatter.format(netPrice)} ریال"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
