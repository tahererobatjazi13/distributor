package ir.kitgroup.distributor.core.utils.componenet

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.utils.contract.ClickListener
import ir.kitgroup.distributor.core.utils.extensions.active
import ir.kitgroup.distributor.core.utils.extensions.deactive
import ir.kitgroup.distributor.core.utils.extensions.gone
import ir.kitgroup.distributor.core.utils.extensions.setSafeOnClickListener
import ir.kitgroup.distributor.core.utils.extensions.show
import ir.kitgroup.distributor.databinding.CompBottomMainBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
class BottomMain @JvmOverloads
constructor(
    private val ctx: Context,
    private val attributeSet: AttributeSet? = null,
    private val defStyleAttr: Int = 0
) : FrameLayout(ctx, attributeSet, defStyleAttr) {

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    private var _binding: CompBottomMainBinding? = null
    private val binding get() = _binding!!

    private var isShowBtnTwo = false
        set(value) {
            if (value)
                binding.clBtnTwo.show()
            else {
                binding.clBtnTwo.gone()
                val layoutParams = binding.clBtnOne.layoutParams as MarginLayoutParams
                layoutParams.marginStart = 0
                binding.clBtnOne.layoutParams = layoutParams
            }
            field = value
        }

    var textBtnOne = ""
        set(value) {
            binding.btnOne.text = value
            field = value
        }
    var textBtnTwo = ""
        set(value) {
            binding.btnTwo.text = value
            field = value
        }

    var backColorBottom: Int = 0
        set(value) {
            binding.llBottomMain.setBackgroundColor(value)
            field = value
        }

    var backColorBtnTwo: Int = 0
        set(value) {
            binding.btnTwo.setBackgroundColor(value)
            field = value
        }
    var backColorBtnOne: Int = 0
        set(value) {
            binding.btnOne.setBackgroundColor(value)
            field = value
        }
    var badgeTextColorBtnTwo: Int = 0
        set(value) {
            binding.btnTwo.setTextColor(value)
            field = value
        }
    var badgeTextColorBtnOne: Int = 0
        set(value) {
            binding.btnOne.setTextColor(value)
            field = value
        }

    // Listeners ------------------------------------
    private var btnOneListener: ClickListener? = null
    private var btnTwoListener: ClickListener? = null

    init {
        _binding = CompBottomMainBinding.inflate(LayoutInflater.from(ctx))
        addView(binding.root)
        // the array of attributes
        val attributes = ctx.obtainStyledAttributes(attributeSet, R.styleable.BottomMainBtn)
        isShowBtnTwo = attributes.getBoolean(R.styleable.BottomMainBtn_isShowBtnTwoBottom, false)
        textBtnOne = attributes.getString(R.styleable.BottomMainBtn_textBtnOne).toString()
        textBtnTwo = attributes.getString(R.styleable.BottomMainBtn_textBtnTwo).toString()
        backColorBottom = attributes.getInteger(R.styleable.BottomMainBtn_backColorBottom, 0)
        backColorBtnTwo = attributes.getInteger(R.styleable.BottomMainBtn_backColorBtnTwo, 0)
        backColorBtnOne = attributes.getInteger(R.styleable.BottomMainBtn_backColorBtnOne, 0)
        badgeTextColorBtnTwo =
            attributes.getInteger(R.styleable.BottomMainBtn_badgeTextColorBtnTwo, 0)
        badgeTextColorBtnOne =
            attributes.getInteger(R.styleable.BottomMainBtn_badgeTextColorBtnOne, 0)
        attributes.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        rxBinding()
    }

    /**
     * This method handle events like click
     */
    private fun rxBinding() {
        //click btnOne
        val disposableClickBtnOne =
            binding.btnOne.setSafeOnClickListener {
                btnOneListener?.invoke()
            }

        //click btnTwo
        val disposableClickBtnTwo =
            binding.btnTwo.setSafeOnClickListener {
                btnTwoListener?.invoke()
            }

        compositeDisposable.add(disposableClickBtnOne)
        compositeDisposable.add(disposableClickBtnTwo)
    }

    /**
     * Call back invoked when click on btn one
     * listener : ClickListener
     */
    fun setOnClickBtnOneListener(listener: ClickListener) {
        this.btnOneListener = listener
    }

    /**
     * Call back invoked when click on btn two
     * listener : ClickListener
     */
    fun setOnClickBtnTwoListener(listener: ClickListener) {
        this.btnTwoListener = listener
    }

    /**
     * check show or hide progress one
     * @param check:Boolean
     */
    fun checkShowPbOne(check: Boolean) {
        if (check) {
            binding.pbOne.show()
            binding.btnOne.deactive()
            binding.btnOne.text = ""
        } else {
            binding.pbOne.gone()
            binding.btnOne.active()
            binding.btnOne.text = textBtnOne
        }
    }

    /**
     * check show or hide progress two
     * @param check:Boolean
     */
    fun checkShowPbTwo(check: Boolean) {
        if (check) {
            binding.pbTwo.show()
            binding.btnTwo.deactive()
            binding.btnTwo.text = ""
        } else {
            binding.pbTwo.gone()
            binding.btnTwo.active()
            binding.btnTwo.text = textBtnOne
        }
    }

    /**
     * clear composite
     */
    fun clearCompositeDisposable() {
        compositeDisposable.clear()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // compositeDisposable.clear()
    }

}