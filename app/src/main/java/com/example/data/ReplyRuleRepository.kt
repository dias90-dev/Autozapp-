package com.example.data

import kotlinx.coroutines.flow.Flow

class ReplyRuleRepository(private val replyRuleDao: ReplyRuleDao) {
    val allRules: Flow<List<ReplyRule>> = replyRuleDao.getAllRules()

    suspend fun getEnabledRules(): List<ReplyRule> = replyRuleDao.getEnabledRules()

    suspend fun insert(rule: ReplyRule) = replyRuleDao.insertRule(rule)

    suspend fun update(rule: ReplyRule) = replyRuleDao.updateRule(rule)

    suspend fun delete(rule: ReplyRule) = replyRuleDao.deleteRule(rule)
}
