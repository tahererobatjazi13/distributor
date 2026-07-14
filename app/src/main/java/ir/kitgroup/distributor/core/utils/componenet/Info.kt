package ir.kitgroup.distributor.core.utils.componenet

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.utils.getColorAttr
import ir.kitgroup.distributor.databinding.CompInfoBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Info @JvmOverloads constructor(
    ctx: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ctx, attributeSet, defStyleAttr) {

    private var _binding: CompInfoBinding? = null
    private val binding get() = _binding!!


    private var message: String = ""
        set(value) {
            binding.tvMessage.text = value
            field = value
        }

    private var iconColor: Int = 0
    private var iconDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_not_found)
        set(value) {
            binding.ivIcon.setImageDrawable(value)
            field = value
        }

    /**
     * @param src:Int - example: R.drawable.name
     */
    infix fun icon(src: Int) {
        val drawable = ContextCompat.getDrawable(context, src)
        binding.ivIcon.setImageDrawable(drawable)
    }

    /**
     * @param color:Int example: R.attr.colorName
     */
    infix fun iconColor(color: Int) {
        val iconColor = getColorAttr(context, color)
        binding.ivIcon.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
    }

    /**
     * @param message:String
     */
    infix fun message(message: String) {
        binding.tvMessage.text = message
    }

    init {
        _binding = CompInfoBinding.inflate(LayoutInflater.from(context))
        addView(binding.root)

        val attributes = ctx.obtainStyledAttributes(attributeSet, R.styleable.Info)
        message = attributes.getString(R.styleable.Info_messageInfo).toString()
        iconColor = attributes.getInteger(R.styleable.Info_iconColor, 0)
        iconDrawable = attributes.getDrawable(R.styleable.Info_iconDrawable)

        attributes.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (iconColor != 0)
            binding.ivIcon.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)

        icon(R.drawable.ic_not_found)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}