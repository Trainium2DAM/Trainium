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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainium2.data.i18n.LocalStrings
import com.example.trainium2.ui.theme.*
import com.example.trainium2.ui.viewmodel.PremiumSelectionViewModel
import kotlinx.coroutines.delay

@Composable
fun PremiumSelectionScreen(
    userId: Int,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val strings = LocalStrings.current
    val viewModel = viewModel<PremiumSelectionViewModel>()
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(if (visible) 1f else 0f, tween(600), label = "alpha")

    val textColor = if (darkTheme) Color.White else BlueDark
    val subtitleColor = if (darkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (darkTheme) Color(0xFF162347) else Color.White
    val bgBrush = if (darkTheme) Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep)) else Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFD6E4FF)))

    LaunchedEffect(Unit) { delay(100); visible = true }
    LaunchedEffect(userId) { viewModel.loadUser(userId) }
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { code ->
            val msg = when (code) {
                "invalid_card_number" -> strings.invalidCardNumber
                "invalid_cvv" -> strings.invalidCvv
                "invalid_expiry_date" -> strings.invalidExpiryDate
                "card_expired" -> strings.cardExpired
                else -> code
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            ScreenHeader(
                title = strings.premiumPlans,
                subtitle = strings.subtitlePremium,
                onBack = onBack,
                trailing = {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = strings.premiumPlans, tint = Color(0xFFFFD700), modifier = Modifier.padding(end = 4.dp))
                },
                textColor = textColor,
                subtitleColor = subtitleColor,
                onToggleTheme = onToggleTheme,
                darkTheme = darkTheme,
                onToggleLanguage = onToggleLanguage,
                strings = strings
            )

            Spacer(Modifier.height(30.dp))

            viewModel.planes.forEachIndexed { index, plan ->
                val isSelected = viewModel.planSeleccionado == index
                Card(
                    onClick = { viewModel.planSeleccionado = index },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).alpha(alphaAnim),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) BlueAccent else textColor.copy(0.1f)),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) BlueAccent.copy(0.1f) else cardBg)
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(BlueAccent.copy(0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Star, contentDescription = strings.premium, tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(plan.nombre, fontWeight = FontWeight.Bold, color = textColor, fontSize = 18.sp)
                            Text("${plan.meses} ${if (plan.meses == 1) strings.month else strings.months}", color = textColor.copy(0.5f))
                        }
                        Text("${plan.precio}€", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                        if (isSelected) {
                            Spacer(Modifier.width(10.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = strings.ok, tint = BlueAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            LaunchedEffect(viewModel.planSeleccionado, viewModel.esRenovacion, viewModel.fechaFinActual) {
                viewModel.calculateDates()
            }

            if (viewModel.fechaInicioCalculada.isNotBlank() && viewModel.fechaFinCalculada.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                val rangeText = strings.dateFromTo.format(viewModel.fechaInicioCalculada, viewModel.fechaFinCalculada)
                Text(rangeText, fontSize = 13.sp, color = BlueAccent, fontWeight = FontWeight.SemiBold, modifier = Modifier.alpha(alphaAnim))
            }

            Spacer(Modifier.height(24.dp))
            Text(strings.paymentMethod, fontSize = 11.sp, color = textColor.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(strings.creditCard to Icons.Default.CreditCard, "PayPal" to Icons.Default.Payments).forEach { (metodo, icon) ->
                    val isSelected = viewModel.metodoSeleccionado == metodo
                    Button(
                        onClick = { viewModel.metodoSeleccionado = metodo },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) BlueAccent else cardBg)
                    ) {
                        Icon(icon, contentDescription = metodo, tint = if (isSelected) Color.White else textColor.copy(0.6f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(metodo, fontSize = 12.sp, color = if (isSelected) Color.White else textColor)
                    }
                }
            }

            if (viewModel.metodoSeleccionado == strings.creditCard) {
                val dColors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = BlueAccent, unfocusedBorderColor = textColor.copy(0.2f), focusedLabelColor = BlueAccent)
                OutlinedTextField(value = viewModel.numeroTarjeta, onValueChange = { if (it.length <= 16) viewModel.numeroTarjeta = it }, modifier = Modifier.fillMaxWidth(), label = { Text(strings.cardNumber) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dColors)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = viewModel.fechaVencimiento, onValueChange = { input -> val clean = input.replace("/", "").filter { it.isDigit() }; if (clean.length <= 4) { viewModel.fechaVencimiento = if (clean.length >= 3) "${clean.substring(0, 2)}/${clean.substring(2)}" else clean } }, modifier = Modifier.weight(1f), label = { Text(strings.expiryDate) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dColors)
                    OutlinedTextField(value = viewModel.cvv, onValueChange = { if (it.length <= 3) viewModel.cvv = it }, modifier = Modifier.weight(0.6f), label = { Text("CVV") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dColors)
                }
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {
                    viewModel.purchase(userId) {
                        Toast.makeText(context, strings.nowPremium, Toast.LENGTH_LONG).show()
                        onSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).alpha(alphaAnim),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = strings.confirmPayment, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.confirmPayment, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
