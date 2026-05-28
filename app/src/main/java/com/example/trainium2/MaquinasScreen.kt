package com.example.trainium2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.trainium2.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MaquinasScreen(isAdmin: Boolean, idUsuario: Int, isDarkTheme: Boolean, onBack: () -> Unit) {
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
    var horaHastaM by remember { mutableStateOf("") }

    var maquinaParaReservar by remember { mutableStateOf<Maquina?>(null) }
    var mostrarSelectorTiempo by remember { mutableStateOf(false) }
    var mostrarSelectorHora by remember { mutableStateOf(false) }
    var reservasDeLaMaquina by remember { mutableStateOf(listOf<Reserva>()) }
    var reservasDelUsuario by remember { mutableStateOf(listOf<Reserva>()) }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var horaSeleccionada by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()

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
                    SupabaseClient.client.from("maquinas")
                        .select()
                        .decodeList<Maquina>()
                }
                withContext(Dispatchers.Main) {
                    listaMaquinas = maquinas
                    cargando = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al cargar máquinas: ${e.message}", Toast.LENGTH_SHORT).show()
                    cargando = false
                }
            }
        }
    }

    fun activarMantenimiento(maquina: Maquina, desde: String, hasta: String) {
        scope.launch {
            try {
                val sdfFull = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val calDesde = Calendar.getInstance().apply { time = sdfFull.parse(desde)!! }
                val calHasta = Calendar.getInstance().apply { time = sdfFull.parse(hasta)!! }

                withContext(Dispatchers.IO) {
                    // 1. Poner máquina en mantenimiento con rango completo
                    SupabaseClient.client.from("maquinas")
                        .update({
                            set("operativa", false)
                            set("mantenimiento_desde", desde)
                            set("mantenimiento_hasta", hasta)
                        }) { filter { eq("id", maquina.id) } }

                    // 2. Buscar TODAS las reservas de esta máquina
                    val reservasAfectadas = SupabaseClient.client.from("reservas")
                        .select {
                            filter {
                                eq("id_maquina", maquina.id)
                                eq("estado", true)
                            }
                        }.decodeList<Reserva>()

                    for (res in reservasAfectadas) {
                        val resInicio = Calendar.getInstance().apply { 
                            val sdfRes = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            time = sdfRes.parse("${res.fecha} ${res.horaInicio}")!! 
                        }
                        val resFin = Calendar.getInstance().apply { 
                            val sdfRes = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            time = sdfRes.parse("${res.fecha} ${res.horaFin}")!! 
                        }

                        // Comprobar solapamiento de rangos
                        val solapa = resInicio.timeInMillis < calHasta.timeInMillis && 
                                     resFin.timeInMillis > calDesde.timeInMillis

                        if (solapa) {
                            SupabaseClient.client.from("reservas")
                                .update({ set("estado", false) }) { filter { eq("id", res.id ?: 0) } }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    cargarDatos()
                    Toast.makeText(context, "Mantenimiento activado hasta el $hasta", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    val pickerMantenimientoHasta = DatePickerDialog(context, { _, y, m, d ->
        val fHasta = String.format("%d-%02d-%02d", y, m + 1, d)
        TimePickerDialog(context, { _, h, min ->
            val hHasta = String.format("%02d:%02d", h, min)
            maquinaParaMantenimiento?.let { 
                activarMantenimiento(it, "$fechaDesdeM $horaDesdeM", "$fHasta $hHasta") 
            }
        }, 12, 0, true).show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    val pickerMantenimientoDesde = DatePickerDialog(context, { _, y, m, d ->
        fechaDesdeM = String.format("%d-%02d-%02d", y, m + 1, d)
        TimePickerDialog(context, { _, h, min ->
            horaDesdeM = String.format("%02d:%02d", h, min)
            // Una vez elegido el inicio, pedimos el fin
            Toast.makeText(context, "Ahora selecciona la fecha de FIN", Toast.LENGTH_SHORT).show()
            pickerMantenimientoHasta.show()
        }, 12, 0, true).show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    fun alternarEstadoOperativo(maquina: Maquina) {
        if (maquina.operativa) {
            maquinaParaMantenimiento = maquina
            pickerMantenimientoDesde.show()
        } else {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        SupabaseClient.client.from("maquinas")
                            .update({
                                set("operativa", true)
                                set<String?>("mantenimiento_desde", null)
                                set<String?>("mantenimiento_hasta", null)
                            }) { filter { eq("id", maquina.id) } }
                    }
                    withContext(Dispatchers.Main) {
                        cargarDatos(); Toast.makeText(context, "Máquina operativa", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }

    fun eliminarMaquina(id: Int) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("maquinas")
                        .delete {
                            filter {
                                eq("id", id)
                            }
                        }
                }
                withContext(Dispatchers.Main) {
                    cargarDatos()
                    Toast.makeText(context, "Máquina eliminada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    fun ejecutarReserva(maquina: Maquina, fecha: String, horaInicio: String, duracionMinutos: Int) {
        scope.launch {
            try {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val calInicioNueva = Calendar.getInstance().apply { time = sdf.parse(horaInicio)!! }
                val calFinNueva = (calInicioNueva.clone() as Calendar).apply { add(Calendar.MINUTE, duracionMinutos) }
                val horaFin = sdf.format(calFinNueva.time)
                
                // 1. Obtener TODAS las reservas de esta máquina para ese día (Global)
                val reservasMaquina = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("reservas")
                        .select {
                            filter {
                                eq("id_maquina", maquina.id)
                                eq("fecha", fecha)
                                eq("estado", true)
                            }
                        }.decodeList<Reserva>()
                }

                // 2. Comprobar solapamiento global (Máquina ocupada)
                for (res in reservasMaquina) {
                    val exInicio = Calendar.getInstance().apply { time = sdf.parse(res.horaInicio)!! }
                    val exFin = Calendar.getInstance().apply { time = sdf.parse(res.horaFin)!! }
                    if (calInicioNueva.timeInMillis < exFin.timeInMillis && calFinNueva.timeInMillis > exInicio.timeInMillis) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "⚠️ Esta máquina ya está ocupada de ${res.horaInicio} a ${res.horaFin}", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }
                }

                // 3. Comprobar solapamiento del USUARIO (Ya tiene otra reserva)
                val reservasUsuario = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("reservas")
                        .select {
                            filter {
                                eq("id_usuario", idUsuario)
                                eq("fecha", fecha)
                                eq("estado", true)
                            }
                        }.decodeList<Reserva>()
                }

                for (res in reservasUsuario) {
                    val exInicio = Calendar.getInstance().apply { time = sdf.parse(res.horaInicio)!! }
                    val exFin = Calendar.getInstance().apply { time = sdf.parse(res.horaFin)!! }
                    if (calInicioNueva.timeInMillis < exFin.timeInMillis && calFinNueva.timeInMillis > exInicio.timeInMillis) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "⚠️ Ya tienes una reserva de ${res.horaInicio} a ${res.horaFin}", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }
                }

                val nuevaReserva = Reserva(
                    idUsuario = idUsuario,
                    idMaquina = maquina.id,
                    fecha = fecha,
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    estado = true
                )

                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("reservas").insert(nuevaReserva)
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ Reserva confirmada", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val datePickerDialog = DatePickerDialog(context, { _, y, m, d ->
        fechaSeleccionada = String.format("%d-%02d-%02d", y, m + 1, d)
        scope.launch {
            try {
                cargando = true
                val dataM = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("reservas")
                        .select {
                            filter {
                                eq("id_maquina", maquinaParaReservar?.id ?: 0)
                                eq("fecha", fechaSeleccionada)
                                eq("estado", true)
                            }
                        }.decodeList<Reserva>()
                }
                val dataU = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("reservas")
                        .select {
                            filter {
                                eq("id_usuario", idUsuario)
                                eq("fecha", fechaSeleccionada)
                                eq("estado", true)
                            }
                        }.decodeList<Reserva>()
                }
                withContext(Dispatchers.Main) {
                    reservasDeLaMaquina = dataM
                    reservasDelUsuario = dataU
                    mostrarSelectorHora = true
                    cargando = false
                }
            } catch (e: Exception) {
                cargando = false
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
        datePicker.minDate = System.currentTimeMillis()
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { mostrarDialogoAdd = true }, containerColor = BlueAccent, contentColor = Color.White) {
                    Icon(Icons.Default.Add, "Añadir")
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(bgBrush)) {
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text("← Volver", color = BlueAccent, fontWeight = FontWeight.Bold) }
                    Column(Modifier.weight(1f)) {
                        Text("Equipamiento", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Text("Gestiona tus entrenamientos", fontSize = 12.sp, color = subtitleColor)
                    }
                }
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
                                        onClick = { maquinaParaReservar = maquina; datePickerDialog.show() },
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

    if (mostrarSelectorHora) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { mostrarSelectorHora = false },
            containerColor = cardBg,
            title = { Text("¿A qué hora?", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.height(300.dp)) {
                    Text("Gris: Máquina ocupada | Amarillo: Tú tienes reserva", color = subtitleColor, fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val horas = (7..20).flatMap { h -> listOf("$h:00", "$h:30") }
                        itemsIndexed(horas) { _, hStr ->
                            val formattedH = if (hStr.length == 4) "0$hStr" else hStr
                            val calH = Calendar.getInstance().apply { time = sdf.parse(formattedH)!! }
                            
                            // 1. ¿Máquina ocupada por OTRO usuario?
                            val ocupadoMaquina = reservasDeLaMaquina.any { r ->
                                val rIni = Calendar.getInstance().apply { time = sdf.parse(r.horaInicio)!! }
                                val rFin = Calendar.getInstance().apply { time = sdf.parse(r.horaFin)!! }
                                calH.timeInMillis >= rIni.timeInMillis && calH.timeInMillis < rFin.timeInMillis
                            }

                            // 2. ¿USUARIO ocupado él mismo?
                            val ocupadoUsuario = reservasDelUsuario.any { r ->
                                val rIni = Calendar.getInstance().apply { time = sdf.parse(r.horaInicio)!! }
                                val rFin = Calendar.getInstance().apply { time = sdf.parse(r.horaFin)!! }
                                calH.timeInMillis >= rIni.timeInMillis && calH.timeInMillis < rFin.timeInMillis
                            }
                            
                            // 3. ¿Solapamiento potencial (Amarillo clickable)?
                            // Si a esta hora estoy libre, pero tengo algo en la próxima hora
                            val riesgoUsuario = !ocupadoUsuario && reservasDelUsuario.any { r ->
                                val rIni = Calendar.getInstance().apply { time = sdf.parse(r.horaInicio)!! }
                                // Si mi próxima reserva empieza en menos de 60 min
                                calH.timeInMillis < rIni.timeInMillis && (calH.timeInMillis + 60*60*1000) > rIni.timeInMillis
                            }

                            val colorBoton = when {
                                ocupadoMaquina -> Color.LightGray
                                ocupadoUsuario -> Color(0xFFFFEB3B).copy(0.6f) // Amarillo claro
                                riesgoUsuario -> Color(0xFFFFEB3B).copy(0.9f) // Amarillo más intenso
                                else -> BlueAccent
                            }

                            Button(
                                onClick = { 
                                    horaSeleccionada = formattedH
                                    mostrarSelectorHora = false
                                    mostrarSelectorTiempo = true 
                                },
                                enabled = !ocupadoMaquina && !ocupadoUsuario,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorBoton,
                                    disabledContainerColor = colorBoton.copy(0.4f)
                                )
                            ) {
                                Text(formattedH, color = if (ocupadoMaquina || ocupadoUsuario || riesgoUsuario) Color.DarkGray else Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (mostrarSelectorTiempo) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { mostrarSelectorTiempo = false },
            containerColor = cardBg,
            title = { Text("Duración de la reserva", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Selecciona cuánto tiempo vas a usar la máquina:", color = textColor.copy(0.7f), fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 45, 60).forEach { mins ->
                            val calInicio = Calendar.getInstance().apply { time = sdf.parse(horaSeleccionada)!! }
                            val calFin = (calInicio.clone() as Calendar).apply { add(Calendar.MINUTE, mins) }
                            
                            // Comprobamos si esta duración choca con algo del usuario
                            val chocaUsuario = reservasDelUsuario.any { r ->
                                val rIni = Calendar.getInstance().apply { time = sdf.parse(r.horaInicio)!! }
                                val rFin = Calendar.getInstance().apply { time = sdf.parse(r.horaFin)!! }
                                calInicio.timeInMillis < rFin.timeInMillis && calFin.timeInMillis > rIni.timeInMillis
                            }
                            
                            val chocaMaquina = reservasDeLaMaquina.any { r ->
                                val rIni = Calendar.getInstance().apply { time = sdf.parse(r.horaInicio)!! }
                                val rFin = Calendar.getInstance().apply { time = sdf.parse(r.horaFin)!! }
                                calInicio.timeInMillis < rFin.timeInMillis && calFin.timeInMillis > rIni.timeInMillis
                            }

                            Button(
                                onClick = {
                                    maquinaParaReservar?.let { 
                                        ejecutarReserva(it, fechaSeleccionada, horaSeleccionada, mins) 
                                    }
                                    mostrarSelectorTiempo = false
                                },
                                enabled = !chocaUsuario && !chocaMaquina,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (chocaUsuario) Color(0xFFFFEB3B).copy(0.6f) else BlueAccent,
                                    disabledContainerColor = Color.LightGray.copy(0.3f)
                                )
                            ) {
                                Text("${mins} minutos", color = if(chocaUsuario) Color.DarkGray else Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (mostrarDialogoAdd) {
        val dColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BlueAccent, unfocusedBorderColor = textColor.copy(0.2f), focusedTextColor = textColor, unfocusedTextColor = textColor, focusedLabelColor = BlueAccent)
        AlertDialog(
            onDismissRequest = { mostrarDialogoAdd = false },
            containerColor = cardBg,
            title = { Text("Nueva Máquina", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre") }, colors = dColors)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = nuevoTipo, onValueChange = { nuevoTipo = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Tipo") }, colors = dColors)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = nuevaDesc, onValueChange = { nuevaDesc = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Descripción") }, colors = dColors)
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            val nuevaM = Maquina(id = 0, nombre = nuevoNombre, tipo = nuevoTipo, descripcion = nuevaDesc, operativa = true, estado = 1)
                            withContext(Dispatchers.IO) {
                                SupabaseClient.client.from("maquinas").insert(nuevaM)
                            }
                            withContext(Dispatchers.Main) { 
                                mostrarDialogoAdd = false; nuevoNombre = ""; nuevoTipo = ""; nuevaDesc = ""; cargarDatos() 
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error al añadir: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) { Text("Añadir", color = Color.White) }
            }
        )
    }
}
