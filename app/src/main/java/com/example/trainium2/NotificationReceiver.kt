package com.example.trainium2

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reservaId = intent.getIntExtra(NotificationHelper.EXTRA_RESERVA_ID, 0)
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Trainium"
        val body = intent.getStringExtra(NotificationHelper.EXTRA_BODY) ?: ""

        NotificationHelper.createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(reservaId, notification)
    }
}
