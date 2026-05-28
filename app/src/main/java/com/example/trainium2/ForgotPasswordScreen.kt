package com.example.trainium2

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.models.Usuario
import com.example.trainium2.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ForgotPasswordScreen(isDarkTheme: Boolean, onBack: () -> Unit) {
    var dni by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) }
    var idUsuario by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var headerVisible by remember { mutableStateOf(false) }
    var iconVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    val headerAlpha by animateFloatAsState(if (headerVisible) 1f else 0f, tween(500), label = "h")
    val iconAlpha by animateFloatAsState(if (iconVisible) 1f else 0f, tween(600), label = "i")
    val iconScale by animateFloatAsState(if (iconVisible) 1f else 0.7f, tween(600, easing = FastOutSlowInEasing), label = "is")
    val formAlpha by animateFloatAsState(if (formVisible) 1f else 0f, tween(500), label = "f")

    val infiniteTransition = rememberInfiniteTransition(label = "g")
    val glowAlpha by infiniteTransition.animateFloat(0.1f, 0.3f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "ga")

    LaunchedEffect(Unit) { delay(80); headerVisible = true; delay(150); iconVisible = true; delay(150); formVisible = true }

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
            // ── Header ──
            Row(Modifier.fillMaxWidth().alpha(headerAlpha), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Volver", color = BlueAccent, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f)) {
                    Text("Recuperar Contraseña", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(if (step == 1) "Paso 1: Verificación" else "Paso 2: Nueva contraseña", fontSize = 12.sp, color = subtitleColor)
                }
            }
            Spacer(Modifier.height(30.dp))

            // ── Lock Icon ──
            Box(Modifier.fillMaxWidth().alpha(iconAlpha).scale(iconScale), contentAlignment = Alignment.Center) {
                Box(Modifier.size(100.dp).shadow(20.dp, CircleShape, ambientColor = BlueAccent.copy(glowAlpha), spotColor = BlueAccent.copy(glowAlpha)).background(Brush.radialGradient(listOf(BlueAccent.copy(glowAlpha), Color.Transparent)), CircleShape))
                Text(if (step == 1) "🔐" else "🔑", fontSize = 56.sp)
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
                    if (step == 1) {
                        Text("VERIFICA TU IDENTIDAD", fontSize = 11.sp, color = textColor.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(14.dp))
                        OutlinedTextField(value = dni, onValueChange = { dni = it }, modifier = Modifier.fillMaxWidth(), label = { Text("DNI") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("🪪", fontSize = 16.sp) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("📧", fontSize = 16.sp) })
                    } else {
                        Text("NUEVA CONTRASEÑA", fontSize = 11.sp, color = textColor.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(14.dp))
                        OutlinedTextField(value = newPass, onValueChange = { newPass = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nueva contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("🔒", fontSize = 16.sp) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = confirmPass, onValueChange = { confirmPass = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Confirmar contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("🔒", fontSize = 16.sp) })
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // ── Action Button ──
            Button(
                onClick = {
                    scope.launch {
                        try {
                            if (step == 1) {
                                val user = withContext(Dispatchers.IO) {
                                    SupabaseClient.client.from("usuarios")
                                        .select {
                                            filter {
                                                eq("dni", dni)
                                                eq("email", email)
                                            }
                                        }
                                        .decodeSingleOrNull<Usuario>()
                                }
                                
                                withContext(Dispatchers.Main) {
                                    if (user != null) {
                                        idUsuario = user.id
                                        step = 2
                                    } else {
                                        Toast.makeText(context, "No se encontró el usuario con esos datos", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                if (newPass != confirmPass) { 
                                    Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                                    return@launch 
                                }
                                if (newPass.length < 4) { 
                                    Toast.makeText(context, "Mínimo 4 caracteres", Toast.LENGTH_SHORT).show()
                                    return@launch 
                                }
                                
                                withContext(Dispatchers.IO) {
                                    SupabaseClient.client.from("usuarios").update({
                                        set("contraseniaHash", newPass)
                                    }) {
                                        filter {
                                            eq("id", idUsuario)
                                        }
                                    }
                                }
                                
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "✅ Contraseña actualizada", Toast.LENGTH_LONG).show()
                                    onBack()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp).alpha(formAlpha)
                    .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = BlueAccent.copy(0.3f), spotColor = BlueAccent.copy(0.3f)),
                shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()
            ) {
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Text(if (step == 1) "Verificar identidad" else "Cambiar contraseña", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }
            }

            // ── Step Indicator ──
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth().alpha(formAlpha), horizontalArrangement = Arrangement.Center) {
                Box(Modifier.size(if (step == 1) 10.dp else 8.dp).background(if (step == 1) BlueAccent else textColor.copy(0.2f), CircleShape))
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(if (step == 2) 10.dp else 8.dp).background(if (step == 2) BlueAccent else textColor.copy(0.2f), CircleShape))
            }
        }
    }
}
