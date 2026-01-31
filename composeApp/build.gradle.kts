import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    kotlin("plugin.serialization")
}

kotlin {

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")


    sourceSets {

        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // ✅ Firebase Auth (Android)
            implementation(project.dependencies.platform(libs.android.firebase.bom))
            implementation(libs.firebase.auth.ktx)
            implementation(libs.firebase.analytics.ktx)
            implementation(libs.firebase.firestore.ktx)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.itextpdf)
            implementation(libs.zxing.android.embedded)

            // Ya estaba dos veces
            // implementation(libs.ktor.client.okhttp)

        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.settings)
            implementation(libs.napier)
            implementation(libs.core)
            implementation(compose.materialIconsExtended)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.tabNavigator)


            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.firebase.auth)

        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.ktor.client.ios)
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.firebase.auth)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.pdfbox)

            // ⭐️ Solución al error 'Module with the Main dispatcher is missing'
            implementation(libs.kotlinx.coroutines.swing)

            // ⭐️ Solución al error 'Duplicate class' (conflicto entre Google/Android Compose y JetBrains/Desktop Compose)
            configurations.findByName("compileClasspath")?.exclude(group = "androidx.compose.foundation")
            configurations.findByName("runtimeClasspath")?.exclude(group = "androidx.compose.foundation")

            // Añadir exclusión de UI si el error persiste, aunque Foundation es el más problemático
            configurations.findByName("compileClasspath")?.exclude(group = "androidx.compose.ui")
            configurations.findByName("runtimeClasspath")?.exclude(group = "androidx.compose.ui")
        }

    }
}

android {
    namespace = "com.alius.gmrstock"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.alius.gmrstockplus"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.alius.gmrstockplus.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "com.alius.gmrstockplus"
            packageVersion = "1.0.0"
        }
    }
}