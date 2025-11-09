// 文件位置: app/src/main/java/com/example/easydiary/ui/calendar/CalendarScreen.kt

package com.example.easydiary.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.easydiary.data.DiaryEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// --- 3. 主页：日历 (修改：手势滑动) ---
@Composable
fun CalendarScreen(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    allEntries: List<DiaryEntry>
) {

    val entriesMap = remember(allEntries) {
        allEntries.associateBy { LocalDate.parse(it.date) }
    }

    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    var dragAmountX by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                    isSelected = date == selectedDate,
                    entry = entry,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

// --- 日历单元格 (修改：选中状态) ---
@Composable
fun CalendarDayCell(
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
    return when (score) {
        in 1..4 -> Color(0xFFE57373) // 红
        in 5..7 -> Color(0xFFFFF176) // 黄
        in 8..10 -> Color(0xFF81C784) // 绿
        else -> Color.Gray
    }
}