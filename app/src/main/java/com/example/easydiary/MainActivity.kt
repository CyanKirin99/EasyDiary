package com.example.easydiary

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easydiary.data.DiaryDatabase
import com.example.easydiary.data.DiaryRepository
import com.example.easydiary.ui.AppNavigation
import com.example.easydiary.ui.DiaryViewModel
import com.example.easydiary.ui.DiaryViewModelFactory
import com.example.easydiary.ui.theme.EasyDiaryTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.OutputStreamWriter
import java.lang.StringBuilder

// --- 1. 创建 Application 类以持有 ViewModel ---
class EasyDiaryApplication : Application() {
    val database by lazy { DiaryDatabase.getDatabase(this) }
    val repository by lazy { DiaryRepository(database.diaryDao()) }
    val viewModelFactory by lazy { DiaryViewModelFactory(repository) }
}


class MainActivity : ComponentActivity() {

    // --- 2. 准备文件导出器 (Launcher) ---
    val csvLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                // **重要：获取 ViewModel 实例**
                val viewModel = (application as EasyDiaryApplication).viewModelFactory.create(DiaryViewModel::class.java)
                exportCsvToFile(uri, viewModel)
            }
        }
    }

    // CSV 导出逻辑
    private fun exportCsvToFile(uri: Uri, viewModel: DiaryViewModel) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    runBlocking { // 在这里使用 runBlocking 来等待 flow
                        val allEntries = viewModel.getAllEntriesForExport().first()
                        val csvContent = StringBuilder()
                        csvContent.append("日期,生活记录,学习记录,杂事记录,心情分数(1-10),工作时长(h)\n")

                        allEntries.forEach { entry ->
                            csvContent.append(
                                "${entry.date}," +
                                        "${entry.lifeLog.orEmpty().replace(",", ";")}," +
                                        "${entry.studyLog.orEmpty().replace(",", ";")}," +
                                        "${entry.miscLog.orEmpty().replace(",", ";")}," +
                                        "${entry.moodScore}," +
                                        "${entry.workDuration}\n"
                            )
                        }
                        writer.write(csvContent.toString())
                    }
                }
            }
            Toast.makeText(this, "导出成功！", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EasyDiaryTheme {
                // 获取 ViewModel 实例
                val viewModel: DiaryViewModel = viewModel(
                    factory = (application as EasyDiaryApplication).viewModelFactory
                )
                val uiState by viewModel.uiState.collectAsState()
                val allEntriesForChart by viewModel.allEntriesForChart.collectAsState()

                // --- 处理返回键 ---
                BackHandler(enabled = uiState.currentScreen != com.example.easydiary.ui.NavigationState.CALENDAR) {
                    viewModel.handleBackNavigation()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // --- 3. 传递所有新函数和状态到 UI ---
                    AppNavigation(
                        uiState = uiState,
                        allEntriesASC = allEntriesForChart, // <-- 传递图表数据
                        selectedDate = uiState.selectedDate, // <-- 传递 selectedDate
                        onDateSelected = { date ->
                            viewModel.selectDateAndNavigate(date, com.example.easydiary.ui.NavigationState.VIEWER)
                        },
                        onNavigate = viewModel::navigateTo,
                        onQuery = viewModel::queryByType,
                        onSave = viewModel::saveEntry,
                        onBack = viewModel::handleBackNavigation,

                        // **新增：传递新函数**
                        onEditorDateChange = viewModel::setEditorDate,
                        onViewNextDay = viewModel::viewNextDay,
                        onViewPreviousDay = viewModel::viewPreviousDay,

                        // **新增：传递删除函数**
                        onDelete = viewModel::deleteEntry,

                        onExport = {
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "text/csv"
                                putExtra(Intent.EXTRA_TITLE, "EasyDiaryExport_${System.currentTimeMillis()}.csv")
                            }
                            csvLauncher.launch(intent)
                        }
                    )
                }
            }
        }
    }
}

