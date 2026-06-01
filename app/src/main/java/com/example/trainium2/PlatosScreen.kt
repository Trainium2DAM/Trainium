package com.example.trainium2

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.models.Plato
import com.example.trainium2.DbColumns
import com.example.trainium2.DbTables
import com.example.trainium2.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@Composable
fun PlatosScreen(isAdmin: Boolean, idUsuario: Int, isDarkTheme: Boolean, onToggleTheme: () -> Unit, onBack: () -> Unit) {
    var listaPlatosAceptados by remember { mutableStateOf(listOf<Plato>()) }
    var indicePlatoActual by remember { mutableIntStateOf(0) }
    var sugerenciasPendientes by remember { mutableStateOf(listOf<Plato>()) }
    var cargando by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val platoActual = listaPlatosAceptados.getOrNull(indicePlatoActual)

    // Diálogo sugerencia
    var mostrarDialogoSugerencia by remember { mutableStateOf(false) }
    var nuevoTitulo by remember { mutableStateOf("") }
    var nuevaReceta by remember { mutableStateOf("") }
    var nuevoTiempo by remember { mutableStateOf("") }
    var caloriasTexto by remember { mutableStateOf("") }
    var fotoBase64 by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val base64 = withContext(Dispatchers.IO) { uriToBase64(it, context.contentResolver) }
                fotoBase64 = base64
            }
        }
    }

    var visible by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(if (visible) 1f else 0f, tween(800), label = "alpha")

    val textColor = if (isDarkTheme) Color.White else BlueDark
    val subtitleColor = if (isDarkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (isDarkTheme) Color(0xFF162347) else Color.White

    val bgBrush = if (isDarkTheme) Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    else Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))

    fun cargarDatos() {
        cargando = true
        errorMsg = ""
        visible = false
        scope.launch {
            try {
                // Cargar platos aceptados (visibles)
                val aceptados = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from(DbTables.PLATOS)
                        .select {
                            filter {
                                eq(DbColumns.VISIBILIDAD, true)
                                eq(DbColumns.ACEPTADO, true)
                            }
                        }
                        .decodeList<Plato>()
                }

                // Si es Admin, cargar sugerencias pendientes
                if (isAdmin) {
                    val pendientes = withContext(Dispatchers.IO) {
                        SupabaseClient.client.from(DbTables.PLATOS)
                            .select {
                                filter { eq(DbColumns.ACEPTADO, false) }
                            }
                            .decodeList<Plato>()
                    }
                    sugerenciasPendientes = pendientes
                }

                withContext(Dispatchers.Main) {
                    listaPlatosAceptados = aceptados
                    if (aceptados.isNotEmpty()) {
                        // Inicializar con el plato del día basado en la fecha
                        val dia = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                        indicePlatoActual = dia % aceptados.size
                        delay(100)
                        visible = true
                    }
                    cargando = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMsg = "Error: ${e.message}"
                    cargando = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    fun enviarSugerencia() {
        if (nuevoTitulo.isEmpty() || nuevaReceta.isEmpty() || nuevoTiempo.isEmpty()) {
            Toast.makeText(context, "Por favor, completa los campos", Toast.LENGTH_SHORT).show()
            return
        }
        val cal = caloriasTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
        scope.launch {
            try {
                val nueva = Plato(
                    idUsuario = idUsuario,
                    nombre = nuevoTitulo,
                    descripcion = nuevaReceta,
                    calorias = cal,
                    imagenUrl = fotoBase64,
                    visibilidad = true,
                    tiempo = nuevoTiempo,
                    aceptado = false
                )
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from(DbTables.PLATOS).insert(nueva)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Sugerencia enviada al administrador", Toast.LENGTH_LONG).show()
                    mostrarDialogoSugerencia = false
                    nuevoTitulo = ""; nuevaReceta = ""; nuevoTiempo = ""; caloriasTexto = ""; fotoBase64 = null
                    cargarDatos()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun aprobarSugerencia(plato: Plato) {
        val pId = plato.id ?: return
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from(DbTables.PLATOS).update({
                        set(DbColumns.ACEPTADO, true)
                    }) { filter { eq(DbColumns.ID, pId) } }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Plato aprobado", Toast.LENGTH_SHORT).show()
                    cargarDatos()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun rechazarSugerencia(plato: Plato) {
        val pId = plato.id ?: return
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from(DbTables.PLATOS).delete { filter { eq(DbColumns.ID, pId) } }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Sugerencia eliminada", Toast.LENGTH_SHORT).show()
                    cargarDatos()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            // Header
            ScreenHeader(
                title = "Nutrición",
                subtitle = "Recomendación diaria",
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

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { mostrarDialogoSugerencia = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Sugerir una Receta", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))

            if (cargando) {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlueAccent)
                }
            } else if (errorMsg.isNotEmpty()) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$errorMsg", color = textColor)
                    Button(onClick = { cargarDatos() }, Modifier.padding(top = 16.dp)) { Text("Reintentar") }
                }
            } else {
                // Plato Actual con Animación de Transición
                AnimatedContent(
                    targetState = platoActual,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(400, delayMillis = 90)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400, delayMillis = 90)))
                            .togetherWith(fadeOut(animationSpec = tween(300)))
                    },
                    label = "dishAnimation"
                ) { plato ->
                    if (plato != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "RECOMENDACIÓN ${indicePlatoActual + 1} DE ${listaPlatosAceptados.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = BlueAccent.copy(0.7f),
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(16.dp))

                            Box(Modifier.size(130.dp).shadow(20.dp, CircleShape, ambientColor = BlueAccent.copy(0.3f), spotColor = BlueAccent.copy(0.3f)).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                                if (!plato.imagenUrl.isNullOrEmpty()) {
                                    val bitmap = decodeBase64ToBitmap(plato.imagenUrl!!)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else { Icon(Icons.Default.Restaurant, null, tint = BlueAccent.copy(0.4f), modifier = Modifier.size(60.dp)) }
                                } else { Icon(Icons.Default.Restaurant, null, tint = BlueAccent.copy(0.4f), modifier = Modifier.size(60.dp)) }
                            }

                            Spacer(Modifier.height(24.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth().shadow(15.dp, RoundedCornerShape(22.dp)),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg)
                            ) {
                                Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(plato.nombre, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor, textAlign = TextAlign.Center)

                                    Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF6B35), modifier = Modifier.size(16.dp))
                                            Text("${plato.calorias ?: 0.0} kcal", color = BlueAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                                        }
                                        if (!plato.tiempo.isNullOrEmpty()) {
                                            VerticalDivider(modifier = Modifier.height(14.dp), color = textColor.copy(0.1f))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Timer, null, tint = BlueAccent, modifier = Modifier.size(16.dp))
                                                Text(plato.tiempo!!, color = textColor.copy(0.6f), fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp))
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = textColor.copy(0.1f)); Spacer(Modifier.height(18.dp))
                                    Text(plato.descripcion ?: "Sin descripción", color = textColor.copy(0.7f), textAlign = TextAlign.Center, fontSize = 16.sp, lineHeight = 24.sp)
                                }
                            }
                        }
                    }
                }

                // Botón Siguiente (Ciclo infinito: 1 -> 2 -> ... -> N -> 1)
                if (listaPlatosAceptados.size > 1) {
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            indicePlatoActual = (indicePlatoActual + 1) % listaPlatosAceptados.size
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).alpha(alphaAnim),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent.copy(0.12f), contentColor = BlueAccent)
                    ) {
                        Icon(Icons.Default.ArrowForward, null)
                        Spacer(Modifier.width(10.dp))
                        Text("Siguiente plato recomendado", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }
                }

                // Sugerencias Pendientes (Sólo Admin)
                if (isAdmin && sugerenciasPendientes.isNotEmpty()) {
                    Spacer(Modifier.height(40.dp))
                    Text("SUGERENCIAS PENDIENTES", fontSize = 12.sp, fontWeight = FontWeight.Black, color = BlueAccent, letterSpacing = 2.sp)
                    Spacer(Modifier.height(12.dp))

                    sugerenciasPendientes.forEach { sug ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (!sug.imagenUrl.isNullOrEmpty()) {
                                    val b = decodeBase64ToBitmap(sug.imagenUrl!!)
                                    if (b != null) Image(bitmap = b.asImageBitmap(), null, Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(sug.nombre, fontWeight = FontWeight.Bold, color = textColor)
                                    Text("Tiempo: ${sug.tiempo ?: "N/D"}", fontSize = 12.sp, color = subtitleColor)
                                }
                                Row {
                                    IconButton(onClick = { aprobarSugerencia(sug) }) { Icon(Icons.Default.Check, null, tint = Color(0xFF00E676)) }
                                    IconButton(onClick = { rechazarSugerencia(sug) }) { Icon(Icons.Default.Close, null, tint = Color(0xFFFF6B6B)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para sugerir plato
    if (mostrarDialogoSugerencia) {
        val dColors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = BlueAccent, unfocusedBorderColor = textColor.copy(0.2f), focusedLabelColor = BlueAccent)
        AlertDialog(
            onDismissRequest = { mostrarDialogoSugerencia = false },
            containerColor = cardBg,
            title = { Text("Sugerir Receta", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Box(Modifier.fillMaxWidth().height(120.dp).background(textColor.copy(0.05f), RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).clickable { launcher.launch("image/*") }, contentAlignment = Alignment.Center) {
                        if (!fotoBase64.isNullOrEmpty()) {
                            val bitmap = decodeBase64ToBitmap(fotoBase64!!)
                            if (bitmap != null) Image(bitmap = bitmap.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else { Text("Seleccionar foto", color = BlueAccent, fontWeight = FontWeight.SemiBold) }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = nuevoTitulo, onValueChange = { nuevoTitulo = it }, label = { Text("Título de la Receta") }, modifier = Modifier.fillMaxWidth(), colors = dColors)
                    OutlinedTextField(value = nuevoTiempo, onValueChange = { nuevoTiempo = it }, label = { Text("Tiempo (ej: 40 min)") }, modifier = Modifier.fillMaxWidth(), colors = dColors)
                    OutlinedTextField(value = caloriasTexto, onValueChange = { caloriasTexto = it }, label = { Text("Calorías aproximadas") }, modifier = Modifier.fillMaxWidth(), colors = dColors)
                    OutlinedTextField(value = nuevaReceta, onValueChange = { nuevaReceta = it }, label = { Text("Pasos de la receta") }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = dColors)
                }
            },
            confirmButton = {
                Button(onClick = { enviarSugerencia() }, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) {
                    Text("Enviar sugerencia", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSugerencia = false }) { Text("Cancelar", color = textColor.copy(0.6f)) }
            }
        )
    }
}