package com.example.trainium2

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var logoVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    var featuresVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150); logoVisible = true
        delay(300); textVisible = true
        delay(250); featuresVisible = true
        delay(250); buttonVisible = true
    }

    val logoScale by animateFloatAsState(if (logoVisible) 1f else 0.5f, tween(900, easing = FastOutSlowInEasing), label = "ls")
    val logoAlpha by animateFloatAsState(if (logoVisible) 1f else 0f, tween(900), label = "la")
    val textAlpha by animateFloatAsState(if (textVisible) 1f else 0f, tween(700), label = "ta")
    val featAlpha by animateFloatAsState(if (featuresVisible) 1f else 0f, tween(600), label = "fa")
    val btnAlpha by animateFloatAsState(if (buttonVisible) 1f else 0f, tween(600), label = "ba")
    val btnOffset by animateFloatAsState(if (buttonVisible) 0f else 30f, tween(600, easing = FastOutSlowInEasing), label = "bo")

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(0.2f, 0.6f, infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ga")
    val pulseScale by infiniteTransition.animateFloat(1f, 1.05f, infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "ps")

    val bg = if (isDarkTheme)
        Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    else
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        // Decorative glow circles
        Box(Modifier.size(350.dp).align(Alignment.Center).offset(y = (-80).dp).alpha(glowAlpha).background(Brush.radialGradient(listOf(BlueAccent.copy(0.1f), Color.Transparent)), CircleShape))
        Box(Modifier.size(200.dp).align(Alignment.BottomStart).offset(x = (-50).dp, y = 50.dp).alpha(glowAlpha * 0.5f).background(Brush.radialGradient(listOf(BlueElectric.copy(0.06f), Color.Transparent)), CircleShape))

        // Theme toggle
        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding()
        ) {
            Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Tema", tint = if (isDarkTheme) BlueSoft else BlueAccent, modifier = Modifier.size(26.dp))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo with glow ──
            Box(contentAlignment = Alignment.Center, modifier = Modifier.scale(logoScale).alpha(logoAlpha)) {
                Box(Modifier.size(200.dp).shadow(30.dp, CircleShape, ambientColor = BlueAccent.copy(glowAlpha * 0.3f), spotColor = BlueAccent.copy(glowAlpha * 0.3f)).background(Brush.radialGradient(listOf(BlueAccent.copy(glowAlpha * 0.12f), Color.Transparent)), CircleShape))
                Image(
                    painter = painterResource(if (isDarkTheme) R.drawable.blanco else R.drawable.negro),
                    contentDescription = "Logo Trainium",
                    modifier = Modifier.fillMaxWidth().height(280.dp).padding(horizontal = 20.dp).scale(pulseScale)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Title ──
            Text("TRAINIUM", fontSize = 38.sp, fontWeight = FontWeight.Black, letterSpacing = 10.sp, color = if (isDarkTheme) Color.White else BlueDark, modifier = Modifier.alpha(textAlpha))
            Spacer(Modifier.height(6.dp))
            Text("Tu gimnasio, tu ritmo", fontSize = 15.sp, color = if (isDarkTheme) BlueSoft.copy(0.7f) else BlueAccent.copy(0.7f), modifier = Modifier.alpha(textAlpha))

            Spacer(Modifier.height(40.dp))

            // ── CTA Button ──
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth(0.82f).height(58.dp).alpha(btnAlpha).offset(y = btnOffset.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp), ambientColor = BlueAccent.copy(0.4f), spotColor = BlueAccent.copy(0.4f)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Text("🚀 COMENZAR AHORA", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Color.White)
                }
            }
        }
    }
}
