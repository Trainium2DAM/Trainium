package com.example.trainium2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.trainium2.data.i18n.*
import com.example.trainium2.models.SesionData
import com.example.trainium2.ui.theme.Trainium2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val context = LocalContext.current
            SecureSessionManager.setContext(context)
            val window = (context as? android.app.Activity)?.window

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = {}
                )
                LaunchedEffect(Unit) {
                    NotificationHelper.createNotificationChannel(context)
                    launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                LaunchedEffect(Unit) {
                    NotificationHelper.createNotificationChannel(context)
                }
            }

            val themeManager = remember { ThemeManager(context) }
            val languageManager = remember { LanguageManager(context) }

            val savedDarkMode by themeManager.isDarkMode.collectAsState(initial = null)
            val savedLanguage by languageManager.currentLanguage.collectAsState(initial = null)

            val scope = rememberCoroutineScope()

            val systemTheme = isSystemInDarkTheme()
            var isDarkTheme by remember(savedDarkMode) {
                mutableStateOf(savedDarkMode ?: systemTheme)
            }

            val resolvedLanguage by remember(savedLanguage) {
                derivedStateOf {
                    savedLanguage ?: languageManager.detectSystemLanguage()
                }
            }

            var showLanguagePicker by remember { mutableStateOf(false) }

            val currentStrings = remember(resolvedLanguage) {
                stringsForLanguage(resolvedLanguage)
            }

            val toggleTheme = {
                val newValue = !isDarkTheme
                isDarkTheme = newValue
                scope.launch { themeManager.setDarkMode(newValue) }
                Unit
            }

            val toggleLanguage = { showLanguagePicker = true }

            CompositionLocalProvider(LocalStrings provides currentStrings) {
                Trainium2Theme(darkTheme = isDarkTheme) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    SideEffect {
                        window?.let { w ->
                            val controller = WindowInsetsControllerCompat(w, w.decorView)
                            controller.systemBarsBehavior =
                                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            val isSplash = currentRoute == "splash"
                            if (isSplash) {
                                controller.show(WindowInsetsCompat.Type.navigationBars())
                                w.statusBarColor = android.graphics.Color.WHITE
                                w.navigationBarColor = android.graphics.Color.WHITE
                                controller.isAppearanceLightStatusBars = true
                                controller.isAppearanceLightNavigationBars = true
                            } else {
                                controller.hide(WindowInsetsCompat.Type.navigationBars())
                                val color = if (isDarkTheme)
                                    android.graphics.Color.parseColor("#0B1426")
                                else
                                    android.graphics.Color.parseColor("#F0F4FF")
                                w.statusBarColor = color
                                w.navigationBarColor = color
                                controller.isAppearanceLightStatusBars = !isDarkTheme
                                controller.isAppearanceLightNavigationBars = !isDarkTheme
                            }
                        }
                    }

                    val backgroundColor =
                        if (currentRoute == "splash") Color.White
                        else MaterialTheme.colorScheme.background

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = backgroundColor
                    ) {
                        NavHost(navController = navController, startDestination = "splash") {

                            composable("splash") {
                                SplashVideoScreen(
                                    onVideoFinished = {
                                        navController.navigate("loading") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("loading") {
                                val sesion = SecureSessionManager.obtenerSesion()
                                LaunchedEffect(Unit) {
                                    if (sesion != null) {
                                        navController.navigate("profile/${sesion.userId}") {
                                            popUpTo("loading") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("main") {
                                            popUpTo("loading") { inclusive = true }
                                        }
                                    }
                                }
                            }

                            composable("main") {
                                MainScreen(
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onNavigateToLogin = { navController.navigate("login") }
                                )
                            }

                            composable("login") {
                                LoginScreen(
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToRegister = { navController.navigate("register") },
                                    onNavigateToForgot = { navController.navigate("forgot") },
                                    onLoginSuccess = { _, isAdmin, idUsuario, isPremium ->
                                        navController.navigate("profile/$idUsuario")
                                    }
                                )
                            }

                            composable(
                                "profile/{idUsuario}",
                                arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val idUsuario = backStackEntry.arguments?.getInt("idUsuario") ?: 0
                                ProfileScreen(
                                    userId = idUsuario,
                                    isPremium = false,
                                    darkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onLogout = { navController.navigate("main") { popUpTo(0) } },
                                    onNavigateToMaquinas = { navController.navigate("maquinas/0/$idUsuario") },
                                    onNavigateToEditProfile = { navController.navigate("edit_profile/$idUsuario") },
                                    onNavigateToReservas = { navController.navigate("reservas/0/$idUsuario") },
                                    onNavigateToPlatos = { navController.navigate("platos/0/$idUsuario") },
                                    onNavigateToRegistroPeso = { navController.navigate("registro/$idUsuario") },
                                    onNavigateToHistorial = { navController.navigate("historial/$idUsuario") },
                                    onNavigateToPremium = { navController.navigate("premium_selection/$idUsuario") }
                                )
                            }

                            composable(
                                "edit_profile/{idUsuario}",
                                arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                            ) { bse ->
                                val id = bse.arguments?.getInt("idUsuario") ?: 0
                                EditProfileScreen(
                                    userId = id,
                                    isPremium = false,
                                    darkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onNavigateToProfile = { navController.popBackStack() },
                                    onNavigateToHistorial = { navController.navigate("historial/$id") },
                                    onNavigateToPremium = { navController.navigate("premium_selection/$id") }
                                )
                            }

                            composable(
                                "historial/{idUsuario}",
                                arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                            ) { bse ->
                                val id = bse.arguments?.getInt("idUsuario") ?: 0
                                HistorialScreen(
                                    userId = id,
                                    darkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                "premium_selection/{idUsuario}",
                                arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                            ) { bse ->
                                val id = bse.arguments?.getInt("idUsuario") ?: 0
                                PremiumSelectionScreen(
                                    userId = id,
                                    darkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onBack = { navController.popBackStack() },
                                    onSuccess = { navController.popBackStack() }
                                )
                            }

                            composable(
                                "maquinas/{isAdmin}/{idUsuario}",
                                arguments = listOf(
                                    navArgument("isAdmin") { type = NavType.IntType },
                                    navArgument("idUsuario") { type = NavType.IntType }
                                )
                            ) { bse ->
                                val admin = bse.arguments?.getInt("isAdmin") == 1
                                val id = bse.arguments?.getInt("idUsuario") ?: 0
                                MaquinasScreen(
                                    userId = id,
                                    isAdmin = admin,
                                    darkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                "registro/{idUsuario}",
                                arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                            ) { bse ->
                                val id = bse.arguments?.getInt("idUsuario") ?: 0
                                RegistroScreen(
                                    userId = id,
                                    darkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                "reservas/{isAdmin}/{idUsuario}",
                                arguments = listOf(
                                    navArgument("isAdmin") { type = NavType.IntType },
                                    navArgument("idUsuario") { type = NavType.IntType }
                                )
                            ) { bse ->
                                val admin = bse.arguments?.getInt("isAdmin") == 1
                                val id = bse.arguments?.getInt("idUsuario") ?: 0
                                ReservasScreen(
                                    userId = id,
                                    isAdmin = admin,
                                    darkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                "platos/{isAdmin}/{idUsuario}",
                                arguments = listOf(
                                    navArgument("isAdmin") { type = NavType.IntType },
                                    navArgument("idUsuario") { type = NavType.IntType }
                                )
                            ) { bse ->
                                val admin = bse.arguments?.getInt("isAdmin") == 1
                                val id = bse.arguments?.getInt("idUsuario") ?: 0
                                PlatosScreen(
                                    userId = id,
                                    isAdmin = admin,
                                    darkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("register") {
                                RegisterScreen(
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("forgot") {
                                ForgotPasswordScreen(
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = toggleTheme,
                                    onToggleLanguage = toggleLanguage,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }

                        if (showLanguagePicker) {
                            LanguagePickerDialog(
                                currentLanguage = resolvedLanguage,
                                onLanguageSelected = { lang ->
                                    showLanguagePicker = false
                                    scope.launch { languageManager.setLanguage(lang) }
                                },
                                onDismiss = { showLanguagePicker = false },
                                strings = currentStrings,
                                darkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }
        }
    }
}