package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Device reboot detected: rescheduling all active birthday reminders")
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val birthdays = db.birthdayDao().getAllBirthdays().firstOrNull() ?: emptyList()
                    ReminderScheduler.rescheduleAll(context, birthdays)
                    Log.d("BootReceiver", "Successfully rescheduled ${birthdays.size} birthdays after reboot")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling birthdays after reboot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
