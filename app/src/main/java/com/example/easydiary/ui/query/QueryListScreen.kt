// 文件位置: app/src/main/java/com/example/easydiary/ui/query/QueryListScreen.kt

package com.example.easydiary.ui.query

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.easydiary.data.DiaryEntry
import com.example.easydiary.ui.QueryType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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