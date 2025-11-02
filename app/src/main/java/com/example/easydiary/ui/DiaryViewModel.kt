package com.example.easydiary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easydiary.data.DiaryEntry
import com.example.easydiary.data.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class NavigationState { CALENDAR, EDITOR, VIEWER, QUERY, CURVE }
enum class QueryType { LIFE, STUDY, MISC }

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentEntry: DiaryEntry? = null,
    val currentScreen: NavigationState = NavigationState.CALENDAR,
    val queryType: QueryType = QueryType.LIFE,
    val queryResults: List<DiaryEntry> = emptyList()
)

class DiaryViewModel(private val repository: DiaryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    // **修改：使用 getAllEntriesSortedByDateASC() (升序) 作为图表数据源**
    val allEntriesForChart: StateFlow<List<DiaryEntry>> = repository.getAllEntriesSortedByDateASC()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // 监听 selectedDate 的变化，并自动更新 currentEntry
        _uiState.map { it.selectedDate }
            .distinctUntilChanged()
            .onEach { date ->
                loadEntryForDate(date)
            }
            .launchIn(viewModelScope)
    }

    private fun loadEntryForDate(date: LocalDate) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            repository.getEntryByDate(dateString).collect { entry ->
                _uiState.update { it.copy(currentEntry = entry) }
            }
        }
    }

    fun navigateTo(screen: NavigationState) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun selectDateAndNavigate(date: LocalDate, screen: NavigationState) {
        _uiState.update {
            it.copy(selectedDate = date, currentScreen = screen)
        }
    }

    // **修改：修复编辑器 Bug**
    fun setEditorDate(newDate: LocalDate) {
        _uiState.update { it.copy(selectedDate = newDate) }
        // loadEntryForDate(newDate) 会在 init{} 的 flow 监听中自动被调用
    }

    // **修改：查看页手势**
    fun viewNextDay() {
        val nextDay = _uiState.value.selectedDate.plusDays(1)
        _uiState.update { it.copy(selectedDate = nextDay) }
    }

    fun viewPreviousDay() {
        val prevDay = _uiState.value.selectedDate.minusDays(1)
        _uiState.update { it.copy(selectedDate = prevDay) }
    }

    fun saveEntry(entryData: DiaryEntry) {
        viewModelScope.launch {
            repository.insertOrUpdate(entryData)
            // 保存后自动跳转到该日的查看模式
            _uiState.update { it.copy(currentScreen = NavigationState.VIEWER) }
        }
    }

    // **新增：删除记录功能**
    fun deleteEntry(date: LocalDate) {
        viewModelScope.launch {
            repository.deleteByDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
            // 删除后返回日历主页
            _uiState.update { it.copy(currentScreen = NavigationState.CALENDAR) }
        }
    }

    fun queryByType(type: QueryType) {
        viewModelScope.launch {
            repository.getAllEntries().collect { allEntries ->
                val filtered = allEntries.filter {
                    when (type) {
                        QueryType.LIFE -> !it.lifeLog.isNullOrBlank()
                        QueryType.STUDY -> !it.studyLog.isNullOrBlank()
                        QueryType.MISC -> !it.miscLog.isNullOrBlank()
                    }
                }
                _uiState.update {
                    it.copy(
                        queryResults = filtered,
                        queryType = type,
                        currentScreen = NavigationState.QUERY
                    )
                }
            }
        }
    }

    fun getAllEntriesForExport(): Flow<List<DiaryEntry>> {
        return repository.getAllEntries()
    }

    // **修改：处理返回键**
    fun handleBackNavigation() {
        if (_uiState.value.currentScreen != NavigationState.CALENDAR) {
            _uiState.update { it.copy(currentScreen = NavigationState.CALENDAR) }
        }
        // 如果已在日历页，则不处理（系统会执行默认的退出 App 逻辑）
    }
}

// ViewModel 工厂 (保持不变)
class DiaryViewModelFactory(private val repository: DiaryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

