package ir.kitgroup.distributor.feature.order.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.network.NetworkResult
import ir.kitgroup.distributor.core.utils.ApiConfig.TOKEN
import ir.kitgroup.distributor.core.utils.datastore.MainPreferences
import ir.kitgroup.distributor.core.utils.extensions.gone
import ir.kitgroup.distributor.core.utils.extensions.show
import ir.kitgroup.distributor.databinding.FragmentOrderListBinding
import ir.kitgroup.distributor.feature.order.model.OrderDistribution
import ir.kitgroup.distributor.feature.order.ui.adapter.OrderAdapter
import javax.inject.Inject

@AndroidEntryPoint
class OrderListFragment : Fragment() {

    @Inject
    lateinit var mainPreferences: MainPreferences

    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderAdapter: OrderAdapter
    private val viewModel: OrderViewModel by viewModels()
    private val args: OrderListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        initAdapter()
        setupObservers()
        setupFragmentResult()
        viewModel.fetchOrdersDistributionPlan(args.disPlanGuid, TOKEN)
    }

    private fun setupClicks() {
        binding.tryAgain.setOnClickListener {
            viewModel.fetchOrdersDistributionPlan(args.disPlanGuid, TOKEN)
        }

        binding.hfOrderList.setOnClickImgTwoListener {
            findNavController().navigateUp()
        }
    }

    private fun initAdapter() {
        binding.rvOrderList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            orderAdapter = OrderAdapter(
                onDeliveryConfirmationClick = { order ->
                    DeliveryConfirmationBottomSheet
                        .newInstance(order)
                        .show(childFragmentManager, DeliveryConfirmationBottomSheet.TAG)
                }
            )

            adapter = orderAdapter
        }
    }

    private fun setupFragmentResult() {
        childFragmentManager.setFragmentResultListener(
            DeliveryConfirmationBottomSheet.REQ_REFRESH_ORDER_LIST,
            viewLifecycleOwner
        ) { _, bundle ->
            val shouldRefresh = bundle.getBoolean(
                DeliveryConfirmationBottomSheet.KEY_REFRESH_ORDER_LIST,
                false
            )

            if (shouldRefresh) {
                viewModel.fetchOrdersDistributionPlan(args.disPlanGuid, TOKEN)
            }
        }
    }

    private fun setupObservers() {
        viewModel.orderList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> showLoadingState()
                is NetworkResult.Success -> showSuccessState(result.data)
                is NetworkResult.Empty -> showEmptyState(result.message)
                is NetworkResult.Error -> showErrorState(result.message)
                else -> Unit
            }
        }
    }

    private fun showLoadingState() = binding.apply {
        loading.show()
        tryAgain.gone()
        info.gone()
        rvOrderList.gone()
    }

    private fun showSuccessState(ordersList: List<OrderDistribution>) = binding.apply {
        loading.gone()
        tryAgain.gone()

        if (ordersList.isEmpty()) {
            rvOrderList.gone()
            info.show()
            info.message(getString(R.string.msg_no_data))
        } else {
            info.gone()
            rvOrderList.show()
            orderAdapter.submitList(ordersList)
        }
    }

    private fun showEmptyState(message: String) = binding.apply {
        loading.gone()
        rvOrderList.gone()
        tryAgain.gone()
        info.show()
        info.message(message)
    }

    private fun showErrorState(message: String) = binding.apply {
        loading.gone()
        rvOrderList.gone()
        info.gone()
        tryAgain.show()
        tryAgain.message = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
