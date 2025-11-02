plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")

    // 我们将依赖 composeOptions，而不是在插件中声明 Compose Compiler
    // （这在您的环境中似乎更稳定）
}

android {
    namespace = "com.example.easydiary"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.easydiary"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    // 启用 Compose
    buildFeatures {
        compose = true
    }

    // 明确指定 KCE 版本
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    // **修复：统一 Java 编译环境为 Java 17**
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    // 统一版本号
    val roomVersion = "2.6.1"
    val navVersion = "2.7.5"

    // **1. 关键修复：更新 Compose BOM 到一个更新的稳定版本**
    // 这将获取最新的 material3 库，解决 ListItem 签名问题
    val composeBomVersion = "2024.05.00"
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))

    // Compose 核心依赖
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // 核心 M3 库 (将由 BOM 自动管理版本)
    implementation("androidx.compose.material3:material3")

    // 图标库
    implementation("androidx.compose.material:material-icons-core")
    // **2. 关键修复：添加 Icons Extended 库**
    // 这是解决 "Unresolved reference 'automirrored'" 错误的必需库
    implementation("androidx.compose.material:material-icons-extended")

    // --- 其他依赖 (保持不变) ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // --- Room 数据库 ---
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // --- Compose Navigation ---
    implementation("androidx.navigation:navigation-compose:$navVersion")

    // (测试依赖...)
}

