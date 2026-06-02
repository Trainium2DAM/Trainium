package com.example.trainium2

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainium2.models.Maquina
import com.example.trainium2.data.i18n.LocalStrings
import com.example.trainium2.ui.theme.*
import com.example.trainium2.ui.viewmodel.MaquinasViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaquinasScreen(
    userId: Int,
    isAdmin: Boolean,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val viewModel = viewModel<MaquinasViewModel>()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarPickerMantenimientoDesde by remember { mutableStateOf(false) }
    var mostrarPickerMantenimientoHasta by remember { mutableStateOf(false) }
    var mostrarTimePickerMantenimientoDesde by remember { mutableStateOf(false) }
    var mostrarTimePickerMantenimientoHasta by remember { mutableStateOf(false) }

    var mostrarSelectorHora by remember { mutableStateOf(false) }
    var mostrarSelectorTiempo by remember { mutableStateOf(false) }
    var mostrarDatePickerReserva by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.reservaExitosa) {
        viewModel.reservaExitosa?.let { res ->
            scope.launch {
                NotificationHelper.rescheduleNextReservationNotification(context, userId)
            }
            val result = snackbarHostState.showSnackbar(
                message = strings.reservationBooked,
                actionLabel = strings.cancel,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                val idToCancel = res.id ?: return@let
                viewModel.cancelUltimaReserva()
                scope.launch {
                    NotificationHelper.cancelScheduledNotification(context, idToCancel)
                    NotificationHelper.rescheduleNextReservationNotification(context, userId)
                }
            } else {
                viewModel.clearReservaExitosa()
            }
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    val textColor = if (darkTheme) Color.White else BlueDark
    val subtitleColor = if (darkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (darkTheme) Color(0xFF162347).copy(0.9f) else Color.White

    val bgBrush = if (darkTheme) {
        Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))
    }

    fun alternarEstadoOperativo(maquina: Maquina) {
        if (maquina.operativa) {
            viewModel.maquinaParaMantenimiento = maquina.id
            mostrarPickerMantenimientoDesde = true
        } else {
            viewModel.endMaintenance(maquina.id)
        }
    }

    LaunchedEffect(Unit) { viewModel.loadMachines() }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            ScreenHeader(
                title = strings.machines,
                subtitle = strings.subtitleMachines,
                onBack = onBack,
                textColor = textColor,
                subtitleColor = subtitleColor,
                onToggleTheme = onToggleTheme,
                darkTheme = darkTheme,
                onToggleLanguage = onToggleLanguage,
                strings = strings
            )
            Spacer(Modifier.height(12.dp))

            if (viewModel.isLoading) {
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 100)
                    Spacer(Modifier.height(16.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 100)
                    Spacer(Modifier.height(16.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 100)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(viewModel.listaMaquinas) { index, maquina ->
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { delay(index * 50L); itemVisible = true }
                        val itemAlpha by animateFloatAsState(if (itemVisible) 1f else 0f, tween(400))
                        Card(
                            Modifier.fillMaxWidth().alpha(itemAlpha).shadow(6.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val resId = remember(maquina.id) {
                                        val id = context.resources.getIdentifier("maquina${maquina.id}", "drawable", context.packageName)
                                        if (id == 0) context.resources.getIdentifier("blanco", "drawable", context.packageName) else id
                                    }
                                    Image(painterResource(resId), null, Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(maquina.nombre, fontWeight = FontWeight.Bold, color = textColor)
                                        if (!maquina.operativa) Text(strings.outOfService, color = Color(0xFFFF6B6B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        else maquina.tipo?.let { Text(it, fontSize = 12.sp, color = subtitleColor) }
                                    }
                                    if (isAdmin) {
                                        IconButton(onClick = { alternarEstadoOperativo(maquina) }) {
                                            Icon(if (maquina.operativa) Icons.Default.Build else Icons.Default.CheckCircle, contentDescription = if (maquina.operativa) strings.maintenance else strings.contentDescApprove, tint = if (maquina.operativa) subtitleColor else BlueAccent)
                                        }
                                        IconButton(onClick = { viewModel.deleteMachine(maquina.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = strings.contentDescDelete, tint = Color(0xFFFF6B6B))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                maquina.descripcion?.let { Text(it, fontSize = 13.sp, color = textColor.copy(0.6f)) }
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        viewModel.maquinaParaReservar = maquina.id
                                        mostrarDatePickerReserva = true
                                    },
                                    enabled = maquina.operativa,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = textColor.copy(0.05f)),
                                    contentPadding = PaddingValues()
                                ) {
                                    Box(Modifier.fillMaxWidth().height(42.dp).background(
                                        if (maquina.operativa) Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)) else Brush.horizontalGradient(listOf(textColor.copy(0.1f), textColor.copy(0.1f))),
                                        RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                        Text(if (maquina.operativa) strings.reserve else strings.maintenance, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            FloatingActionButton(
                onClick = { viewModel.showingDialogAdd = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 72.dp, end = 16.dp),
                containerColor = BlueAccent,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.addMachine)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = if (darkTheme) Color(0xFF1E3A6A) else Color(0xFF1C3461),
                contentColor = Color.White,
                actionColor = Color(0xFF82B4FF),
                shape = RoundedCornerShape(14.dp)
            )
        }
    }

    if (mostrarPickerMantenimientoDesde) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarPickerMantenimientoDesde = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        viewModel.fechaDesdeM = String.format("%d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                    }
                    mostrarPickerMantenimientoDesde = false
                    mostrarTimePickerMantenimientoDesde = true
                }) { Text(strings.ok, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarPickerMantenimientoDesde = false }) { Text(strings.cancel, color = textColor.copy(0.5f)) } },
            colors = DatePickerDefaults.colors(selectedDayContentColor = Color.White, selectedDayContainerColor = BlueAccent, todayContentColor = BlueAccent, todayDateBorderColor = BlueAccent, dayContentColor = textColor, titleContentColor = Color.White, headlineContentColor = Color.White)
        ) { DatePicker(state = datePickerState) }
    }

    if (mostrarPickerMantenimientoHasta) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarPickerMantenimientoHasta = false },
            confirmButton = {
                TextButton(onClick = {
                    val mHasta = datePickerState.selectedDateMillis
                    if (mHasta != null) {
                        val cal = Calendar.getInstance().apply { timeInMillis = mHasta }
                        viewModel.fechaHastaM = String.format("%d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                        mostrarPickerMantenimientoHasta = false
                        mostrarTimePickerMantenimientoHasta = true
                    } else { mostrarPickerMantenimientoHasta = false }
                }) { Text(strings.ok, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarPickerMantenimientoHasta = false }) { Text(strings.cancel, color = textColor.copy(0.5f)) } },
            colors = DatePickerDefaults.colors(selectedDayContentColor = Color.White, selectedDayContainerColor = BlueAccent, todayContentColor = BlueAccent, todayDateBorderColor = BlueAccent, dayContentColor = textColor, titleContentColor = Color.White, headlineContentColor = Color.White)
        ) { DatePicker(state = datePickerState) }
    }

    if (mostrarTimePickerMantenimientoDesde) {
        AppTimePickerDialog(
            onDismiss = { mostrarTimePickerMantenimientoDesde = false },
            onConfirm = { h, min ->
                viewModel.horaDesdeM = String.format("%02d:%02d", h, min)
                scope.launch {
                    snackbarHostState.showSnackbar(strings.selectEndDateToast, duration = SnackbarDuration.Short)
                }
                mostrarPickerMantenimientoHasta = true
            },
            title = strings.fromWhatTime, initialHour = 12, initialMinute = 0
        )
    }

    if (mostrarTimePickerMantenimientoHasta) {
        AppTimePickerDialog(
            onDismiss = { mostrarTimePickerMantenimientoHasta = false },
            onConfirm = { h, min ->
                viewModel.horaHastaM = String.format("%02d:%02d", h, min)
                viewModel.maquinaParaMantenimiento?.let { machineId ->
                    viewModel.startMaintenance(machineId, "${viewModel.fechaDesdeM} ${viewModel.horaDesdeM}", "${viewModel.fechaHastaM} ${viewModel.horaHastaM}")
                }
            },
            title = strings.untilWhatTime, initialHour = 12, initialMinute = 0
        )
    }

    if (mostrarDatePickerReserva) {
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
                        viewModel.fechaSeleccionada = String.format("%d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                        mostrarDatePickerReserva = false
                        viewModel.maquinaParaReservar?.let { viewModel.loadReservationsForSlot(it, viewModel.fechaSeleccionada) }
                        mostrarSelectorHora = true
                    } else { mostrarDatePickerReserva = false }
                }) { Text(strings.ok, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarDatePickerReserva = false }) { Text(strings.cancel, color = textColor.copy(0.5f)) } },
            colors = DatePickerDefaults.colors(selectedDayContentColor = Color.White, selectedDayContainerColor = BlueAccent, todayContentColor = BlueAccent, todayDateBorderColor = BlueAccent, dayContentColor = textColor, titleContentColor = Color.White, headlineContentColor = Color.White)
        ) { DatePicker(state = datePickerState) }
    }

    if (mostrarSelectorHora) {
        val hoy = LocalDate.now(ZoneId.systemDefault()).toString()
        val minAhora = LocalTime.now(ZoneId.systemDefault()).let { it.hour * 60 + it.minute }
        val (hInicial, mInicial) = if (viewModel.fechaSeleccionada == hoy) {
            val proxSlot = (minAhora / 30 + 1) * 30
            (proxSlot / 60).coerceIn(7, 20) to (proxSlot % 60)
        } else 7 to 0

        AppTimePickerDialog(
            onDismiss = { mostrarSelectorHora = false },
            onConfirm = { h, min ->
                viewModel.horaSeleccionada = String.format("%02d:%02d", h, min)
                mostrarSelectorTiempo = true
            },
            onValidate = { h, m ->
                val selMin = h * 60 + m
                val nowMin = LocalTime.now(ZoneId.systemDefault()).let { it.hour * 60 + it.minute }
                if (viewModel.fechaSeleccionada == hoy && selMin <= nowMin) return@AppTimePickerDialog strings.timeMustBeLater
                val selStr = String.format("%02d:%02d", h, m)
                val ocupado = viewModel.reservasDeLaMaquina.any { r ->
                    val p1 = r.horaInicio.split(":"); val ini = p1[0].toInt() * 60 + p1[1].toInt()
                    val p2 = r.horaFin.split(":"); val fin = p2[0].toInt() * 60 + p2[1].toInt()
                    selMin >= ini && selMin < fin
                }
                if (ocupado) return@AppTimePickerDialog String.format(strings.machineOccupiedAt, selStr)
                val ocupadoUser = viewModel.reservasDelUsuario.any { r ->
                    val p1 = r.horaInicio.split(":"); val ini = p1[0].toInt() * 60 + p1[1].toInt()
                    val p2 = r.horaFin.split(":"); val fin = p2[0].toInt() * 60 + p2[1].toInt()
                    selMin >= ini && selMin < fin
                }
                if (ocupadoUser) return@AppTimePickerDialog String.format(strings.userReservedAt, selStr)
                null
            },
            title = strings.whatTime, initialHour = hInicial, initialMinute = mInicial
        )
    }

    if (mostrarSelectorTiempo) {
        val pH = viewModel.horaSeleccionada.split(":")
        val hIni = pH[0].toInt(); val mIni = pH[1].toInt()
        val iniTotalMin = hIni * 60 + mIni
        val finDefaultMin = (iniTotalMin + 30).coerceAtMost(hIni * 60 + 60)

        AppTimePickerDialog(
            onDismiss = { mostrarSelectorTiempo = false },
            onConfirm = { h, m ->
                val horaFin = String.format("%02d:%02d", h, m)
                viewModel.maquinaParaReservar?.let { machineId ->
                    viewModel.reserveTimeSlot(machineId, userId, viewModel.fechaSeleccionada, viewModel.horaSeleccionada, horaFin)
                }
                mostrarSelectorTiempo = false
            },
            onValidate = { h, m ->
                val finTotal = h * 60 + m
                val finAdj = if (finTotal <= iniTotalMin) finTotal + 1440 else finTotal
                val dur = finAdj - iniTotalMin
                if (dur > 60) return@AppTimePickerDialog strings.maxOneHour
                val chocaMaquina = viewModel.reservasDeLaMaquina.any { r ->
                    val p1 = r.horaInicio.split(":"); val rIni = p1[0].toInt() * 60 + p1[1].toInt()
                    val p2 = r.horaFin.split(":"); val rFin = p2[0].toInt() * 60 + p2[1].toInt()
                    iniTotalMin < rFin && finAdj > rIni
                }
                if (chocaMaquina) return@AppTimePickerDialog strings.machineOccupiedSchedule
                val chocaUser = viewModel.reservasDelUsuario.any { r ->
                    val p1 = r.horaInicio.split(":"); val rIni = p1[0].toInt() * 60 + p1[1].toInt()
                    val p2 = r.horaFin.split(":"); val rFin = p2[0].toInt() * 60 + p2[1].toInt()
                    iniTotalMin < rFin && finAdj > rIni
                }
                if (chocaUser) return@AppTimePickerDialog strings.userReservationConflict
                null
            },
            title = strings.untilWhatTime, initialHour = finDefaultMin / 60, initialMinute = finDefaultMin % 60
        )
    }

    if (viewModel.showingDialogAdd) {
        val dColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BlueAccent, unfocusedBorderColor = textColor.copy(0.2f), focusedTextColor = textColor, unfocusedTextColor = textColor, focusedLabelColor = BlueAccent)
        AlertDialog(
            onDismissRequest = { viewModel.showingDialogAdd = false },
            containerColor = cardBg,
            title = { Text(strings.newMachine, color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = viewModel.nuevoNombre, onValueChange = { viewModel.nuevoNombre = it }, modifier = Modifier.fillMaxWidth(), label = { Text(strings.machineName) }, colors = dColors)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = viewModel.nuevoTipo, onValueChange = { viewModel.nuevoTipo = it }, modifier = Modifier.fillMaxWidth(), label = { Text(strings.machineType) }, colors = dColors)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = viewModel.nuevaDesc, onValueChange = { viewModel.nuevaDesc = it }, modifier = Modifier.fillMaxWidth(), label = { Text(strings.machineDescription) }, colors = dColors)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addMachine(viewModel.nuevoNombre, viewModel.nuevoTipo, viewModel.nuevaDesc)
                }, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) { Text(strings.add, color = Color.White) }
            }
        )
    }
}
