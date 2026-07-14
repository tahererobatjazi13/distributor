package ir.kitgroup.distributor.core.utils.componenet

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.util.DisplayMetrics
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import ir.kitgroup.distributor.R


class DotLoading : FrameLayout {
    private var margin: Int = convertDpToPixel(1f, context)
    private var radius: Int = convertDpToPixel(12f, context)
    private var numberOfDots = 3
    private var dotColor = 0
    private val animators = mutableListOf<Animator>()
    private var animationDuration = 700L
    private var minScale = 0.7f
    private var maxScale = 1f
    private var primaryAnimator: ValueAnimator? = null
    private lateinit var dotProgressBar: LinearLayout
    var dotBackground: Drawable? = null
    private var dotAnimator: ValueAnimator? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        init(context, attrs)
    }

    private fun init(ctx: Context, attrs: AttributeSet) {

        val attributes = ctx.obtainStyledAttributes(attrs, R.styleable.DotLoading)
        val count = attributes.getInteger(R.styleable.DotLoading_count, 3)
        val dotMargin = attributes.getInteger(R.styleable.DotLoading_dotMargin, 1)
        val dotSize = attributes.getInteger(R.styleable.DotLoading_dotSize, 30)
        val dotColor = attributes.getInteger(R.styleable.DotLoading_dotColor, 0)
        val dotStyle: Drawable? = attributes.getDrawable(R.styleable.DotLoading_dotDrawable)
        numberOfDots = count
        margin = dotMargin
        dotBackground = dotStyle
        radius = dotSize

        if (dotStyle == null) {
            dotBackground = ContextCompat.getDrawable(context, R.drawable.ic_progress_dot)
        }
        attributes.recycle()


        clipChildren = false
        clipToPadding = false
        dotProgressBar = LinearLayout(context)
        val progressBarLayoutParams =
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        progressBarLayoutParams.gravity = Gravity.CENTER
        dotProgressBar.layoutParams = progressBarLayoutParams
        dotProgressBar.clipChildren = false
        dotProgressBar.clipToPadding = false
        addView(dotProgressBar)
        animators.clear()
        for (i in 0 until numberOfDots) {
            val dot = ImageView(context)
            val layoutParams = LayoutParams(radius * 2, radius * 2)
            layoutParams.setMargins(margin, margin, margin, margin)
            dot.layoutParams = layoutParams
            dot.scaleX = minScale
            dot.scaleY = minScale
            dot.setImageDrawable(dotBackground)
            if (dotColor != 0) {
                dot.setColorFilter(dotColor, PorterDuff.Mode.SRC_IN)
            }
            dotProgressBar.addView(dot)
            val animator = getScaleAnimator(dot)
            animators.add(animator)
        }
        primaryAnimator?.cancel()
        primaryAnimator = ValueAnimator.ofInt(0, numberOfDots)
        primaryAnimator?.addUpdateListener {
            if (it.animatedValue != numberOfDots)
                animators[it.animatedValue as Int].start()
        }
        primaryAnimator?.repeatMode = ValueAnimator.RESTART
        primaryAnimator?.repeatCount = ValueAnimator.INFINITE
        primaryAnimator?.duration = animationDuration
        primaryAnimator?.interpolator = LinearInterpolator()
    }

    private fun getScaleAnimator(view: View): Animator {
        if (dotAnimator != null)
            return dotAnimator as ValueAnimator
        val animator = ValueAnimator.ofFloat(minScale, maxScale)
        animator.addUpdateListener {
            view.scaleX = it.animatedValue as Float
            view.scaleY = it.animatedValue as Float
        }
        animator.duration = animationDuration / numberOfDots.toLong()
        animator.repeatCount = 1
        animator.repeatMode = ValueAnimator.REVERSE
        animator.interpolator = LinearInterpolator()
        return animator
    }

    fun stopAnimation() {
        primaryAnimator?.cancel()
    }

    fun startAnimation() {
        primaryAnimator?.start()
    }

    fun isAnimationRunning(): Boolean {
        return primaryAnimator!!.isRunning
    }

    override fun setVisibility(visibility: Int) {
        if (visibility == View.VISIBLE) startAnimation()
        else stopAnimation()
        super.setVisibility(visibility)
    }

    companion object {
        fun convertDpToPixel(dp: Float, context: Context): Int {
            return (dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        }
    }
}