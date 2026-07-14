package ir.kitgroup.distributor.core.utils

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import ir.kitgroup.distributor.R
import retrofit2.HttpException
import java.io.IOException


fun getTypefaceRegular(context: Context): Typeface {
    return ResourcesCompat.getFont(context, R.font.iran_sans)!!
}

fun getColorAttr(ctx: Context, attrId: Int): Int {
    val typedValue = TypedValue()
    ctx.theme.resolveAttribute(attrId, typedValue, true)
    return ContextCompat.getColor(ctx, typedValue.resourceId)
}


fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = activity.currentFocus
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

class ReceiptTopEdgeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val redPaint = Paint().apply {
        color = getColorAttr(context, R.attr.colorPassive)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = 20f
        val gap = 10f
        var startX = 0f

        while (startX < width) {
            canvas.drawCircle(startX + radius, 0f, radius, redPaint)
            startX += 2 * radius + gap
        }

    }
}

object ErrorHandler {
    fun getHttpErrorMessage(context: Context, code: Int, message: String?): String {
        return when (code) {
            400 -> context.getString(R.string.error_bad_request)
            401 -> context.getString(R.string.error_unauthorized)
            403 -> context.getString(R.string.error_forbidden)
            404 -> context.getString(R.string.error_not_found)
            500 -> context.getString(R.string.error_internal_server)
            else -> getErrorMessage(context, message)
        }
    }

    private fun getErrorMessage(context: Context, message: String?): String {
        return when {
            message.isNullOrBlank() -> context.getString(R.string.error_unknown)
            message.contains("timeout", ignoreCase = true) ->
                context.getString(R.string.error_timeout)

            message.contains("Unable to resolve host", ignoreCase = true) ->
                context.getString(R.string.error_network_internet)

            else -> message
        }
    }

    fun getExceptionMessage(context: Context, throwable: Throwable): String {
        return when (throwable) {
            is IOException -> context.getString(R.string.error_network_internet)
            is HttpException -> getErrorMessage(context, throwable.message())
            else -> context.getString(R.string.error_unknown)
        }
    }
}

fun fixPersianChars(input: String): String {
    return input
        .replace('ي', 'ی') // Arabic yeh to Persian yeh
        .replace('ك', 'ک') // Arabic kaf to Persian kaf
}

fun convertNumbersToEnglish(input: String): String {
    val arabicNumbers = listOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val persianNumbers = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val englishNumbers = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    var result = input
    for (i in 0..9) {
        result = result.replace(persianNumbers[i], englishNumbers[i])
        result = result.replace(arabicNumbers[i], englishNumbers[i])
    }
    return result
}
