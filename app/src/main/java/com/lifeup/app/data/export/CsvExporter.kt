package com.lifeup.app.data.export

import android.content.Context
import com.lifeup.app.data.db.dao.SkillDao
import com.lifeup.app.data.db.dao.TimeRecordDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExporter @Inject constructor(
    private val timeRecordDao: TimeRecordDao,
    private val skillDao: SkillDao,
    @ApplicationContext private val context: Context
) {

    suspend fun exportCsv(): File {
        val records = timeRecordDao.getAll()
        val skills = skillDao.getAll().associateBy { it.id }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val sb = StringBuilder()
        sb.appendLine("日期,技能,开始时间,结束时间,时长(分钟),类型,专注度,备注")

        for (record in records) {
            val skillName = skills[record.skillId]?.name ?: "未知技能"
            val date = dateFormat.format(Date(record.startTime))
            val startTime = timeFormat.format(Date(record.startTime))
            val endTime = timeFormat.format(Date(record.endTime))
            val recordTypeLabel = when (record.recordType) {
                "INVESTMENT" -> "投资"
                "CONSUMPTION" -> "消费"
                else -> record.recordType
            }
            val focusTypeLabel = when (record.focusType) {
                "FOCUSED" -> "专注"
                "UNFOCUSED" -> "不专注"
                else -> record.focusType
            }
            val note = (record.note ?: "").replace(",", "，").replace("\n", " ")

            sb.appendLine("$date,$skillName,$startTime,$endTime,${record.durationMinutes},$recordTypeLabel,$focusTypeLabel,$note")
        }

        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS
        )
        val lifeUpDir = File(downloadsDir, "LifeUp")
        if (!lifeUpDir.exists()) {
            lifeUpDir.mkdirs()
        }

        val fileName = "lifeup_records_${System.currentTimeMillis()}.csv"
        val file = File(lifeUpDir, fileName)
        file.writeText(sb.toString(), Charsets.UTF_8)

        return file
    }
}
