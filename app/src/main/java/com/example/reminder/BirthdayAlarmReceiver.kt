package com.example.reminder

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.WishYaApp
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BirthdayAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_BIRTHDAY_REMINDER) return

        val birthdayId = intent.getLongExtra(ReminderScheduler.EXTRA_BIRTHDAY_ID, -1L)
        val personName = intent.getStringExtra(ReminderScheduler.EXTRA_PERSON_NAME) ?: "Someone Special"
        val relationship = intent.getStringExtra(ReminderScheduler.EXTRA_RELATIONSHIP) ?: "Friend"
        val isOneDayBefore = intent.getBooleanExtra(ReminderScheduler.EXTRA_IS_ONE_DAY_BEFORE, false)

        Log.d("BirthdayAlarmReceiver", "Received alarm for $personName (ID: $birthdayId), oneDayBefore=$isOneDayBefore")

        // Notification permission check on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w("BirthdayAlarmReceiver", "Notification permission not granted, skipping notification")
                return
            }
        }

        val title = if (isOneDayBefore) {
            "🎂 Tomorrow is $personName's Birthday!"
        } else {
            "🎉 Happy Birthday $personName!"
        }

        val message = if (isOneDayBefore) {
            "Don't forget to prepare a special wish or gift for $personName ($relationship) tomorrow!"
        } else {
            "Today is $personName's special day! Tap WishYa to send a heartfelt birthday message."
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "birthdays")
            putExtra("EXTRA_BIRTHDAY_ID", birthdayId)
            putExtra("EXTRA_PERSON_NAME", personName)
        }

        val notificationId = (birthdayId.hashCode() * 31) + (if (isOneDayBefore) 1 else 0)

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, WishYaApp.CHANNEL_BIRTHDAY_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(notificationId, notification)

        // Reschedule for next year in the background
        if (birthdayId > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val birthday = db.birthdayDao().getBirthdayById(birthdayId)
                    if (birthday != null) {
                        ReminderScheduler.scheduleBirthdayReminders(context, birthday)
                    }
                } catch (e: Exception) {
                    Log.e("BirthdayAlarmReceiver", "Failed to reschedule birthday for next cycle", e)
                }
            }
        }
    }
}
