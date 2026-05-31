package com.example.trainium2

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
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
fun LoginScreen(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit,
    onLoginSuccess: (String, Int, Int, Int) -> Unit
) {
    var dni by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var logoVisible by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    var btnVisible by remember { mutableStateOf(false) }
    var linksVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80); logoVisible = true
        delay(150); titleVisible = true
        delay(150); formVisible = true
        delay(150); btnVisible = true
        delay(100); linksVisible = true
    }

    val logoAlpha by animateFloatAsState(if (logoVisible) 1f else 0f, tween(700), label = "la")
    val logoScale by animateFloatAsState(if (logoVisible) 1f else 0.6f, tween(700, easing = FastOutSlowInEasing), label = "ls")
    val titleAlpha by animateFloatAsState(if (titleVisible) 1f else 0f, tween(500), label = "ta")
    val formAlpha by animateFloatAsState(if (formVisible) 1f else 0f, tween(500), label = "fa")
    val formScale by animateFloatAsState(if (formVisible) 1f else 0.95f, tween(500, easing = FastOutSlowInEasing), label = "fs")
    val btnAlpha by animateFloatAsState(if (btnVisible) 1f else 0f, tween(500), label = "ba")
    val btnScale by animateFloatAsState(if (btnVisible) 1f else 0.85f, tween(500, easing = FastOutSlowInEasing), label = "bs")
    val linksAlpha by animateFloatAsState(if (linksVisible) 1f else 0f, tween(400), label = "lka")

    val textColor = if (isDarkTheme) Color.White else BlueDark
    val subtitleColor = if (isDarkTheme) BlueSoft.copy(0.5f) else BlueAccent.copy(0.7f)
    val cardBg = if (isDarkTheme) Color(0xFF162347).copy(0.85f) else Color.White.copy(0.9f)

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BlueAccent,
        unfocusedBorderColor = if (isDarkTheme) Color.White.copy(0.15f) else BlueDark.copy(0.15f),
        focusedLabelColor = BlueAccent,
        unfocusedLabelColor = if (isDarkTheme) Color.White.copy(0.4f) else BlueDark.copy(0.4f),
        cursorColor = BlueAccent,
        focusedTextColor = textColor,
        unfocusedTextColor = textColor.copy(0.9f)
    )

    val bgOverlay = if (isDarkTheme)
        Brush.verticalGradient(listOf(BlueDark.copy(0.95f), BlueMid.copy(0.92f), BlueDeep.copy(0.95f)))
    else
        Brush.verticalGradient(listOf(WhiteSoft.copy(0.95f), BlueLight.copy(0.92f), WhiteSoft.copy(0.95f)))

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.fondopantalla), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(bgOverlay))

        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.alpha(logoAlpha).scale(logoScale)) {
                Image(
                    painter = painterResource(if (isDarkTheme) R.drawable.blanco else R.drawable.negro),
                    contentDescription = "Logo Trainium",
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("INICIAR SESIÓN", fontSize = 26.sp, fontWeight = FontWeight.Black, color = textColor, letterSpacing = 4.sp, modifier = Modifier.alpha(titleAlpha))
            Text("Bienvenido de vuelta a Trainium", fontSize = 13.sp, color = subtitleColor, modifier = Modifier.alpha(titleAlpha))

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth().alpha(formAlpha).scale(formScale)
                    .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = BlueAccent.copy(0.08f), spotColor = BlueAccent.copy(0.08f)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("CREDENCIALES", fontSize = 11.sp, color = textColor.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = dni, onValueChange = { dni = it.uppercase() },
                        label = { Text("DNI") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(14.dp), colors = inputColors,
                        leadingIcon = { Icon(Icons.Default.Badge, null, tint = BlueAccent.copy(0.6f)) }
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pass, onValueChange = { pass = it },
                        label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(14.dp), colors = inputColors,
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = BlueAccent.copy(0.6f)) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val user = withContext(Dispatchers.IO) {
                                SupabaseClient.client.from("usuarios")
                                    .select {
                                        filter {
                                            eq("dni", dni)
                                        }
                                    }.decodeSingleOrNull<Usuario>()
                            }

                            withContext(Dispatchers.Main) {
                                if (user != null) {
                                    if (user.contraseniaHash == pass) {
                                        onLoginSuccess(
                                            user.nombre,
                                            user.admin,
                                            user.id,
                                            if (user.premium) 1 else 0
                                        )
                                    } else {
                                        Toast.makeText(context, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).scale(btnScale).alpha(btnAlpha),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Login, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ENTRAR", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(Modifier.alpha(linksAlpha), horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(onClick = onNavigateToForgot) {
                    Text("¿Olvidaste tu contraseña?", color = textColor.copy(0.5f), fontSize = 13.sp)
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BlueAccent.copy(0.3f))
                ) {
                    Text("CREAR CUENTA NUEVA", color = BlueAccent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onBack) { Text("← INICIO", fontWeight = FontWeight.Bold, color = textColor.copy(0.3f)) }
            }
        }
    }
}