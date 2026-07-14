package ir.kitgroup.distributor.feature.main.home.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.network.NetworkResult
import ir.kitgroup.distributor.core.utils.ApiConfig.TOKEN
import ir.kitgroup.distributor.core.utils.SnackBarType
import ir.kitgroup.distributor.core.utils.componenet.CustomDialog
import ir.kitgroup.distributor.core.utils.componenet.CustomSnackBar
import ir.kitgroup.distributor.core.utils.datastore.MainPreferences
import ir.kitgroup.distributor.core.utils.extensions.getTodayPersianDate
import ir.kitgroup.distributor.core.utils.extensions.gone
import ir.kitgroup.distributor.core.utils.extensions.show
import ir.kitgroup.distributor.databinding.FragmentHomeBinding
import ir.kitgroup.distributor.feature.main.home.ui.adapter.DistributionPlanAdapter
import ir.kitgroup.distributor.feature.splash.SplashActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.view.MotionEvent
import androidx.core.view.ViewCompat
import ir.kitgroup.distributor.feature.main.home.model.DistributionPlan

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var mainPreferences: MainPreferences

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeListViewModel by viewModels()

    private var doubleBackToExit = false
    private var customDialog: CustomDialog? = null
    private lateinit var distributionPlanAdapter: DistributionPlanAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupDialog()
        setupClicks()
        setupSearch()
        setupBackPress()
        setupAdapter()
        setupObservers()
        loadInitialData()
    }

    private fun setupDialog() {
        customDialog?.apply {
            setOnClickNegativeButton { hideProgress() }
            setOnClickPositiveButton { logout() }
        }
    }

    private fun setupUi() = binding.apply {
        customDialog = CustomDialog()
        tvDate.text = getTodayPersianDate()
    }

    private fun setupClicks() = binding.apply {
        ivExit.setOnClickListener {
            customDialog?.showDialog(
                activity,
                "",
                getString(R.string.msg_log_out),
                true,
                getString(R.string.label_close),
                getString(R.string.label_confirm),
                showPositiveButton = true,
                showNegativeButton = true
            )
        }

        tryAgain.setOnClickListener {
            fetchReportFactorVisitorList()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearch() = binding.apply {
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) {
                navigateToOrderByNo(etSearch.text.toString().trim())
                true
            } else {
                false
            }
        }

        etSearch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && isEndDrawableClicked(event)) {
                navigateToOrderByNo(etSearch.text.toString().trim())
                true
            } else {
                false
            }
        }
   }


    private fun isEndDrawableClicked(event: MotionEvent): Boolean {
        val drawableEnd = binding.etSearch.compoundDrawables[2] ?: return false
        val drawableWidth = drawableEnd.bounds.width()
        return event.rawX >= (
                binding.etSearch.right - drawableWidth - binding.etSearch.paddingEnd
                )
    }

    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (doubleBackToExit) {
                requireActivity().finish()
                return@addCallback
            }

            doubleBackToExit = true
            CustomSnackBar.make(
                binding.root,
                getString(R.string.msg_press_back_button_again_exit),
                SnackBarType.Warning.value
            )?.show()

            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExit = false
            }, 2000)
        }
    }


    private fun setupAdapter() {
        distributionPlanAdapter = DistributionPlanAdapter { selectedPlan ->
            val action =
                HomeFragmentDirections.actionHomeFragmentToOrderFragment(selectedPlan.PlanGuid)
            findNavController().navigate(action)
        }

        binding.rvPlans.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = distributionPlanAdapter
        }
    }

    private fun setupObservers() {
        viewModel.distributionPlansList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> showLoadingState()
                is NetworkResult.Success -> showSuccessState(result.data)
                is NetworkResult.Empty -> showEmptyState(result.message)
                is NetworkResult.Error -> showErrorState(result.message)
                else -> Unit
            }
        }
    }

    private fun showEmptyState(message: String) = binding.apply {
        loading.gone()
        rvPlans.gone()
        tryAgain.gone()
        info.show()
        info.message(message)
    }


    private fun showLoadingState() = binding.apply {
        loading.show()
        tryAgain.gone()
        info.gone()
        rvPlans.gone()
    }

    private fun showSuccessState(distributionPlansList: List<DistributionPlan>) = binding.apply {
        loading.gone()
        tryAgain.gone()

        if (distributionPlansList.isEmpty()) {
            rvPlans.gone()
            info.show()
            info.message(getString(R.string.msg_no_data))
        } else {
            info.gone()
            rvPlans.show()
            distributionPlanAdapter.submitList(distributionPlansList)
        }
    }

    private fun showErrorState(message: String) = binding.apply {
        loading.gone()
        rvPlans.gone()
        info.gone()
        tryAgain.show()
        tryAgain.message = message
    }


    private fun loadInitialData() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.tvVisitorName.text = mainPreferences.name.first().orEmpty()
            fetchReportFactorVisitorList()
        }
    }

    private fun fetchReportFactorVisitorList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val prsGuid = mainPreferences.prsGuid.first().orEmpty()
            viewModel.fetchDistributionPlans(prsGuid, TOKEN)
        }
    }

    private fun navigateToOrderByNo(searchText: String) {
        if (searchText.isEmpty()) {
            CustomSnackBar.make(
                requireView(),
                getString(R.string.error_enter_order_number),
                SnackBarType.Warning.value
            )?.show()
            return
        }

        val action = HomeFragmentDirections.actionHomeFragmentToOrderByNoFragment(
            search = searchText
        )
        findNavController().navigate(action)
    }

    private fun logout() {
        viewLifecycleOwner.lifecycleScope.launch {
            mainPreferences.clearUserInfo()
            val intent = Intent(requireContext(), SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
