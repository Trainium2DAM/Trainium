package com.example.trainium2

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.models.Pago
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
import java.util.*

@Composable
fun PremiumSelectionScreen(idUsuario: Int, isDarkTheme: Boolean, onToggleTheme: () -> Unit, onBack: () -> Unit, onSuccess: () -> Unit) {
    var planSeleccionado by remember { mutableStateOf("") }
    var metodoSeleccionado by remember { mutableStateOf("") }
    var numeroTarjeta by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(if (visible) 1f else 0f, tween(600), label = "alpha")

    val planes = listOf(
        Triple("Mensual", 9.99, 1),
        Triple("Semestral", 49.99, 6),
        Triple("Anual", 89.99, 12)
    )

    val textColor = if (isDarkTheme) Color.White else BlueDark
    val subtitleColor = if (isDarkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (isDarkTheme) Color(0xFF162347) else Color.White
    val bgBrush = if (isDarkTheme) Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep)) else Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFD6E4FF)))

    LaunchedEffect(Unit) { delay(100); visible = true }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            ScreenHeader(
                title = "Hazte Premium",
                subtitle = "Entrena sin límites",
                onBack = onBack,
                trailing = {
                    Icon(Icons.Default.WorkspacePremium, null, tint = Color(0xFFFFD700), modifier = Modifier.padding(end = 4.dp))
                },
                textColor = textColor,
                subtitleColor = subtitleColor,
                onToggleTheme = onToggleTheme,
                darkTheme = isDarkTheme
            )

            Spacer(Modifier.height(30.dp))

            planes.forEach { (nombre, precio, meses) ->
                val isSelected = planSeleccionado == nombre
                Card(
                    onClick = { planSeleccionado = nombre },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).alpha(alphaAnim),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) BlueAccent else textColor.copy(0.1f)),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) BlueAccent.copy(0.1f) else cardBg)
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(BlueAccent.copy(0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(nombre, fontWeight = FontWeight.Bold, color = textColor, fontSize = 18.sp)
                            Text("$meses ${if (meses == 1) "mes" else "meses"}", color = textColor.copy(0.5f))
                        }
                        Text("${precio}€", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                        if (isSelected) {
                            Spacer(Modifier.width(10.dp))
                            Icon(Icons.Default.CheckCircle, null, tint = BlueAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("MÉTODO DE PAGO", fontSize = 11.sp, color = textColor.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Tarjeta de crédito" to Icons.Default.CreditCard, "PayPal" to Icons.Default.Payments).forEach { (metodo, icon) ->
                    val isSelected = metodoSeleccionado == metodo
                    Button(
                        onClick = { metodoSeleccionado = metodo },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) BlueAccent else cardBg)
                    ) {
                        Icon(icon, null, tint = if (isSelected) Color.White else textColor.copy(0.6f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(metodo, fontSize = 12.sp, color = if (isSelected) Color.White else textColor)
                    }
                }
            }

            if (metodoSeleccionado == "Tarjeta de crédito") {
                val dColors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = BlueAccent, unfocusedBorderColor = textColor.copy(0.2f), focusedLabelColor = BlueAccent)
                OutlinedTextField(value = numeroTarjeta, onValueChange = { if (it.length <= 16) numeroTarjeta = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Número de tarjeta") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dColors)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = fechaVencimiento, onValueChange = { input -> val clean = input.replace("/", "").filter { it.isDigit() }; if (clean.length <= 4) { fechaVencimiento = if (clean.length >= 3) "${clean.substring(0, 2)}/${clean.substring(2)}" else clean } }, modifier = Modifier.weight(1f), label = { Text("MM/AA") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dColors)
                    OutlinedTextField(value = cvv, onValueChange = { if (it.length <= 3) cvv = it }, modifier = Modifier.weight(0.6f), label = { Text("CVV") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dColors)
                }
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {
                    if (planSeleccionado.isEmpty() || metodoSeleccionado.isEmpty()) { Toast.makeText(context, "Completa la selección", Toast.LENGTH_SHORT).show(); return@Button }

                    scope.launch {
                        try {
                            val plan = planes.first { it.first == planSeleccionado }
                            val sdf = AppConfig.FORMAT_ISO_DATE
                            val hoy = sdf.format(Date())
                            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, plan.third) }
                            val fin = sdf.format(cal.time)

                            val nuevoPago = Pago(
                                idUsuario = idUsuario,
                                monto = plan.second,
                                fechaPago = hoy,
                                tipo = plan.first,
                                metodoPago = metodoSeleccionado
                            )

                            withContext(Dispatchers.IO) {
                                // 1. Registrar pago
                                SupabaseClient.client.from(DbTables.PAGOS).insert(nuevoPago)

                                // 2. Actualizar estado premium del usuario
                                SupabaseClient.client.from(DbTables.USUARIOS).update({
                                    set(DbColumns.PREMIUM, true)
                                    set("fecha_ini_prem", hoy)
                                    set("fecha_fin_prem", fin)
                                }) {
                                    filter { eq(DbColumns.ID, idUsuario) }
                                }
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "¡Ya eres Premium!", Toast.LENGTH_LONG).show()
                                onSuccess()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error al procesar el pago: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).alpha(alphaAnim),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("CONFIRMAR PAGO", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}