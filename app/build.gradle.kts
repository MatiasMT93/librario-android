plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // Reemplaza al viejo KAPT
    alias(libs.plugins.hilt) // Inyección de dependencias
}

android {
    namespace = "com.example.librario"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.librario"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    // BLOQUE PARA RENOMBRAR EL APK
    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            // Podés ponerle "Librario.apk" o incluso agregarle la versión: "Librario_v1.0.apk"
            output.outputFileName = "Librario.apk"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // --- ARQUITECTURA PROFESIONAL ---

    // 1. Hilt (El pegamento de la arquitectura)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // Usamos KSP para procesar las anotaciones
    implementation(libs.androidx.hilt.navigation.compose)

    // 2. Room (La Base de Datos)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // Extensiones de Kotlin para Room
    ksp(libs.androidx.room.compiler)

    // 3. Retrofit (Conexión a Internet)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson) // Para convertir JSON a Objetos
    implementation(libs.okhttp.logging) // Para ver los logs de red en consola

    // 4. Coil (Carga de imágenes eficiente)
    implementation(libs.coil.compose)

    // 5. Extras (Cámara y Navegación)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.mlkit.barcode)

    implementation("com.google.code.gson:gson:2.10.1")
}