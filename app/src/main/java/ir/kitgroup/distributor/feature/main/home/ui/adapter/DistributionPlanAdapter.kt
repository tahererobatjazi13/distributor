package ir.kitgroup.distributor.feature.main.home.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.utils.extensions.gone
import ir.kitgroup.distributor.core.utils.extensions.show
import ir.kitgroup.distributor.databinding.ItemDistributionPlanBinding
import ir.kitgroup.distributor.feature.main.home.model.DistributionPlan


class DistributionPlanAdapter(
    private val onClick: (DistributionPlan) -> Unit = {},
) : ListAdapter<DistributionPlan, DistributionPlanAdapter.OnlineOrderListViewHolder>(
    DistributionPlanDiffCallback()
) {
    inner class OnlineOrderListViewHolder(private val binding: ItemDistributionPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: DistributionPlan) = with(binding) {
            val totalCount = item.CountOrder
            val distributedCount = item.distributedQuantity
            val undistributedCount = item.UndistributedQuantity
            val cancellationsCount = (totalCount - (distributedCount + undistributedCount)).coerceAtLeast(0)

            tvPlanName.text = item.Name
            tvPlanNumber.text = item.NoPlan.toString()
            val unit = root.context.getString(R.string.unit_count)
            tvCountOrder.text = "$totalCount $unit"
            tvDistributedQuantity.text = "$distributedCount $unit"
            tvUnDistributedQuantity.text = "$undistributedCount $unit"
            tvCancellations.text = "$cancellationsCount $unit"

            if (item.DateDisp == "-") {
                clPlanDate.gone()
            } else {
                clPlanDate.show()
                tvPlanDate.text = item.DateDisp
            }

            root.setOnClickListener { onClick(item) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlineOrderListViewHolder {
        val binding =
            ItemDistributionPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OnlineOrderListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnlineOrderListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}


class DistributionPlanDiffCallback : DiffUtil.ItemCallback<DistributionPlan>() {
    override fun areItemsTheSame(oldItem: DistributionPlan, newItem: DistributionPlan) =
        oldItem.PlanGuid == newItem.PlanGuid

    override fun areContentsTheSame(oldItem: DistributionPlan, newItem: DistributionPlan) =
        oldItem == newItem
}
