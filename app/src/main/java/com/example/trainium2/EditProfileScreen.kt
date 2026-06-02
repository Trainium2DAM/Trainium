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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainium2.data.i18n.LocalStrings
import com.example.trainium2.ui.theme.*
import com.example.trainium2.ui.viewmodel.EditProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    userId: Int,
    isPremium: Boolean,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val strings = LocalStrings.current
    val viewModel = viewModel<EditProfileViewModel>()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { scope.launch { val base64 = uriToBase64(it, context.contentResolver); viewModel.setFoto(base64) } }
    }
    val archivosLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { scope.launch { val base64 = uriToBase64(it, context.contentResolver); viewModel.setFoto(base64) } }
    }

    var headerVisible by remember { mutableStateOf(false) }
    var avatarVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    var premiumVisible by remember { mutableStateOf(false) }
    val avatarAlpha by animateFloatAsState(if (avatarVisible) 1f else 0f, tween(600), label = "av")
    val avatarScale by animateFloatAsState(if (avatarVisible) 1f else 0.8f, tween(600, easing = FastOutSlowInEasing), label = "as")
    val formAlpha by animateFloatAsState(if (formVisible) 1f else 0f, tween(500), label = "f")
    val premiumAlpha by animateFloatAsState(if (premiumVisible) 1f else 0f, tween(500), label = "p")

    val textColor = if (darkTheme) Color.White else BlueDark
    val subtitleColor = if (darkTheme) Color.White.copy(0.35f) else BlueDark.copy(0.4f)
    val cardBg = if (darkTheme) Color(0xFF162347) else Color.White

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BlueAccent,
        unfocusedBorderColor = if (darkTheme) Color.White.copy(0.15f) else BlueDark.copy(0.15f),
        focusedLabelColor = BlueAccent,
        unfocusedLabelColor = if (darkTheme) Color.White.copy(0.4f) else BlueDark.copy(0.4f),
        cursorColor = BlueAccent,
        focusedTextColor = textColor,
        unfocusedTextColor = textColor.copy(0.9f)
    )

    val bgBrush = if (darkTheme)
        Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    else
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))

    LaunchedEffect(Unit) {
        delay(80); headerVisible = true
        delay(120); avatarVisible = true
        delay(150); formVisible = true
        delay(150); premiumVisible = true
    }
    LaunchedEffect(userId) { viewModel.loadUser(userId) }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            ScreenHeader(
                title = strings.editProfile,
                subtitle = strings.personalInfo,
                onBack = onNavigateToProfile,
                textColor = textColor,
                subtitleColor = subtitleColor,
                onToggleTheme = onToggleTheme,
                darkTheme = darkTheme,
                onToggleLanguage = onToggleLanguage
            )

            if (viewModel.isLoading) {
                Column(Modifier.fillMaxWidth().padding(top = 20.dp)) {
                    SkeletonCircle(modifier = Modifier.align(Alignment.CenterHorizontally), size = 100)
                    Spacer(Modifier.height(24.dp))
                    SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 250)
                }
            } else {
                Spacer(Modifier.height(20.dp))

                Box(Modifier.fillMaxWidth().alpha(avatarAlpha).scale(avatarScale), contentAlignment = Alignment.Center) {
                    Box {
                        Surface(
                            modifier = Modifier.size(100.dp).clip(CircleShape),
                            shape = CircleShape,
                            shadowElevation = 8.dp,
                            color = if (darkTheme) Color(0xFF1E2D52) else Color(0xFFE8F0FF)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().clip(CircleShape).clickable { viewModel.avatarMenuExpanded = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!viewModel.fotoBase64.isNullOrEmpty()) {
                                    val bitmap = decodeBase64ToBitmap(viewModel.fotoBase64!!)
                                    if (bitmap != null) {
                                        Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                                    } else {
                                        Text(if (viewModel.nombre.isNotEmpty()) viewModel.nombre.first().uppercaseChar().toString() else "?", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                                    }
                                } else {
                                    Text(if (viewModel.nombre.isNotEmpty()) viewModel.nombre.first().uppercaseChar().toString() else "?", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                                }
                                Box(Modifier.matchParentSize().background(Color.Black.copy(0.7f), CircleShape), contentAlignment = Alignment.BottomCenter) {
                                    Text(strings.edit, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                }
                            }
                        }

                        DropdownMenu(expanded = viewModel.avatarMenuExpanded, onDismissRequest = { viewModel.avatarMenuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(strings.deletePhoto) },
                                onClick = { viewModel.deleteFoto(); viewModel.avatarMenuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF6B6B)) }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.gallery) },
                                onClick = { galeriaLauncher.launch("image/*"); viewModel.avatarMenuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.Collections, null, tint = BlueAccent) }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.files) },
                                onClick = { archivosLauncher.launch(arrayOf("image/*")); viewModel.avatarMenuExpanded = false },
                                leadingIcon = { Icon(Icons.Default.Folder, null, tint = BlueAccent) }
                            )
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
                        Text(strings.personalInfo, fontSize = 11.sp, color = textColor.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(14.dp))
                        OutlinedTextField(value = viewModel.nombre, onValueChange = { viewModel.nombre = it }, modifier = Modifier.fillMaxWidth(), label = { Text(strings.fullName) }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Icon(Icons.Default.Person, null, tint = BlueAccent.copy(0.6f)) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = viewModel.email, onValueChange = { viewModel.email = it }, modifier = Modifier.fillMaxWidth(), label = { Text(strings.email) }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Icon(Icons.Default.Email, null, tint = BlueAccent.copy(0.6f)) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = viewModel.telefono, onValueChange = { viewModel.telefono = it }, modifier = Modifier.fillMaxWidth(), label = { Text(strings.phone) }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Icon(Icons.Default.Phone, null, tint = BlueAccent.copy(0.6f)) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = viewModel.password, onValueChange = { viewModel.password = it }, modifier = Modifier.fillMaxWidth(), label = { Text(strings.newPasswordOptional) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Icon(Icons.Default.Lock, null, tint = BlueAccent.copy(0.6f)) })
                    }
                }

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = {
                        viewModel.saveUser(userId) {
                            Toast.makeText(context, strings.profileUpdated, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp).alpha(formAlpha).shadow(12.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Text(strings.saveChanges, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().alpha(premiumAlpha).shadow(if (viewModel.isPremium) 12.dp else 4.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    val premCardBg = if (darkTheme)
                        if (viewModel.isPremium) Brush.linearGradient(listOf(Color(0xFF1A2D54), Color(0xFF2A1D54))) else Brush.linearGradient(listOf(Color(0xFF1A2D54), Color(0xFF162347)))
                    else
                        if (viewModel.isPremium) Brush.linearGradient(listOf(Color(0xFFFFFAEC), Color(0xFFFFF4D6))) else Brush.linearGradient(listOf(Color.White, Color.White))

                    Box(Modifier.fillMaxWidth().background(premCardBg).padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(if (viewModel.isPremium) Color(0xFFFFD700).copy(0.15f) else BlueAccent.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Star, null, tint = if (viewModel.isPremium) Color(0xFFFFD700) else textColor.copy(0.3f), modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(strings.accountStatus, fontSize = 11.sp, color = textColor.copy(0.4f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                if (viewModel.isPremium) Text(strings.premium, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                else Text(strings.standard, fontWeight = FontWeight.SemiBold, color = textColor, fontSize = 16.sp)
                            }
                            if (!viewModel.isPremium) {
                                Button(onClick = onNavigateToPremium, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) {
                                    Text(strings.upgrade, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(Modifier.padding(vertical = 6.dp), 1.dp, textColor.copy(0.06f))
                OutlinedButton(onClick = { onNavigateToHistorial() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, BlueAccent.copy(0.3f))) {
                    Text(strings.paymentHistory, color = BlueAccent)
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}