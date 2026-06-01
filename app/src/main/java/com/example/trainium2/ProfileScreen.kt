package com.example.trainium2

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.models.Usuario
import com.example.trainium2.AppConfig
import com.example.trainium2.DbColumns
import com.example.trainium2.DbTables
import com.example.trainium2.ui.theme.*
import io.github.jan.supabase.postgrest.from
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// Variable global para controlar que solo salga una vez por inicio de app
private var avisoMantenimientoMostradoEnSesion = false

@Composable
fun ProfileScreen(
    nombre: String,
    isAdmin: Boolean,
    idUsuario: Int,
    isPremium: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToMaquinas: (Boolean, Int) -> Unit,
    onNavigateToPlatos: (Boolean, Int) -> Unit,
    onNavigateToRegistro: (Int) -> Unit,
    onNavigateToReservas: (Boolean, Int) -> Unit,
    onNavigateToEditProfile: (Int) -> Unit
) {
    var usuarioLocal by remember { mutableStateOf<Usuario?>(null) }
    var avisoMantenimiento by remember { mutableStateOf<String?>(null) }
    var headerVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val user = withContext(Dispatchers.IO) {
                SupabaseClient.client.from(DbTables.USUARIOS)
                    .select { filter { eq(DbColumns.ID, idUsuario) } }
                    .decodeSingleOrNull<Usuario>()
            }
            usuarioLocal = user

            if (!avisoMantenimientoMostradoEnSesion) {
                // Comprobar si tiene alguna reserva cancelada recientemente por mantenimiento
                val hoy = AppConfig.FORMAT_ISO_DATE.format(Date())
                val canceladas = withContext(Dispatchers.IO) {
                    val columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, maquinas(*)")
                    SupabaseClient.client.from(DbTables.RESERVAS)
                        .select(columns) {
                            filter {
                                eq(DbColumns.ID_USUARIO, idUsuario)
                                eq(DbColumns.FECHA, hoy)
                                eq(DbColumns.ESTADO, false)
                            }
                        }.decodeList<com.example.trainium2.models.ReservaConDetalles>()
                }

                val reservaAfectada = canceladas.find { it.maquina?.operativa == false && it.maquina.mantenimiento_hasta != null }
                if (reservaAfectada != null) {
                    val desde = reservaAfectada.maquina?.mantenimiento_desde ?: ""
                    val hasta = reservaAfectada.maquina?.mantenimiento_hasta ?: ""
                    avisoMantenimiento = "AVISO MÁQUINA EN MANTENIMIENTO\n\nLa máquina '${reservaAfectada.maquina?.nombre}' estará en mantenimiento desde el $desde hasta el $hasta. Tu reserva ha sido cancelada. Lamentamos las molestias."
                    avisoMantenimientoMostradoEnSesion = true
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        delay(100); headerVisible = true
        delay(250); buttonsVisible = true
    }

    val headerAlpha by animateFloatAsState(if (headerVisible) 1f else 0f, tween(500), label = "ha")
    val buttonsAlpha by animateFloatAsState(if (buttonsVisible) 1f else 0f, tween(500), label = "ba")

    val bgBrush = if (isDarkTheme) Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    else Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))

    val cardBg = if (isDarkTheme) Color(0xFF162347) else Color.White
    val cardIconBg = if (isDarkTheme) BlueAccent.copy(0.12f) else BlueAccent.copy(0.08f)
    val titleColor = if (isDarkTheme) Color.White else Color(0xFF1A1A2E)
    val subtitleColor = if (isDarkTheme) Color.White.copy(0.4f) else Color(0xFF6B7B99)
    val topIconTint = if (isDarkTheme) BlueSoft else BlueAccent

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleTheme) {
                        Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null, tint = topIconTint)
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = { onNavigateToEditProfile(idUsuario) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = topIconTint, modifier = Modifier.size(24.dp))
                    }
                }
                Image(painter = painterResource(if (isDarkTheme) R.drawable.blanco else R.drawable.negro), contentDescription = "Trainium", modifier = Modifier.height(56.dp))
            }

            Spacer(Modifier.height(24.dp))

            Column(modifier = Modifier.alpha(headerAlpha), horizontalAlignment = Alignment.CenterHorizontally) {
                val userPhoto = usuarioLocal?.foto
                val userInitial = (usuarioLocal?.nombre ?: nombre).take(1).uppercase()

                val profileImage = remember(userPhoto) {
                    if (!userPhoto.isNullOrEmpty()) {
                        decodeBase64ToBitmap(userPhoto)?.asImageBitmap()
                    } else null
                }

                Box(
                    modifier = Modifier.size(110.dp).shadow(16.dp, CircleShape, ambientColor = BlueAccent.copy(0.4f), spotColor = BlueAccent.copy(0.4f)).clip(CircleShape).background(Brush.linearGradient(listOf(BlueAccent, BlueElectric)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImage != null) {
                        Image(bitmap = profileImage, contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(userInitial, fontSize = 46.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(text = "¡Hola, ${usuarioLocal?.nombre ?: nombre}!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = titleColor, textAlign = TextAlign.Center)
                if (isAdmin) {
                    Spacer(Modifier.height(6.dp))
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BlueAccent.copy(0.15f))) {
                        Text("ADMINISTRADOR", Modifier.padding(horizontal = 16.dp, vertical = 4.dp), fontSize = 12.sp, color = BlueAccent, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                if (usuarioLocal?.premium == true || isPremium) {
                    Spacer(Modifier.height(4.dp))
                    Text("PREMIUM", fontSize = 13.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }

            Spacer(Modifier.height(36.dp))

            Column(modifier = Modifier.alpha(buttonsAlpha), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ProfileMenuCard("Visualizar máquinas", Icons.Default.FitnessCenter, "Explora el equipamiento", cardBg, cardIconBg, titleColor, subtitleColor, isDarkTheme) { onNavigateToMaquinas(isAdmin, idUsuario) }
                ProfileMenuCard("Máquinas reservadas", Icons.Default.CalendarMonth, "Tus reservas activas", cardBg, cardIconBg, titleColor, subtitleColor, isDarkTheme) { onNavigateToReservas(isAdmin, idUsuario) }
                ProfileMenuCard("Recomendación de platos", Icons.Default.Restaurant, "Nutrición personalizada", cardBg, cardIconBg, titleColor, subtitleColor, isDarkTheme) { onNavigateToPlatos(isAdmin, idUsuario) }
                ProfileMenuCard("Mi registro de peso", Icons.Default.MonitorWeight, "Seguimiento de progreso", cardBg, cardIconBg, titleColor, subtitleColor, isDarkTheme) { onNavigateToRegistro(idUsuario) }
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    avisoMantenimientoMostradoEnSesion = false
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).alpha(buttonsAlpha),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B6B).copy(if (isDarkTheme) 0.25f else 0.15f),
                    contentColor = Color(0xFFFF6B6B)
                )
            ) {
                Icon(Icons.Default.Logout, null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión", color = Color(0xFFFF6B6B), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (avisoMantenimiento != null) {
        AlertDialog(
            onDismissRequest = { avisoMantenimiento = null },
            confirmButton = {
                Button(onClick = { avisoMantenimiento = null }, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) {
                    Text("Entendido", color = Color.White)
                }
            },
            title = { Text("Aviso de Mantenimiento", fontWeight = FontWeight.Bold) },
            text = { Text(avisoMantenimiento!!, color = titleColor) },
            containerColor = cardBg
        )
    }
}

@Composable
private fun ProfileMenuCard(text: String, icon: ImageVector, subtitle: String, cardBg: Color, iconBg: Color, titleColor: Color, subtitleColor: Color, isDark: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().shadow(if (isDark) 8.dp else 4.dp, RoundedCornerShape(18.dp)), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(iconBg, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = BlueAccent, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = titleColor)
                Text(subtitle, fontSize = 12.sp, color = subtitleColor)
            }
            Icon(Icons.Default.ChevronRight, null, tint = BlueAccent.copy(0.5f), modifier = Modifier.size(24.dp))
        }
    }
}