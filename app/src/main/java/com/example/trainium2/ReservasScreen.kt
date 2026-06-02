package com.example.trainium2

import android.app.DatePickerDialog
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
import com.example.trainium2.ui.theme.*
import com.example.trainium2.ui.viewmodel.ReservasViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservasScreen(
    userId: Int,
    isAdmin: Boolean,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel = viewModel<ReservasViewModel>()

    val hoyStr = AppConfig.FORMAT_ISO_DATE.format(Date())

    val context = LocalContext.current

    val textColor = if (darkTheme) Color.White else BlueDark
    val subtitleColor = if (darkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (darkTheme) Color(0xFF162347) else Color.White

    val bgBrush = if (darkTheme) Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    else Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))

    LaunchedEffect(Unit) { viewModel.loadReservations(isAdmin, userId) }

    var mostrarDatePickerReserva by remember { mutableStateOf(false) }

    val reservasFiltradas = viewModel.getFilteredReservations()

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            // --- HEADER ---
            ScreenHeader(
                title = "Mis Reservas",
                subtitle = if (isAdmin) "Administración global" else "Tus entrenamientos",
                onBack = onBack,
                trailing = {
                    IconButton(onClick = { viewModel.loadReservations(isAdmin, userId) }) {
                        Icon(Icons.Default.Refresh, null, tint = BlueAccent)
                    }
                },
                textColor = textColor,
                subtitleColor = subtitleColor,
                onToggleTheme = onToggleTheme,
                darkTheme = darkTheme
            )

            Spacer(Modifier.height(16.dp))

            // --- FILTROS (Chips + Calendario) ---
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf("Próximas", "Hoy", "Todas").forEach { texto ->
                    val sel = viewModel.filtroSeleccionado == texto
                    FilterChip(
                        selected = sel,
                        onClick = { viewModel.setFilter(texto) },
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
                    onClick = { mostrarDatePickerReserva = true },
                    modifier = Modifier.size(32.dp).background(if(viewModel.filtroSeleccionado == "Fecha") BlueAccent else Color.Transparent, CircleShape)
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = if(viewModel.filtroSeleccionado == "Fecha") Color.White else BlueAccent, modifier = Modifier.size(18.dp))
                }
            }

            if (viewModel.filtroSeleccionado == "Fecha") {
                Text("Filtrando día: ${viewModel.fechaFiltroManual}", fontSize = 12.sp, color = BlueAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(12.dp))

            // --- LISTA DE RESERVAS ---
            if (viewModel.isLoading) {
                Column(Modifier.fillMaxWidth().weight(1f).padding(top = 12.dp)) {
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 80)
                    Spacer(Modifier.height(10.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 80)
                    Spacer(Modifier.height(10.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 80)
                }
            } else if (viewModel.error != null) {
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
                                containerColor = if (esHoy) (if (darkTheme) Color(0xFF1E2D52) else Color(0xFFE8F0FF)) else cardBg
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
                                    viewModel.deleteReservation(r.id) {
                                        viewModel.loadReservations(isAdmin, userId)
                                    }
                                }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF6B6B).copy(0.7f), modifier = Modifier.size(20.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDatePickerReserva) {
        val calendar = Calendar.getInstance()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= System.currentTimeMillis() - 86400000L
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerReserva = false },
            confirmButton = {
                TextButton(onClick = {
                    val mReserva = datePickerState.selectedDateMillis
                    if (mReserva != null) {
                        val cal = Calendar.getInstance().apply { timeInMillis = mReserva }
                        val fechaSel = String.format("%d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                        viewModel.setDateFilter(fechaSel)
                        viewModel.setFilter("Fecha")
                        mostrarDatePickerReserva = false
                    } else { mostrarDatePickerReserva = false }
                }) { Text("OK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarDatePickerReserva = false }) { Text("Cancelar", color = textColor.copy(0.5f)) } },
            colors = DatePickerDefaults.colors(
                selectedDayContentColor = Color.White, selectedDayContainerColor = BlueAccent,
                todayContentColor = BlueAccent, todayDateBorderColor = BlueAccent,
                dayContentColor = textColor, titleContentColor = Color.White, headlineContentColor = Color.White
            )
        ) { DatePicker(state = datePickerState) }
    }
}