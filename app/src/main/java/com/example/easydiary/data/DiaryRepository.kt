package com.example.easydiary.data

import kotlinx.coroutines.flow.Flow

// Repository 封装了数据源的访问逻辑
class DiaryRepository(private val diaryDao: DiaryDao) {

    fun getEntryByDate(date: String) = diaryDao.getEntryByDate(date)

    fun getAllEntries() = diaryDao.getAllEntries()

    fun getRecentEntries(limit: Int) = diaryDao.getRecentEntries(limit)

    fun getAllEntriesSortedByDateASC() = diaryDao.getAllEntriesSortedByDateASC()

    suspend fun insertOrUpdate(entry: DiaryEntry) {
        diaryDao.insertOrUpdate(entry)
    }

    // **新增：暴露删除功能**
    suspend fun deleteByDate(date: String) {
        diaryDao.deleteByDate(date)
    }
}

