package com.example.trainium2

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.models.ReservaConDetalles
import com.example.trainium2.AppConfig
import com.example.trainium2.DbColumns
import com.example.trainium2.DbTables
import com.example.trainium2.ui.theme.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservasScreen(isAdmin: Boolean, idUsuario: Int, isDarkTheme: Boolean, onToggleTheme: () -> Unit, onBack: () -> Unit) {
    var todasLasReservas by remember { mutableStateOf(listOf<ReservaConDetalles>()) }
    var cargando by remember { mutableStateOf(true) }
    var errorConexion by remember { mutableStateOf(false) }

    // Estados del filtro: "Próximas", "Hoy", "Todas", "Fecha"
    var filtroSeleccionado by remember { mutableStateOf("Próximas") }
    var fechaFiltroManual by remember { mutableStateOf("") }

    val hoyStr = AppConfig.FORMAT_ISO_DATE.format(Date())
    val calendar = Calendar.getInstance()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var headerVisible by remember { mutableStateOf(false) }

    val textColor = if (isDarkTheme) Color.White else BlueDark
    val subtitleColor = if (isDarkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (isDarkTheme) Color(0xFF162347) else Color.White

    val bgBrush = if (isDarkTheme) Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    else Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))

    fun cargarDatos() {
        cargando = true; errorConexion = false; headerVisible = false
        scope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    val columns = Columns.raw("*, usuarios(*), maquinas(*)")
                    SupabaseClient.client.from(DbTables.RESERVAS).select(columns) {
                        if (!isAdmin) {
                            filter {
                                eq(DbColumns.ID_USUARIO, idUsuario)
                            }
                        }
                    }.decodeList<ReservaConDetalles>()
                }
                withContext(Dispatchers.Main) {
                    // Ordenamos por fecha y luego por hora
                    todasLasReservas = data.sortedWith(compareBy({ it.fecha }, { it.horaInicio }))
                    cargando = false; headerVisible = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { errorConexion = true; cargando = false; headerVisible = true }
            }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    // Selector de Fecha
    val datePickerDialog = DatePickerDialog(context, { _, y, m, d ->
        val fechaSel = String.format("%d-%02d-%02d", y, m + 1, d)
        fechaFiltroManual = fechaSel
        filtroSeleccionado = "Fecha"
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    // Lógica de filtrado en memoria
    val reservasFiltradas = remember(todasLasReservas, filtroSeleccionado, fechaFiltroManual) {
        when (filtroSeleccionado) {
            "Hoy" -> todasLasReservas.filter { it.fecha == hoyStr }
            "Próximas" -> todasLasReservas.filter { it.fecha >= hoyStr }
            "Fecha" -> todasLasReservas.filter { it.fecha == fechaFiltroManual }
            else -> todasLasReservas // "Todas"
        }
    }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            // --- HEADER ---
            ScreenHeader(
                title = "Mis Reservas",
                subtitle = if (isAdmin) "Administración global" else "Tus entrenamientos",
                onBack = onBack,
                trailing = {
                    IconButton(onClick = { cargarDatos() }) {
                        Icon(Icons.Default.Refresh, null, tint = BlueAccent)
                    }
                },
                textColor = textColor,
                subtitleColor = subtitleColor,
                onToggleTheme = onToggleTheme,
                darkTheme = isDarkTheme
            )

            Spacer(Modifier.height(16.dp))

            // --- FILTROS (Chips + Calendario) ---
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf("Próximas", "Hoy", "Todas").forEach { texto ->
                    val sel = filtroSeleccionado == texto
                    FilterChip(
                        selected = sel,
                        onClick = { filtroSeleccionado = texto },
                        label = { Text(texto) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlueAccent,
                            selectedLabelColor = Color.White,
                            labelColor = textColor.copy(0.6f),
                            containerColor = cardBg.copy(0.4f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(borderColor = BlueAccent.copy(0.2f), enabled = true, selected = sel)
                    )
                }

                IconButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.size(32.dp).background(if(filtroSeleccionado == "Fecha") BlueAccent else Color.Transparent, CircleShape)
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = if(filtroSeleccionado == "Fecha") Color.White else BlueAccent, modifier = Modifier.size(18.dp))
                }
            }

            if (filtroSeleccionado == "Fecha") {
                Text("Filtrando día: $fechaFiltroManual", fontSize = 12.sp, color = BlueAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(12.dp))

            // --- LISTA DE RESERVAS ---
            if (cargando) {
                Column(Modifier.fillMaxWidth().weight(1f).padding(top = 12.dp)) {
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 80)
                    Spacer(Modifier.height(10.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 80)
                    Spacer(Modifier.height(10.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 80)
                }
            } else if (errorConexion) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Error de conexión", color = textColor)
                }
            } else if (reservasFiltradas.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay reservas programadas", color = textColor.copy(0.4f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    itemsIndexed(reservasFiltradas) { _, r ->
                        val esHoy = r.fecha == hoyStr
                        Card(
                            modifier = Modifier.fillMaxWidth().shadow(if (esHoy) 6.dp else 1.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (esHoy) (if (isDarkTheme) Color(0xFF1E2D52) else Color(0xFFE8F0FF)) else cardBg
                            )
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                var resId = context.resources.getIdentifier("maquina${r.maquina?.id}", "drawable", context.packageName)
                                if (resId == 0) resId = context.resources.getIdentifier("blanco", "drawable", context.packageName)

                                Image(
                                    painter = painterResource(resId),
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(r.maquina?.nombre ?: "Equipo", fontWeight = FontWeight.Bold, color = textColor, fontSize = 15.sp)
                                    Text(
                                        text = if (esHoy) "HOY: ${r.horaInicio.take(5)} - ${r.horaFin.take(5)}"
                                        else "${r.fecha} | ${r.horaInicio.take(5)} - ${r.horaFin.take(5)}",
                                        fontSize = 12.sp, color = if (esHoy) BlueAccent else subtitleColor,
                                        fontWeight = if (esHoy) FontWeight.Black else FontWeight.Normal
                                    )
                                    if (isAdmin) Text("${r.usuario?.nombre ?: "Usuario"}", fontSize = 11.sp, color = textColor.copy(0.5f))
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                SupabaseClient.client.from(DbTables.RESERVAS).delete { filter { eq(DbColumns.ID, r.id) } }
                                            }
                                            cargarDatos()
                                            Toast.makeText(context, "Reserva cancelada", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) { e.printStackTrace() }
                                    }
                                }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF6B6B).copy(0.7f), modifier = Modifier.size(20.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}