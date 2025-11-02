package com.example.easydiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey
    val date: String, // 格式: "YYYY-MM-DD", 确保唯一性
    val lifeLog: String?, // 事项记录：生活
    val studyLog: String?, // 事项记录：学习
    val miscLog: String?, // 事项记录：杂事
    val moodScore: Int, // 心情分数 (1-10)
    val workDuration: Float // 工作时长
) {
    // 用于初始化新日记的构造函数
    constructor(date: String) : this(
        date = date,
        lifeLog = null,
        studyLog = null,
        miscLog = null,
        moodScore = 5,
        workDuration = 0.0f
    )
}