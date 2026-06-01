package com.example.trainium2

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.trainium2.models.Maquina
import com.example.trainium2.models.Reserva
import com.example.trainium2.AppConfig
import com.example.trainium2.DbColumns
import com.example.trainium2.DbTables
import com.example.trainium2.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaquinasScreen(isAdmin: Boolean, idUsuario: Int, isDarkTheme: Boolean, onToggleTheme: () -> Unit, onBack: () -> Unit) {
    var listaMaquinas by remember { mutableStateOf(listOf<Maquina>()) }
    var cargando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var mostrarDialogoAdd by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoTipo by remember { mutableStateOf("") }
    var nuevaDesc by remember { mutableStateOf("") }
    var maquinaParaMantenimiento by remember { mutableStateOf<Maquina?>(null) }
    var fechaDesdeM by remember { mutableStateOf("") }
    var horaDesdeM by remember { mutableStateOf("") }
    var fechaHastaM by remember { mutableStateOf("") }
    var mostrarPickerMantenimientoDesde by remember { mutableStateOf(false) }
    var mostrarPickerMantenimientoHasta by remember { mutableStateOf(false) }
    var mostrarTimePickerMantenimientoDesde by remember { mutableStateOf(false) }
    var mostrarTimePickerMantenimientoHasta by remember { mutableStateOf(false) }

    var maquinaParaReservar by remember { mutableStateOf<Maquina?>(null) }
    var mostrarSelectorTiempo by remember { mutableStateOf(false) }
    var mostrarSelectorHora by remember { mutableStateOf(false) }
    var reservasDeLaMaquina by remember { mutableStateOf(listOf<Reserva>()) }
    var reservasDelUsuario by remember { mutableStateOf(listOf<Reserva>()) }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var horaSeleccionada by remember { mutableStateOf("") }
    var mostrarDatePickerReserva by remember { mutableStateOf(false) }

    val textColor = if (isDarkTheme) Color.White else BlueDark
    val subtitleColor = if (isDarkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (isDarkTheme) Color(0xFF162347).copy(0.9f) else Color.White

    val bgBrush = if (isDarkTheme) {
        Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))
    }

    fun cargarDatos() {
        cargando = true
        scope.launch {
            try {
                val maquinas = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from(DbTables.MAQUINAS).select().decodeList<Maquina>()
                }
                withContext(Dispatchers.Main) { listaMaquinas = maquinas; cargando = false }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show(); cargando = false }
            }
        }
    }

    fun activarMantenimiento(maquina: Maquina, desde: String, hasta: String) {
        scope.launch {
            try {
                val sdfFull = SimpleDateFormat(AppConfig.FORMAT_DATE_TIME, Locale.getDefault())
                val calDesde = Calendar.getInstance().apply { time = sdfFull.parse(desde)!! }
                val calHasta = Calendar.getInstance().apply { time = sdfFull.parse(hasta)!! }
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from(DbTables.MAQUINAS).update({
                        set(DbColumns.OPERATIVA, false)
                        set("mantenimiento_desde", desde)
                        set("mantenimiento_hasta", hasta)
                    }) { filter { eq(DbColumns.ID, maquina.id) } }
                    val reservasAfectadas = SupabaseClient.client.from(DbTables.RESERVAS).select {
                        filter { eq(DbColumns.ID_MAQUINA, maquina.id); eq(DbColumns.ESTADO, true) }
                    }.decodeList<Reserva>()
                    for (res in reservasAfectadas) {
                        val sdfRes = SimpleDateFormat(AppConfig.FORMAT_DATE_TIME, Locale.getDefault())
                        val resInicio = Calendar.getInstance().apply { time = sdfRes.parse("${res.fecha} ${res.horaInicio}")!! }
                        val resFin = Calendar.getInstance().apply { time = sdfRes.parse("${res.fecha} ${res.horaFin}")!! }
                        if (resInicio.timeInMillis < calHasta.timeInMillis && resFin.timeInMillis > calDesde.timeInMillis) {
                            SupabaseClient.client.from(DbTables.RESERVAS).update({ set(DbColumns.ESTADO, false) }) { filter { eq(DbColumns.ID, res.id ?: 0) } }
                        }
                    }
                }
                withContext(Dispatchers.Main) { cargarDatos(); Toast.makeText(context, "Mantenimiento activado hasta el $hasta", Toast.LENGTH_LONG).show() }
            } catch (e: Exception) { withContext(Dispatchers.Main) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() } }
        }
    }

    fun alternarEstadoOperativo(maquina: Maquina) {
        if (maquina.operativa) {
            maquinaParaMantenimiento = maquina
            mostrarPickerMantenimientoDesde = true
        } else {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        SupabaseClient.client.from(DbTables.MAQUINAS).update({
                            set(DbColumns.OPERATIVA, true)
                            set<String?>("mantenimiento_desde", null)
                            set<String?>("mantenimiento_hasta", null)
                        }) { filter { eq(DbColumns.ID, maquina.id) } }
                    }
                    withContext(Dispatchers.Main) { cargarDatos(); Toast.makeText(context, "Maquina operativa", Toast.LENGTH_SHORT).show() }
                } catch (e: Exception) { withContext(Dispatchers.Main) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() } }
            }
        }
    }

    fun eliminarMaquina(id: Int) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) { SupabaseClient.client.from(DbTables.MAQUINAS).delete { filter { eq(DbColumns.ID, id) } } }
                withContext(Dispatchers.Main) { cargarDatos(); Toast.makeText(context, "Maquina eliminada", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) { withContext(Dispatchers.Main) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() } }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    fun ejecutarReserva(maquina: Maquina, fecha: String, horaInicio: String, duracionMinutos: Int) {
        scope.launch {
            try {
                val sdf = SimpleDateFormat(AppConfig.FORMAT_HOUR_MINUTE, Locale.getDefault())
                val calInicioNueva = Calendar.getInstance().apply { time = sdf.parse(horaInicio)!! }
                val calFinNueva = (calInicioNueva.clone() as Calendar).apply { add(Calendar.MINUTE, duracionMinutos) }
                val horaFin = sdf.format(calFinNueva.time)

                val reservasMaquina = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from(DbTables.RESERVAS).select {
                        filter { eq(DbColumns.ID_MAQUINA, maquina.id); eq(DbColumns.FECHA, fecha); eq(DbColumns.ESTADO, true) }
                    }.decodeList<Reserva>()
                }
                for (res in reservasMaquina) {
                    val exInicio = Calendar.getInstance().apply { time = sdf.parse(res.horaInicio)!! }
                    val exFin = Calendar.getInstance().apply { time = sdf.parse(res.horaFin)!! }
                    if (calInicioNueva.timeInMillis < exFin.timeInMillis && calFinNueva.timeInMillis > exInicio.timeInMillis) {
                        withContext(Dispatchers.Main) { Toast.makeText(context, "Esta maquina ya esta ocupada de ${res.horaInicio} a ${res.horaFin}", Toast.LENGTH_LONG).show() }
                        return@launch
                    }
                }

                val reservasUsuario = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from(DbTables.RESERVAS).select {
                        filter { eq(DbColumns.ID_USUARIO, idUsuario); eq(DbColumns.FECHA, fecha); eq(DbColumns.ESTADO, true) }
                    }.decodeList<Reserva>()
                }
                for (res in reservasUsuario) {
                    val exInicio = Calendar.getInstance().apply { time = sdf.parse(res.horaInicio)!! }
                    val exFin = Calendar.getInstance().apply { time = sdf.parse(res.horaFin)!! }
                    if (calInicioNueva.timeInMillis < exFin.timeInMillis && calFinNueva.timeInMillis > exInicio.timeInMillis) {
                        withContext(Dispatchers.Main) { Toast.makeText(context, "Ya tienes una reserva de ${res.horaInicio} a ${res.horaFin}", Toast.LENGTH_LONG).show() }
                        return@launch
                    }
                }

                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from(DbTables.RESERVAS).insert(Reserva(idUsuario = idUsuario, idMaquina = maquina.id, fecha = fecha, horaInicio = horaInicio, horaFin = horaFin, estado = true))
                }
                withContext(Dispatchers.Main) { Toast.makeText(context, "Reserva confirmada", Toast.LENGTH_LONG).show() }
            } catch (e: Exception) { withContext(Dispatchers.Main) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() } }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { mostrarDialogoAdd = true }, containerColor = BlueAccent, contentColor = Color.White) {
                    Icon(Icons.Default.Add, "Anadir")
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(bgBrush)) {
            Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
                ScreenHeader(
                    title = "Equipamiento",
                    subtitle = "Gestiona tus entrenamientos",
                    onBack = onBack,
                    textColor = textColor,
                    subtitleColor = subtitleColor,
                    onToggleTheme = onToggleTheme,
                    darkTheme = isDarkTheme
                )
                Spacer(Modifier.height(12.dp))

                if (cargando) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BlueAccent) }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(listaMaquinas) { index, maquina ->
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
                                            if (!maquina.operativa) Text("FUERA DE SERVICIO", color = Color(0xFFFF6B6B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            else maquina.tipo?.let { Text(it, fontSize = 12.sp, color = subtitleColor) }
                                        }
                                        if (isAdmin) {
                                            IconButton(onClick = { alternarEstadoOperativo(maquina) }) {
                                                Icon(if (maquina.operativa) Icons.Default.Build else Icons.Default.CheckCircle, null, tint = if (maquina.operativa) subtitleColor else BlueAccent)
                                            }
                                            IconButton(onClick = { eliminarMaquina(maquina.id) }) {
                                                Icon(Icons.Default.Delete, null, tint = Color(0xFFFF6B6B))
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    maquina.descripcion?.let { Text(it, fontSize = 13.sp, color = textColor.copy(0.6f)) }
                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = { maquinaParaReservar = maquina; mostrarDatePickerReserva = true },
                                        enabled = maquina.operativa,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = textColor.copy(0.05f)),
                                        contentPadding = PaddingValues()
                                    ) {
                                        Box(Modifier.fillMaxWidth().height(42.dp).background(
                                            if (maquina.operativa) Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)) else Brush.horizontalGradient(listOf(textColor.copy(0.1f), textColor.copy(0.1f))),
                                            RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                            Text(if (maquina.operativa) "Reservar" else "Mantenimiento", fontWeight = FontWeight.SemiBold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
                        fechaDesdeM = String.format("%d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                    }
                    mostrarPickerMantenimientoDesde = false
                    mostrarTimePickerMantenimientoDesde = true
                }) { Text("OK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarPickerMantenimientoDesde = false }) { Text("Cancelar", color = textColor.copy(0.5f)) } },
            colors = DatePickerDefaults.colors(
                selectedDayContentColor = Color.White, selectedDayContainerColor = BlueAccent,
                todayContentColor = BlueAccent, todayDateBorderColor = BlueAccent,
                dayContentColor = textColor, titleContentColor = Color.White, headlineContentColor = Color.White
            )
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
                        fechaHastaM = String.format("%d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                        mostrarPickerMantenimientoHasta = false
                        mostrarTimePickerMantenimientoHasta = true
                    } else { mostrarPickerMantenimientoHasta = false }
                }) { Text("OK", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarPickerMantenimientoHasta = false }) { Text("Cancelar", color = textColor.copy(0.5f)) } },
            colors = DatePickerDefaults.colors(
                selectedDayContentColor = Color.White, selectedDayContainerColor = BlueAccent,
                todayContentColor = BlueAccent, todayDateBorderColor = BlueAccent,
                dayContentColor = textColor, titleContentColor = Color.White, headlineContentColor = Color.White
            )
        ) { DatePicker(state = datePickerState) }
    }

    if (mostrarTimePickerMantenimientoDesde) {
        AppTimePickerDialog(
            onDismiss = { mostrarTimePickerMantenimientoDesde = false },
            onConfirm = { h, min ->
                horaDesdeM = String.format("%02d:%02d", h, min)
                Toast.makeText(context, "Ahora selecciona la fecha de FIN", Toast.LENGTH_SHORT).show()
                mostrarPickerMantenimientoHasta = true
            },
            title = "Desde que hora?",
            initialHour = 12,
            initialMinute = 0
        )
    }

    if (mostrarTimePickerMantenimientoHasta) {
        AppTimePickerDialog(
            onDismiss = { mostrarTimePickerMantenimientoHasta = false },
            onConfirm = { h, min ->
                val hHasta = String.format("%02d:%02d", h, min)
                maquinaParaMantenimiento?.let { activarMantenimiento(it, "$fechaDesdeM $horaDesdeM", "$fechaHastaM $hHasta") }
            },
            title = "Hasta que hora?",
            initialHour = 12,
            initialMinute = 0
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
                        fechaSeleccionada = String.format("%d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                        mostrarDatePickerReserva = false
                        scope.launch {
                            try {
                                cargando = true
                                val dataM = withContext(Dispatchers.IO) {
                                    SupabaseClient.client.from(DbTables.RESERVAS).select {
                                        filter { eq(DbColumns.ID_MAQUINA, maquinaParaReservar?.id ?: 0); eq(DbColumns.FECHA, fechaSeleccionada); eq(DbColumns.ESTADO, true) }
                                    }.decodeList<Reserva>()
                                }
                                val dataU = withContext(Dispatchers.IO) {
                                    SupabaseClient.client.from(DbTables.RESERVAS).select {
                                        filter { eq(DbColumns.ID_USUARIO, idUsuario); eq(DbColumns.FECHA, fechaSeleccionada); eq(DbColumns.ESTADO, true) }
                                    }.decodeList<Reserva>()
                                }
                                withContext(Dispatchers.Main) { reservasDeLaMaquina = dataM; reservasDelUsuario = dataU; mostrarSelectorHora = true; cargando = false }
                            } catch (e: Exception) { cargando = false; Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
                        }
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

    if (mostrarSelectorHora) {
        val hoy = LocalDate.now(ZoneId.systemDefault()).toString()
        val minAhora = LocalTime.now(ZoneId.systemDefault()).let { it.hour * 60 + it.minute }
        val (hInicial, mInicial) = if (fechaSeleccionada == hoy) {
            val proxSlot = (minAhora / 30 + 1) * 30
            (proxSlot / 60).coerceIn(7, 20) to (proxSlot % 60)
        } else 7 to 0

        AppTimePickerDialog(
            onDismiss = { mostrarSelectorHora = false },
            onConfirm = { h, min ->
                horaSeleccionada = String.format("%02d:%02d", h, min)
                mostrarSelectorTiempo = true
            },
            onValidate = { h, m ->
                val selMin = h * 60 + m
                val nowMin = LocalTime.now(ZoneId.systemDefault()).let { it.hour * 60 + it.minute }
                if (fechaSeleccionada == hoy && selMin <= nowMin) return@AppTimePickerDialog "La hora debe ser posterior a la actual"
                val selStr = String.format("%02d:%02d", h, m)
                val ocupado = reservasDeLaMaquina.any { r ->
                    val p1 = r.horaInicio.split(":"); val ini = p1[0].toInt() * 60 + p1[1].toInt()
                    val p2 = r.horaFin.split(":"); val fin = p2[0].toInt() * 60 + p2[1].toInt()
                    selMin >= ini && selMin < fin
                }
                if (ocupado) return@AppTimePickerDialog "Esta maquina ya esta ocupada a las $selStr"
                val ocupadoUser = reservasDelUsuario.any { r ->
                    val p1 = r.horaInicio.split(":"); val ini = p1[0].toInt() * 60 + p1[1].toInt()
                    val p2 = r.horaFin.split(":"); val fin = p2[0].toInt() * 60 + p2[1].toInt()
                    selMin >= ini && selMin < fin
                }
                if (ocupadoUser) return@AppTimePickerDialog "Ya tienes una reserva a las $selStr"
                null
            },
            title = "A que hora?",
            initialHour = hInicial,
            initialMinute = mInicial
        )
    }

    if (mostrarSelectorTiempo) {
        val pH = horaSeleccionada.split(":")
        val hIni = pH[0].toInt(); val mIni = pH[1].toInt()
        val iniTotalMin = hIni * 60 + mIni
        val finDefaultMin = (iniTotalMin + 30).coerceAtMost(hIni * 60 + 60)

        AppTimePickerDialog(
            onDismiss = { mostrarSelectorTiempo = false },
            onConfirm = { h, m ->
                val finTotal = h * 60 + m
                val dur = if (finTotal <= iniTotalMin) finTotal + 1440 - iniTotalMin else finTotal - iniTotalMin
                maquinaParaReservar?.let { ejecutarReserva(it, fechaSeleccionada, horaSeleccionada, dur) }
            },
            onValidate = { h, m ->
                val finTotal = h * 60 + m
                val finAdj = if (finTotal <= iniTotalMin) finTotal + 1440 else finTotal
                val dur = finAdj - iniTotalMin
                if (dur > 60) return@AppTimePickerDialog "Maximo 1 hora por reserva"
                val chocaMaquina = reservasDeLaMaquina.any { r ->
                    val p1 = r.horaInicio.split(":"); val rIni = p1[0].toInt() * 60 + p1[1].toInt()
                    val p2 = r.horaFin.split(":"); val rFin = p2[0].toInt() * 60 + p2[1].toInt()
                    iniTotalMin < rFin && finAdj > rIni
                }
                if (chocaMaquina) return@AppTimePickerDialog "La maquina ya esta ocupada en ese horario"
                val chocaUser = reservasDelUsuario.any { r ->
                    val p1 = r.horaInicio.split(":"); val rIni = p1[0].toInt() * 60 + p1[1].toInt()
                    val p2 = r.horaFin.split(":"); val rFin = p2[0].toInt() * 60 + p2[1].toInt()
                    iniTotalMin < rFin && finAdj > rIni
                }
                if (chocaUser) return@AppTimePickerDialog "Ya tienes una reserva en ese horario"
                null
            },
            title = "Hasta que hora?",
            initialHour = finDefaultMin / 60,
            initialMinute = finDefaultMin % 60
        )
    }

    if (mostrarDialogoAdd) {
        val dColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BlueAccent, unfocusedBorderColor = textColor.copy(0.2f), focusedTextColor = textColor, unfocusedTextColor = textColor, focusedLabelColor = BlueAccent)
        AlertDialog(
            onDismissRequest = { mostrarDialogoAdd = false },
            containerColor = cardBg,
            title = { Text("Nueva Maquina", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre") }, colors = dColors)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = nuevoTipo, onValueChange = { nuevoTipo = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Tipo") }, colors = dColors)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = nuevaDesc, onValueChange = { nuevaDesc = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Descripcion") }, colors = dColors)
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            val nuevaM = Maquina(id = 0, nombre = nuevoNombre, tipo = nuevoTipo, descripcion = nuevaDesc, operativa = true, estado = 1)
                            withContext(Dispatchers.IO) { SupabaseClient.client.from(DbTables.MAQUINAS).insert(nuevaM) }
                            withContext(Dispatchers.Main) { mostrarDialogoAdd = false; nuevoNombre = ""; nuevoTipo = ""; nuevaDesc = ""; cargarDatos() }
                        } catch (e: Exception) { withContext(Dispatchers.Main) { Toast.makeText(context, "Error al anadir: ${e.message}", Toast.LENGTH_SHORT).show() } }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) { Text("Anadir", color = Color.White) }
            }
        )
    }
}
