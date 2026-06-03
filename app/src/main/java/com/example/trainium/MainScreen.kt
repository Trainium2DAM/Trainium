package com.example.trainium

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium.data.i18n.LocalStrings
import com.example.trainium.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val strings = LocalStrings.current

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
    val btnAlpha by animateFloatAsState(if (buttonVisible) 1f else 0f, tween(600), label = "ba")
    val btnOffset by animateFloatAsState(if (buttonVisible) 0f else 30f, tween(600, easing = FastOutSlowInEasing), label = "bo")

    val bg = if (isDarkTheme)
        Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    else
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleLanguage) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = strings.language,
                        tint = if (isDarkTheme) BlueSoft else BlueAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = strings.theme,
                        tint = if (isDarkTheme) BlueSoft else BlueAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.scale(logoScale).alpha(logoAlpha)) {
                    Image(
                        painter = painterResource(if (isDarkTheme) R.drawable.blanco else R.drawable.negro),
                        contentDescription = "Logo Trainium",
                        modifier = Modifier.fillMaxWidth().height(280.dp).padding(horizontal = 20.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text("TRAINIUM", fontSize = 38.sp, fontWeight = FontWeight.Black, letterSpacing = 10.sp, color = if (isDarkTheme) Color.White else BlueDark, modifier = Modifier.alpha(textAlpha))
                Spacer(Modifier.height(6.dp))
                Text(strings.appTagline, fontSize = 15.sp, color = if (isDarkTheme) BlueSoft.copy(0.7f) else BlueAccent.copy(0.7f), modifier = Modifier.alpha(textAlpha))

                Spacer(Modifier.height(40.dp))

                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth(0.82f).height(58.dp).alpha(btnAlpha).offset(y = btnOffset.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(strings.startNow, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Color.White)
                            Spacer(Modifier.width(10.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}