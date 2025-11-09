// 文件位置: app/src/main/java/com/example/easydiary/ui/viewer/DiaryViewerScreen.kt

@file:OptIn(ExperimentalMaterial3Api::class) // 确保添加这一行

package com.example.easydiary.ui.viewer

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.easydiary.data.DiaryEntry
import com.example.easydiary.ui.components.ViewerSection
import com.example.easydiary.ui.components.ViewerStat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// --- 5. 日记查看页 (修改：新增删除按钮) ---
@Composable
fun DiaryViewerScreen(
    entry: DiaryEntry?,
    selectedDate: LocalDate,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit,
    onDelete: (LocalDate) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("您确定要删除 ${selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)} 的日记吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(selectedDate)
                        showDeleteDialog = false
                    }
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry?.date ?: selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (entry != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "删除")
                        }
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, "编辑")
                        }
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.AutoMirrored.Filled.List, "导出")
                    }
                }
            )
        }
    ) { paddingValues ->

        var dragAmountX by remember { mutableStateOf(0f) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { dragAmountX = 0f },
                        onDragEnd = {
                            when {
                                dragAmountX < -150 -> onSwipeNext()
                                dragAmountX > 150 -> onSwipePrevious()
                            }
                        },
                        onDrag = { change, dragDelta ->
                            change.consume()
                            dragAmountX += dragDelta.x
                        }
                    )
                }
        ) {
            if (entry == null) {
                Text(
                    text = "该日暂无记录。",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(text = "去添加记录")
                }
            } else {
                ViewerSection("生活", entry.lifeLog)
                ViewerSection("学习", entry.studyLog)
                ViewerSection("杂事", entry.miscLog)
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                ViewerStat("心情分数", entry.moodScore.toString())
                ViewerStat("工作时长", "${entry.workDuration} 小时")
            }
        }
    }
}