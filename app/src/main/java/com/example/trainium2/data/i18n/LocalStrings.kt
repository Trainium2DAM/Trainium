package com.example.trainium2.data.i18n

import androidx.compose.runtime.staticCompositionLocalOf

val LocalStrings = staticCompositionLocalOf<AppStrings> { StringsEs }

fun stringsForLanguage(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.SPANISH -> StringsEs
    AppLanguage.ENGLISH -> StringsEn
    AppLanguage.VALENCIAN -> StringsVal
    AppLanguage.CATALAN -> StringsCa
    AppLanguage.FRENCH -> StringsFr
    AppLanguage.MOROCCAN_ARABIC -> StringsAr
    AppLanguage.BASQUE -> StringsEu
    AppLanguage.GALICIAN -> StringsGl
    AppLanguage.GERMAN -> StringsDe
    AppLanguage.PORTUGUESE -> StringsPt
    AppLanguage.RUSSIAN -> StringsRu
    AppLanguage.CHINESE -> StringsZh
}