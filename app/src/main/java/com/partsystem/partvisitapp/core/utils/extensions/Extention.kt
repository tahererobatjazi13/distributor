package com.partsystem.partvisitapp.core.utils.extensions

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding4.view.clicks
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.SnackBarType
import com.partsystem.partvisitapp.core.utils.componenet.CustomSnackBar
import com.partsystem.partvisitapp.core.utils.persiancalendar.calendar.PersianCalendar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import ir.huri.jcal.JalaliCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

fun convertPersianDigitsToLatin(persianDate: String): String {
    var latinDate = persianDate
    latinDate = latinDate.replace("۰", "0")
    latinDate = latinDate.replace("۱", "1")
    latinDate = latinDate.replace("۲", "2")
    latinDate = latinDate.replace("۳", "3")
    latinDate = latinDate.replace("۴", "4")
    latinDate = latinDate.replace("۵", "5")
    latinDate = latinDate.replace("۶", "6")
    latinDate = latinDate.replace("۷", "7")
    latinDate = latinDate.replace("۸", "8")
    latinDate = latinDate.replace("۹", "9")
    return latinDate
}

fun getTodayPersianDateLatin(): String {
    return getTodayPersianDate()
        .replace("۰", "0")
        .replace("۱", "1")
        .replace("۲", "2")
        .replace("۳", "3")
        .replace("۴", "4")
        .replace("۵", "5")
        .replace("۶", "6")
        .replace("۷", "7")
        .replace("۸", "8")
        .replace("۹", "9")
}

fun getTodayGregorian(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date())
}

@SuppressLint("DefaultLocale")
fun persianToGregorian(persianDate: String): String {
    val parts = persianDate.split("/")
    val shYear = parts[0].toInt()
    val shMonth = parts[1].toInt()
    val shDay = parts[2].toInt()

    val persianCalendar = PersianCalendar()
    persianCalendar.setPersian(shYear, shMonth, shDay)

    return String.format(
        "%04d-%02d-%02d",
        persianCalendar.get(Calendar.YEAR),
        persianCalendar.get(Calendar.MONTH) + 1,
        persianCalendar.get(Calendar.DAY_OF_MONTH)
    )
}

@SuppressLint("DefaultLocale")
fun gregorianToPersian(gregorianDate: String): String {
    val parts = gregorianDate.split("-")
    val year = parts[0].toInt()
    val month = parts[1].toInt()
    val day = parts[2].toInt()

    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, day)

    val persianCalendar = PersianCalendar(calendar)

    return String.format(
        "%04d/%02d/%02d",
        persianCalendar.year,
        persianCalendar.month + 1,
        persianCalendar.day
    )
}


fun getCurrentTime(): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date())
}

fun Fragment.showSyncRequiredMessage() {
    CustomSnackBar.make(
        requireView(),
        getString(R.string.error_click_sync_button_download_information),
        SnackBarType.Warning.value
    )?.show()
}

/*
git add .
git commit -m "add snackbar isIndefinite for validate credit"
git push -u origin master
git push
*/

/*
http://178.131.164.145:52439/WebService.asmx
*/
