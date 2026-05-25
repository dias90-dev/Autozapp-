package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplyRuleDao {
    @Query("SELECT * FROM reply_rules ORDER BY timestamp DESC")
    fun getAllRules(): Flow<List<ReplyRule>>

    @Query("SELECT * FROM reply_rules WHERE isEnabled = 1")
    suspend fun getEnabledRules(): List<ReplyRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ReplyRule)

    @Update
    suspend fun updateRule(rule: ReplyRule)

    @Delete
    suspend fun deleteRule(rule: ReplyRule)
}
