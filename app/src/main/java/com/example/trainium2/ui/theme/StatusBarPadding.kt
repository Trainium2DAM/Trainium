package com.example.trainium2.ui.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.safeStatusBarPadding(): Modifier = this.then(
    Modifier.padding(top = WindowInsets.statusBars.getTop(LocalDensity.current).dp)
)