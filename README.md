<div align="center">
  <img src="https://i.ibb.co/V03qnzQd/Proyecto-nuevo-7.png"/>

  # Trainium

  **Aplicación móvil de gestión de gimnasio para Android**

  [![Android](https://img.shields.io/badge/Android-API%2024%2B-brightgreen?logo=android)](https://developer.android.com)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
  [![Supabase](https://img.shields.io/badge/Supabase-Database-3ECF8E?logo=supabase)](https://supabase.com)
  [![Firebase](https://img.shields.io/badge/Firebase-Auth-FFCA28?logo=firebase)](https://firebase.google.com)
</div>

<div align="center">

# [Documentación](https://trainium2dam.github.io/docs/intro/)

</div>

---

## ¿Qué es Trainium?

Trainium es una aplicación Android para la gestión integral de un gimnasio. Permite a los usuarios reservar máquinas, consultar el menú de platos, registrar su evolución de peso y gestionar su suscripción premium, todo desde el móvil.

## Características principales

- **Autenticación** — Login con DNI/NIE/pasaporte, registro de nuevos usuarios y recuperación de contraseña
- **Reservas de máquinas** — Consulta disponibilidad y reserva máquinas por franja horaria
- **Menú de platos** — Visualiza los platos disponibles con información nutricional y pasos de receta
- **Registro de peso** — Añade tu peso diario y consulta tu evolución con un gráfico histórico
- **Perfil de usuario** — Edita tus datos personales y gestiona tu cuenta
- **Suscripción premium** — Activa planes premium con pago con tarjeta
- **Historial de pagos** — Consulta todas tus transacciones
- **Notificaciones** — Recordatorios configurables antes de tus reservas
- **Panel de administración** — Gestión de máquinas, reservas y platos para administradores
- **Tema oscuro/claro** — Adaptable al sistema o configuración manual
- **Multidioma** — Soporte para 12 idiomas: Español, English, Català, Valencià, Français, Euskara, Galego, Deutsch, Português, Русский, 中文 y Darija (árabe marroquí)

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| UI | Jetpack Compose + Material 3 |
| Navegación | Navigation Compose |
| Arquitectura | MVVM + Repository pattern |
| Base de datos | Supabase (PostgreSQL) |
| Autenticación | Firebase Auth |
| Almacenamiento local | DataStore Preferences + EncryptedSharedPreferences |
| Red | Ktor Client |
| Mínimo SDK | Android 7.0 (API 24) |