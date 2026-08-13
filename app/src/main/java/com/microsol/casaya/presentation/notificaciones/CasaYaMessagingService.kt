// Servicio de Firebase Messaging: recibe notificaciones push y las muestra en el dispositivo.
package com.microsol.casaya.presentation.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.microsol.casaya.MainActivity
import com.microsol.casaya.R
import com.microsol.casaya.data.repository.FirestoreUsuarioRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CasaYaMessagingService : FirebaseMessagingService() {

    private val usuarioRepository = FirestoreUsuarioRepositoryImpl()
    private val canalId = "casaya_notificaciones"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            usuarioRepository.guardarTokenNotificacion(uid, token)
        }
    }

    override fun onMessageReceived(mensaje: RemoteMessage) {
        super.onMessageReceived(mensaje)
        val titulo = mensaje.notification?.title ?: "CasaYa"
        val cuerpo = mensaje.notification?.body ?: ""
        mostrarNotificacion(titulo, cuerpo)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String) {
        crearCanalSiNoExiste()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(this, canalId)
            .setSmallIcon(R.drawable.logo_casaya)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)
        notificationManager?.notify(System.currentTimeMillis().toInt(), notificacion)
    }

    private fun crearCanalSiNoExiste() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId, "Notificaciones CasaYa", NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)
            notificationManager?.createNotificationChannel(canal)
        }
    }
}