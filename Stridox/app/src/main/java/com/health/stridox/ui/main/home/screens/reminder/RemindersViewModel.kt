package com.health.stridox.ui.main.home.screens.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.stridox.alarm.ReminderAlarmController
import com.health.stridox.data.IntakeReminder
import com.health.stridox.data.repo.ReminderRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(
    private val repo: ReminderRepository,
    private val alarmController: ReminderAlarmController
) : ViewModel() {

    private val _toastMsg = MutableSharedFlow<String>()
    val toastMsg = _toastMsg.asSharedFlow()

    val reminders: StateFlow<List<IntakeReminder>> =
        repo.allReminders()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addReminder(title: String, note: String, date: String, time: String, isActive: Boolean) =
        viewModelScope.launch {
            val id = repo.add(
                IntakeReminder(
                    title = title,
                    note = note,
                    date = date,
                    time = time,
                    isActive = isActive
                )
            )

        if (id > 0L) {
            if (isActive) alarmController.schedule(id, title, note, date, time)
            _toastMsg.emit("Reminder added successfully")
        } else {
            _toastMsg.emit("Error adding reminder")
        }
    }

    fun updateReminder(reminder: IntakeReminder) = viewModelScope.launch {
        val rows = repo.update(reminder)
        if (rows > 0) {
            alarmController.cancel(reminder.id)
            if (reminder.isActive) { // Reschedule with new date if active
                alarmController.schedule(
                    reminder.id,
                    reminder.title,
                    reminder.note,
                    reminder.date,
                    reminder.time
                )
            }
            _toastMsg.emit("Reminder updated successfully")
        } else {
            _toastMsg.emit("Error updating reminder")
        }
    }

    fun deleteReminder(reminder: IntakeReminder) = viewModelScope.launch {
        val rows = repo.delete(reminder)
        if (rows > 0) {
            alarmController.cancel(reminder.id)
            _toastMsg.emit("Reminder deleted successfully")
        } else {
            _toastMsg.emit("Error deleting reminder")
        }
    }

    fun toggleActive(reminder: IntakeReminder) = viewModelScope.launch {
        val updated = reminder.copy(isActive = !reminder.isActive)
        repo.update(updated)

        if (updated.isActive) {
            alarmController.schedule(
                updated.id,
                updated.title,
                updated.note,
                updated.date,
                updated.time
            )
        } else {
            alarmController.cancel(updated.id)
        }
    }
}
