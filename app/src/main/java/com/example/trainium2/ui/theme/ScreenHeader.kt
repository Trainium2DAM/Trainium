package com.example.trainium2.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    visible: Boolean = true,
    onBack: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    textColor: Color,
    subtitleColor: Color,
    onToggleTheme: (() -> Unit)? = null,
    darkTheme: Boolean? = null
) {
    val headerAlpha by animateFloatAsState(if (visible) 1f else 0f, tween(500), label = "h")

    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().alpha(headerAlpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            TextButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = BlueAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(subtitle, fontSize = 12.sp, color = subtitleColor)
        }
        if (darkTheme != null && onToggleTheme != null) {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Tema",
                    tint = BlueAccent
                )
            }
        }
        trailing?.invoke()
    }
}
