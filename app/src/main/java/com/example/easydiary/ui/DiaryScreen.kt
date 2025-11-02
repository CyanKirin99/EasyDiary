@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.easydiary.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easydiary.data.DiaryEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.abs
import kotlin.math.roundToInt

// 显式导入
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState

// **Bug 修复：添加 LazyColumn 和 item 的导入**
import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.item // **<-- 修复 1：重新添加 item 导入**


// --- 1. 导航入口 (签名已完全更新) ---
@Composable
fun AppNavigation(
// ... (代码未更改) ...
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
// ... (代码未更改) ...
                    entry = uiState.currentEntry,
                    selectedDate = selectedDate,
                    onSave = onSave,
                    onBack = onBack,
                    onDateChanged = onEditorDateChange
                )
                NavigationState.VIEWER -> DiaryViewerScreen(
// ... (代码未更改) ...
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
// ... (代码未更改) ...
                    queryType = uiState.queryType,
                    entries = uiState.queryResults,
                    onEntryClick = onDateSelected
                )
                NavigationState.CURVE -> CurveScreen(
// ... (代码未更改) ...
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
// ... (代码未更改) ...
    currentScreen: NavigationState,
    onNavigate: (NavigationState) -> Unit,
    onQuery: (QueryType) -> Unit
) {
    val navItems = listOf(
        // **修改：更新图标**
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
// ... (代码未更改) ...
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val navTarget: Any
)


// --- 3. 主页：日历 (修改：手势滑动) ---
@Composable
fun CalendarScreen(
// ... (代码未更改) ...
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    allEntries: List<DiaryEntry>
) {

    val entriesMap = remember(allEntries) {
        allEntries.associateBy { LocalDate.parse(it.date) }
    }

    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    // **新增：手势检测状态**
    var dragAmountX by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            // **新增：手势检测**
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragAmountX = 0f },
                    onDragEnd = {
                        when {
                            dragAmountX < -150 -> currentMonth = currentMonth.plusMonths(1)
                            dragAmountX > 150 -> currentMonth = currentMonth.minusMonths(1)
                        }
                    },
                    onDrag = { change, dragDelta ->
                        change.consume()
                        dragAmountX += dragDelta.x
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- 月份切换器 ---
// ... (代码未更改) ...
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Text("<") }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy 年 MM 月")),
                style = MaterialTheme.typography.headlineSmall
            )
            Button(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Text(">") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 星期标题 ---
// ... (代码未更改) ...
        Row(modifier = Modifier.fillMaxWidth()) {
            val days = listOf("日", "一", "二", "三", "四", "五", "六")
            days.forEach {
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // --- 日期网格 ---
// ... (代码未更改) ...
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7 // 星期一=1, 星期日=0

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(firstDayOfWeek) {
                Box(modifier = Modifier.aspectRatio(1f))
            }
            items(daysInMonth) { dayIndex ->
                val day = dayIndex + 1
                val date = currentMonth.atDay(day)
                val entry = entriesMap[date]

                CalendarDayCell(
                    day = day,
                    isSelected = date == selectedDate, // **新增：标记选中日期**
                    entry = entry,
                    onClick = { onDateSelected(date) } // **修改：点击即导航**
                )
            }
        }
    }
}

// --- 日历单元格 (修改：选中状态) ---
@Composable
fun CalendarDayCell(
// ... (代码未更改) ...
    day: Int,
    isSelected: Boolean,
    entry: DiaryEntry?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    LocalDate.now().dayOfMonth == day && LocalDate.now().month == entry?.date?.let { LocalDate.parse(it).month } ->
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
        )
        entry?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(getMoodColor(it.moodScore))
            )
        }
    }
}

fun getMoodColor(score: Int): Color {
// ... (代码未更改) ...
    return when (score) {
        in 1..4 -> Color(0xFFE57373) // 红
        in 5..7 -> Color(0xFFFFF176) // 黄
        in 8..10 -> Color(0xFF81C784) // 绿
        else -> Color.Gray
    }
}


// --- 4. 日记编辑页 (修改：工作时长滑动条) ---
@Composable
fun DiaryEditorScreen(
// ... (代码未更改) ...
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
    // **修改：工作时长现在是 Float**
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
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (showDatePicker) {
// ... (代码未更改) ...
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
// ... (代码未更改) ...
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
// ... (代码未更改) ...
            MoodScoreSelector(
                score = moodScore,
                onScoreChange = { moodScore = it }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // **修改：使用新的工作时长滑动条**
// ... (代码未更改) ...
            WorkDurationSlider(
                duration = workDuration,
                onDurationChange = { workDuration = it }
            )
        }
    }
}

// --- 5. 日记查看页 (修改：新增删除按钮) ---
@Composable
fun DiaryViewerScreen(
// ... (代码未更改) ...
    entry: DiaryEntry?,
    selectedDate: LocalDate,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit,
    onDelete: (LocalDate) -> Unit // **新增：删除回调**
) {
    // **新增：删除确认对话框**
    var showDeleteDialog by remember { mutableStateOf(false) }
// ... (代码未更改) ...
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
// ... (代码未更改) ...
                title = { Text(entry?.date ?: selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (entry != null) {
                        // **新增：删除按钮**
// ... (代码未更改) ...
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
// ... (代码未更改) ...
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
// ... (代码未更改) ...
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

@Composable
fun ViewerSection(title: String, content: String?) {
// ... (代码未更改) ...
    if (!content.isNullOrBlank()) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
    }
}
@Composable
fun ViewerStat(title: String, value: String) {
// ... (代码未更改) ...
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
    }
    Spacer(modifier = Modifier.height(8.dp))
}


// --- 6. 查询结果列表页 (使用替代方案) ---
@Composable
fun QueryListScreen(
    queryType: QueryType,
    entries: List<DiaryEntry>,
    onEntryClick: (LocalDate) -> Unit
) {
    val title = when (queryType) {
        QueryType.LIFE -> "生活记录"
        QueryType.STUDY -> "学习记录"
        QueryType.MISC -> "杂事记录"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(0.dp)
    ) {
        item {
            Text(title, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(16.dp))
        }
        items(entries) { entry ->
            val content = when (queryType) {
                QueryType.LIFE -> entry.lifeLog
                QueryType.STUDY -> entry.studyLog
                QueryType.MISC -> entry.miscLog
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEntryClick(LocalDate.parse(entry.date)) }
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = content ?: "",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = LocalDate.parse(entry.date).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Divider()
        }
    }
}

// --- 7. "曲线"图表页 (修改：重绘折线图) ---
@Composable
fun CurveScreen(
// ... (代码未更改) ...
    entries: List<DiaryEntry>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
// ... (代码未更改) ...
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无数据", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
// ... (代码未更改) ...
        Text(
            "心情曲线 (1-10)",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        // **修改：使用折线图**
        InternalLineChart(
// ... (代码未更改) ...
            entries = entries,
            onDateSelected = onDateSelected,
            dataSelector = { it.moodScore.toFloat() },
            maxVal = 10f,
            color = Color.Blue
        )

        Spacer(modifier = Modifier.height(24.dp))
// ... (代码未更改) ...
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        Text(
// ... (代码未更改) ...
            "工作时长 (小时)",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        // **修改：使用折线图**
        InternalLineChart(
// ... (代码未更改) ...
            entries = entries,
            onDateSelected = onDateSelected,
            dataSelector = { it.workDuration },
            maxVal = entries.maxOfOrNull { it.workDuration }?.coerceAtLeast(1f) ?: 1f,
            color = Color.Red
        )
    }
}

// **修改：折线图 Composable**
@Composable
private fun InternalLineChart(
// ... (代码未更改) ...
    entries: List<DiaryEntry>,
    onDateSelected: (LocalDate) -> Unit,
    dataSelector: (DiaryEntry) -> Float,
    maxVal: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val yAxisLeftPadding = 100f
// ... (代码未更改) ...
    val xAxisBottomPadding = 100f
    val pointSpacing = 150f
// ... (代码未更改) ...
    val chartWidth = (entries.size - 1).coerceAtLeast(0) * pointSpacing + yAxisLeftPadding * 2
    val chartScrollState = rememberScrollState()

    val density = LocalDensity.current

    // **Bug 修复 2：使用更明确的初始化方式**
    val yAxisLabelPaint = remember(density) {
        Paint().apply {
            this.color = android.graphics.Color.GRAY // 使用 native Int 颜色
            this.textAlign = Paint.Align.CENTER
            this.textSize = density.run { 12.sp.toPx() }
        }
    }
    val xAxisLabelPaint = remember(density) {
        Paint().apply {
            this.color = android.graphics.Color.BLACK
            this.textAlign = Paint.Align.CENTER
            this.textSize = density.run { 12.sp.toPx() }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .horizontalScroll(chartScrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(chartWidth.dp)
                .fillMaxHeight()
                .background(Color(0xFFFAFAFA))
                .pointerInput(entries) {
                    detectTapGestures { offset ->
                        val xIndex = ((offset.x - yAxisLeftPadding) / pointSpacing).roundToInt()
                        if (xIndex in entries.indices) {
                            onDateSelected(LocalDate.parse(entries[xIndex].date))
                        }
                    }
                }
        ) {
            val yBottom = size.height - xAxisBottomPadding
// ... (代码未更改) ...
            val yTop = 0f
            val xStart = yAxisLeftPadding

            // 绘制 Y 轴
// ... (代码未更改) ...
            drawLine(
                color = Color.LightGray,
                start = Offset(xStart, yTop),
                end = Offset(xStart, yBottom),
                strokeWidth = 2f
            )

            // Y 轴标签
// ... (代码未更改) ...
            val yLabelCount = 5
            (0..yLabelCount).forEach { i ->
                val value = maxVal * i / yLabelCount
                val y = yBottom - (value / maxVal) * (yBottom - yTop)

                drawContext.canvas.nativeCanvas.drawText(
                    "%.1f".format(value),
                    xStart - 40,
                    y + 5,
                    yAxisLabelPaint // <-- 使用独立的 Paint
                )
            }

            // 绘制 X 轴
// ... (代码未更改) ...
            drawLine(
                color = Color.LightGray,
                start = Offset(xStart, yBottom),
                end = Offset(size.width, yBottom),
                strokeWidth = 2f
            )

            val path = Path()

            entries.forEachIndexed { index, entry ->
// ... (代码未更改) ...
                val x = xStart + (index * pointSpacing)
                val yValue = dataSelector(entry)
                val y = yBottom - (yValue / maxVal) * (yBottom - yTop)

                if (index == 0) {
// ... (代码未更改) ...
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                drawCircle(color, radius = 8f, center = Offset(x, y))

                drawContext.canvas.nativeCanvas.drawText(
// ... (代码未更改) ...
                    entry.date.substring(5),
                    x,
                    yBottom + 40,
                    xAxisLabelPaint // <-- 使用独立的 Paint
                )
            }

// ... (代码未更改) ...
            drawPath(path, color, style = Stroke(width = 4f))
        }
    }
}


// --- 共享的子组件 (修改：工作时长滑动条) ---

@Composable
fun LogInput(title: String, content: String, onContentChange: (String) -> Unit) {
// ... (代码未更改) ...
    OutlinedTextField(
        value = content,
        onValueChange = onContentChange,
        label = { Text(title) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        maxLines = 5
    )
}

@Composable
fun MoodScoreSelector(score: Int, onScoreChange: (Int) -> Unit) {
// ... (代码未更改) ...
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "心情分数: $score / 10", style = MaterialTheme. typography.titleMedium)
        Slider(
            value = score.toFloat(),
            onValueChange = { onScoreChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8, // 1, 2, 3, ... 10 (9 个步进)
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// **修改：使用滑动条替代 OutlinedTextField**
@Composable
fun WorkDurationSlider(duration: Float, onDurationChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "工作时长: ${"%.1f".format(duration)} 小时", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = duration,
            onValueChange = {
                // 将值四舍五入到最近的 0.5
                val rounded = (it * 2).roundToInt() / 2.0f
                onDurationChange(rounded)
            },
            valueRange = 0f..12f,
            // (12 - 0) / 0.5 = 24 个点，23 个步进
            steps = 23,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0.0 h", style = MaterialTheme.typography.bodySmall)
            Text("12.0 h", style = MaterialTheme.typography.bodySmall)
        }
    }
}

