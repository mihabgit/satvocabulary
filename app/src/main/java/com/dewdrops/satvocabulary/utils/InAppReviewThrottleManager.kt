package com.dewdrops.satvocabulary.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object InAppReviewThrottleManager {
    private const val PREF_NAME = "sat_review_prefs"
    private const val KEY_LAST_REQUEST_TIME = "last_request_time"
    private const val KEY_CONSECUTIVE_DAYS = "consec_days"
    private const val KEY_LAST_OPEN_DATE = "last_open_date"

    fun checkAndShowReview(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            val lastRequestTime = prefs.getLong(KEY_LAST_REQUEST_TIME, 0L)

            // Cooldown rule: Do not show if asked within the last 14 days (Google best practice)
            val cooldownMillis = TimeUnit.DAYS.toMillis(14)
            if (now - lastRequestTime < cooldownMillis) {
                return
            }

            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val lastOpenDate = prefs.getString(KEY_LAST_OPEN_DATE, "")

            if (lastOpenDate != today) {
                val consecutiveDays = prefs.getInt(KEY_CONSECUTIVE_DAYS, 0) + 1
                prefs.edit()
                    .putString(KEY_LAST_OPEN_DATE, today)
                    .putInt(KEY_CONSECUTIVE_DAYS, consecutiveDays)
                    .apply()

                // Trigger after 3 days of app usage / engagement
                if (consecutiveDays >= 3) {
                    prefs.edit().putLong(KEY_LAST_REQUEST_TIME, now).apply()
                    InAppReviewHelper.requestReview(context)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
