@file:OptIn(ExperimentalMaterial3Api::class)

package com.health.stridox.ui.main.home.screens.reminder

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.isPm
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.health.stridox.R
import com.health.stridox.data.IntakeReminder
import com.health.stridox.ui.main.LocalBluetoothActions
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun RemindersScreen(navBack: () -> Unit, viewModel: RemindersViewModel = koinViewModel()) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editing: IntakeReminder? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.toastMsg.collect {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = navBack) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.back_content_description)
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.reminders_title), fontWeight = FontWeight.Bold)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showDialog = true
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.add_24px),
                    contentDescription = stringResource(R.string.add_content_description)
                )
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.no_reminders_text),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp)
            ) {
                items(reminders, key = { it.id }) { r ->
                    ReminderCard(
                        modifier = Modifier.animateItem(),
                        reminder = r,
                        onToggle = { viewModel.toggleActive(r) },
                        onEdit = {
                            editing = r
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteReminder(r) }
                    )
                }
            }
        }
        if (showDialog) {
            AddEditReminderDialog(
                initial = editing,
                onDismiss = { showDialog = false },
                onSave = { title, note, date, time, active ->
                    if (editing != null) {
                        viewModel.updateReminder(
                            editing!!.copy(
                                title = title,
                                note = note,
                                date = date,
                                time = time,
                                isActive = active
                            )
                        )
                    } else {
                        viewModel.addReminder(title, note, date, time, active)
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun ReminderCard(
    modifier: Modifier = Modifier,
    reminder: IntakeReminder,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reminder.title, fontWeight = FontWeight.Bold)
                if (reminder.note.isNotEmpty()) {
                    Text(
                        reminder.note,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    reminder.date, // Display the date
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    convert24StringTo12(reminder.time),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = reminder.isActive, onCheckedChange = { onToggle() })
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onEdit) {
                    Icon(
                        painterResource(R.drawable.edit_calendar),
                        contentDescription = stringResource(R.string.edit_desc),
                        modifier = Modifier
                    )
                }
                IconButton(onDelete) {
                    Icon(
                        painterResource(R.drawable.delete),
                        contentDescription = stringResource(R.string.delete_desc),
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

fun convert24StringTo12(input: String): String {
    val parts = input.split(":")
    val hour24 = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val isPM = hour24 >= 12

    val hour12 = when {
        hour24 == 0 -> 12
        hour24 == 12 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }

    val mm = minute.toString().padStart(2, '0')   // fix extra 0 issue

    return "$hour12:$mm ${if (isPM) "PM" else "AM"}"
}

fun convertTo24Hour(hour: Int, minute: Int, isPM: Boolean): String {
    var h24 = hour

    if (isPM && hour < 12) {
        h24 += 12
    } else if (!isPM && hour == 12) {
        h24 = 0
    }

    val hh = h24.toString().padStart(2, '0')
    val mm = minute.toString().padStart(2, '0')
    return "$hh:$mm"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogM3(
    initialTime: String,
    is24HourUI: Boolean = false, // show 12-hour clock to user
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    // parse "HH:mm" safely
    val parts = initialTime.split(":").mapNotNull { it.toIntOrNull() }
    val initHour24 = parts.firstOrNull() ?: 8
    val initMinute = parts.lastOrNull() ?: 0

    val state = rememberTimePickerState(
        initialHour = initHour24,
        initialMinute = initMinute,
        is24Hour = is24HourUI
    )

    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pick_time_title), fontWeight = FontWeight.Medium) },
        confirmButton = {
            TextButton(onClick = {
                val selected24 = convertTo24Hour(
                    hour = state.hour,
                    minute = state.minute,
                    isPM = state.isPm
                )
                onTimeSelected(selected24)
            }) {
                Text(stringResource(R.string.ok_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) }
        },
        content = {
            TimePicker(state = state)
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogM3(
    initialDate: String, // "YYYY-MM-DD"
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val initialLocalDate = try {
        LocalDate.parse(initialDate, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        LocalDate.now()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialLocalDate.toEpochDay() * 24 * 60 * 60 * 1000 // Convert to millis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedDateMillis = datePickerState.selectedDateMillis
                if (selectedDateMillis != null) {
                    val selectedLocalDate =
                        LocalDate.ofEpochDay(selectedDateMillis / (24 * 60 * 60 * 1000))
                    onDateSelected(selectedLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
                onDismiss()
            }) { Text(stringResource(R.string.ok_button)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun AddEditReminderDialog(
    initial: IntakeReminder?,
    onDismiss: () -> Unit,
    onSave: (title: String, note: String, date: String, time: String, isActive: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var date by remember {
        mutableStateOf(
            initial?.date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        )
    }
    var time by remember { mutableStateOf(initial?.time ?: "08:00") }
    var isActive by remember { mutableStateOf(initial?.isActive ?: true) }

    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val actions = LocalBluetoothActions.current

    if (showTimePicker) {
        TimePickerDialogM3(
            initialTime = time,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { newTime ->
                time = newTime
                showTimePicker = false
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialogM3(
            initialDate = date,
            onDismiss = { showDatePicker = false },
            onDateSelected = { newDate ->
                date = newDate
                showDatePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) stringResource(R.string.add_reminder_title) else stringResource(
                    R.string.edit_reminder_title
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Reminder title (e.g. Medicine)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Short note to show on crutch screen") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Date: $date")
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(stringResource(R.string.date_label))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Time: ${convert24StringTo12(time)}")
                    TextButton(onClick = { showTimePicker = true }) {
                        Text(stringResource(R.string.time_label))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.active_label))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        actions.sendNotificationToCrutch(title, note)
                        onSave(title, note, date, time, isActive)
                    }
                }
            ) {
                Text("Add Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}
