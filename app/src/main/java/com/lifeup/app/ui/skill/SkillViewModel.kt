package com.lifeup.app.ui.skill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.SeedData
import com.lifeup.app.data.db.entity.SkillEntity
import com.lifeup.app.data.repository.SkillRepository
import com.lifeup.app.data.repository.TimeSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SkillViewModel @Inject constructor(
    private val skillRepository: SkillRepository,
    private val timeSessionRepository: TimeSessionRepository
) : ViewModel() {

    private val _skills = MutableStateFlow<List<SkillEntity>>(emptyList())
    val skills: StateFlow<List<SkillEntity>> = _skills.asStateFlow()

    private val _selectedSkill = MutableStateFlow<SkillEntity?>(null)
    val selectedSkill: StateFlow<SkillEntity?> = _selectedSkill.asStateFlow()

    private val _childSkills = MutableStateFlow<List<SkillEntity>>(emptyList())
    val childSkills: StateFlow<List<SkillEntity>> = _childSkills.asStateFlow()

    private val _skillStats = MutableStateFlow<SkillStats?>(null)
    val skillStats: StateFlow<SkillStats?> = _skillStats.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        loadSkills()
    }

    private fun loadSkills() {
        skillRepository.getAllSkillsFlow()
            .onEach { _skills.value = it }
            .launchIn(viewModelScope)
    }

    fun selectSkill(skillId: Long) {
        viewModelScope.launch {
            val skill = skillRepository.getSkillById(skillId)
            _selectedSkill.value = skill
            skill?.let {
                val children = skillRepository.getChildSkills(skillId)
                _childSkills.value = children
                loadSkillStats(skillId)
            }
        }
    }

    private suspend fun loadSkillStats(skillId: Long) {
        val skill = _selectedSkill.value ?: return
        val sessions = timeSessionRepository.getSessionsBySkill(skillId)

        // Calculate weekly minutes (last 7 days)
        val weeklyMap = mutableMapOf<String, Long>()
        val today = LocalDate.now()
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val dayLabel = if (i == 0) "今天" else if (i == 1) "昨天" else {
                date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
            }
            val dateStr = date.format(dateFormatter)
            val minutes = sessions
                .filter { it.date == dateStr }
                .sumOf { it.durationMinutes }
            weeklyMap[dayLabel] = minutes
        }

        _skillStats.value = SkillStats(
            totalMinutes = skill.totalMinutesInvested,
            weeklyMinutes = weeklyMap
        )
    }

    fun addSkill(name: String, category: String, icon: String, description: String, parentSkillId: Long? = null) {
        viewModelScope.launch {
            val skill = SkillEntity(
                name = name,
                category = category,
                icon = icon,
                description = description,
                parentSkillId = parentSkillId,
                unlocked = parentSkillId == null
            )
            skillRepository.insert(skill)
        }
    }

    fun deleteSkill(skillId: Long) {
        viewModelScope.launch {
            skillRepository.deleteById(skillId)
        }
    }

    fun clearSelection() {
        _selectedSkill.value = null
        _childSkills.value = emptyList()
        _skillStats.value = null
    }

    fun getSkillsByCategory(): Map<String, List<SkillEntity>> {
        return _skills.value.groupBy { it.category }
            .toSortedMap(compareBy { SeedData.categoryNames.keys.indexOf(it) })
    }
}

data class SkillStats(
    val totalMinutes: Long,
    val weeklyMinutes: Map<String, Long>
)