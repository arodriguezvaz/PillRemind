import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

/**
 * Credenciales de firma locales. El archivo keystore.properties está en .gitignore
 * y solo se usa cuando no hay variables de entorno (por ejemplo, en GitHub Actions).
 */
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

/**
 * Resuelve un dato de firma: primero la variable de entorno, después keystore.properties.
 */
fun signingProperty(environmentVariable: String, propertyName: String): String? =
    System.getenv(environmentVariable)?.takeIf { it.isNotBlank() }
        ?: keystoreProperties.getProperty(propertyName)?.takeIf { it.isNotBlank() }

/** Keystore de release; si no existe, el APK se genera sin firmar. */
val releaseKeystore = signingProperty("KEYSTORE_FILE", "storeFile")?.let { path ->
    if (File(path).isAbsolute) File(path) else rootProject.file(path)
}

android {
    namespace = "com.andres.pillremind"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.andres.pillremind"
        minSdk = 26
        targetSdk = 36
        versionCode = providers.gradleProperty("versionCode").orNull?.toIntOrNull() ?: 1
        versionName = providers.gradleProperty("versionName").orNull ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        // Solo se crea si hay keystore, para que un clon sin credenciales siga compilando.
        if (releaseKeystore != null) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = signingProperty("KEYSTORE_PASSWORD", "storePassword")
                keyAlias = signingProperty("KEY_ALIAS", "keyAlias")
                keyPassword = signingProperty("KEY_PASSWORD", "keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Permite instalar la build de debug junto a la app real del móvil,
            // sin compartir firma ni datos.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    sourceSets {
        // Los esquemas solo se incluyen en la variante debug, que es la que
        // usan los tests locales e instrumentados. Así MigrationTestHelper
        // puede leerlos sin enviarlos en el APK de release.
        getByName("debug").assets.srcDir("$projectDir/schemas")
    }
}

ksp {
    // Los esquemas exportados son la base de las auto-migraciones y de los
    // tests de migración de Room. Se versionan en app/schemas.
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    //ICONOS
    implementation(libs.androidx.compose.material.icons.extended)
    //ROOM
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    //NAVIGATION
    implementation(libs.androidx.navigation.compose)
    //DATASTORE
    implementation(libs.androidx.datastore.preferences)
    //HILT
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    //WORKMANAGER
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    //COIL
    implementation(libs.coil.compose)
    // Lector de códigos para el benchmark de la fase 0. Solo en debug: usa el
    // modelo de Google Play Services y añade ~200 KB al APK.
    debugImplementation(libs.mlkit.barcode.scanning)
    // OCR de envases y del CN: forma parte de la app.
    implementation(libs.mlkit.text.recognition)
    //TESTING
//TESTING
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

}