package com.health.stridox.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.health.stridox.R
import kotlin.jvm.java

object NotificationUtils {

    private const val CHANNEL_ID = "reminders_channel"
    private const val CHANNEL_NAME = "Reminders"
    private const val CHANNEL_DESC = "Medication reminders"

    fun createNotificationChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (nm.getNotificationChannel(CHANNEL_ID) == null) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC

                // enable vibration
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)

                // enable sound (default alarm sound, fallback to notification)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )
            }

            nm.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        context: Context,
        id: Int,
        title: String,
        body: String,
    ): Notification {

        val doneIntent = Intent(context, ReminderDoneReceiver::class.java).apply {
            putExtra("reminder_id", id.toLong())
        }

        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            id,  // unique per reminder
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.medication)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            )
            // Vibration for old API levels
            .setVibrate(longArrayOf(0, 400, 200, 400))
            // Add action button
            .addAction(
                R.drawable.medication,
                "Med Taken",
                donePendingIntent
            )
            .build()
    }
}
