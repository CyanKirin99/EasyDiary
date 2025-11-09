// 文件位置: app/src/main/java/com/example/easydiary/ui/editor/DiaryEditorScreen.kt

@file:OptIn(ExperimentalMaterial3Api::class) // 确保添加这一行

package com.example.easydiary.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.easydiary.data.DiaryEntry
import com.example.easydiary.ui.components.LogInput
import com.example.easydiary.ui.components.MoodScoreSelector
import com.example.easydiary.ui.components.WorkDurationSlider
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// --- 4. 日记编辑页 (修改：淡黄色背景) ---
@Composable
fun DiaryEditorScreen(
    entry: DiaryEntry?,
    selectedDate: LocalDate,
    onSave: (DiaryEntry) -> Unit,
    onBack: () -> Unit,
    onDateChanged: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var lifeLog by remember(entry) { mutableStateOf(entry?.lifeLog ?: "") }
    var studyLog by remember(entry) { mutableStateOf(entry?.studyLog ?: "") }
    var miscLog by remember(entry) { mutableStateOf(entry?.miscLog ?: "") }
    var moodScore by remember(entry) { mutableStateOf(entry?.moodScore ?: 5) }
    var workDuration by remember(entry) { mutableStateOf(entry?.workDuration ?: 0.0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        modifier = Modifier.clickable { showDatePicker = true }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val newEntry = DiaryEntry(
                            date = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            lifeLog = lifeLog.ifBlank { null },
                            studyLog = studyLog.ifBlank { null },
                            miscLog = miscLog.ifBlank { null },
                            moodScore = moodScore,
                            workDuration = workDuration
                        )
                        onSave(newEntry)
                    }) {
                        Icon(Icons.Default.Check, "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // **修改2：应用淡黄色背景 (来自 Theme.kt)**
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val newDate = java.time.Instant.ofEpochMilli(millis)
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate()
                                    onDateChanged(newDate)
                                }
                                showDatePicker = false
                            }
                        ) { Text("确认") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("取消") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            LogInput(
                title = "生活 (可留空)",
                content = lifeLog,
                onContentChange = { lifeLog = it }
            )
            LogInput(
                title = "学习 (可留空)",
                content = studyLog,
                onContentChange = { studyLog = it }
            )
            LogInput(
                title = "杂事 (可留空)",
                content = miscLog,
                onContentChange = { miscLog = it }
            )
            Spacer(modifier = Modifier.height(24.dp))
            MoodScoreSelector(
                score = moodScore,
                onScoreChange = { moodScore = it }
            )
            Spacer(modifier = Modifier.height(24.dp))

            WorkDurationSlider(
                duration = workDuration,
                onDurationChange = { workDuration = it }
            )
        }
    }
}