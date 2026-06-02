package com.example.trainium2

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainium2.ui.viewmodel.ForgotPasswordViewModel
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordScreen(isDarkTheme: Boolean, onToggleTheme: () -> Unit, onBack: () -> Unit) {
    val viewModel = viewModel<ForgotPasswordViewModel>()
    val context = LocalContext.current

    var headerVisible by remember { mutableStateOf(false) }
    var iconVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    val iconAlpha by animateFloatAsState(if (iconVisible) 1f else 0f, tween(600), label = "i")
    val iconScale by animateFloatAsState(if (iconVisible) 1f else 0.7f, tween(600, easing = FastOutSlowInEasing), label = "is")
    val formAlpha by animateFloatAsState(if (formVisible) 1f else 0f, tween(500), label = "f")

    LaunchedEffect(Unit) { delay(80); headerVisible = true; delay(150); iconVisible = true; delay(150); formVisible = true }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    val textColor = if (isDarkTheme) Color.White else BlueDark
    val subtitleColor = if (isDarkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (isDarkTheme) Color(0xFF162347) else Color.White

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BlueAccent,
        unfocusedBorderColor = if (isDarkTheme) Color.White.copy(0.15f) else BlueDark.copy(0.15f),
        focusedLabelColor = BlueAccent,
        unfocusedLabelColor = if (isDarkTheme) Color.White.copy(0.4f) else BlueDark.copy(0.4f),
        cursorColor = BlueAccent,
        focusedTextColor = textColor,
        unfocusedTextColor = textColor.copy(0.9f)
    )

    val bgBrush = if (isDarkTheme) {
        Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))
    }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            ScreenHeader(
                title = "Recuperar Contrasena",
                subtitle = if (viewModel.step == 1) "Paso 1: Verificacion" else "Paso 2: Nueva contrasena",
                onBack = onBack,
                textColor = textColor,
                subtitleColor = subtitleColor,
                onToggleTheme = onToggleTheme,
                darkTheme = isDarkTheme
            )
            Spacer(Modifier.height(30.dp))

            // ── Lock Icon ──
            Box(Modifier.fillMaxWidth().alpha(iconAlpha).scale(iconScale), contentAlignment = Alignment.Center) {
                Box(Modifier.size(80.dp).background(BlueAccent.copy(0.1f), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                    Icon(if (viewModel.step == 1) Icons.Default.Lock else Icons.Default.LockOpen, null, tint = BlueAccent, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(Modifier.height(30.dp))

            // ── Form Card ──
            Card(
                modifier = Modifier.fillMaxWidth().alpha(formAlpha)
                    .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = BlueAccent.copy(0.05f), spotColor = BlueAccent.copy(0.05f)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(Modifier.padding(20.dp)) {
                    if (viewModel.step == 1) {
                        Text("VERIFICA TU IDENTIDAD", fontSize = 11.sp, color = textColor.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(14.dp))
                        OutlinedTextField(value = viewModel.dni, onValueChange = { viewModel.dni = it }, modifier = Modifier.fillMaxWidth(), label = { Text("DNI") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Icon(Icons.Default.Badge, null, tint = BlueAccent.copy(0.6f)) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = viewModel.email, onValueChange = { viewModel.email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Icon(Icons.Default.Email, null, tint = BlueAccent.copy(0.6f)) })
                    } else {
                        Text("NUEVA CONTRASEÑA", fontSize = 11.sp, color = textColor.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(14.dp))
                        OutlinedTextField(value = viewModel.newPass, onValueChange = { viewModel.newPass = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nueva contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Icon(Icons.Default.Lock, null, tint = BlueAccent.copy(0.6f)) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = viewModel.confirmPass, onValueChange = { viewModel.confirmPass = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Confirmar contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Icon(Icons.Default.Lock, null, tint = BlueAccent.copy(0.6f)) })
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // ── Action Button ──
            Button(
                onClick = {
                    if (viewModel.step == 1) {
                        viewModel.verifyUser()
                    } else {
                        viewModel.updatePassword(onBack)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp).alpha(formAlpha),
                shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()
            ) {
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(if (viewModel.step == 1) Icons.Default.Badge else Icons.Default.LockOpen, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (viewModel.step == 1) "Verificar identidad" else "Cambiar contraseña", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                }
            }

            // ── Step Indicator ──
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth().alpha(formAlpha), horizontalArrangement = Arrangement.Center) {
                Box(Modifier.size(if (viewModel.step == 1) 10.dp else 8.dp).background(if (viewModel.step == 1) BlueAccent else textColor.copy(0.2f), CircleShape))
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(if (viewModel.step == 2) 10.dp else 8.dp).background(if (viewModel.step == 2) BlueAccent else textColor.copy(0.2f), CircleShape))
            }
        }
    }
}