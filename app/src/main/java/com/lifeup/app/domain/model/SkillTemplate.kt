package com.lifeup.app.domain.model

import com.lifeup.app.data.db.BoundAttribute
import com.lifeup.app.data.db.SkillCategory

data class SkillTemplate(
    val name: String,
    val category: SkillCategory,
    val boundAttribute: BoundAttribute,
    val suggestedThresholds: Map<Int, Int>,
    val suggestedIconKey: String
)
