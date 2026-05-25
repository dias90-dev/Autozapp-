package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reply_rules")
data class ReplyRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pattern: String,
    val reply: String,
    val isEnabled: Boolean = true,
    val matchType: MatchType = MatchType.EXACT,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MatchType {
    EXACT, CONTAINS, REGEX
}
