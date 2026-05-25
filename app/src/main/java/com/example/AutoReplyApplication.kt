package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.PreferenceManager
import com.example.data.ReplyRuleRepository

class AutoReplyApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ReplyRuleRepository(database.replyRuleDao()) }
    val preferenceManager by lazy { PreferenceManager(this) }
}
