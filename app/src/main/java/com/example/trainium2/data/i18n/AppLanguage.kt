package com.example.trainium2.data.i18n

import java.util.Locale

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    SPANISH("es", "Español", "Español"),
    ENGLISH("en", "English", "English"),
    VALENCIAN("ca-VALENCIA", "Valenciano", "Valencià"),
    CATALAN("ca", "Catalán", "Català"),
    FRENCH("fr", "Francés", "Français"),
    MOROCCAN_ARABIC("ar-MA", "Marroquí", "الدارجة"),
    BASQUE("eu", "Euskera", "Euskara"),
    GALICIAN("gl", "Gallego", "Galego"),
    GERMAN("de", "Alemán", "Deutsch"),
    PORTUGUESE("pt", "Portugués", "Português"),
    RUSSIAN("ru", "Ruso", "Русский"),
    CHINESE("zh", "Chino", "中文");

    companion object {
        fun fromLocale(locale: Locale): AppLanguage {
            val tag = locale.toLanguageTag()
            return when {
                tag.startsWith("es") -> SPANISH
                tag.startsWith("en") -> ENGLISH
                tag.startsWith("ca") && tag.contains("VALENCIA", ignoreCase = true) -> VALENCIAN
                tag.startsWith("ca") -> CATALAN
                tag.startsWith("fr") -> FRENCH
                tag.startsWith("ar") -> MOROCCAN_ARABIC
                tag.startsWith("eu") -> BASQUE
                tag.startsWith("gl") -> GALICIAN
                tag.startsWith("de") -> GERMAN
                tag.startsWith("pt") -> PORTUGUESE
                tag.startsWith("ru") -> RUSSIAN
                tag.startsWith("zh") -> CHINESE
                else -> SPANISH
            }
        }

        fun fromCode(code: String): AppLanguage =
            entries.find { it.code == code } ?: SPANISH
    }
}