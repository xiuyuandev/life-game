package com.lifeup.app.domain.model

import com.lifeup.app.data.db.FocusType
import com.lifeup.app.data.db.RecordType

data class TimeRecord(
    val id: Long = 0,
    val skillId: Long,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val recordType: RecordType = RecordType.INVESTMENT,
    val focusType: FocusType = FocusType.FOCUSED,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isLocked: Boolean = false
)
