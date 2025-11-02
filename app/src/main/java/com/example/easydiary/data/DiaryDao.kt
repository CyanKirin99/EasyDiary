package com.example.easydiary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    // 插入或更新，如果日期已存在则替换
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: DiaryEntry)

    // 查询特定日期的日记
    @Query("SELECT * FROM diary_entries WHERE date = :date")
    fun getEntryByDate(date: String): Flow<DiaryEntry?>

    // 查询所有日记（降序，用于查询列表）
    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    // 查询最近 14 天的日记 (用于旧的图表，保留)
    @Query("SELECT * FROM diary_entries ORDER BY date DESC LIMIT :limit")
    fun getRecentEntries(limit: Int): Flow<List<DiaryEntry>>

    // 查询所有日记（升序，用于新的可拖动图表）
    @Query("SELECT * FROM diary_entries ORDER BY date ASC")
    fun getAllEntriesSortedByDateASC(): Flow<List<DiaryEntry>>

    // **新增：按日期删除条目**
    @Query("DELETE FROM diary_entries WHERE date = :date")
    suspend fun deleteByDate(date: String)
}

