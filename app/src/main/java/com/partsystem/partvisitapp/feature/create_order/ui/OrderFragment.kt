package com.partsystem.partvisitapp.feature.create_order.ui

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.CustomerCreditKind
import com.partsystem.partvisitapp.core.utils.DiscountApplyKind
import com.partsystem.partvisitapp.core.utils.FactorFormKind
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.CustomDialog
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.extensions.getTodayPersianDateLatin
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.hide
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentOrderBinding
import com.partsystem.partvisitapp.feature.create_order.adapter.OrderAdapter
import com.partsystem.partvisitapp.feature.report_factor.offline.model.FactorDetailUiModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@AndroidEntryPoint
class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderAdapter: OrderAdapter
    private val factorViewModel: FactorViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    private val formatter = DecimalFormat("#,###,###,###")
    private var currentCartItems: List<FactorDetailUiModel> = emptyList()
    private val args: OrderFragmentArgs by navArgs()
    private var customDialog: CustomDialog? = null
    private var backCallback: OnBackPressedCallback? = null
    private var isEditingCompletedOrder = false //  فلگ برای تشخیص حالت ویرایش
    private var customDialogSend: CustomDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackNavigationRestriction()
        isEditingCompletedOrder = args.isEditingCompletedOrder

        setupClicks()
        initAdapter()
        setupObserver()
        observeSendFactor()
        observeValidateCredit()

        customDialog = CustomDialog()

        if (args.factorId > 0) {
            binding.cbSabt.isChecked = (args.sabt == 1)
            binding.cbSabt.isEnabled = true

            // تنظیم وضعیت اولیه آداپتر بر اساس مقدار ذخیره‌شده
            orderAdapter.setOrderCompleted(args.sabt == 1)
        } else {
            binding.cbSabt.isChecked = false
            binding.cbSabt.isEnabled = true

            // وضعیت پیش‌فرض: سفارش تکمیل نشده
            orderAdapter.setOrderCompleted(false)
        }
    }

    /**
     * راه‌اندازی یکپارچه مدیریت بازگشت (هم سخت‌افزاری و هم هدر)
     *  این متد حتماً باید در ابتدای onViewCreated فراخوانی شود
     */
    private fun setupBackNavigationRestriction() {
        // مدیریت دکمه بازگشت سخت‌افزاری - همیشه فعال باشد
        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPressAttempt()
            }
        }.also {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it)
        }

        // مدیریت دکمه بازگشت هدر
        binding.hfOrder.setOnClickImgTwoListener {
            handleBackPressAttempt()
        }
    }

    /**
     * منطق هوشمند بازگشت:
     * - اگر در حالت ویرایش سفارش تکمیل‌شده هستیم → اجازه بازگشت به صفحه جزئیات
     * - اگر در حالت عادی و تیک تکمیل زده شده → نمایش هشدار
     * - در غیر این صورت → بازگشت عادی
     */
    private fun handleBackPressAttempt() {
        // حالت ویرایش سفارش تکمیل ‌شده: همیشه اجازه بازگشت به صفحه جزئیات
        if (isEditingCompletedOrder) {
            findNavController().navigateUp()
            return
        }

        //  حالت عادی + تیک تکمیل زده شده: مسدود کردن بازگشت
        if (binding.cbSabt.isChecked) {
            CustomDialog().apply {
                setOnClickNegativeButton { hideProgress() }
                setOnClickPositiveButton { hideProgress() }
            }.showDialog(
                requireActivity(),
                getString(R.string.label_attention),
                getString(R.string.msg_cannot_go_back_warning),
                false,
                getString(R.string.label_understand),
                null,
                showPositiveButton = false,
                showNegativeButton = true
            )
            return
        }

        // بازگشت عادی
        findNavController().navigateUp()
    }

    /**
     *     تنظیم کلیک روی دکمه ورود و بررسی ورودی‌ها
     */

    private fun setupClicks() = binding.apply {

        btnDraftOrder.setOnClickListener {
            factorViewModel.resetHeader()
            factorViewModel.enteredProductPage = false
            navigateToReportFactor()
        }
        bmbSendOrder.setOnClickBtnOneListener {
            if (currentCartItems.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    R.string.error_no_row_for_order,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickBtnOneListener
            }

            if (cbSabt.isChecked) {
                showSendDialog()

            } else {
                // تیک نزده → هشدار
                showWarningDialog()
            }
        }
        cbSabt.setOnCheckedChangeListener { _, isChecked ->
            orderAdapter.setOrderCompleted(isChecked)

            if (isChecked) {
                lifecycleScope.launch {
                    val hasTaxConnection = factorViewModel.getHasTaxConnection()
                    // گرفتن هدر
                    val header = if (args.factorId > 0) {
                        factorViewModel.getFactorHeaderById(args.factorId) ?: return@launch
                    } else {
                        factorViewModel.factorHeader.value ?: return@launch
                    }
                    if (args.sabt == 0 || factorViewModel.discountManuallyRemoved.value) {

                        factorViewModel.calculateDiscountInsert(
                            applyKind = DiscountApplyKind.FactorLevel.ordinal,
                            factorHeader = header,
                            factorDetail = null,
                            hasTaxConnection
                        )
                        factorViewModel.markDiscountApplied()
                        calculateTotalPrices(
                            currentCartItems,
                            targetSabt = 1
                        )

                    } else {
                        calculateTotalPrices(currentCartItems, targetSabt = 1)
                    }

                    val customerStatus = factorViewModel
                        .getCustomerErrorStatus(header.customerId!!)
                        .firstOrNull()

                    if (customerStatus == null) {
                        Toast.makeText(context, "Customer not found", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val hasError = customerStatus.hasErrorOrder
                    val hasWarning = customerStatus.hasWarningOrder

                    if (hasError || hasWarning) {

                        factorViewModel.checkCustomerCredit(
                            customerId = header.customerId!!,
                            persianDate = getTodayPersianDateLatin(),
                            kind = CustomerCreditKind.Customer,
                            formKind = FactorFormKind.Factor
                        )
                        observeValidateCredit()
                    }
                }
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    factorViewModel.removeGiftsAndDiscounts(args.factorId)
                    factorViewModel.markDiscountRemoved()
                    calculateTotalPrices(
                        currentCartItems,
                        targetSabt = 0
                    )
                }
            }
        }
    }

    private fun showSendDialog() {
        customDialogSend = CustomDialog().apply {

            setOnClickNegativeButton {
                hideProgress()
            }

            setOnClickPositiveButton {
                calculateTotalPrices(currentCartItems, targetSabt = 1)
                // تکمیل سفارش → ارسال به سرور
                factorViewModel.sendFactor(
                    factorId = args.factorId
                )
            }
        }

        customDialogSend?.showDialog(
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


    private fun showWarningDialog() {
        customDialog = CustomDialog().apply {

            setOnClickNegativeButton { hideProgress() }
            setOnClickPositiveButton {
                factorViewModel.resetHeader()
                factorViewModel.enteredProductPage = false
                navigateToReportFactor()
                hideProgress()
            }
        }

        customDialog?.showDialog(
            activity,
            getString(R.string.error_order_not_sabt),
            getString(R.string.error_save_order_draft),
            true,
            getString(R.string.label_close),
            getString(R.string.label_confirm),
            showPositiveButton = true,
            showNegativeButton = true
        )
    }

    private fun navigateToReportFactor() {
        val navController = findNavController()
        navController.navigate(
            R.id.reportFactorFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .build()
        )
    }

    private fun observeSendFactor() {

        factorViewModel.sendFactorResult.observe(viewLifecycleOwner) { event ->

            event.getContentIfNotHandled()?.let { result ->

                when (result) {

                    is NetworkResult.Loading -> binding.bmbSendOrder.checkShowPbOne(true)

                    is NetworkResult.Success -> {
                        binding.bmbSendOrder.checkShowPbOne(false)

                        val message =
                            result.message ?: getString(R.string.msg_order_successfully_sent)

                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToHomeClearOrder()
                    }

                    is NetworkResult.Error -> {
                        binding.bmbSendOrder.checkShowPbOne(false)


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

    private fun initAdapter() {
        orderAdapter = OrderAdapter(
            onDelete = { item ->
                if (binding.cbSabt.isChecked) {
                    CustomDialog().apply {
                        setOnClickNegativeButton { hideProgress() }
                        setOnClickPositiveButton { hideProgress() }
                    }.showDialog(
                        requireActivity(),
                        getString(R.string.label_attention),
                        getString(R.string.msg_cannot_delete_when_completed),
                        false,
                        getString(R.string.label_understand),
                        null,
                        showPositiveButton = false,
                        showNegativeButton = true
                    )
                    return@OrderAdapter
                }
                // حذف
                factorViewModel.deleteFactorDetail(item)
            }
        )

        binding.rvOrder.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = orderAdapter
        }
    }

    private fun setupObserver() {
        factorViewModel.getFactorDetailUi(factorId = args.factorId)
            .observe(viewLifecycleOwner) { details ->
                currentCartItems = details ?: emptyList()

                if (details.isNullOrEmpty()) {
                    binding.info.show()
                    binding.info.message(requireContext().getString(R.string.msg_no_data))
                    binding.svMain.hide()
                } else {
                    binding.info.gone()
                    binding.svMain.show()
                }
                orderAdapter.submitList(details)
                val currentSabt = if (binding.cbSabt.isChecked) 1 else 0
                calculateTotalPrices(details, targetSabt = currentSabt)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateTotalPrices(items: List<FactorDetailUiModel>?, targetSabt: Int? = null) {
        items ?: return

        val sumPrice = items.sumOf { it.unit1Rate * it.unit1Value }
        val sumVat = items.sumOf { it.vat }

        lifecycleScope.launch {
            val totalDiscount = factorViewModel.getTotalDiscountForFactor(args.factorId)
            val finalPrice = (sumPrice - totalDiscount) + sumVat

            // آپدیت UI فوری (بدون انتظار برای دیتابیس)
            with(binding) {
                tvSumPrice.text = "${formatter.format(sumPrice)} ریال"
                tvSumDiscountPrice.text = "${"-" + formatter.format(totalDiscount)} ریال"
                tvSumVat.text = "${formatter.format(sumVat)} ریال"
                tvFinalPrice.text = "${formatter.format(finalPrice)} ریال"
            }

            // برای حالت ویرایش: مستقیماً از دیتابیس هدر را بخوان و آپدیت کن
            if (args.factorId > 0) {
                val currentHeaderFromDb = factorViewModel.getFactorHeaderById(args.factorId)
                if (currentHeaderFromDb == null) {
                    Log.e(
                        "OrderFragment",
                        "هدر فاکتور با id=${args.factorId} در دیتابیس وجود ندارد!"
                    )
                    return@launch
                }

                // آپدیت مقادیر مورد نیاز (فقط finalPrice و در صورت نیاز sabt)
                val updatedHeader = currentHeaderFromDb.copy(
                    finalPrice = finalPrice,
                    sabt = targetSabt
                        ?: currentHeaderFromDb.sabt // فقط اگر targetSabt مشخص شده بود آپدیت شود
                )

                factorViewModel.updateFactorHeader(updatedHeader)
            } else {
                // حالت سفارش جدید: استفاده از وضعیت ViewModel
                val currentHeader = factorViewModel.factorHeader.value ?: return@launch
                val updatedHeader = currentHeader.copy(
                    finalPrice = finalPrice,
                    sabt = targetSabt ?: currentHeader.sabt
                )
                factorViewModel.updateHeader(finalPrice = finalPrice, sabt = targetSabt)
                factorViewModel.updateFactorHeader(updatedHeader)
            }
        }
    }

    private fun navigateToHomeClearOrder() {
        val navController = findNavController()
        navController.navigate(
            R.id.homeFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(navController.graph.startDestinationId, true)
                .build()
        )
    }

    private fun observeValidateCredit() {

        factorViewModel.validationCredit.observe(viewLifecycleOwner) { event ->
            val state = event.getContentIfNotHandled() ?: return@observe
            when (state) {
                is NetworkResult.Loading -> {
                    binding.progressBar.show()
                    binding.tvProgressBar.show()
                }

                is NetworkResult.Success -> {
                    binding.progressBar.gone()
                    binding.tvProgressBar.gone()

                    CustomSnackBar.make(
                        requireView(),
                        getString(R.string.msg_success_validate_credit),
                        SnackBarType.Success.value
                    )?.show()
                }

                is NetworkResult.Error -> {
                    binding.progressBar.gone()
                    binding.tvProgressBar.gone()

                    Log.d("finalPriceMessage", state.message)

                    CustomSnackBar.make(
                        requireView(),
                        "  ${state.message}",
                        SnackBarType.Error.value,
                        isIndefinite = true
                    )?.show()
                    viewLifecycleOwner.lifecycleScope.launch {
                        factorViewModel.removeGiftsAndDiscounts(args.factorId)
                        factorViewModel.markDiscountRemoved()
                        calculateTotalPrices(
                            currentCartItems,
                            targetSabt = 0
                        )
                    }
                    binding.cbSabt.isChecked = false
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        backCallback?.remove()
        _binding = null
    }
}