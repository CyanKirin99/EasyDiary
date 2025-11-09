// 文件位置: app/src/main/java/com/example/easydiary/ui/curve/CurveScreen.kt

package com.example.easydiary.ui.curve

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easydiary.data.DiaryEntry
import com.example.easydiary.ui.theme.ChartMood
import com.example.easydiary.ui.theme.ChartWork
import java.time.LocalDate
import kotlin.math.roundToInt

// --- 7. "曲线"图表页 (修改：颜色和背景) ---
@Composable
fun CurveScreen(
    entries: List<DiaryEntry>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
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
        Text(
            "心情曲线 (1-10)",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        InternalLineChart(
            entries = entries,
            onDateSelected = onDateSelected,
            dataSelector = { it.moodScore.toFloat() },
            maxVal = 10f,
            color = ChartMood // **修改1：使用新的愉悦颜色**
        )

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "工作时长 (小时)",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        InternalLineChart(
            entries = entries,
            onDateSelected = onDateSelected,
            dataSelector = { it.workDuration },
            maxVal = entries.maxOfOrNull { it.workDuration }?.coerceAtLeast(1f) ?: 1f,
            color = ChartWork // **修改1：使用新的愉悦颜色**
        )
    }
}

@Composable
private fun InternalLineChart(
    entries: List<DiaryEntry>,
    onDateSelected: (LocalDate) -> Unit,
    dataSelector: (DiaryEntry) -> Float,
    maxVal: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val yAxisLeftPadding = 100f
    val xAxisBottomPadding = 100f
    val pointSpacing = 150f
    val chartWidth = (entries.size - 1).coerceAtLeast(0) * pointSpacing + yAxisLeftPadding * 2
    val chartScrollState = rememberScrollState()

    val density = LocalDensity.current

    val yAxisLabelPaint = remember(density) {
        Paint().apply {
            this.color = android.graphics.Color.GRAY
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
                // **修改1：使用透明背景**
                .background(Color.Transparent)
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
            val yTop = 0f
            val xStart = yAxisLeftPadding

            // 绘制 Y 轴
            drawLine(
                color = Color.LightGray,
                start = Offset(xStart, yTop),
                end = Offset(xStart, yBottom),
                strokeWidth = 2f
            )

            // Y 轴标签
            val yLabelCount = 5
            (0..yLabelCount).forEach { i ->
                val value = maxVal * i / yLabelCount
                val y = yBottom - (value / maxVal) * (yBottom - yTop)

                drawContext.canvas.nativeCanvas.drawText(
                    "%.1f".format(value),
                    xStart - 40,
                    y + 5,
                    yAxisLabelPaint
                )
            }

            // 绘制 X 轴
            drawLine(
                color = Color.LightGray,
                start = Offset(xStart, yBottom),
                end = Offset(size.width, yBottom),
                strokeWidth = 2f
            )

            val path = Path()

            entries.forEachIndexed { index, entry ->
                val x = xStart + (index * pointSpacing)
                val yValue = dataSelector(entry)
                val y = yBottom - (yValue / maxVal) * (yBottom - yTop)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                drawCircle(color, radius = 8f, center = Offset(x, y))

                drawContext.canvas.nativeCanvas.drawText(
                    entry.date.substring(5),
                    x,
                    yBottom + 40,
                    xAxisLabelPaint
                )
            }

            drawPath(path, color, style = Stroke(width = 4f))
        }
    }
}