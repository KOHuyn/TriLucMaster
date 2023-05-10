package com.mobileplus.dummytriluc.ui.utils

import android.text.format.DateUtils
import com.mobileplus.dummytriluc.ui.utils.extensions.getDateDDMMMYYYY
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object DateTimeUtil {

    const val DATE_SERVER = "yyyy-MM-dd"
    const val DATE_SERVER_UTC = "yyyy-MM-dd HH:mm:ss"
    const val DATE_CALENDAR = "dd/MM/yyyy"

    fun convertCurrentDate(): String {
        val dt = SimpleDateFormat(DATE_CALENDAR, Locale.getDefault())
        return dt.format(Calendar.getInstance().time)
    }

    fun convertCurrentDateToDetail(): String {
        val currDate = Calendar.getInstance()
        val tmpThu =
            if (currDate.get(Calendar.DAY_OF_WEEK) == 1) "Chủ nhật" else "Thứ " + currDate.get(
                Calendar.DAY_OF_WEEK
            )
        return tmpThu + ", Ngày " + currDate.get(Calendar.DAY_OF_MONTH) + "," +
                " Tháng " + (currDate.get(Calendar.MONTH) + 1) + ", Năm " + currDate.get(Calendar.YEAR)
    }

    fun getTimeHHMM(date: Date): String {
        val df = SimpleDateFormat("HH:mm", Locale.getDefault())
        return df.format(date)
    }

    fun getDateYYYYMMDD(date: Date): String {
        val df = SimpleDateFormat(DATE_SERVER, Locale.getDefault())
        return df.format(date)
    }

    fun convertDate(date_s: String, oldFormat: String, newFormat: String): String {
        val dt = SimpleDateFormat(oldFormat, Locale.getDefault())
        val date: Date?
        try {
//            dt.timeZone = TimeZone.getTimeZone("UTC")
            date = dt.parse(date_s)
            val dt1 = SimpleDateFormat(newFormat, Locale.getDefault())
            return dt1.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return ""
    }

    fun convertDate(date_s: String): Calendar {
        val dt = SimpleDateFormat(DATE_CALENDAR, Locale.getDefault())
        val date: Date?
        try {
            date = dt.parse(date_s)
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

    fun convertDate(date_s: String,pattern:String): Calendar {
        val dt = SimpleDateFormat(pattern, Locale.getDefault())
        val date: Date?
        try {
            date = dt.parse(date_s)
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

    fun convertDateServerToDateClient(date: String): String {
        return convertDate(date, DATE_SERVER, DATE_CALENDAR)
    }

    fun convertDateToLong(dateS: String): String {
        val dt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date: Date?
        try {
            date = dt.parse(dateS)
            return "${date!!.time / 1000}"
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return "0"
    }


    fun convertTimeStampToHHMM(time: Long?): String = if (time != null) {
        val dt = SimpleDateFormat("HH:mm", Locale.getDefault())
        dt.format(Date(time * 1000))
    } else "0"

    fun convertTimeStampToMMSS(time: Long?): String = if (time != null) {
        val dt = SimpleDateFormat("mm:ss", Locale.getDefault())
        dt.format(Date(time * 1000))
    } else "0"

    fun convertTimeStampToDate(time: Long?): String = if (time != null) {
        val dt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dt.format(Date(time * 1000))
    } else {
        ""
    }

    fun convertTimeStampToDate(time: Long?, pattern: String = "dd/MM/yyyy"): String =
        if (time != null) {
            val dt = SimpleDateFormat(pattern, Locale.getDefault())
            dt.format(Date(time * 1000))
        } else {
            ""
        }

    fun convertTimeStampToDateDot(time: Long?): String = if (time != null) {
        val dt = SimpleDateFormat("MMM dd. yyyy", Locale.getDefault())
        dt.format(Date(time * 1000))
    } else {
        ""
    }

    fun convertTimeStampToDateEEE(time: Long?): String = if (time != null) {
        val dt = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
        dt.format(Date(time * 1000))
    } else {
        ""
    }

    fun convertTimeStampToDateTime(time: Long): String {
        val dt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dt.format(Date(time * 1000))
    }

    fun convertTimeStampToDateTimeFull(time: Long?): String {
        if (time == null) return "_"
        val dt = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
        return dt.format(Date(time * 1000))
    }

    fun convertTimeStampToDateTimeFullRes(time: Long?): String {
        if (time == null) return "_"
        val dt = SimpleDateFormat("hh:mm:ss a dd/MM/yyyy", Locale.getDefault())
        return dt.format(Date(time * 1000))
    }


    fun formatDateNotification(): String {
        val c = Calendar.getInstance()
        val DATE_PATTERN = "HH:mm"
        return SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(c.time)
    }

    fun formatDateDDMM(date: Date): String {
        return SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
    }

    fun convertDateToTimeStampMin(date: String): Long {
        val minDate = "$date 00:00:00"
        val dt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val r: Date?
        try {
            r = dt.parse(minDate)
            return (r.time / 1000)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return 0
    }

    fun convertDateToTimeStampMax(date: String): Long {
        val minDate = "$date 23:59:59"
        val dt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val r: Date?
        try {
            r = dt.parse(minDate)
            return (r.time / 1000)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return 0
    }

    fun returnDDMMinReportHistory(dateStart: Date, dateEnd: Date): String {
        val calStart = Calendar.getInstance()
        calStart.time = dateStart
        val calEnd = Calendar.getInstance()
        calEnd.time = dateEnd
        return if (calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR)) {
            formatDateDDMM(dateStart)
        } else dateStart.getDateDDMMMYYYY()
    }

    fun convertTimeStampToString(time: Long): String {
//        val date = Date(time * 1000)
//
//        val today = Calendar.getInstance()
//        val yesterday = Calendar.getInstance() // today
//        yesterday.add(Calendar.DAY_OF_YEAR, -1) // yesterday
//
//        val currDate = Calendar.getInstance()
//        currDate.time = date
//
//        return when {
//            yesterday.get(Calendar.YEAR) == currDate.get(Calendar.YEAR)
//                    && yesterday.get(Calendar.DAY_OF_YEAR) == currDate.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
//            today.get(Calendar.YEAR) == currDate.get(Calendar.YEAR)
//                    && today.get(Calendar.DAY_OF_YEAR) == currDate.get(Calendar.DAY_OF_YEAR) -> {
//                val dt = SimpleDateFormat("hh:mm a", Locale.getDefault())
//                dt.format(Date(time * 1000))
//            }
//            else -> {
//                val dt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//                dt.format(Date(time * 1000))
//            }
//        }
        val date = Date(time * 1000)
        val currDate = Calendar.getInstance()
        val endWeek = Calendar.getInstance()
        val startWeek = Calendar.getInstance()

        endWeek.add(Calendar.DAY_OF_MONTH, 8 - currDate.get(Calendar.DAY_OF_WEEK))

        val d = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val endWeekString = d.format(endWeek.time)

        val endWeekLong = convertDateToTimeStamp(endWeekString)
        startWeek.time = Date((endWeekLong - 86400 + 1) * 1000)
        startWeek.add(Calendar.DAY_OF_MONTH, -8)

        return when {
            DateUtils.isToday(time * 1000) -> {
                val dt = SimpleDateFormat("hh:mm a", Locale.getDefault())
                dt.format(date)
            }
            time > startWeek.time.time / 1000 -> {
                val dt = SimpleDateFormat("EEE hh:mm a", Locale.getDefault())
                dt.format(date)
            }
            else -> {
                val dt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dt.format(date)
            }
        }
    }

    fun convertTimeStampToStringMessage(time: Long): String {
        val date = Date(time * 1000)
        val currDate = Calendar.getInstance()
        val endWeek = Calendar.getInstance()
        val startWeek = Calendar.getInstance()

        endWeek.add(Calendar.DAY_OF_MONTH, 8 - currDate.get(Calendar.DAY_OF_WEEK))

        val d = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val endWeekString = d.format(endWeek.time)

        val endWeekLong = convertDateToTimeStamp(endWeekString)
        startWeek.time = Date((endWeekLong - 86400 + 1) * 1000)
        startWeek.add(Calendar.DAY_OF_MONTH, -8)

        return when {
            DateUtils.isToday(time * 1000) -> {
                val dt = SimpleDateFormat("hh:mm a", Locale.getDefault())
                dt.format(date)
            }
            time > startWeek.time.time / 1000 -> {
                val dt = SimpleDateFormat("EEE hh:mm a", Locale.getDefault())
                dt.format(date)
            }
            else -> {
                val dt = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                dt.format(date)
            }
        }
    }

    fun convertDateTimeToday(): String {
        val date = Calendar.getInstance().time
        val df = SimpleDateFormat("EEE. MMM dd", Locale.ENGLISH)
        return df.format(date)
    }

    fun convertDateTimeYear(): String {
        val date = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy", Locale.ENGLISH)
        return df.format(date)
    }

    fun convertDateWithNoUTC(date_s: String, oldFormat: String, newFormat: String): String {
        val dt = SimpleDateFormat(oldFormat, Locale.getDefault())
        val date: Date?
        try {
            dt.timeZone = TimeZone.getTimeZone("UTC")
            date = dt.parse(date_s)
            val dt1 = SimpleDateFormat(newFormat, Locale.getDefault())
            return dt1.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return ""
    }

    fun convertDateWithOtherFormat(date_s: String, oldFormat: String, newFormat: String): String {
        val dt = SimpleDateFormat(oldFormat, Locale.getDefault())
        val date: Date?
        try {
//            dt.timeZone = TimeZone.getTimeZone("UTC")
            date = dt.parse(date_s)
            val dt1 = SimpleDateFormat(newFormat, Locale.getDefault())
            return dt1.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return ""
    }

    fun convertDateToTimeStamp(date: String): Long {
        val minDate = "$date 23:59:59"
        val dt = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val r: Date?
        try {
            r = dt.parse(minDate)
            return (r.time / 1000)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return 0
    }

    fun convertDateToTimeStampServer(date: String): Long {
        val dt = SimpleDateFormat(DATE_SERVER_UTC, Locale.getDefault())
        val r: Date?
        try {
            r = dt.parse(date)
            return (r.time / 1000)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return 0
    }

    fun convertDateToTimeStampServer(date: String, format: String): Long {
        val dt = SimpleDateFormat(format, Locale.getDefault())
        val r: Date?
        try {
            r = dt.parse(date)
            return (r.time / 1000)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return 0
    }
}