pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Repositorio de respaldo para asegurar la descarga de plugins de Firebase en Preview
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Trainium2"
include(":app")
