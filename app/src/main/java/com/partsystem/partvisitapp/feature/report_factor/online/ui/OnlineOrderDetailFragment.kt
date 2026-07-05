package com.partsystem.partvisitapp.feature.report_factor.online.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.network.NetworkResult
import com.partsystem.partvisitapp.core.utils.datastore.MainPreferences
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.FragmentOrderDetailBinding
import com.partsystem.partvisitapp.feature.report_factor.online.adapter.OnlineOrderDetailAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class OnlineOrderDetailFragment : Fragment() {
    @Inject
    lateinit var mainPreferences: MainPreferences
    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnlineOrderListViewModel by viewModels()

    private lateinit var onlineOrderDetailAdapter: OnlineOrderDetailAdapter
    private val args: OnlineOrderDetailFragmentArgs by navArgs()

    private val formatter = DecimalFormat("#,###,###,###")

    private var currentPage = 1
    private val pageSize = 10
    private var isLoadingMore = false
    private var hasMoreData = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initAdapter()
        setupClicks()
        setupScrollListener()
        viewModel.fetchReportFactorDetail(1, args.id, currentPage, pageSize)
        setupObserver()
    }

    private fun setupScrollListener() {
        binding.svMain.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->

            if (scrollY <= oldScrollY) return@setOnScrollChangeListener
            if (isLoadingMore || !hasMoreData) return@setOnScrollChangeListener

            val child = binding.svMain.getChildAt(0) ?: return@setOnScrollChangeListener

            val isAtBottom = scrollY >= child.measuredHeight - binding.svMain.measuredHeight

            if (isAtBottom) {
                isLoadingMore = true
                currentPage++

                viewModel.fetchReportFactorDetail(
                    type = 1,
                    factorId = args.id,
                    pageNumber = currentPage,
                    pageSize = pageSize
                )
            }
        }
    }

    private fun init() {
        binding.btnEditOrder.gone()
    }

    private fun initAdapter() {
        binding.rvOrderDetail.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            onlineOrderDetailAdapter = OnlineOrderDetailAdapter()
            adapter = onlineOrderDetailAdapter
        }
    }

    private fun setupClicks() = binding.apply {
        hfOrderDetail.setOnClickImgTwoListener {
            hfOrderDetail.gone()
            svMain.gone()
            findNavController().navigateUp()
        }

        tryAgain.setOnClickListener {
            viewModel.fetchReportFactorDetail(1, args.id, currentPage, pageSize)
            tryAgain.gone()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObserver() = binding.apply {
        viewModel.reportFactorDetail.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    if (currentPage == 1) {
                        loading.show()
                        svMain.gone()
                    }
                }

                is NetworkResult.Success -> {
                    loading.gone()
                    svMain.show()
                    isLoadingMore = false

                    val list = result.data
                    hasMoreData = list.size >= pageSize

                    if (list.isEmpty()) {
                        svMain.gone()
                        info.show()
                        info.message(getString(R.string.msg_no_data))
                    } else {
                        info.gone()
                        onlineOrderDetailAdapter.submitList(list)

                        // فقط در صفحه اول هدر را پر کن (برای جلوگیری از رندر مجدد)
                        if (currentPage == 1) {
                            val first = list[0]
                            tvOrderNumber.text = first.id.toString()
                            tvCustomerName.text = first.customerName
                            tvPatternName.text = first.patternName
                            tvDateTime.text = "${first.persianDate} _ ${first.createTime}"
                            tvSumPrice.text = "${formatter.format(first.sumPrice)} ریال"
                            tvSumDiscountPrice.text =
                                "${formatter.format(first.sumDiscountPrice)} ریال"
                            tvSumVat.text = "${formatter.format(first.sumVat)} ریال"
                            tvFinalPrice.text = "${formatter.format(first.finalPrice)} ریال"
                        }
                    }
                }

                is NetworkResult.Error -> {
                    loading.gone()
                    isLoadingMore = false
                    if (currentPage == 1) {
                        tryAgain.show()
                        tryAgain.message = result.message
                    } else {
                        // مدیریت خطا در حالت paging
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}