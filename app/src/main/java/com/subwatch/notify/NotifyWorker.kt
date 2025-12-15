package com.subwatch.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.subwatch.R
import com.subwatch.data.SubWatchDatabase
import com.subwatch.util.Dates
import kotlinx.coroutines.flow.first

class NotifyWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val dao = SubWatchDatabase.get().subscriptionDao()
        val subs = dao.observeAll().first()

        val expiring = subs.mapNotNull { sub ->
            val end = Dates.epochDayToLocalDate(sub.endDateEpochDay)
            val left = Dates.daysLeft(end)
            if (left in 0..5) Triple(sub.name, Dates.format(end), left) else null
        }

        if (expiring.isNotEmpty()) {
            ensureChannel()
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            expiring.forEachIndexed { idx, item ->
                val (name, endDate, left) = item
                val title = if (left == 0L) "Expires today" else "Expires in $left day(s)"
                val text = "$name • ends $endDate"

                val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .build()

                nm.notify(1000 + idx, notif)
            }
        }

        return Result.success()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = nm.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "SubWatch alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                nm.createNotificationChannel(ch)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "subwatch.alerts"
    }
}
