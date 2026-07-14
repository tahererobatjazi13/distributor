package ir.kitgroup.distributor.core.utils.componenet

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.utils.contract.ClickListener
import ir.kitgroup.distributor.core.utils.extensions.gone
import ir.kitgroup.distributor.core.utils.extensions.setSafeOnClickListener
import ir.kitgroup.distributor.core.utils.extensions.show
import ir.kitgroup.distributor.databinding.CompHeaderBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject


@AndroidEntryPoint
class Header @JvmOverloads constructor(
    private val ctx: Context,
    private val attributeSet: AttributeSet? = null,
    private val defStyleAttr: Int = 0
) : FrameLayout(ctx, attributeSet, defStyleAttr) {

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    private var _binding: CompHeaderBinding? = null
    private val binding get() = _binding!!

    var isShowBadge = false
        set(value) {
            if (value)
                binding.clBadge.show()
            else {
                binding.clBadge.gone()
            }
            field = value
        }
    var textBadge = ""
        set(value) {
            binding.tvBadge.text = value
            field = value
        }
    var isShowImgOne = false
        set(value) {
            if (value) {
                binding.imgOneHeader.show()
                binding.clBadge.gone()
            } else {
                binding.imgOneHeader.gone()
                binding.clBadge.gone()
            }
            field = value
        }
    var isShowImgTwo = false
        set(value) {
            if (value)
                binding.imgTwoHeader.show()
            else {
                binding.imgTwoHeader.gone()
            }
            field = value
        }

    var backgroundColorHeader: Int = 0

    var isShowSeparator = false
        set(value) {
            if (value)
                binding.vSeparator.show()
            else {
                binding.vSeparator.gone()
            }
            field = value
        }


    private var drawableImgOne: Drawable? = null
        set(value) {
            binding.imgOneHeader.setImageDrawable(value)
            field = value
        }

    var drawableImgTwo: Drawable? = null
        set(value) {
            binding.imgTwoHeader.setImageDrawable(value)
            field = value
        }

    var textTitle = ""
        set(value) {
            binding.tvTitleHeader.text = value
            field = value
        }

    var isShowLabel = false
        set(value) {
            if (value)
                binding.tvLabelHeader.show()
            else {
                binding.tvLabelHeader.gone()
            }
            field = value
        }
    var textLabel = ""
        set(value) {
            binding.tvLabelHeader.text = value
            field = value
        }

    private var imgOneColorHeader: Int = 0
    private var imgTwoColorHeader: Int = 0
    private var labelColorHeader: Int = 0

    // Listeners ------------------------------------
    private var imgOneListener: ClickListener? = null
    private var imgTwoListener: ClickListener? = null
    private var imgThreeListener: ClickListener? = null
    private var labelListener: ClickListener? = null
    lateinit var disposableClickImgOne: Disposable

    init {
        _binding = CompHeaderBinding.inflate(LayoutInflater.from(ctx))
        addView(binding.root)

        val attributes = ctx.obtainStyledAttributes(attributeSet, R.styleable.HeaderField)
        isShowBadge = attributes.getBoolean(R.styleable.HeaderField_isShowBadge, false)
        textBadge = attributes.getString(R.styleable.HeaderField_textBadge).toString()
        isShowImgOne = attributes.getBoolean(R.styleable.HeaderField_isShowImgOneHeader, false)
        isShowImgTwo = attributes.getBoolean(R.styleable.HeaderField_isShowImgTwoHeader, false)
        isShowSeparator =
            attributes.getBoolean(R.styleable.HeaderField_isShowSeparatorHeader, false)
        textTitle = attributes.getString(R.styleable.HeaderField_textTitleHeader).toString()
        backgroundColorHeader =
            attributes.getInteger(R.styleable.HeaderField_backgroundColorHeader, 0)
        imgOneColorHeader = attributes.getInteger(R.styleable.HeaderField_imgOneColorHeader, 0)
        imgTwoColorHeader = attributes.getInteger(R.styleable.HeaderField_imgTwoColorHeader, 0)
        labelColorHeader = attributes.getInteger(R.styleable.HeaderField_labelColorHeader, 0)
        drawableImgOne = attributes.getDrawable(R.styleable.HeaderField_drawableImgOneHeader)
        drawableImgTwo = attributes.getDrawable(R.styleable.HeaderField_drawableImgTwoHeader)
        isShowLabel = attributes.getBoolean(R.styleable.HeaderField_isShowLabelHeader, false)
        textLabel = attributes.getString(R.styleable.HeaderField_textLabelHeader).toString()

        attributes.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        rxBinding()

        //set color to image-view
        if (imgOneColorHeader != 0)
            binding.imgOneHeader.setColorFilter(imgOneColorHeader, PorterDuff.Mode.SRC_IN)
        if (imgTwoColorHeader != 0)
            binding.imgTwoHeader.setColorFilter(imgTwoColorHeader, PorterDuff.Mode.SRC_IN)
        if (labelColorHeader != 0)
            binding.tvLabelHeader.setTextColor(labelColorHeader)
    }

    /**
     * This method handle events like click
     */
    private fun rxBinding() {
        //click btnOne
        disposableClickImgOne =
            binding.imgOneHeader.setSafeOnClickListener {
                imgOneListener?.invoke()
            }

        //click btnTwo
        val disposableClickImgTwo =
            binding.imgTwoHeader.setSafeOnClickListener {
                imgTwoListener?.invoke()
            }
        //click btnTwo
        val disposableClickLabel =
            binding.tvLabelHeader.setSafeOnClickListener {
                labelListener?.invoke()
            }
        compositeDisposable.add(disposableClickImgOne)
        compositeDisposable.add(disposableClickImgTwo)
        compositeDisposable.add(disposableClickLabel)
    }

    /**
     * Call back invoked when click on img One
     * listener : ClickListener
     */
    fun setOnClickImgOneListener(listener: ClickListener) {
        this.imgOneListener = listener
    }

    /**
     * Call back invoked when click on img two
     * listener : ClickListener
     */
    fun setOnClickImgTwoListener(listener: ClickListener) {
        this.imgTwoListener = listener
    }

    /**
     * Call back invoked when click on img three
     * listener : ClickListener
     */
    fun setOnClickBtnThreeListener(listener: ClickListener) {
        this.imgThreeListener = listener
    }

    /**
     * Call back invoked when click on label
     * listener : ClickListener
     */
    fun setOnClickLabelListener(listener: ClickListener) {
        this.labelListener = listener
    }

    fun showImageOne(result: Boolean) {
        isShowImgOne = result
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable.clear()
    }

    fun removeImgOneListener() {
        disposableClickImgOne.dispose()
    }

    fun attach() {
        disposableClickImgOne =
            binding.imgOneHeader.setSafeOnClickListener {
                imgOneListener?.invoke()
            }
        compositeDisposable.add(disposableClickImgOne)
    }

    /**
     * clear composite
     */
    fun clearCompositeDisposable() {
        compositeDisposable.clear()
    }
}