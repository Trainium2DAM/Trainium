package com.example.trainium

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainium.data.i18n.AppStrings
import com.example.trainium.data.i18n.LocalStrings
import com.example.trainium.ui.theme.*
import com.example.trainium.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    userId: Int,
    isPremium: Boolean,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToMaquinas: () -> Unit,
    onNavigateToReservas: () -> Unit,
    onNavigateToPlatos: () -> Unit,
    onNavigateToRegistroPeso: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val strings = LocalStrings.current
    val viewModel = viewModel<ProfileViewModel>()
    var headerVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(userId) { viewModel.loadProfile(userId) }
    LaunchedEffect(Unit) {
        delay(100); headerVisible = true
        delay(250); buttonsVisible = true
    }

    val headerAlpha by animateFloatAsState(if (headerVisible) 1f else 0f, tween(500), label = "ha")
    val buttonsAlpha by animateFloatAsState(if (buttonsVisible) 1f else 0f, tween(500), label = "ba")

    val bgBrush = if (darkTheme) Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    else Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))

    val cardBg = if (darkTheme) Color(0xFF162347) else Color.White
    val cardIconBg = if (darkTheme) BlueAccent.copy(0.12f) else BlueAccent.copy(0.08f)
    val titleColor = if (darkTheme) Color.White else Color(0xFF1A1A2E)
    val subtitleColor = if (darkTheme) Color.White.copy(0.4f) else Color(0xFF6B7B99)
    val topIconTint = if (darkTheme) BlueSoft else BlueAccent

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        if (viewModel.isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SkeletonCircle(size = 32)
                        Spacer(Modifier.width(4.dp))
                        SkeletonCircle(size = 32)
                    }
                    SkeletonText(width = 100, height = 40)
                }
                Spacer(Modifier.height(24.dp))
                SkeletonCircle(size = 110)
                Spacer(Modifier.height(18.dp))
                SkeletonText(width = 200)
                Spacer(Modifier.height(36.dp))
                SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 80)
                Spacer(Modifier.height(14.dp))
                SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 80)
                Spacer(Modifier.height(14.dp))
                SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 80)
                Spacer(Modifier.height(14.dp))
                SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 80)
                Spacer(Modifier.height(32.dp))
                SkeletonCard(modifier = Modifier.fillMaxWidth(), height = 52)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleLanguage) {
                            Icon(Icons.Default.Language, contentDescription = strings.language, tint = topIconTint, modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, strings.theme, tint = topIconTint)
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onNavigateToEditProfile) {
                            Icon(Icons.Default.Settings, contentDescription = strings.contentDescSettings, tint = topIconTint, modifier = Modifier.size(24.dp))
                        }
                    }
                    Image(painter = painterResource(if (darkTheme) R.drawable.blanco else R.drawable.negro), contentDescription = "Trainium", modifier = Modifier.height(56.dp))
                }

                Spacer(Modifier.height(24.dp))

                Column(modifier = Modifier.alpha(headerAlpha), horizontalAlignment = Alignment.CenterHorizontally) {
                    val userPhoto = viewModel.usuario?.foto
                    val userInitial = (viewModel.usuario?.nombre ?: "").take(1).uppercase()

                    val profileImage = remember(userPhoto) {
                        if (!userPhoto.isNullOrEmpty()) {
                            decodeBase64ToBitmap(userPhoto)?.asImageBitmap()
                        } else null
                    }

                    Box(
                        modifier = Modifier.size(110.dp).shadow(16.dp, CircleShape, ambientColor = BlueAccent.copy(0.4f), spotColor = BlueAccent.copy(0.4f)).clip(CircleShape).background(Brush.linearGradient(listOf(BlueAccent, BlueElectric)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImage != null) {
                            Image(bitmap = profileImage, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Text(userInitial, fontSize = 46.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(text = "${strings.hello}, ${viewModel.usuario?.nombre ?: ""}!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = titleColor, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
                    if (viewModel.usuario?.admin == 1) {
                        Spacer(Modifier.height(6.dp))
                        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BlueAccent.copy(0.15f))) {
                            Text(strings.administrator, Modifier.padding(horizontal = 16.dp, vertical = 4.dp), fontSize = 12.sp, color = BlueAccent, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                    if (viewModel.usuario?.premium == true || isPremium) {
                        Spacer(Modifier.height(4.dp))
                        Text(strings.premium, fontSize = 13.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        val dias = viewModel.diasRestantes
                        if (dias >= 0) {
                            Spacer(Modifier.height(2.dp))
                            val diasText = if (dias == 0) strings.expiresToday else "$dias ${strings.daysLeft}"
                            Text(diasText, fontSize = 12.sp, color = if (dias <= 7) Color(0xFFFF6B6B) else Color(0xFFFFD700).copy(0.7f), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(36.dp))

                Column(modifier = Modifier.alpha(buttonsAlpha), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    ProfileMenuCard(strings.machines, Icons.Default.FitnessCenter, strings.reservations, cardBg, cardIconBg, titleColor, subtitleColor, darkTheme, strings) { onNavigateToMaquinas() }

                    ProfileMenuCard(strings.reservations, Icons.Default.CalendarMonth, strings.machines, cardBg, cardIconBg, titleColor, subtitleColor, darkTheme, strings) { onNavigateToReservas() }

                    ProfileMenuCard(strings.dishes, Icons.Default.Restaurant, strings.dishes, cardBg, cardIconBg, titleColor, subtitleColor, darkTheme, strings) { onNavigateToPlatos() }

                    ProfileMenuCard(strings.weightRecord, Icons.Default.MonitorWeight, strings.history, cardBg, cardIconBg, titleColor, subtitleColor, darkTheme, strings) { onNavigateToRegistroPeso() }
                }

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(52.dp).alpha(buttonsAlpha),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B).copy(if (darkTheme) 0.25f else 0.15f),
                        contentColor = Color(0xFFFF6B6B)
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = strings.logout, tint = Color(0xFFFF6B6B), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.logout, color = Color(0xFFFF6B6B), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

    if (viewModel.avisoMantenimiento != null) {
        val cardBg2 = if (darkTheme) Color(0xFF162347) else Color.White
        val titleColor2 = if (darkTheme) Color.White else Color(0xFF1A1A2E)
        AlertDialog(
            onDismissRequest = { viewModel.clearMaintenanceWarning() },
            confirmButton = {
                Button(onClick = { viewModel.clearMaintenanceWarning() }, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) {
                    Text(strings.understood, color = Color.White)
                }
            },
            title = { Text(strings.maintenanceNotice, fontWeight = FontWeight.Bold) },
            text = { Text(viewModel.avisoMantenimiento!!, color = titleColor2) },
            containerColor = cardBg2
        )
    }
}

@Composable
private fun ProfileMenuCard(text: String, icon: ImageVector, subtitle: String, cardBg: Color, iconBg: Color, titleColor: Color, subtitleColor: Color, isDark: Boolean, strings: AppStrings, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().shadow(if (isDark) 8.dp else 4.dp, RoundedCornerShape(18.dp)), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(iconBg, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = text, tint = BlueAccent, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = titleColor)
                Text(subtitle, fontSize = 12.sp, color = subtitleColor)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = strings.contentDescNext, tint = BlueAccent.copy(0.5f), modifier = Modifier.size(24.dp))
        }
    }
}