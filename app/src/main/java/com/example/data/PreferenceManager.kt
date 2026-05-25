package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("autozapp_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_PRO = "is_pro"
        private const val KEY_DAILY_REPLY_COUNT = "daily_reply_count"
        private const val KEY_LAST_REPLY_DATE = "last_reply_date"
        
        const val FREE_RULE_LIMIT = 2
        const val FREE_DAILY_LIMIT = 10
    }

    var isPro: Boolean
        get() = prefs.getBoolean(KEY_IS_PRO, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_PRO, value).apply()

    fun canReply(): Boolean {
        if (isPro) return true
        
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        val lastDate = prefs.getLong(KEY_LAST_REPLY_DATE, 0)
        
        var count = if (today == lastDate) {
            prefs.getInt(KEY_DAILY_REPLY_COUNT, 0)
        } else {
            0
        }
        
        return count < FREE_DAILY_LIMIT
    }

    fun incrementReplyCount() {
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        val lastDate = prefs.getLong(KEY_LAST_REPLY_DATE, 0)
        
        var count = if (today == lastDate) {
            prefs.getInt(KEY_DAILY_REPLY_COUNT, 0)
        } else {
            0
        }
        
        prefs.edit()
            .putLong(KEY_LAST_REPLY_DATE, today)
            .putInt(KEY_DAILY_REPLY_COUNT, count + 1)
            .apply()
    }

    fun getDailyRemaining(): Int {
        if (isPro) return Int.MAX_VALUE
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        val lastDate = prefs.getLong(KEY_LAST_REPLY_DATE, 0)
        val count = if (today == lastDate) prefs.getInt(KEY_DAILY_REPLY_COUNT, 0) else 0
        return (FREE_DAILY_LIMIT - count).coerceAtLeast(0)
    }
}
