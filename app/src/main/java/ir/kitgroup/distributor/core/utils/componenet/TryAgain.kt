package ir.kitgroup.distributor.core.utils.componenet

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.utils.contract.ClickListener
import ir.kitgroup.distributor.databinding.CompTryAgainBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject


@AndroidEntryPoint
class TryAgain @JvmOverloads constructor(
    private val ctx: Context,
    private val attributeSet: AttributeSet? = null,
    private val defStyleAttr: Int = 0
) : FrameLayout(ctx, attributeSet, defStyleAttr) {

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    private var _binding: CompTryAgainBinding? = null
    private val binding get() = _binding!!


    /**
     * message
     */
    var message: String = ""
        set(value) {
            binding.tvMessage.text = value
            field = value
        }

    private var buttonListener: ClickListener? = null

    /**
     *  init
     */
    init {
        _binding = CompTryAgainBinding.inflate(LayoutInflater.from(context))
        addView(binding.root)

        val attributeSet = ctx.obtainStyledAttributes(attributeSet, R.styleable.TryAgain)
        attributeSet.recycle()
    }

    /**
     * Handel event
     */
    private fun rxBinding() {
        //click btnOne
        binding.bmbTryAgain.setOnClickBtnOneListener {
            buttonListener?.invoke()
        }
    }

    /**
     * call back invoked when click on btn
     * listener : ClickListener
     */
    fun setOnClickListener(listener: ClickListener) {
        this.buttonListener = listener
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        rxBinding()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //compositeDisposable.clear()
    }
}