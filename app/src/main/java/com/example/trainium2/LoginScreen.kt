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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.data.i18n.LocalStrings
import com.example.trainium2.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainium2.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit,
    onLoginSuccess: (String, Int, Int, Int) -> Unit
) {
    val strings = LocalStrings.current
    val viewModel = viewModel<LoginViewModel>()
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

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
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
        Box(Modifier.fillMaxSize().background(bgOverlay))

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleLanguage) {
                    Icon(Icons.Default.Language, contentDescription = strings.language, tint = BlueAccent, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = onToggleTheme) {
                    Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = strings.theme, tint = BlueAccent, modifier = Modifier.size(26.dp))
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(28.dp),
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

            Text(strings.loginTitle, fontSize = 26.sp, fontWeight = FontWeight.Black, color = textColor, letterSpacing = 4.sp, modifier = Modifier.semantics { heading() }.alpha(titleAlpha))
            Text(strings.loginWelcome, fontSize = 13.sp, color = subtitleColor, modifier = Modifier.alpha(titleAlpha))

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth().alpha(formAlpha).scale(formScale)
                    .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = BlueAccent.copy(0.08f), spotColor = BlueAccent.copy(0.08f)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(strings.credentials, fontSize = 11.sp, color = textColor.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = viewModel.dni, onValueChange = { viewModel.dni = it.uppercase() },
                        label = { Text(strings.dni) }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(14.dp), colors = inputColors,
                        leadingIcon = { Icon(Icons.Default.Badge, null, tint = BlueAccent.copy(0.6f)) }
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = viewModel.pass, onValueChange = { viewModel.pass = it },
                        label = { Text(strings.password) }, visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(14.dp), colors = inputColors,
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = BlueAccent.copy(0.6f)) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(onSuccess = onLoginSuccess) },
                modifier = Modifier.fillMaxWidth().height(56.dp).scale(btnScale).alpha(btnAlpha),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = strings.enter, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.enter, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(Modifier.alpha(linksAlpha), horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(onClick = onNavigateToForgot) {
                    Text(strings.forgotPassword, color = textColor.copy(0.5f), fontSize = 13.sp)
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BlueAccent.copy(0.3f))
                ) {
                    Text(strings.createNewAccount, color = BlueAccent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onBack) { Text(strings.backToHome, fontWeight = FontWeight.Bold, color = textColor.copy(0.3f)) }
            }
        }
    }
}
}