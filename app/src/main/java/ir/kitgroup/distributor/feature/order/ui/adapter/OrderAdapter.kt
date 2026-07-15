package ir.kitgroup.distributor.feature.order.ui.adapter

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.utils.extensions.gone
import ir.kitgroup.distributor.core.utils.extensions.show
import ir.kitgroup.distributor.databinding.ItemOrderDistributionBinding
import ir.kitgroup.distributor.feature.order.model.OrderDistribution
import java.text.DecimalFormat

class OrderAdapter(
    private val onDeliveryConfirmationClick: (OrderDistribution) -> Unit = {}
) : ListAdapter<OrderDistribution, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    private val formatter = DecimalFormat("#,###")

    inner class OrderViewHolder(
        private val binding: ItemOrderDistributionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: OrderDistribution) = with(binding) {
            tvAccountName.text = item.accountName
            tvOrderNumber.text = item.noOrder
            tvAccountAddress.text = item.address
            tvAccountMobile.text = item.mobile
            tvOrderDate.text = item.ordDate
            if (item.distributorDeliveryStatus == true) {

                tvDistributorDeliveryStatus.text = root.context.getString(R.string.label_yes)
                bmbDeliveryConfirmation.show()
                tvDistributorDeliveryStatus.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        root.context,
                        R.color.green_21BF73
                    )
                )
            } else {
                root.strokeWidth = 2
                root.strokeColor =
                    androidx.core.content.ContextCompat.getColor(root.context, R.color.colorError)

                tvDistributorDeliveryStatus.text = root.context.getString(R.string.label_no)

                bmbDeliveryConfirmation.gone()
                tvDistributorDeliveryStatus.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        root.context,
                        R.color.colorError
                    )
                )
            }

            if (item.descOther == "-" || item.descOther == "") {
                clDescriptionOrder.gone()
            } else {
                clDescriptionOrder.show()
                tvDescriptionOrder.text = item.descOther
            }

            if (item.settlementTypeName == "-") {
                clSettlementTypeName.gone()
            } else {
                clSettlementTypeName.show()
                tvSettlementTypeName.text = item.settlementTypeName
            }
            val netPrice = item.sumNetPrice ?: 0.0
            tvSumNetPrice.text = "${formatter.format(netPrice)} ریال"

            bmbDeliveryConfirmation.setOnClickBtnOneListener {
                onDeliveryConfirmationClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderDistributionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<OrderDistribution>() {
    override fun areItemsTheSame(oldItem: OrderDistribution, newItem: OrderDistribution) =
        oldItem.hdrGid == newItem.hdrGid

    override fun areContentsTheSame(oldItem: OrderDistribution, newItem: OrderDistribution) =
        oldItem == newItem
}
