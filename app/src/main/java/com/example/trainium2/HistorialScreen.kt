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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainium2.models.Pago
import com.example.trainium2.data.i18n.LocalStrings
import com.example.trainium2.ui.theme.*
import com.example.trainium2.AppConfig
import com.example.trainium2.ui.viewmodel.HistorialViewModel
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HistorialScreen(userId: Int, darkTheme: Boolean, onToggleTheme: () -> Unit, onToggleLanguage: () -> Unit, onBack: () -> Unit) {
    val strings = LocalStrings.current
    val viewModel = viewModel<HistorialViewModel>()
    var visible by remember { mutableStateOf(false) }

    val textColor = if (darkTheme) Color.White else BlueDark
    val subtitleColor = if (darkTheme) Color.White.copy(0.4f) else BlueDark.copy(0.4f)
    val cardBg = if (darkTheme) Color(0xFF162347) else Color.White

    val bgBrush = if (darkTheme) {
        Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))
    }

    LaunchedEffect(Unit) { delay(100); visible = true; viewModel.loadPagos(userId) }

    val mesesPorTipo = mapOf(
        "Mensual" to 1, "Monthly" to 1,
        "Semestral" to 6, "Semiannual" to 6,
        "Anual" to 12, "Annual" to 12, "Yearly" to 12
    )

    fun calcularFinSuscripcion(inicio: String, tipo: String): String? {
        val meses = mesesPorTipo.entries.firstOrNull { tipo.contains(it.key, ignoreCase = true) }?.value ?: return null
        val cal = Calendar.getInstance()
        AppConfig.FORMAT_ISO_DATE.parse(inicio)?.let { cal.time = it } ?: return null
        cal.add(Calendar.MONTH, meses)
        return AppConfig.FORMAT_ISO_DATE.format(cal.time)
    }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            ScreenHeader(
                title = strings.paymentHistory2,
                subtitle = strings.subtitleHistory,
                onBack = onBack,
                trailing = {
                    IconButton(onClick = { viewModel.loadPagos(userId) }) {
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
            Spacer(Modifier.height(30.dp))

            if (viewModel.isLoading) {
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 70)
                    Spacer(Modifier.height(12.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 70)
                    Spacer(Modifier.height(12.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 70)
                    Spacer(Modifier.height(12.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 70)
                }
            } else if (viewModel.error != null || viewModel.pagos.isEmpty()) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (viewModel.error != null) strings.connectionError else strings.noPayments, color = textColor.copy(0.6f), modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadPagos(userId) }, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent.copy(0.1f))) {
                        Text(strings.retry, color = BlueAccent)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                    itemsIndexed(viewModel.pagos) { index, pago ->
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
                                    val isCard = pago.metodoPago.contains("Tarj", ignoreCase = true) || pago.metodoPago.contains(strings.creditCard.take(4), ignoreCase = true)
                                    Icon(if (isCard) Icons.Default.CreditCard else Icons.Default.Payments, contentDescription = strings.paymentMethod, tint = BlueAccent, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(pago.tipo, fontWeight = FontWeight.Bold, color = textColor)
                                    val finPago = calcularFinSuscripcion(pago.fechaPago, pago.tipo)
                                    val rango = if (finPago != null) strings.dateFromTo.format(pago.fechaPago, finPago) else pago.fechaPago
                                    Text(rango, fontSize = 11.sp, color = subtitleColor)
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