package com.androidyug.marsrover.common

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by IAMONE on 12/28/2015.
 */
object Utils {

    fun stringToDate(dateStr: String?): Date {

        var date = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd")

        if (dateStr == null) return date

        try {

            date = sdf.parse(dateStr)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return date
    }

    fun readableDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(date)
    }
}
