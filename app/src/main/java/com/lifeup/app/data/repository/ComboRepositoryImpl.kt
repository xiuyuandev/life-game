package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.ComboDao
import com.lifeup.app.data.db.entity.ComboEntity
import com.lifeup.app.domain.model.Combo
import com.lifeup.app.domain.repository.ComboRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComboRepositoryImpl @Inject constructor(
    private val comboDao: ComboDao
) : ComboRepository {

    override fun getActiveCombos(): Flow<List<Combo>> {
        return comboDao.getActive().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertCombo(combo: Combo): Long {
        return comboDao.insert(combo.toEntity())
    }

    override suspend fun updateCombo(combo: Combo) {
        comboDao.update(combo.toEntity())
    }

    override suspend fun deleteCombo(combo: Combo) {
        comboDao.delete(combo.toEntity())
    }
}

fun ComboEntity.toDomain(): Combo {
    return Combo(
        id = id,
        name = name,
        primarySkillId = primarySkillId,
        secondarySkillId = secondarySkillId,
        requiredLevel = requiredLevel,
        expBonus = expBonus,
        suggestion = suggestion,
        connectionColor = connectionColor,
        isActive = isActive,
        createdAt = createdAt
    )
}

fun Combo.toEntity(): ComboEntity {
    return ComboEntity(
        id = id,
        name = name,
        primarySkillId = primarySkillId,
        secondarySkillId = secondarySkillId,
        requiredLevel = requiredLevel,
        expBonus = expBonus,
        suggestion = suggestion,
        connectionColor = connectionColor,
        isActive = isActive,
        createdAt = createdAt
    )
}
