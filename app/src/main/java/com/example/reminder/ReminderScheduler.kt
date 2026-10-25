package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.model.Birthday
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"

    const val ACTION_BIRTHDAY_REMINDER = "com.example.ACTION_BIRTHDAY_REMINDER"
    const val EXTRA_BIRTHDAY_ID = "extra_birthday_id"
    const val EXTRA_PERSON_NAME = "extra_person_name"
    const val EXTRA_RELATIONSHIP = "extra_relationship"
    const val EXTRA_IS_ONE_DAY_BEFORE = "extra_is_one_day_before"

    fun canScheduleExact(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    fun scheduleBirthdayReminders(context: Context, birthday: Birthday) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val zoneId = ZoneId.systemDefault()
        val now = LocalDateTime.now(zoneId)
        val today = now.toLocalDate()
        val nextDate = birthday.nextBirthday(today)

        val reminderTime = LocalTime.of(
            birthday.remindHour.coerceIn(0, 23),
            birthday.remindMinute.coerceIn(0, 59)
        )

        // 1. Remind on the birthday
        if (birthday.remindOnDay) {
            var targetDateTime = LocalDateTime.of(nextDate, reminderTime)
            // If the time already passed today, push to next year
            if (targetDateTime.isBefore(now)) {
                val nextYearDate = birthday.nextBirthday(today.plusDays(1))
                targetDateTime = LocalDateTime.of(nextYearDate, reminderTime)
            }
            val triggerMillis = targetDateTime.atZone(zoneId).toInstant().toEpochMilli()
            setAlarm(
                context = context,
                alarmManager = alarmManager,
                requestCode = (birthday.id * 10).toInt(),
                triggerMillis = triggerMillis,
                birthday = birthday,
                isOneDayBefore = false
            )
        } else {
            cancelSpecificAlarm(context, alarmManager, (birthday.id * 10).toInt())
        }

        // 2. Remind one day before the birthday
        if (birthday.remindOneDayBefore) {
            var targetBeforeDate = nextDate.minusDays(1)
            var targetDateTime = LocalDateTime.of(targetBeforeDate, reminderTime)
            if (targetDateTime.isBefore(now)) {
                val nextYearDate = birthday.nextBirthday(today.plusDays(1))
                targetBeforeDate = nextYearDate.minusDays(1)
                targetDateTime = LocalDateTime.of(targetBeforeDate, reminderTime)
            }
            val triggerMillis = targetDateTime.atZone(zoneId).toInstant().toEpochMilli()
            setAlarm(
                context = context,
                alarmManager = alarmManager,
                requestCode = (birthday.id * 10 + 1).toInt(),
                triggerMillis = triggerMillis,
                birthday = birthday,
                isOneDayBefore = true
            )
        } else {
            cancelSpecificAlarm(context, alarmManager, (birthday.id * 10 + 1).toInt())
        }
    }

    private fun setAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        triggerMillis: Long,
        birthday: Birthday,
        isOneDayBefore: Boolean
    ) {
        val intent = Intent(context, BirthdayAlarmReceiver::class.java).apply {
            action = ACTION_BIRTHDAY_REMINDER
            putExtra(EXTRA_BIRTHDAY_ID, birthday.id)
            putExtra(EXTRA_PERSON_NAME, birthday.name)
            putExtra(EXTRA_RELATIONSHIP, birthday.relationship)
            putExtra(EXTRA_IS_ONE_DAY_BEFORE, isOneDayBefore)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val canExact = canScheduleExact(context)
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm req=$requestCode for ${birthday.name} at millis=$triggerMillis")
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission denied, falling back to setAndAllowWhileIdle", e)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to schedule alarm", ex)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    fun cancelBirthdayReminders(context: Context, birthdayId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        cancelSpecificAlarm(context, alarmManager, (birthdayId * 10).toInt())
        cancelSpecificAlarm(context, alarmManager, (birthdayId * 10 + 1).toInt())
    }

    private fun cancelSpecificAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, BirthdayAlarmReceiver::class.java).apply {
            action = ACTION_BIRTHDAY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun rescheduleAll(context: Context, birthdays: List<Birthday>) {
        birthdays.forEach { birthday ->
            scheduleBirthdayReminders(context, birthday)
        }
    }
}
