package ir.kitgroup.distributor.core.utils.componenet

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.util.Log
import ir.kitgroup.distributor.core.utils.contract.ClickListener
import ir.kitgroup.distributor.core.utils.extensions.gone
import ir.kitgroup.distributor.core.utils.extensions.hide
import ir.kitgroup.distributor.core.utils.extensions.show
import ir.kitgroup.distributor.databinding.CompRetryBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
class Retry @JvmOverloads constructor(
    private val ctx: Context,
    private val attributeSet: AttributeSet? = null,
    private val defStyleAttr: Int = 0
) : FrameLayout(ctx, attributeSet, defStyleAttr) {

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    private var _binding: CompRetryBinding? = null
    private val binding get() = _binding!!

    private var actionListener: ClickListener? = null

    private var message: String = ""
        set(value) {
            binding.tvMessage.text = value
            field = value
        }

    private var isLoading: Boolean = false
        set(value) {
            loadingConfig(value)
            field = value
        }

    init {
        _binding = CompRetryBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        rxBinding()
    }

    private fun rxBinding() {
        binding.tvAction.setOnClickListener {
            actionListener?.invoke()
        }
    }

    infix fun message(message: String) {
        binding.tvMessage.text = message
    }

    infix fun actionText(text: String) {
        binding.tvAction.text = text
    }

    infix fun hasAction(value: Boolean) {
        if (value) {
            binding.tvAction.show()
        } else {
            binding.tvAction.gone()
        }
    }

    infix fun isLoading(value: Boolean) {
        isLoading = value
    }

    fun setActionListener(listener: ClickListener) {
        this.actionListener = listener
    }

    private fun loadingConfig(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                tvAction.hide()
                dotLoading.show()
                dotLoading.startAnimation()
            } else {
                tvAction.show()
                dotLoading.gone()
                dotLoading.stopAnimation()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable.clear()
    }

    fun clearCompositeDisposable() {
        compositeDisposable.clear()
    }
}
