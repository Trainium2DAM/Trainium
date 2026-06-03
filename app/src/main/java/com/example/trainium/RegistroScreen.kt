package com.example.trainium

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainium.models.PesoUsuario
import com.example.trainium.AppConfig
import com.example.trainium.data.i18n.LocalStrings
import com.example.trainium.ui.theme.*
import com.example.trainium.ui.viewmodel.RegistroViewModel
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun RegistroScreen(userId: Int, darkTheme: Boolean, onToggleTheme: () -> Unit, onToggleLanguage: () -> Unit, onBack: () -> Unit) {
    val strings = LocalStrings.current
    val viewModel = viewModel<RegistroViewModel>()

    val fechaHoy = AppConfig.FORMAT_ISO_DATE.format(Date())

    var visible by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(if (visible) 1f else 0f, tween(600), label = "alpha")
    val textColor = if (darkTheme) Color.White else BlueDark
    val subtitleColor = if (darkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (darkTheme) Color(0xFF162347) else Color.White

    val bgBrush = if (darkTheme) {
        Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))
    }

    LaunchedEffect(Unit) { delay(100); visible = true; viewModel.loadRegistros(userId) }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp)
        ) {
            item {
                ScreenHeader(
                    title = strings.weightRecord,
                    subtitle = if (viewModel.yaExisteHoy) strings.subtitleWeightToday else strings.subtitleWeightAdd,
                    onBack = onBack,
                    trailing = {
                        IconButton(onClick = { viewModel.loadRegistros(userId) }) {
                            Icon(Icons.Default.Refresh, contentDescription = strings.contentDescRefresh, tint = BlueAccent)
                        }
                    },
                    textColor = textColor,
                    subtitleColor = subtitleColor,
                    onToggleTheme = onToggleTheme,
                    darkTheme = darkTheme,
                    onToggleLanguage = onToggleLanguage,
                    strings = strings
                )
                Spacer(Modifier.height(16.dp))
            }

            if (!viewModel.yaExisteHoy) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().alpha(alphaAnim).shadow(12.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = viewModel.pesoCampo, onValueChange = { viewModel.pesoCampo = it },
                                modifier = Modifier.weight(1f), label = { Text(strings.currentWeight) },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor, unfocusedTextColor = textColor,
                                    focusedBorderColor = BlueAccent, unfocusedBorderColor = textColor.copy(0.2f),
                                    focusedLabelColor = BlueAccent, unfocusedLabelColor = textColor.copy(0.4f)
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            FloatingActionButton(
                                onClick = {
                                    viewModel.pesoCampo = viewModel.pesoCampo.replace(",", ".")
                                    viewModel.addRegistro(userId) { }
                                },
                                containerColor = BlueAccent, modifier = Modifier.size(50.dp)
                            ) { Icon(Icons.Default.Add, contentDescription = strings.subtitleWeightAdd, tint = Color.White) }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            if (viewModel.isLoading) {
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 20.dp)) {
                        SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 60)
                        Spacer(Modifier.height(12.dp))
                        SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 180)
                    }
                }
            } else {
                item {
                    Text(strings.history, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor.copy(0.3f), letterSpacing = 3.sp)
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    val historyScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 245.dp) // Altura aproximada para 3 registros
                            .verticalScroll(historyScrollState),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        viewModel.registros.forEach { reg ->
                            val esHoy = reg.fecha == fechaHoy
                            Card(
                                modifier = Modifier.fillMaxWidth().alpha(alphaAnim),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (esHoy) (if (darkTheme) Color(0xFF1E2D52) else Color(0xFFE8F0FF)) else cardBg)
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).background(if (esHoy) BlueAccent else textColor.copy(0.2f), CircleShape))
                                    Spacer(Modifier.width(16.dp))

                                    Column(Modifier.weight(1f)) {
                                        if (viewModel.editandoId == reg.id) {
                                            OutlinedTextField(value = viewModel.editandoPeso, onValueChange = { viewModel.editandoPeso = it }, modifier = Modifier.width(90.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor))
                                        } else {
                                            Text("${reg.peso} kg", fontWeight = FontWeight.Bold, color = textColor, fontSize = 18.sp)
                                            Text(if (esHoy) strings.today else reg.fecha, fontSize = 12.sp, color = textColor.copy(0.4f))
                                        }
                                    }

                                    if (esHoy) {
                                        if (viewModel.editandoId == reg.id) {
                                            IconButton(onClick = {
                                                viewModel.editandoPeso = viewModel.editandoPeso.replace(",", ".")
                                                viewModel.saveEdit()
                                            }) { Icon(Icons.Default.Check, contentDescription = strings.contentDescApprove, tint = BlueAccent) }
                                        } else {
                                            IconButton(onClick = { reg.id?.let { viewModel.startEdit(it, reg.peso) } }) { Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = textColor.copy(0.5f), modifier = Modifier.size(18.dp)) }
                                        }
                                        IconButton(onClick = { reg.id?.let { viewModel.deleteRegistro(it) } }) { Icon(Icons.Default.Delete, contentDescription = strings.contentDescDelete, tint = Color(0xFFFF6B6B).copy(0.7f), modifier = Modifier.size(18.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }

                if (viewModel.registros.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(30.dp))
                        Text(strings.evolutionChart, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor.copy(0.3f), letterSpacing = 3.sp)
                        Spacer(Modifier.height(16.dp))
                        GraficaPeso(viewModel.registros, darkTheme)
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GraficaPeso(registros: List<PesoUsuario>, isDark: Boolean) {
    val strings = LocalStrings.current
    val data = remember(registros) { registros.reversed() } // Orden cronológico memorizado
    if (data.size < 2) {
        Box(Modifier.fillMaxWidth().height(150.dp).background(if(isDark) Color.White.copy(0.05f) else Color.Black.copy(0.05f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text(strings.needTwoRecords, color = (if(isDark) Color.White else Color.Black).copy(0.4f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
        return
    }

    // Cálculos memorizados para evitar re-cálculos en cada frame del Canvas
    val stats = remember(data) {
        val minVal = data.minOf { it.peso }.toFloat()
        val maxVal = data.maxOf { it.peso }.toFloat()
        val startY = (Math.floor(minVal.toDouble() / 2.0) * 2.0).toFloat()
        val endY = (Math.ceil(maxVal.toDouble() / 2.0) * 2.0).toFloat()
        val rangeY = if (endY == startY) 2f else endY - startY
        val numLines = (rangeY / 2).toInt()
        Triple(startY, rangeY, numLines)
    }

    val startY = stats.first
    val rangeY = stats.second
    val numLines = stats.third

    val textColor = if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.6f)

    Card(
        modifier = Modifier.fillMaxWidth().height(280.dp).shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF162347) else Color.White)
    ) {
        Row(Modifier.padding(16.dp).fillMaxSize()) {
            // Eje Y: Leyendas de peso (cada 2kg)
            Column(
                modifier = Modifier.fillMaxHeight().padding(end = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                for (i in numLines downTo 0) {
                    Text("${(startY + i * 2).toInt()}kg", color = textColor, fontSize = 10.sp)
                }
            }

            Column(Modifier.fillMaxSize()) {
                // Área de dibujo
                Box(Modifier.weight(1f)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val spaceX = width / (data.size - 1)

                        val points = data.mapIndexed { index, reg ->
                            val x = index * spaceX
                            val y = height - ((reg.peso.toFloat() - startY) / rangeY * height)
                            Offset(x, y)
                        }

                        // Líneas de fondo (grid)
                        for (i in 0..numLines) {
                            val y = height - (i * height / numLines)
                            drawLine(color = textColor.copy(0.1f), start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1.dp.toPx())
                        }

                        // Línea de evolución
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path = path, color = BlueAccent, style = Stroke(width = 3.dp.toPx()))

                        // Puntos en cada registro
                        points.forEach { point ->
                            drawCircle(color = BlueAccent, radius = 4.dp.toPx(), center = point)
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = point)
                        }
                    }
                }

                // Eje X: Contador de registros (1, 2, 3...)
                Box(Modifier.fillMaxWidth().height(24.dp)) {
                    val count = data.size
                    for (i in 0 until count) {
                        // Calculamos la posición horizontal (bias de -1f a 1f)
                        val bias = if (count > 1) (i.toFloat() / (count - 1)) * 2f - 1f else 0f
                        Text(
                            text = "${i + 1}",
                            color = textColor,
                            fontSize = 10.sp,
                            modifier = Modifier.align(BiasAlignment(bias, 1f))
                        )
                    }
                }
            }
        }
    }
}