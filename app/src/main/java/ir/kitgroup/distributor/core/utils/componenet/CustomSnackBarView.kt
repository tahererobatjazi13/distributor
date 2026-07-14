package ir.kitgroup.distributor.core.utils.componenet

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.ContentViewCallback
import ir.kitgroup.distributor.R
import ir.kitgroup.distributor.core.utils.getTypefaceRegular

class CustomSnackBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ContentViewCallback {

    var tvMessage: TextView
    var ivIcon: ImageView
    var ivBackground: ImageView
    var clMain: ConstraintLayout
    var clRoot: ConstraintLayout

    init {
        View.inflate(context, R.layout.comp_snackbar_view, this)
        clipToPadding = false
        this.tvMessage = findViewById(R.id.tvMessage)
        this.ivIcon = findViewById(R.id.ivIcon)
        this.ivBackground = findViewById(R.id.ivBackground)
        this.clMain = findViewById(R.id.clMain)
        this.clRoot = findViewById(R.id.clRoot)
        this.tvMessage.typeface = getTypefaceRegular(context)
    }

    override fun animateContentIn(delay: Int, duration: Int) {
    }

    override fun animateContentOut(delay: Int, duration: Int) {
    }
}


