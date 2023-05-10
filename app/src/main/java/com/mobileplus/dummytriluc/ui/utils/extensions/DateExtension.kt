package com.mobileplus.dummytriluc.ui.utils.extensions

import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


fun Date?.formatDateSendServer(): String {
    return try {
        val output = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        output.format(this)
    } catch (e: Exception) {
        ""
    }
}

fun String.dateInFormat(format: String): Date? {
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    var parsedDate: Date? = null
    try {
        parsedDate = dateFormat.parse(this)
    } catch (ignored: ParseException) {
        ignored.printStackTrace()
    }
    return parsedDate
}

fun Date?.getDateFormatter(pattern:String): String {
    return try {
        val df = SimpleDateFormat(pattern, Locale.getDefault())
        df.format(this)
    } catch (e: Exception) {
        ""
    }
}

fun Date?.getDateDDMMMYYYY(): String {
    return try {
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        df.format(this)
    } catch (e: Exception) {
        ""
    }
}

fun Date?.getDateHHmm(): String {
    return try {
        val df = SimpleDateFormat("HH:mm", Locale.getDefault())
        df.format(this)
    } catch (e: Exception) {
        ""
    }
}

fun Date.convertDateToDetail(): String {
    val currDate = Calendar.getInstance()
    currDate.time = this
    val tmpThu = if (currDate.get(Calendar.DAY_OF_WEEK) == 1) "Chủ nhật" else "Thứ " + currDate.get(
        Calendar.DAY_OF_WEEK)
    return tmpThu + ", Ngày " + currDate.get(Calendar.DAY_OF_MONTH) + "," +
            " Tháng " + (currDate.get(Calendar.MONTH) + 1) + ", Năm " + currDate.get(Calendar.YEAR)
}

fun String.formatDateSendServer(): String {
    val dt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date: Date?
    try {
        date = dt.parse(this)
        val dt1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        return dt1.format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
    } catch (e: NullPointerException) {
        e.printStackTrace()
    }
    return ""
}


fun String.formatToDateSendServer(): String {
    val dt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date: Date?
    try {
        date = dt.parse(this)
        val dt1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dt1.format(date) + "T23:59:59.000Z"
    } catch (e: ParseException) {
        e.printStackTrace()
    } catch (e: NullPointerException) {
        e.printStackTrace()
    }
    return ""
}

fun String.formatDDMMYYToDate(): Date {
    val dt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    try {
        return dt.parse(this)
    } catch (e: ParseException) {
        e.printStackTrace()
    } catch (e: NullPointerException) {
        e.printStackTrace()
    }
    return Date()
}

fun Date?.newFormat(): Date {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.parse(this.getDateDDMMMYYYY())
}

fun Date.compareWithToday(): Int {
    val date = Calendar.getInstance().time.newFormat()
    val curr = this.convertDateToCalendar().time.newFormat()
    return when {
        curr.after(date) -> 1
        curr.before(date) -> -1
        else -> 0
    }
}

fun String.convertDateServerToCalendar(): Calendar {
    val dt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val date: Date?
    try {
        dt.timeZone = TimeZone.getTimeZone("UTC")
        date = dt.parse(this)
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    } catch (e: ParseException) {
        e.printStackTrace()
    } catch (e: NullPointerException) {
        e.printStackTrace()
    }
    return Calendar.getInstance()
}

fun Date?.convertDateToCalendar(): Calendar {
    val calendar = Calendar.getInstance()
//    calendar.timeZone = TimeZone.getTimeZone("UTC")
    calendar.time = this
    return calendar
}

fun String.formatDecimal(): String {
    return NumberFormat.getInstance().format(this.toFloat())
}
