package com.health.stridox.data.repo

import com.health.stridox.data.IntakeReminder
import com.health.stridox.data.dao.IntakeReminderDao
import kotlinx.coroutines.flow.Flow

class ReminderRepositoryImpl(private val dao: IntakeReminderDao) : ReminderRepository {
    override fun allReminders(): Flow<List<IntakeReminder>> = dao.getAllFlow()
    override suspend fun add(reminder: IntakeReminder) = dao.insert(reminder)
    override suspend fun update(reminder: IntakeReminder) = dao.update(reminder)
    override suspend fun delete(reminder: IntakeReminder) = dao.delete(reminder)
    override suspend fun getAllReminders(): List<IntakeReminder> = dao.getAll()
}

interface ReminderRepository {
    fun allReminders(): Flow<List<IntakeReminder>>
    suspend fun add(reminder: IntakeReminder): Long
    suspend fun update(reminder: IntakeReminder): Int
    suspend fun delete(reminder: IntakeReminder): Int
    suspend fun getAllReminders(): List<IntakeReminder>
}