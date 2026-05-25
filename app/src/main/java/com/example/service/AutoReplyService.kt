package com.example.service

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.AutoReplyApplication
import com.example.data.MatchType
import com.example.data.ReplyRule
import com.example.data.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AutoReplyService : NotificationListenerService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") return

        val notification = sbn.notification
        val extras = notification.extras
        val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""

        if (message.isEmpty()) return

        Log.d("AutoReplyService", "Received message from $sender: $message")

        serviceScope.launch {
            val app = application as AutoReplyApplication
            val repository = app.repository
            val prefManager = app.preferenceManager
            
            if (!prefManager.canReply()) {
                Log.w("AutoReplyService", "Daily limit reached for free user")
                return@launch
            }

            val rules = repository.getEnabledRules()
            
            for (rule in rules) {
                if (isMatch(message, rule)) {
                    Log.d("AutoReplyService", "Match found for rule: ${rule.pattern}")
                    reply(notification, rule.reply)
                    prefManager.incrementReplyCount()
                    break
                }
            }
        }
    }

    private fun isMatch(message: String, rule: ReplyRule): Boolean {
        val lowerMessage = message.lowercase()
        val lowerPattern = rule.pattern.lowercase()
        
        return when (rule.matchType) {
            MatchType.EXACT -> lowerMessage == lowerPattern
            MatchType.CONTAINS -> lowerMessage.contains(lowerPattern)
            MatchType.REGEX -> {
                try {
                    Regex(rule.pattern, RegexOption.IGNORE_CASE).containsMatchIn(message)
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

    private fun reply(notification: Notification, replyText: String) {
        val actions = notification.actions
        if (actions == null) {
            Log.e("AutoReplyService", "No actions found in notification")
            return
        }

        for (action in actions) {
            val remoteInputs = action.remoteInputs
            if (remoteInputs != null) {
                for (remoteInput in remoteInputs) {
                    if (remoteInput.allowFreeFormInput) {
                        try {
                            val intent = Intent()
                            val bundle = Bundle()
                            bundle.putCharSequence(remoteInput.resultKey, replyText)
                            RemoteInput.addResultsToIntent(remoteInputs, intent, bundle)
                            action.actionIntent.send(this, 0, intent)
                            Log.i("AutoReplyService", "Replied successfully with: $replyText")
                            return
                        } catch (e: Exception) {
                            Log.e("AutoReplyService", "Error sending reply", e)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
