package com.health.stridox.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.health.stridox.data.IntakeReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeReminderDao {
    @Query("SELECT * FROM intake_reminders ORDER BY date, time, title")
    fun getAllFlow(): Flow<List<IntakeReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: IntakeReminder): Long

    @Update
    suspend fun update(reminder: IntakeReminder): Int

    @Delete
    suspend fun delete(reminder: IntakeReminder): Int

    @Query("SELECT * FROM intake_reminders ORDER BY date, time, title")
    suspend fun getAll(): List<IntakeReminder>
}
