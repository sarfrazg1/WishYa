package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Birthday
import java.time.Month

private val RELATIONSHIPS = listOf(
    "Friend", "Best Friend", "Love", "Wife", "Husband",
    "Mom", "Dad", "Brother", "Sister", "Son", "Daughter",
    "Colleague", "Boss", "Teacher", "Cousin", "Relative", "Other"
)

private val MONTHS = (1..12).map { Month.of(it) }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditBirthdayDialog(
    birthdayToEdit: Birthday? = null,
    defaultHour: Int = 9,
    defaultMinute: Int = 0,
    defaultOneDayBefore: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        relationship: String,
        day: Int,
        month: Int,
        year: Int?,
        notes: String,
        remindOnDay: Boolean,
        remindOneDayBefore: Boolean,
        hour: Int,
        minute: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf(birthdayToEdit?.name ?: "") }
    var relationship by remember { mutableStateOf(birthdayToEdit?.relationship ?: "Friend") }
    var month by remember { mutableIntStateOf(birthdayToEdit?.birthMonth ?: 1) }
    var dayText by remember { mutableStateOf(birthdayToEdit?.birthDay?.toString() ?: "1") }
    var yearText by remember { mutableStateOf(birthdayToEdit?.birthYear?.toString() ?: "") }
    var notes by remember { mutableStateOf(birthdayToEdit?.notes ?: "") }
    var remindOnDay by remember { mutableStateOf(birthdayToEdit?.remindOnDay ?: true) }
    var remindOneDayBefore by remember { mutableStateOf(birthdayToEdit?.remindOneDayBefore ?: defaultOneDayBefore) }
    var remindHour by remember { mutableIntStateOf(birthdayToEdit?.remindHour ?: defaultHour) }
    var remindMinute by remember { mutableIntStateOf(birthdayToEdit?.remindMinute ?: defaultMinute) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Cake,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (birthdayToEdit == null) "Add Birthday" else "Edit Birthday",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Person's Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("birthday_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Relationship selection chips
                Text(
                    text = "Relationship",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RELATIONSHIPS.forEach { rel ->
                        FilterChip(
                            selected = relationship == rel,
                            onClick = { relationship = rel },
                            label = { Text(rel, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Month selection
                Text(
                    text = "Birth Month",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MONTHS.forEach { m ->
                        val isSelected = month == m.value
                        FilterChip(
                            selected = isSelected,
                            onClick = { month = m.value },
                            label = { Text(m.name.take(3), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Day & Year
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) dayText = it },
                        label = { Text("Day (1-31)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("birthday_day_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) yearText = it },
                        label = { Text("Year (Optional)") },
                        placeholder = { Text("e.g. 1998") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("birthday_year_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Gift ideas (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Reminder switches
                Text(
                    text = "Reminder Settings",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Remind on Birthday", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("Notification on the special day", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = remindOnDay, onCheckedChange = { remindOnDay = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Remind 1 Day Before", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("Get advance notice to prepare", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = remindOneDayBefore, onCheckedChange = { remindOneDayBefore = it })
                }

                // Reminder Time
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = !showTimePicker }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reminder Time", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    val timeFormatted = String.format("%02d:%02d %s", if (remindHour % 12 == 0) 12 else remindHour % 12, remindMinute, if (remindHour >= 12) "PM" else "AM")
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showTimePicker) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick hour selection chips
                        listOf(8 to "8:00 AM", 9 to "9:00 AM", 10 to "10:00 AM", 12 to "12:00 PM", 18 to "6:00 PM", 20 to "8:00 PM").forEach { (h, label) ->
                            FilterChip(
                                selected = remindHour == h,
                                onClick = {
                                    remindHour = h
                                    remindMinute = 0
                                    showTimePicker = false
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        errorMessage = "Please enter person's name"
                        return@Button
                    }
                    val dayInt = dayText.toIntOrNull()
                    if (dayInt == null || dayInt < 1 || dayInt > 31) {
                        errorMessage = "Please enter a valid day (1-31)"
                        return@Button
                    }
                    val maxDays = Month.of(month).maxLength()
                    if (dayInt > maxDays) {
                        errorMessage = "This month only has $maxDays days"
                        return@Button
                    }
                    val yearInt = yearText.toIntOrNull()
                    if (yearInt != null && (yearInt < 1900 || yearInt > 2030)) {
                        errorMessage = "Please enter a valid birth year"
                        return@Button
                    }

                    onSave(
                        trimmedName,
                        relationship,
                        dayInt,
                        month,
                        yearInt,
                        notes,
                        remindOnDay,
                        remindOneDayBefore,
                        remindHour,
                        remindMinute
                    )
                },
                modifier = Modifier.testTag("save_birthday_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
