package com.partsystem.partvisitapp.feature.report_factor.online.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.partsystem.partvisitapp.feature.report_factor.online.model.ReportFactorDto
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.ItemOrderListBinding
import java.text.DecimalFormat

class OnlineOrderListAdapter(
    private val showSyncButton: Boolean = false,
    private val onClick: (ReportFactorDto) -> Unit = {},
) : ListAdapter<ReportFactorDto, OnlineOrderListAdapter.OnlineOrderListViewHolder>(
    OnlineOrderListDiffCallback()
) {

    private val formatter = DecimalFormat("#,###")

    inner class OnlineOrderListViewHolder(private val binding: ItemOrderListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: ReportFactorDto) = with(binding) {
          // tvOrderNumber.text = "${bindingAdapterPosition + 1}_  ${item.id}"

            tvOrderNumber.text = item.code.toString()
            tvCustomerName.text = item.customerName

            if (!item.directionName.isNullOrEmpty()) {
                binding.clCustomerDirection.show()
                binding.tvCustomerDirection.text = item.directionName
            } else {
                binding.clCustomerDirection.gone()
            }
            tvPatternName.text = item.patternName
            tvDateTime.text = item.persianDate + " _ " + item.createTime
            tvFinalPrice.text = formatter.format(item.finalPrice) + " ریال"
            root.setOnClickListener { onClick(item) }
            ivDelete.gone()
            cbSabt.gone()

            if (showSyncButton) {
                tvSyncOrder.show()
            } else {
                tvSyncOrder.gone()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlineOrderListViewHolder {
        val binding =
            ItemOrderListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OnlineOrderListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnlineOrderListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<ReportFactorDto>) {
        submitList(data)
    }
}

class OnlineOrderListDiffCallback : DiffUtil.ItemCallback<ReportFactorDto>() {
    override fun areItemsTheSame(oldItem: ReportFactorDto, newItem: ReportFactorDto) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ReportFactorDto, newItem: ReportFactorDto) =
        oldItem == newItem
}
