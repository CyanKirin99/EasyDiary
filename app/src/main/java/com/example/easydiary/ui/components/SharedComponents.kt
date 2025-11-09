// 文件位置: app/src/main/java/com/example/easydiary/ui/components/SharedComponents.kt

package com.example.easydiary.ui.components

// 导入所有这些函数需要的依赖
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt

// --- 共享的子组件 ---

@Composable
fun LogInput(title: String, content: String, onContentChange: (String) -> Unit) {
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

@Composable
fun ViewerSection(title: String, content: String?) {
    if (!content.isNullOrBlank()) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
    }
}
@Composable
fun ViewerStat(title: String, value: String) {
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