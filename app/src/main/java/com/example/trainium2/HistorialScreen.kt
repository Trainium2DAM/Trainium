package com.example.trainium2

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.models.Pago
import com.example.trainium2.DbColumns
import com.example.trainium2.DbTables
import com.example.trainium2.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HistorialScreen(idUsuario: Int, isDarkTheme: Boolean, onToggleTheme: () -> Unit, onBack: () -> Unit) {
    var pagos by remember { mutableStateOf(listOf<Pago>()) }
    var cargando by remember { mutableStateOf(true) }
    var errorConexion by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var visible by remember { mutableStateOf(false) }

    val textColor = if (isDarkTheme) Color.White else BlueDark
    val subtitleColor = if (isDarkTheme) Color.White.copy(0.4f) else BlueDark.copy(0.4f)
    val cardBg = if (isDarkTheme) Color(0xFF162347) else Color.White

    val bgBrush = if (isDarkTheme) {
        Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))
    }

    fun cargarDatos() {
        cargando = true
        errorConexion = false
        scope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from(DbTables.PAGOS)
                        .select {
                            filter {
                                eq(DbColumns.ID_USUARIO, idUsuario)
                            }
                        }
                        .decodeList<Pago>()
                        .sortedByDescending { it.fechaPago }
                }
                withContext(Dispatchers.Main) {
                    pagos = data
                    cargando = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorConexion = true
                    cargando = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { delay(100); visible = true; cargarDatos() }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            ScreenHeader(
                title = "Pagos",
                subtitle = "Tu historial financiero",
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
            Spacer(Modifier.height(30.dp))

            if (cargando) {
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 70)
                    Spacer(Modifier.height(12.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 70)
                    Spacer(Modifier.height(12.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 70)
                    Spacer(Modifier.height(12.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 70)
                }
            } else if (errorConexion || pagos.isEmpty()) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (errorConexion) "Error de conexión" else "Sin pagos registrados", color = textColor.copy(0.6f))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { cargarDatos() }, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent.copy(0.1f))) {
                        Text("Reintentar", color = BlueAccent)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                    itemsIndexed(pagos) { index, pago ->
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { delay(index * 100L); itemVisible = true }
                        val animAlpha by animateFloatAsState(if (itemVisible) 1f else 0f, tween(400))

                        Card(
                            modifier = Modifier.fillMaxWidth().alpha(animAlpha).shadow(8.dp, RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg)
                        ) {
                            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(40.dp).background(BlueAccent.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(if (pago.metodoPago.contains("Tarj", true)) Icons.Default.CreditCard else Icons.Default.Payments, null, tint = BlueAccent, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(pago.tipo, fontWeight = FontWeight.Bold, color = textColor)
                                    Text(pago.fechaPago, fontSize = 11.sp, color = subtitleColor)
                                }
                                Text("${pago.monto}€", fontWeight = FontWeight.ExtraBold, color = BlueAccent, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}