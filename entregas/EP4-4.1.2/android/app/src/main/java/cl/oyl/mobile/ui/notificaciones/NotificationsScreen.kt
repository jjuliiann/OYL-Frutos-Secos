package cl.oyl.mobile.ui.notificaciones

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import cl.oyl.mobile.R
import cl.oyl.mobile.data.local.entity.LocalNotice
import cl.oyl.mobile.ui.components.ActionButton
import cl.oyl.mobile.ui.components.OylOutlinedButton
import cl.oyl.mobile.ui.components.OylTextButton
import cl.oyl.mobile.ui.components.ScreenTitle
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(
    notices: List<LocalNotice>,
    markRead: (Long) -> Unit,
    demo: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenTitle("Notificaciones", "Tus novedades y actualizaciones de pedidos")
        ActionButton("Permitir notificaciones", onClick = {
            if (Build.VERSION.SDK_INT >= 33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        })
        OylOutlinedButton(onClick = {
            demo()
            val allowed = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (allowed) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification = NotificationCompat.Builder(context, "oyl_demo")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("OYL Frutos Secos")
                    .setContentText("Novedades OYL")
                    .setAutoCancel(true)
                    .build()
                manager.notify(System.currentTimeMillis().toInt(), notification)
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Recibir novedades OYL") }
        if (notices.isEmpty()) Text("Sin notificaciones guardadas")
        LazyColumn {
            items(notices) { notice ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(notice.title + if (notice.read) "" else " • Nueva", fontWeight = FontWeight.Bold)
                        Text(notice.content)
                        OylTextButton(onClick = { markRead(notice.id) }) { Text("Marcar como leída") }
                    }
                }
            }
        }
    }
}
