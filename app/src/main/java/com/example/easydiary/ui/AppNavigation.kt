// 文件位置: app/src/main/java/com/example/easydiary/ui/AppNavigation.kt

@file:OptIn(ExperimentalMaterial3Api::class) // 确保添加这一行

package com.example.easydiary.ui

// 导入所有屏幕和 ViewModel 的定义
import com.example.easydiary.data.DiaryEntry
import com.example.easydiary.ui.calendar.CalendarScreen
import com.example.easydiary.ui.curve.CurveScreen
import com.example.easydiary.ui.editor.DiaryEditorScreen
import com.example.easydiary.ui.query.QueryListScreen
import com.example.easydiary.ui.viewer.DiaryViewerScreen

// 导入所有需要的 UI 组件
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate

// --- 1. 导航入口 (签名已完全更新) ---
@Composable
fun AppNavigation(
    uiState: DiaryUiState,
    allEntriesASC: List<DiaryEntry>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onNavigate: (NavigationState) -> Unit,
    onQuery: (QueryType) -> Unit,
    onSave: (DiaryEntry) -> Unit,
    onExport: () -> Unit,
    onBack: () -> Unit,
    onEditorDateChange: (LocalDate) -> Unit,
    onViewNextDay: () -> Unit,
    onViewPreviousDay: () -> Unit,
    onDelete: (LocalDate) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = uiState.currentScreen,
                onNavigate = onNavigate,
                onQuery = onQuery
            )
        },
        floatingActionButton = {
            if (uiState.currentScreen == NavigationState.CALENDAR) {
                FloatingActionButton(onClick = { onNavigate(NavigationState.EDITOR) }) {
                    Icon(Icons.Default.Add, contentDescription = "添加新日记")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (uiState.currentScreen) {
                NavigationState.CALENDAR -> CalendarScreen(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    allEntries = allEntriesASC
                )
                NavigationState.EDITOR -> DiaryEditorScreen(
                    entry = uiState.currentEntry,
                    selectedDate = selectedDate,
                    onSave = onSave,
                    onBack = onBack,
                    onDateChanged = onEditorDateChange
                )
                NavigationState.VIEWER -> DiaryViewerScreen(
                    entry = uiState.currentEntry,
                    selectedDate = selectedDate,
                    onEdit = { onNavigate(NavigationState.EDITOR) },
                    onBack = onBack,
                    onExport = onExport,
                    onSwipeNext = onViewNextDay,
                    onSwipePrevious = onViewPreviousDay,
                    onDelete = onDelete
                )
                NavigationState.QUERY -> QueryListScreen(
                    queryType = uiState.queryType,
                    entries = uiState.queryResults,
                    onEntryClick = onDateSelected
                )
                NavigationState.CURVE -> CurveScreen(
                    entries = allEntriesASC,
                    onDateSelected = onDateSelected
                )
            }
        }
    }
}

// --- 2. 底部导航栏 (修改：新图标) ---
@Composable
fun BottomNavigationBar(
    currentScreen: NavigationState,
    onNavigate: (NavigationState) -> Unit,
    onQuery: (QueryType) -> Unit
) {
    val navItems = listOf(
        BottomNavItem("日历", Icons.Default.CalendarMonth, NavigationState.CALENDAR),
        BottomNavItem("学习", Icons.Default.Book, QueryType.STUDY), // 书本
        BottomNavItem("生活", Icons.Default.FilterVintage, QueryType.LIFE), // 花朵
        BottomNavItem("杂事", Icons.Default.Hardware, QueryType.MISC), // 锤子
        BottomNavItem("曲线", Icons.Default.QueryStats, NavigationState.CURVE)
    )

    NavigationBar {
        navItems.forEach { item ->
            val isSelected = when (item.navTarget) {
                is NavigationState -> item.navTarget == currentScreen
                is QueryType -> currentScreen == NavigationState.QUERY
                else -> false
            }

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = isSelected,
                onClick = {
                    when (item.navTarget) {
                        is NavigationState -> onNavigate(item.navTarget)
                        is QueryType -> onQuery(item.navTarget)
                    }
                }
            )
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val navTarget: Any
)