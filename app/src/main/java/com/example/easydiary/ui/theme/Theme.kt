package com.example.easydiary.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- 1. 使用新的浅绿色和淡黄色定义日间主题 ---
private val LightColorScheme = lightColorScheme(
    primary = RelaxingGreen,
    secondary = RelaxingGreenDark,
    tertiary = Pink40,
    surfaceVariant = PaperYellowLight // ** 将淡黄色用于表单背景 (surfaceVariant) **
    /* 其他颜色可以保持默认或自定义 */
)

// --- 2. (可选) 定义夜间主题 ---
private val DarkColorScheme = darkColorScheme(
    primary = RelaxingGreenDarker,
    secondary = RelaxingGreenDark,
    tertiary = Pink80,
    surfaceVariant = PaperYellowDark // ** 将深褐色用于夜间表单背景 **
)

@Composable
fun EasyDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // ** 修改：禁用动态颜色，强制使用我们的浅绿色主题 **
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // ** 3. 应用我们自定义的配色方案 **
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            // ** 修复：日间主题使用亮色状态栏图标 (true)，夜间主题使用暗色 (false) **
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // 假设 Typography.kt 存在
        content = content
    )
}

