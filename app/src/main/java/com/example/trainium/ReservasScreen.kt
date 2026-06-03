package com.example.trainium

import android.content.Intent
import android.provider.CalendarContract
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainium.AppConfig
import com.example.trainium.data.i18n.LocalStrings
import com.example.trainium.models.ReservaConDetalles
import com.example.trainium.ui.theme.*
import com.example.trainium.ui.viewmodel.ReservasViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservasScreen(
    userId: Int,
    isAdmin: Boolean,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val viewModel = viewModel<ReservasViewModel>()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val hoyStr = AppConfig.FORMAT_ISO_DATE.format(Date())

    val textColor = if (darkTheme) Color.White else BlueDark
    val subtitleColor = if (darkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (darkTheme) Color(0xFF162347) else Color.White

    val bgBrush = if (darkTheme) Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    else Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))

    LaunchedEffect(Unit) {
        viewModel.loadReservations(isAdmin, userId)
        NotificationHelper.rescheduleNextReservationNotification(context, userId)
    }

    var mostrarDatePickerReserva by remember { mutableStateOf(false) }

    var reservaParaCalendario by remember { mutableStateOf<ReservaConDetalles?>(null) }

    val reservasFiltradas = viewModel.getFilteredReservations()

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {

            ScreenHeader(
                title = strings.reservations,
                subtitle = if (isAdmin) strings.subtitleReservationsAdmin else strings.subtitleReservationsUser,
                onBack = onBack,
                trailing = {
                    IconButton(onClick = { viewModel.loadReservations(isAdmin, userId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = strings.contentDescRefresh, tint = BlueAccent)
                    }
                },
                textColor = textColor,
                subtitleColor = subtitleColor,
                onToggleTheme = onToggleTheme,
                darkTheme = darkTheme,
                onToggleLanguage = onToggleLanguage,
                strings = strings
            )

            Spacer(Modifier.height(16.dp))

            val allFilters = listOf(
                "upcoming" to strings.filterUpcoming,
                "today"    to strings.today,
                "past"     to strings.filterPast,
                "all"      to strings.filterAll
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                allFilters.forEach { (key, label) ->
                    val sel = viewModel.filtroSeleccionado == key
                    FilterChip(
                        selected = sel,
                        onClick = { viewModel.setFilter(key) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (key == "past") Color(0xFF6B6B8A) else BlueAccent,
                            selectedLabelColor = Color.White,
                            labelColor = textColor.copy(0.6f),
                            containerColor = cardBg.copy(0.4f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (key == "past") Color(0xFF6B6B8A).copy(0.3f) else BlueAccent.copy(0.2f),
                            enabled = true,
                            selected = sel
                        )
                    )
                }

                Spacer(Modifier.weight(1f))

                IconButton(
                    onClick = { mostrarDatePickerReserva = true },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (viewModel.filtroSeleccionado == "date") BlueAccent else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = strings.today,
                        tint = if (viewModel.filtroSeleccionado == "date") Color.White else BlueAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (viewModel.filtroSeleccionado == "date") {
                Text(
                    "${strings.filteringDay} ${viewModel.fechaFiltroManual}",
                    fontSize = 12.sp, color = BlueAccent, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

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
                    Text(strings.connectionError, color = textColor, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
            } else if (reservasFiltradas.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(strings.noReservations, color = textColor.copy(0.4f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    itemsIndexed(reservasFiltradas) { _, r ->
                        val esPasada = viewModel.isPastReservation(r)
                        val esHoy = r.fecha == hoyStr && !esPasada

                        val cardColor = when {
                            esPasada -> if (darkTheme) Color(0xFF1A1A2E) else Color(0xFFF0F0F4)
                            esHoy    -> if (darkTheme) Color(0xFF1E2D52) else Color(0xFFE8F0FF)
                            else     -> cardBg
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (esPasada) 0.65f else 1f)
                                .shadow(if (esHoy) 6.dp else if (esPasada) 0.dp else 1.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                var resId = context.resources.getIdentifier(
                                    "maquina${r.maquina?.id}",
                                    "drawable",
                                    context.packageName
                                )
                                if (resId == 0) resId = context.resources.getIdentifier(
                                    "blanco",
                                    "drawable",
                                    context.packageName
                                )
                                Image(
                                    painter = painterResource(resId),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .alpha(if (esPasada) 0.5f else 1f),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(Modifier.width(14.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        r.maquina?.nombre ?: strings.equipment,
                                        fontWeight = FontWeight.Bold,
                                        color = if (esPasada) textColor.copy(0.5f) else textColor,
                                        fontSize = 15.sp,
                                        textDecoration = if (esPasada) TextDecoration.None else TextDecoration.None
                                    )
                                    Text(
                                        text = when {
                                            esHoy    -> "${strings.today.uppercase()}: ${r.horaInicio.take(5)} - ${r.horaFin.take(5)}"
                                            esPasada -> "${r.fecha} | ${r.horaInicio.take(5)} - ${r.horaFin.take(5)}"
                                            else     -> "${r.fecha} | ${r.horaInicio.take(5)} - ${r.horaFin.take(5)}"
                                        },
                                        fontSize = 12.sp,
                                        color = if (esHoy) BlueAccent else if (esPasada) subtitleColor.copy(0.6f) else subtitleColor,
                                        fontWeight = if (esHoy) FontWeight.Black else FontWeight.Normal
                                    )
                                    if (isAdmin) Text(r.usuario?.nombre ?: strings.userLabel, fontSize = 11.sp, color = textColor.copy(0.4f))
                                    if (esPasada) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            strings.filterPast.uppercase(),
                                            fontSize = 9.sp,
                                            color = Color(0xFF9E9EC0),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                if (!esPasada) {
                                    IconButton(onClick = { reservaParaCalendario = r }) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = strings.addToCalendar,
                                            tint = BlueAccent.copy(0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        val idToCancel = r.id
                                        viewModel.deleteReservation(r.id) {
                                            viewModel.loadReservations(isAdmin, userId)
                                            scope.launch {
                                                NotificationHelper.cancelScheduledNotification(context, idToCancel)
                                                NotificationHelper.rescheduleNextReservationNotification(context, userId)
                                            }
                                        }
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = strings.contentDescDelete,
                                            tint = Color(0xFFFF6B6B).copy(0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Spacer(Modifier.size(44.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (mostrarDatePickerReserva) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
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
                        viewModel.setFilter("date")
                        mostrarDatePickerReserva = false
                    } else { mostrarDatePickerReserva = false }
                }) { Text(strings.ok, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarDatePickerReserva = false }) { Text(strings.cancel, color = textColor.copy(0.5f)) } },
            colors = DatePickerDefaults.colors(selectedDayContentColor = Color.White, selectedDayContainerColor = BlueAccent, todayContentColor = BlueAccent, todayDateBorderColor = BlueAccent, dayContentColor = textColor, titleContentColor = Color.White, headlineContentColor = Color.White)
        ) { DatePicker(state = datePickerState) }
    }
    reservaParaCalendario?.let { r ->
        AlertDialog(
            onDismissRequest = { reservaParaCalendario = null },
            icon = {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = BlueAccent,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    strings.addToCalendar,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) Color.White else BlueDark
                )
            },
            text = {
                Column {
                    Text(
                        strings.addToCalendarMessage,
                        color = if (darkTheme) Color.White.copy(0.8f) else BlueDark.copy(0.75f),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BlueAccent.copy(0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(r.maquina?.nombre ?: strings.equipment, fontWeight = FontWeight.SemiBold, color = BlueAccent, fontSize = 13.sp)
                            Text("${r.fecha}  ${r.horaInicio.take(5)} – ${r.horaFin.take(5)}", fontSize = 12.sp, color = if (darkTheme) Color.White.copy(0.7f) else BlueDark.copy(0.6f))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val startMs = fmt.parse("${r.fecha} ${r.horaInicio.take(5)}")?.time ?: 0L
                            val endMs   = fmt.parse("${r.fecha} ${r.horaFin.take(5)}")?.time ?: 0L
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, "Trainium – ${r.maquina?.nombre ?: ""}")
                                putExtra(CalendarContract.Events.DESCRIPTION, "${r.horaInicio.take(5)} – ${r.horaFin.take(5)}")
                                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMs)
                                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMs)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                        reservaParaCalendario = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                ) {
                    Text(strings.addToCalendar, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { reservaParaCalendario = null }) {
                    Text(strings.cancel, color = if (darkTheme) Color.White.copy(0.6f) else BlueDark.copy(0.5f))
                }
            },
            containerColor = if (darkTheme) Color(0xFF162347) else Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}