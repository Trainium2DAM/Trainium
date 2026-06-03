package com.example.trainium

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainium.models.Plato
import com.example.trainium.data.i18n.LocalStrings
import com.example.trainium.ui.theme.*
import com.example.trainium.ui.viewmodel.PlatosViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlatosScreen(
    userId: Int,
    isAdmin: Boolean,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val viewModel = viewModel<PlatosViewModel>()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val platoActual = viewModel.listaPlatosAceptados.getOrNull(viewModel.indicePlatoActual)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val base64 = uriToBase64(it, context.contentResolver)
                viewModel.fotoBase64 = base64
            }
        }
    }

    LaunchedEffect(Unit) { viewModel.loadPlatos(isAdmin) }

    var sugerenciaParaVer by remember { mutableStateOf<Plato?>(null) }

    LaunchedEffect(viewModel.error) {
        viewModel.error?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    var visible by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(if (visible) 1f else 0f, tween(800), label = "alpha")

    LaunchedEffect(viewModel.isLoading) {
        if (!viewModel.isLoading && viewModel.error == null && viewModel.listaPlatosAceptados.isNotEmpty()) {
            delay(100)
            visible = true
        }
    }

    val textColor = if (darkTheme) Color.White else BlueDark
    val subtitleColor = if (darkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (darkTheme) Color(0xFF162347) else Color.White

    val bgBrush = if (darkTheme) Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    else Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))

    fun enviarSugerencia() {
        if (viewModel.nuevoTitulo.isEmpty() || viewModel.nuevaReceta.isEmpty() || viewModel.nuevoTiempo.isEmpty()) {
            Toast.makeText(context, strings.completeFields, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.enviarSugerencia(userId) {
            viewModel.loadPlatos(isAdmin)
        }
    }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            // Header
            ScreenHeader(
                title = strings.dishes,
                subtitle = strings.subtitleDishes,
                onBack = onBack,
                trailing = {
                    IconButton(onClick = { visible = false; viewModel.loadPlatos(isAdmin) }) {
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

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { viewModel.showingDialogoSugerencia = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.suggestRecipe, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(strings.suggestRecipe, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))

            if (viewModel.isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    SkeletonCircle(size = 130)
                    Spacer(Modifier.height(24.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 200)
                }
            } else if (viewModel.error != null) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${viewModel.error}", color = textColor, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    Button(onClick = { viewModel.loadPlatos(isAdmin) }, Modifier.padding(top = 16.dp)) { Text(strings.retry) }
                }
            } else {
                // Plato Actual con Animación de Transición
                AnimatedContent(
                    targetState = platoActual,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(400, delayMillis = 90)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400, delayMillis = 90)))
                            .togetherWith(fadeOut(animationSpec = tween(300)))
                    },
                    label = "dishAnimation"
                ) { plato ->
                    if (plato != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(strings.recommendationFormat, viewModel.indicePlatoActual + 1, viewModel.listaPlatosAceptados.size),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = BlueAccent.copy(0.7f),
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(16.dp))

                            Box(Modifier.size(130.dp).shadow(20.dp, CircleShape, ambientColor = BlueAccent.copy(0.3f), spotColor = BlueAccent.copy(0.3f)).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                                if (!plato.imagenUrl.isNullOrEmpty()) {
                                    val bitmap = decodeBase64ToBitmap(plato.imagenUrl!!)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else { Icon(Icons.Default.Restaurant, contentDescription = strings.dishes, tint = BlueAccent.copy(0.4f), modifier = Modifier.size(60.dp)) }
                                } else { Icon(Icons.Default.Restaurant, contentDescription = strings.dishes, tint = BlueAccent.copy(0.4f), modifier = Modifier.size(60.dp)) }
                            }

                            Spacer(Modifier.height(24.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth().shadow(15.dp, RoundedCornerShape(22.dp)),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg)
                            ) {
                                Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(plato.nombre, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor, textAlign = TextAlign.Center)

                                    Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalFireDepartment, contentDescription = strings.approximateCalories, tint = Color(0xFFFF6B35), modifier = Modifier.size(16.dp))
                                            Text("${plato.calorias ?: 0.0} kcal", color = BlueAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                                        }
                                        if (!plato.tiempo.isNullOrEmpty()) {
                                            VerticalDivider(modifier = Modifier.height(14.dp), color = textColor.copy(0.1f))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Timer, contentDescription = strings.cookingTime, tint = BlueAccent, modifier = Modifier.size(16.dp))
                                                Text(plato.tiempo!!, color = textColor.copy(0.6f), fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp))
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = textColor.copy(0.1f)); Spacer(Modifier.height(18.dp))
                                    Text(plato.descripcion ?: strings.noDescription, color = textColor.copy(0.7f), textAlign = TextAlign.Center, fontSize = 16.sp, lineHeight = 24.sp)
                                }
                            }
                        }
                    }
                }

                // Botón Siguiente (Ciclo infinito: 1 -> 2 -> ... -> N -> 1)
                if (viewModel.listaPlatosAceptados.size > 1) {
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.nextDish() },
                        modifier = Modifier.fillMaxWidth().height(56.dp).alpha(alphaAnim),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent.copy(0.12f), contentColor = BlueAccent)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = strings.contentDescNext)
                        Spacer(Modifier.width(10.dp))
                        Text(strings.nextRecommended, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }
                }

                // Sugerencias Pendientes (Sólo Admin)
                if (isAdmin && viewModel.sugerenciasPendientes.isNotEmpty()) {
                    Spacer(Modifier.height(40.dp))
                    Text(strings.pendingSuggestions, fontSize = 12.sp, fontWeight = FontWeight.Black, color = BlueAccent, letterSpacing = 2.sp)
                    Spacer(Modifier.height(12.dp))

                    viewModel.sugerenciasPendientes.forEach { sug ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { sugerenciaParaVer = sug }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (!sug.imagenUrl.isNullOrEmpty()) {
                                    val b = decodeBase64ToBitmap(sug.imagenUrl!!)
                                    if (b != null) Image(bitmap = b.asImageBitmap(), null, Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(sug.nombre, fontWeight = FontWeight.Bold, color = textColor)
                                    Text("${strings.timeLabel} ${sug.tiempo ?: strings.notAvailable}", fontSize = 12.sp, color = subtitleColor)
                                }
                                Row {
                                    IconButton(onClick = { viewModel.aprobarSugerencia(sug.id ?: return@IconButton, isAdmin) }) { Icon(Icons.Default.Check, contentDescription = strings.contentDescApprove, tint = Color(0xFF00E676)) }
                                    IconButton(onClick = { viewModel.rechazarSugerencia(sug.id ?: return@IconButton, isAdmin) }) { Icon(Icons.Default.Close, contentDescription = strings.contentDescReject, tint = Color(0xFFFF6B6B)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para ver detalle de sugerencia pendiente
    sugerenciaParaVer?.let { sug ->
        AlertDialog(
            onDismissRequest = { sugerenciaParaVer = null },
            containerColor = cardBg,
            title = { Text(sug.nombre, color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (!sug.imagenUrl.isNullOrEmpty()) {
                        val b = decodeBase64ToBitmap(sug.imagenUrl!!)
                        if (b != null) {
                            Image(bitmap = b.asImageBitmap(), null, Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (sug.calorias != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF6B35), modifier = Modifier.size(16.dp))
                                Text("${sug.calorias} kcal", color = BlueAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                        if (!sug.tiempo.isNullOrEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, null, tint = BlueAccent, modifier = Modifier.size(16.dp))
                                Text(sug.tiempo!!, color = textColor.copy(0.6f), fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = textColor.copy(0.1f))
                    Spacer(Modifier.height(12.dp))
                    Text(sug.descripcion ?: strings.noDescription, color = textColor.copy(0.7f), fontSize = 15.sp, lineHeight = 22.sp)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        sug.id?.let { viewModel.rechazarSugerencia(it, isAdmin) }
                        sugerenciaParaVer = null
                    }) { Text(strings.contentDescReject, color = Color(0xFFFF6B6B)) }
                    Button(onClick = {
                        sug.id?.let { viewModel.aprobarSugerencia(it, isAdmin) }
                        sugerenciaParaVer = null
                    }, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) { Text(strings.contentDescApprove, color = Color.White) }
                }
            },
            dismissButton = {
                TextButton(onClick = { sugerenciaParaVer = null }) { Text(strings.cancel, color = textColor.copy(0.6f)) }
            }
        )
    }

    // Diálogo para sugerir plato
    if (viewModel.showingDialogoSugerencia) {
        val dColors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = BlueAccent, unfocusedBorderColor = textColor.copy(0.2f), focusedLabelColor = BlueAccent)
        AlertDialog(
            onDismissRequest = { viewModel.showingDialogoSugerencia = false },
            containerColor = cardBg,
            title = { Text(strings.suggestRecipe, color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Box(Modifier.fillMaxWidth().height(120.dp).background(textColor.copy(0.05f), RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).clickable { launcher.launch("image/*") }, contentAlignment = Alignment.Center) {
                        if (!viewModel.fotoBase64.isNullOrEmpty()) {
                            val bitmap = decodeBase64ToBitmap(viewModel.fotoBase64!!)
                            if (bitmap != null) Image(bitmap = bitmap.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else { Text(strings.selectPhoto, color = BlueAccent, fontWeight = FontWeight.SemiBold) }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = viewModel.nuevoTitulo, onValueChange = { viewModel.nuevoTitulo = it }, label = { Text(strings.recipeTitle) }, modifier = Modifier.fillMaxWidth(), colors = dColors)
                    OutlinedTextField(value = viewModel.nuevoTiempo, onValueChange = { viewModel.nuevoTiempo = it }, label = { Text(strings.cookingTime) }, modifier = Modifier.fillMaxWidth(), colors = dColors)
                    OutlinedTextField(value = viewModel.caloriasTexto, onValueChange = { viewModel.caloriasTexto = it }, label = { Text(strings.approximateCalories) }, modifier = Modifier.fillMaxWidth(), colors = dColors)
                    OutlinedTextField(value = viewModel.nuevaReceta, onValueChange = { viewModel.nuevaReceta = it }, label = { Text(strings.recipeSteps) }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = dColors)
                }
            },
            confirmButton = {
                Button(onClick = { enviarSugerencia() }, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) {
                    Text(strings.sendSuggestion, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showingDialogoSugerencia = false }) { Text(strings.cancel, color = textColor.copy(0.6f)) }
            }
        )
    }
}
