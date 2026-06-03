package com.example.trainium2

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.data.i18n.LocalStrings
import com.example.trainium2.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainium2.ui.viewmodel.RegisterViewModel
import kotlinx.coroutines.delay

private val PREFIJOS_PAIS = listOf(
    "+34" to "🇪🇸 España",
    "+1"  to "🇺🇸 EE.UU. / Canadá",
    "+44" to "🇬🇧 Reino Unido",
    "+33" to "🇫🇷 Francia",
    "+49" to "🇩🇪 Alemania",
    "+39" to "🇮🇹 Italia",
    "+351" to "🇵🇹 Portugal",
    "+52" to "🇲🇽 México",
    "+54" to "🇦🇷 Argentina",
    "+57" to "🇨🇴 Colombia",
    "+56" to "🇨🇱 Chile",
    "+51" to "🇵🇪 Perú",
    "+58" to "🇻🇪 Venezuela",
    "+55" to "🇧🇷 Brasil",
    "+593" to "🇪🇨 Ecuador",
    "+591" to "🇧🇴 Bolivia",
    "+598" to "🇺🇾 Uruguay",
    "+595" to "🇵🇾 Paraguay",
    "+503" to "🇸🇻 El Salvador",
    "+502" to "🇬🇹 Guatemala",
    "+504" to "🇭🇳 Honduras",
    "+505" to "🇳🇮 Nicaragua",
    "+506" to "🇨🇷 Costa Rica",
    "+507" to "🇵🇦 Panamá",
    "+53" to "🇨🇺 Cuba",
    "+1809" to "🇩🇴 Rep. Dominicana",
    "+212" to "🇲🇦 Marruecos",
    "+213" to "🇩🇿 Argelia",
    "+216" to "🇹🇳 Túnez",
    "+20" to "🇪🇬 Egipto",
    "+234" to "🇳🇬 Nigeria",
    "+27" to "🇿🇦 Sudáfrica",
    "+86" to "🇨🇳 China",
    "+91" to "🇮🇳 India",
    "+81" to "🇯🇵 Japón",
    "+82" to "🇰🇷 Corea del Sur",
    "+7" to "🇷🇺 Rusia",
    "+380" to "🇺🇦 Ucrania",
    "+48" to "🇵🇱 Polonia",
    "+31" to "🇳🇱 Países Bajos",
    "+32" to "🇧🇪 Bélgica",
    "+41" to "🇨🇭 Suiza",
    "+43" to "🇦🇹 Austria",
    "+46" to "🇸🇪 Suecia",
    "+47" to "🇳🇴 Noruega",
    "+45" to "🇩🇰 Dinamarca",
    "+358" to "🇫🇮 Finlandia",
    "+30" to "🇬🇷 Grecia",
    "+90" to "🇹🇷 Turquía",
    "+972" to "🇮🇱 Israel",
    "+966" to "🇸🇦 Arabia Saudí",
    "+971" to "🇦🇪 Emiratos Árabes",
    "+61" to "🇦🇺 Australia",
    "+64" to "🇳🇿 Nueva Zelanda"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(isDarkTheme: Boolean, onToggleTheme: () -> Unit, onToggleLanguage: () -> Unit, onBack: () -> Unit) {
    val strings = LocalStrings.current
    val viewModel = viewModel<RegisterViewModel>()
    val context = LocalContext.current

    var iconVisible by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    var btnVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80); iconVisible = true
        delay(120); titleVisible = true
        delay(150); formVisible = true
        delay(150); btnVisible = true
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(viewModel.successMessage) {
        viewModel.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccess()
        }
    }

    val iconAlpha by animateFloatAsState(if (iconVisible) 1f else 0f, tween(700), label = "ia")
    val iconScale by animateFloatAsState(if (iconVisible) 1f else 0.6f, tween(700, easing = FastOutSlowInEasing), label = "is")
    val titleAlpha by animateFloatAsState(if (titleVisible) 1f else 0f, tween(500), label = "ta")
    val formAlpha by animateFloatAsState(if (formVisible) 1f else 0f, tween(500), label = "fa")
    val formScale by animateFloatAsState(if (formVisible) 1f else 0.95f, tween(500, easing = FastOutSlowInEasing), label = "fs")
    val btnAlpha by animateFloatAsState(if (btnVisible) 1f else 0f, tween(500), label = "ba")

    val textColor = if (isDarkTheme) Color.White else BlueDark
    val subtitleColor = if (isDarkTheme) BlueSoft.copy(0.5f) else BlueAccent.copy(0.7f)
    val cardBg = if (isDarkTheme) Color(0xFF162347).copy(0.85f) else Color.White

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

            Column(Modifier.fillMaxWidth().weight(1f).padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            TextButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.contentDescBack, tint = BlueAccent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.alpha(iconAlpha).scale(iconScale)) {
                Box(Modifier.size(80.dp).background(Color(0xFF00E676).copy(0.12f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PersonAdd, contentDescription = strings.register, tint = Color(0xFF00E676), modifier = Modifier.size(40.dp))
                }
            }
            Spacer(Modifier.height(10.dp))

            Text(strings.registerTitle, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = textColor, letterSpacing = 1.sp, modifier = Modifier.semantics { heading() }.alpha(titleAlpha))
            Text(strings.registerSubtitle, fontSize = 13.sp, color = subtitleColor, modifier = Modifier.alpha(titleAlpha))
            Spacer(Modifier.height(22.dp))

            Card(
                modifier = Modifier.fillMaxWidth().alpha(formAlpha).scale(formScale)
                    .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = BlueAccent.copy(0.08f), spotColor = BlueAccent.copy(0.08f)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(strings.personalData, fontSize = 11.sp, color = textColor.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(value = viewModel.nombre, onValueChange = { viewModel.nombre = it }, modifier = Modifier.fillMaxWidth(), label = { Text(strings.fullName) }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Icon(Icons.Default.Person, null, tint = BlueAccent.copy(0.6f)) })
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = viewModel.tipoDocDesplegado,
                            onExpandedChange = { viewModel.tipoDocDesplegado = it },
                            modifier = Modifier.width(120.dp)
                        ) {
                            OutlinedTextField(
                                value = viewModel.tipoDocumento,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { Text(strings.docType) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.tipoDocDesplegado) },
                                shape = RoundedCornerShape(14.dp),
                                colors = inputColors,
                                modifier = Modifier.menuAnchor(
                                    type = MenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = viewModel.tipoDocDesplegado,
                                onDismissRequest = { viewModel.tipoDocDesplegado = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("DNI") },
                                    onClick = {
                                        viewModel.tipoDocumento = "DNI"
                                        viewModel.tipoDocDesplegado = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("NIE") },
                                    onClick = {
                                        viewModel.tipoDocumento = "NIE"
                                        viewModel.tipoDocDesplegado = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.passportOption) },
                                    onClick = {
                                        viewModel.tipoDocumento = "PASAPORTE"
                                        viewModel.tipoDocDesplegado = false
                                    }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = viewModel.dni,
                            onValueChange = { viewModel.dni = it.uppercase() },
                            modifier = Modifier.weight(1f),
                            label = { Text(strings.documentNumber) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = inputColors,
                            leadingIcon = { Icon(Icons.Default.Badge, null, tint = BlueAccent.copy(0.6f)) }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = viewModel.email, onValueChange = { viewModel.email = it }, modifier = Modifier.fillMaxWidth(), label = { Text(strings.email) }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Icon(Icons.Default.Email, null, tint = BlueAccent.copy(0.6f)) })
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = viewModel.pass, onValueChange = { viewModel.pass = it }, modifier = Modifier.fillMaxWidth(), label = { Text(strings.password) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Icon(Icons.Default.Lock, null, tint = BlueAccent.copy(0.6f)) })
                    Spacer(Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = viewModel.prefijoDesplegado,
                            onExpandedChange = { viewModel.prefijoDesplegado = it },
                            modifier = Modifier.width(115.dp)
                        ) {
                            OutlinedTextField(
                                value = viewModel.prefijo,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                label = { Text(strings.prefix) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.prefijoDesplegado) },
                                shape = RoundedCornerShape(14.dp),
                                colors = inputColors,
                                modifier = Modifier.menuAnchor(
                                    type = MenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = viewModel.prefijoDesplegado,
                                onDismissRequest = { viewModel.prefijoDesplegado = false }
                            ) {
                                PREFIJOS_PAIS.forEach { (codigo, nombrePais) ->
                                    DropdownMenuItem(
                                        text = { Text("$codigo  $nombrePais", fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.prefijo = codigo
                                            viewModel.prefijoDesplegado = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = viewModel.numeroTelf,
                            onValueChange = { viewModel.numeroTelf = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.weight(1f),
                            label = { Text(strings.number) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = inputColors,
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = BlueAccent.copy(0.6f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = { viewModel.register(onBack) },
                modifier = Modifier.fillMaxWidth().height(56.dp).alpha(btnAlpha),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()
            ) {
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.createAccount, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onBack, Modifier.fillMaxWidth().alpha(btnAlpha)) { Text(strings.alreadyHaveAccount, color = textColor.copy(0.5f), fontSize = 13.sp) }
            Spacer(Modifier.height(16.dp))
        }
    }
}
}