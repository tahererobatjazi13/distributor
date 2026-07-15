package ir.kitgroup.distributor.core.utils.extensions

import android.annotation.SuppressLint
import android.view.View
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import ir.huri.jcal.JalaliCalendar
import java.util.concurrent.TimeUnit

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.active() {
    isEnabled = true
}

fun View.deactive() {
    isEnabled = false
}

fun View.setSafeOnClickListener(onClick: (View) -> Unit): Disposable {
    return this.clicks().observeOn(AndroidSchedulers.mainThread())
        .throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            onClick(this)
        }
}

fun Double.clean(): String {
    return if (this % 1 == 0.0) {
        this.toInt().toString()
    } else {
        this.toString()
    }
}

fun String.toEnglishDigits(): String {
    val arabic = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val persian = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    var output = this
    for (i in 0..9) {
        output = output
            .replace(arabic[i], ('0' + i))
            .replace(persian[i], ('0' + i))
    }
    return output
}

@SuppressLint("DefaultLocale")
fun getTodayPersianDate(): String {
    val jalaliDate = JalaliCalendar()
    return String.format(
        "%d/%02d/%02d",
        jalaliDate.year,
        jalaliDate.month,
        jalaliDate.day
    )
}


/*
git add .
git commit -m "change color background for dark mode"
git push -u origin master
git push
*/

/*
http://178.131.164.145:52439/WebService.asmx
*/
