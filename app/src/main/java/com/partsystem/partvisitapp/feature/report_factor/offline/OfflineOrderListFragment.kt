package com.partsystem.partvisitapp.feature.report_factor.offline

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import dagger.hilt.android.AndroidEntryPoint
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.OrderType
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.convertNumbersToEnglish
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.core.utils.fixPersianChars
import com.partsystem.partvisitapp.databinding.FragmentOrderListBinding
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import com.partsystem.partvisitapp.feature.report_factor.offline.adapter.OfflineOrderListAdapter
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorHeaderUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@SuppressLint("UseCompatLoadingForDrawables")
@AndroidEntryPoint
class OfflineOrderListFragment : Fragment() {

    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = _binding!!
    private lateinit var offlineOrderListAdapter: OfflineOrderListAdapter

    private val factorViewModel: FactorViewModel by viewModels()

    private var sendDialog: CustomDialog? = null
    private var deleteDialog: CustomDialog? = null

    private val searchIcon by lazy { requireContext().getDrawable(R.drawable.ic_search) }
    private val clearIcon by lazy { requireContext().getDrawable(R.drawable.ic_clear) }

    private var selectedFactor: FactorHeaderUiModel? = null
    private var currentFactorId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDialogs()
        setupRecyclerView()
        setupClicks()
        observeData()
        setupClearIcon()
        observeSendFactor()
        observeValidateCredit()
        setupSearch()
    }

    private fun initDialogs() {
        sendDialog = CustomDialog()
        deleteDialog = CustomDialog()
    }

    private fun setupRecyclerView() = binding.apply {
        rvOrderList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            offlineOrderListAdapter = OfflineOrderListAdapter(
                showSyncButton = true,
                onDelete = { item -> showDeleteDialog(item) },
                onSabtChanged = { item, checked -> handleSabtChange(item, checked) },
                onSync = { item ->
                    selectedFactor = item
                    showSendDialog(item)
                }
            ) { factors ->
                navigateToFactor(factors)

            }
            adapter = offlineOrderListAdapter
        }
    }

    private fun handleSabtChange(item: FactorHeaderUiModel, isChecked: Boolean) {
        currentFactorId = item.factorId

        viewLifecycleOwner.lifecycleScope.launch {

            val header =
                factorViewModel.getFactorHeaderById(item.factorId) ?: return@launch

            val customerStatus =
                factorViewModel.getCustomerErrorStatus(header.customerId!!)
                    .firstOrNull()

            if (customerStatus == null) {
                Toast.makeText(context, "Customer not found", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val hasError = customerStatus.hasErrorOrder
            val hasWarning = customerStatus.hasWarningOrder

            if (hasError || hasWarning) {
                updateCreditResult(item.factorId, true)
                delay(500)
            }

            factorViewModel.updateSabtFromOfflineList(
                factorId = item.factorId,
                sabt = if (isChecked) 1 else 0
            )
        }
    }

    private fun navigateToFactor(item: FactorHeaderUiModel) {
        if (item.hasDetail) {
            val action =
                OfflineOrderListFragmentDirections
                    .actionOfflineOrderListFragmentToOfflineOrderDetailFragment(
                        item.factorId,
                        item.sabt,
                        item.actId
                    )

            findNavController().navigate(action)

        } else {
            val bundle = bundleOf(
                "typeCustomer" to true,
                "typeOrder" to OrderType.Edit.value,
                "customerId" to 0,
                "customerName" to "",
                "factorId" to item.factorId
            )

            requireActivity()
                .findNavController(R.id.mainNavHost)
                .navigate(R.id.action_global_to_headerOrderFragment, bundle)
        }
    }

    private fun showDeleteDialog(item: FactorHeaderUiModel) {
        selectedFactor = item

        deleteDialog = CustomDialog().apply {

            setOnClickNegativeButton {
                selectedFactor = null
                hideProgress()
            }

            setOnClickPositiveButton {
                selectedFactor?.let {
                    factorViewModel.deleteFactor(it.factorId)
                }
                selectedFactor = null
                hideProgress()
            }
        }

        deleteDialog?.showDialog(
            requireActivity(),
            "",
            getString(R.string.msg_sure_delete_orders),
            true,
            getString(R.string.label_no),
            getString(R.string.label_ok),
            showPositiveButton = true,
            showNegativeButton = true
        )
    }

    private fun showSendDialog(item: FactorHeaderUiModel) {
        sendDialog = CustomDialog().apply {

            setOnClickNegativeButton {
                selectedFactor = null
                hideProgress()
            }

            setOnClickPositiveButton {
                factorViewModel.sendFactor(
                    factorId = item.factorId
                )
            }
        }

        sendDialog?.showDialog(
            requireActivity(),
            "",
            getString(R.string.msg_sure_send_order),
            true,
            getString(R.string.label_no),
            getString(R.string.label_ok),
            showPositiveButton = true,
            showNegativeButton = true
        )
    }

    private fun observeData() = binding.apply {
        factorViewModel.filteredHeaders.observe(viewLifecycleOwner) { headers ->
            if (headers.isNullOrEmpty()) {
                info.show()
                info.message(getString(R.string.msg_no_order))
                rvOrderList.hide()
                // btnSyncAllOrder.gone()
            } else {
                info.gone()
                rvOrderList.show()
                offlineOrderListAdapter.setData(headers)

                /*  btnSyncAllOrder.visibility =
                      if (headers.size > 2) View.VISIBLE else View.GONE*/
            }
        }
    }

    private fun observeSendFactor() {
        factorViewModel.sendFactorResult.observe(viewLifecycleOwner) { event ->

            event.getContentIfNotHandled()?.let { result ->

                when (result) {

                    is NetworkResult.Loading -> Unit

                    is NetworkResult.Success -> {
                        CustomSnackBar.make(
                            requireActivity().findViewById(android.R.id.content),
                            result.message ?: getString(R.string.msg_order_successfully_sent),
                            SnackBarType.Success.value
                        )?.show()
                    }

                    is NetworkResult.Error -> {
                        CustomSnackBar.make(
                            requireActivity().findViewById(android.R.id.content),
                            result.message,
                            SnackBarType.Error.value
                        )?.show()
                    }
                }
            }
        }
    }

    private fun observeValidateCredit() {

        factorViewModel.validationCredit.observe(viewLifecycleOwner) { event ->
            val state = event.getContentIfNotHandled() ?: return@observe
            val factorId = currentFactorId ?: return@observe

            when (state) {
                is NetworkResult.Loading -> {
                    updateCreditResult(factorId, true)
                }

                is NetworkResult.Success -> {
                    updateCreditResult(factorId, false)

                    CustomSnackBar.make(
                        requireView(),
                        getString(R.string.msg_success_validate_credit),
                        SnackBarType.Success.value
                    )?.show()
                }

                is NetworkResult.Error -> {
                    updateCreditResult(factorId, false)

                    Log.d("finalPriceMessage", state.message)
                    CustomSnackBar.make(
                        requireView(),
                        "  ${state.message}",
                        SnackBarType.Error.value,
                        isIndefinite = true
                    )?.show()

                    factorViewModel.updateSabtFromOfflineList(
                        factorId = factorId,
                        sabt = 0
                    )
                    viewLifecycleOwner.lifecycleScope.launch {
                        val updatedList = factorViewModel.getAllOfflineOrders()
                        factorViewModel.setOfflineOrders(updatedList)
                    }
                }
            }
        }
    }

    private fun updateCreditResult(factorId: Int, validateState: Boolean) {
        val updated = offlineOrderListAdapter.currentList.map {
            if (it.factorId == factorId)
                it.copy(isValidateCredit = validateState)
            else it
        }
        offlineOrderListAdapter.submitList(updated)
    }

    private fun setupClicks() = binding.apply {
        btnSyncAllOrder.setOnClickBtnOneListener {
            sendDialog?.showDialog(
                activity,
                "",
                getString(R.string.msg_sure_send_orders),
                true,
                getString(R.string.label_no),
                getString(R.string.label_ok),
                showPositiveButton = true,
                showNegativeButton = true
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClearIcon() = binding.apply {
        // پاک کردن جستجو با لمس آیکون ضربدر
        etSearch.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd =
                    etSearch.compoundDrawablesRelative[2] ?: return@setOnTouchListener false

                val touchAreaStart =
                    etSearch.width - etSearch.paddingEnd - drawableEnd.intrinsicWidth

                if (event.x >= touchAreaStart) {
                    etSearch.text?.clear()
                    v.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun setupSearch() = binding.apply {
        etSearch.addTextChangedListener { editable ->
            val query = convertNumbersToEnglish(fixPersianChars(editable.toString()))
            etSearch.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                if (query.isEmpty()) searchIcon else clearIcon,
                null
            )
            /* btnSyncAllOrder.visibility =
                if (query.isNullOrEmpty() && offlineOrderListAdapter.itemCount > 2)
                    View.VISIBLE
                else
                    View.GONE*/
            factorViewModel.filterHeaders(query)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}