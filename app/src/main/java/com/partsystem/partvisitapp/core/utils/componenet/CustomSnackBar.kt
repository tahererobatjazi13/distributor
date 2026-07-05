package com.partsystem.partvisitapp.core.utils.componenet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.getColorAttr

class CustomSnackBar(
    parent: ViewGroup,
    content: CustomSnackBarView
) : BaseTransientBottomBar<CustomSnackBar>(parent, content, content) {
    init {
        animationMode = ANIMATION_MODE_SLIDE
        getView().setBackgroundColor(
            ContextCompat.getColor(
                view.context,
                android.R.color.transparent
            )
        )
        getView().setPadding(0, 0, 0, 0)
    }

    companion object {

        fun make(
            view: View,
            message: String,
            snackBarType: String,
            dimen: Int? = 0,
            isIndefinite: Boolean = false
        ): CustomSnackBar? {
            val h = view
            h.elevation = 1000F

            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )

            try {
                val customView = LayoutInflater.from(view.context).inflate(
                    R.layout.comp_snackbar_layout,
                    parent,
                    false
                ) as CustomSnackBarView

                customView.tvMessage.text = message

                if (dimen != 0) {
                    customView.clRoot.setPadding(0, 0, 0, dimen!!)
                } else {
                    val tDimen = view.context.resources
                        .getDimension(R.dimen.xxx_small_size)
                        .toInt()
                    customView.clRoot.setPadding(0, 0, 0, tDimen)
                }

                val backColor: Int
                val duration: Int

                when (snackBarType) {
                    SnackBarType.Error.value -> {
                        customView.ivIcon.setImageResource(R.drawable.ic_warning)
                        backColor = getColorAttr(view.context, R.attr.colorError)
                        customView.ivBackground.setColorFilter(backColor)

                        duration = if (isIndefinite) {
                            Snackbar.LENGTH_INDEFINITE
                        } else {
                            Snackbar.LENGTH_SHORT
                        }
                    }


                    SnackBarType.Success.value -> {
                        customView.ivIcon.setImageResource(R.drawable.ic_check)
                        backColor = getColorAttr(view.context, R.attr.colorSuccess)
                        customView.ivBackground.setColorFilter(backColor)

                        duration = Snackbar.LENGTH_SHORT
                    }

                    SnackBarType.Warning.value -> {
                        customView.ivIcon.setImageResource(R.drawable.ic_warning)
                        backColor = getColorAttr(view.context, R.attr.colorWarning)
                        customView.ivBackground.setColorFilter(backColor)

                        duration = Snackbar.LENGTH_SHORT
                    }

                    else -> {
                        backColor = getColorAttr(view.context, R.attr.colorError)
                        customView.ivBackground.setColorFilter(backColor)

                        duration = Snackbar.LENGTH_SHORT
                    }
                }

                val snackBar = CustomSnackBar(parent, customView).setDuration(duration)

                customView.ivClose.setOnClickListener {
                    snackBar.dismiss()
                }

                return snackBar


            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        private fun View?.findSuitableParent(): ViewGroup? {
            var view = this
            var fallback: ViewGroup? = null
            do {
                if (view is CoordinatorLayout) {
                    return view
                } else if (view is FrameLayout) {
                    if (view.id == android.R.id.content) {
                        return view
                    } else {
                        fallback = view
                    }
                }
                if (view != null) {
                    val parent = view.parent
                    view = if (parent is View) parent else null
                }
            } while (view != null)
            return fallback
        }
    }
}


