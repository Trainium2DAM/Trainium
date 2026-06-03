package com.example.trainium2

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.trainium2.data.repository.ReservaRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

object NotificationHelper {
    const val CHANNEL_ID = "trainium_reservations"
    const val CHANNEL_NAME = "Reservas"
    const val EXTRA_TITLE = "notif_title"
    const val EXTRA_BODY = "notif_body"
    const val EXTRA_RESERVA_ID = "reserva_id"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios de reservas de máquinas"
                enableVibration(true)
            }
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    fun scheduleReservationNotification(
        context: Context,
        reservaId: Int,
        maquinaNombre: String,
        fecha: String,
        horaInicio: String,
        horaFin: String,
        minutesBefore: Int
    ) {
        try {
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val horaIni5 = horaInicio.take(5)
            val horaFin5 = horaFin.take(5)
            val dateTime = fmt.parse("$fecha $horaIni5") ?: return
            val triggerAt = dateTime.time - (minutesBefore * 60 * 1000L)
            if (triggerAt <= System.currentTimeMillis()) return

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra(EXTRA_RESERVA_ID, reservaId)
                putExtra(EXTRA_TITLE, "Trainium — Recordatorio de reserva")
                putExtra(EXTRA_BODY, "$maquinaNombre: $horaIni5 – $horaFin5")
            }
            val pi = PendingIntent.getBroadcast(
                context,
                reservaId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (_: Exception) {}
    }

    fun cancelScheduledNotification(context: Context, reservaId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, reservaId, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(it)
            it.cancel()
        }
    }

    suspend fun rescheduleNextReservationNotification(context: Context, userId: Int) {
        try {
            val settings = NotificationSettingsManager(context)
            if (!settings.notificationsEnabled.first()) return

            val repo = ReservaRepository()
            val list = repo.getAllWithDetails(isAdmin = false, userId = userId)

            val minutesBefore = settings.minutesBefore.first()

            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val now = Date()

            val upcoming = list.filter { r ->
                try {
                    val startStr = r.horaInicio.take(5)
                    val start = fmt.parse("${r.fecha} $startStr")
                    start != null && start.after(now)
                } catch (_: Exception) {
                    false
                }
            }.minByOrNull { r ->
                val startStr = r.horaInicio.take(5)
                fmt.parse("${r.fecha} $startStr")!!.time
            }

            if (upcoming != null) {
                scheduleReservationNotification(
                    context = context,
                    reservaId = upcoming.id,
                    maquinaNombre = upcoming.maquina?.nombre ?: "Máquina",
                    fecha = upcoming.fecha,
                    horaInicio = upcoming.horaInicio,
                    horaFin = upcoming.horaFin,
                    minutesBefore = minutesBefore
                )
            }
        } catch (_: Exception) {}
    }
}
