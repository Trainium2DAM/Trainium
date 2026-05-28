package com.example.trainium2

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
fun EditProfileScreen(
    idUsuario: Int,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onNavigateToHistorial: (Int) -> Unit,
    onNavigateToPremium: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }
    var fotoBase64 by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Launcher para seleccionar imagen de la galería
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    uriToBase64(it, context.contentResolver)
                }
                fotoBase64 = base64
            }
        }
    }

    var headerVisible by remember { mutableStateOf(false) }
    var avatarVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    var premiumVisible by remember { mutableStateOf(false) }
    val headerAlpha by animateFloatAsState(if (headerVisible) 1f else 0f, tween(500), label = "h")
    val avatarAlpha by animateFloatAsState(if (avatarVisible) 1f else 0f, tween(600), label = "av")
    val avatarScale by animateFloatAsState(if (avatarVisible) 1f else 0.8f, tween(600, easing = FastOutSlowInEasing), label = "as")
    val formAlpha by animateFloatAsState(if (formVisible) 1f else 0f, tween(500), label = "f")
    val premiumAlpha by animateFloatAsState(if (premiumVisible) 1f else 0f, tween(500), label = "p")

    val infiniteTransition = rememberInfiniteTransition(label = "g")
    val glowAlpha by infiniteTransition.animateFloat(0.1f, 0.3f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "ga")

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

    fun cargarDatos() {
        cargando = true
        scope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("usuarios")
                        .select {
                            filter {
                                eq("id", idUsuario)
                            }
                        }
                        .decodeSingleOrNull<Usuario>()
                }
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        nombre = user.nombre
                        email = user.email ?: ""
                        telefono = user.telefono ?: ""
                        isPremium = user.premium
                        fotoBase64 = user.foto
                    }
                    cargando = false
                    delay(80); headerVisible = true; delay(120); avatarVisible = true; delay(150); formVisible = true; delay(150); premiumVisible = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    cargando = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Row(Modifier.fillMaxWidth().alpha(headerAlpha), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Volver", color = BlueAccent, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f)) {
                    Text("Ajustes de Perfil", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text("Edita tu información personal", fontSize = 12.sp, color = subtitleColor)
                }
            }

            if (cargando) {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BlueAccent) }
            } else {
                Spacer(Modifier.height(20.dp))

                // Avatar Clickable
                Box(Modifier.fillMaxWidth().alpha(avatarAlpha).scale(avatarScale), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(100.dp)
                            .shadow(20.dp, CircleShape, ambientColor = BlueAccent.copy(glowAlpha), spotColor = BlueAccent.copy(glowAlpha))
                            .background(Brush.linearGradient(listOf(BlueAccent.copy(0.2f), BlueElectric.copy(0.1f))), CircleShape)
                            .clip(CircleShape)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!fotoBase64.isNullOrEmpty()) {
                            val bitmap = decodeBase64ToBitmap(fotoBase64!!)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(if (nombre.isNotEmpty()) nombre.first().uppercaseChar().toString() else "👤", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                            }
                        } else {
                            Text(if (nombre.isNotEmpty()) nombre.first().uppercaseChar().toString() else "👤", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                        }
                        
                        // Overlay para indicar que se puede editar
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.2f)), contentAlignment = Alignment.BottomCenter) {
                            Text("EDITAR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().alpha(formAlpha).shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("INFORMACIÓN PERSONAL", fontSize = 11.sp, color = textColor.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(14.dp))
                        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre completo") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("👤", fontSize = 16.sp) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("📧", fontSize = 16.sp) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = telefono, onValueChange = { telefono = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Teléfono") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("📱", fontSize = 16.sp) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nueva contraseña (opcional)") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("🔒", fontSize = 16.sp) })
                    }
                }

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    SupabaseClient.client.from("usuarios").update({
                                        set("nombre", nombre)
                                        set("email", email)
                                        set("telefono", telefono)
                                        set("foto", fotoBase64)
                                        if (password.isNotEmpty()) {
                                            set("contraseniaHash", password)
                                        }
                                    }) {
                                        filter { eq("id", idUsuario) }
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "✅ Perfil actualizado", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp).alpha(formAlpha).shadow(12.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()
                ) {
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Text("💾 Guardar cambios", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().alpha(premiumAlpha).shadow(if (isPremium) 12.dp else 4.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    val premCardBg = if (isDarkTheme) 
                        if (isPremium) Brush.linearGradient(listOf(Color(0xFF1A2D54), Color(0xFF2A1D54))) else Brush.linearGradient(listOf(Color(0xFF1A2D54), Color(0xFF162347)))
                    else
                        if (isPremium) Brush.linearGradient(listOf(Color(0xFFFFFAEC), Color(0xFFFFF4D6))) else Brush.linearGradient(listOf(Color.White, Color.White))
                        
                    Box(Modifier.fillMaxWidth().background(premCardBg).padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(if (isPremium) Color(0xFFFFD700).copy(0.15f) else BlueAccent.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Star, null, tint = if (isPremium) Color(0xFFFFD700) else textColor.copy(0.3f), modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("ESTADO DE CUENTA", fontSize = 11.sp, color = textColor.copy(0.4f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                if (isPremium) Text("PREMIUM ⭐", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                else Text("Estándar", fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 16.sp)
                            }
                            if (!isPremium) {
                                Button(onClick = onNavigateToPremium, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) {
                                    Text("Upgrade", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(Modifier.padding(vertical = 6.dp), 1.dp, textColor.copy(0.06f))
                OutlinedButton(onClick = { onNavigateToHistorial(idUsuario) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, BlueAccent.copy(0.3f))) {
                    Text("💳 Ver historial de pagos", color = BlueAccent)
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
