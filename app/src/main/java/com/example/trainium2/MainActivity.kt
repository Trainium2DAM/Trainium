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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.trainium2.ui.theme.Trainium2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val themeManager = remember { ThemeManager(context) }
            val savedDarkMode by themeManager.isDarkMode.collectAsState(initial = null)
            val scope = rememberCoroutineScope()
            
            val systemTheme = isSystemInDarkTheme()
            var isDarkTheme by remember(savedDarkMode) { 
                mutableStateOf(savedDarkMode ?: systemTheme) 
            }
            
            val toggleTheme = {
                val newValue = !isDarkTheme
                isDarkTheme = newValue
                scope.launch { themeManager.setDarkMode(newValue) }
                Unit
            }

            Trainium2Theme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // Forzamos fondo blanco en la superficie si estamos en el splash
                val backgroundColor = if (currentRoute == "splash") Color.White else MaterialTheme.colorScheme.background
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                    NavHost(navController = navController, startDestination = "splash") {

                        composable("splash") {
                            SplashVideoScreen(
                                onVideoFinished = {
                                    navController.navigate("main") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("main") {
                            MainScreen(
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = toggleTheme,
                                onNavigateToLogin = { navController.navigate("login") }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                isDarkTheme = isDarkTheme,
                                onBack = { navController.popBackStack() },
                                onNavigateToRegister = { navController.navigate("register") },
                                onNavigateToForgot = { navController.navigate("forgot") },
                                onLoginSuccess = { nombre, isAdmin, idUsuario, isPremium ->
                                    navController.navigate("profile/$nombre/$isAdmin/$idUsuario/$isPremium")
                                }
                            )
                        }

                        composable(
                            "profile/{nombre}/{isAdmin}/{idUsuario}/{isPremium}",
                            arguments = listOf(
                                navArgument("nombre") { type = NavType.StringType },
                                navArgument("isAdmin") { type = NavType.IntType },
                                navArgument("idUsuario") { type = NavType.IntType },
                                navArgument("isPremium") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
                            val isAdmin = backStackEntry.arguments?.getInt("isAdmin") == 1
                            val idUsuario = backStackEntry.arguments?.getInt("idUsuario") ?: 0
                            val isPremium = backStackEntry.arguments?.getInt("isPremium") == 1

                            ProfileScreen(
                                nombre = nombre,
                                isAdmin = isAdmin,
                                idUsuario = idUsuario,
                                isPremium = isPremium,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = toggleTheme,
                                onLogout = { navController.navigate("main") { popUpTo(0) } },
                                onNavigateToMaquinas = { admin, id ->
                                    navController.navigate("maquinas/${if(admin) 1 else 0}/$id")
                                },
                                onNavigateToPlatos = { admin, id -> 
                                    navController.navigate("platos/${if(admin) 1 else 0}/$id") 
                                },
                                onNavigateToRegistro = { id -> navController.navigate("registro/$id") },
                                onNavigateToReservas = { admin, id ->
                                    navController.navigate("reservas/${if(admin) 1 else 0}/$id")
                                },
                                onNavigateToEditProfile = { id -> navController.navigate("edit_profile/$id") }
                            )
                        }

                        composable(
                            "edit_profile/{idUsuario}",
                            arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                        ) { bse ->
                            val id = bse.arguments?.getInt("idUsuario") ?: 0
                            EditProfileScreen(
                                idUsuario = id,
                                isDarkTheme = isDarkTheme,
                                onBack = { navController.popBackStack() },
                                onNavigateToHistorial = { i -> navController.navigate("historial/$i") },
                                onNavigateToPremium = { navController.navigate("premium_selection/$id") }
                            )
                        }

                        composable(
                            "historial/{idUsuario}",
                            arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                        ) { bse ->
                            val id = bse.arguments?.getInt("idUsuario") ?: 0
                            HistorialScreen(idUsuario = id, isDarkTheme = isDarkTheme, onBack = { navController.popBackStack() })
                        }

                        composable(
                            "premium_selection/{idUsuario}",
                            arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                        ) { bse ->
                            val id = bse.arguments?.getInt("idUsuario") ?: 0
                            PremiumSelectionScreen(idUsuario = id, isDarkTheme = isDarkTheme, onBack = { navController.popBackStack() }, onSuccess = { navController.popBackStack() })
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
                            MaquinasScreen(isAdmin = admin, idUsuario = id, isDarkTheme = isDarkTheme, onBack = { navController.popBackStack() })
                        }

                        composable(
                            "registro/{idUsuario}",
                            arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                        ) { bse ->
                            val id = bse.arguments?.getInt("idUsuario") ?: 0
                            RegistroScreen(idUsuario = id, isDarkTheme = isDarkTheme, onBack = { navController.popBackStack() })
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
                            ReservasScreen(isAdmin = admin, idUsuario = id, isDarkTheme = isDarkTheme, onBack = { navController.popBackStack() })
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
                            PlatosScreen(isAdmin = admin, idUsuario = id, isDarkTheme = isDarkTheme) { navController.popBackStack() }
                        }
                        
                        composable("register") { RegisterScreen(isDarkTheme = isDarkTheme) { navController.popBackStack() } }
                        composable("forgot") { ForgotPasswordScreen(isDarkTheme = isDarkTheme) { navController.popBackStack() } }
                    }
                }
            }
        }
    }
}
