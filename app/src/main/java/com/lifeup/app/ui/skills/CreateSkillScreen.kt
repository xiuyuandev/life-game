package com.lifeup.app.ui.skills

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.BoundAttribute
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.ui.theme.CategoryArt
import com.lifeup.app.ui.theme.CategoryLanguage
import com.lifeup.app.ui.theme.CategoryLife
import com.lifeup.app.ui.theme.CategoryLivelihood
import com.lifeup.app.ui.theme.CategoryMental
import com.lifeup.app.ui.theme.CategoryPhysical
import com.lifeup.app.ui.theme.CategorySocial

private val SkillCategory.color: Color
    get() = when (this) {
        SkillCategory.LIVELIHOOD -> CategoryLivelihood
        SkillCategory.SOCIAL -> CategorySocial
        SkillCategory.LANGUAGE -> CategoryLanguage
        SkillCategory.LIFE -> CategoryLife
        SkillCategory.PHYSICAL -> CategoryPhysical
        SkillCategory.MENTAL -> CategoryMental
        SkillCategory.ART -> CategoryArt
    }

private val SkillCategory.displayName: String
    get() = when (this) {
        SkillCategory.LIVELIHOOD -> "谋生"
        SkillCategory.SOCIAL -> "社交"
        SkillCategory.LANGUAGE -> "语言"
        SkillCategory.LIFE -> "生活"
        SkillCategory.PHYSICAL -> "体能"
        SkillCategory.MENTAL -> "心智"
        SkillCategory.ART -> "艺术"
    }

private val BoundAttribute.displayName: String
    get() = when (this) {
        BoundAttribute.STRENGTH -> "力量"
        BoundAttribute.INTELLIGENCE -> "智力"
        BoundAttribute.CHARISMA -> "魅力"
        BoundAttribute.PERCEPTION -> "感知"
        BoundAttribute.CREATIVITY -> "创造力"
        BoundAttribute.WILLPOWER -> "意志力"
        BoundAttribute.DEXTERITY -> "灵巧"
    }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateSkillScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateSkillViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建技能") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Template quick-select
            Text(
                text = "快速选择模板",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SKILL_TEMPLATES.forEach { template ->
                    SuggestionChip(
                        label = template.name,
                        color = template.category.color,
                        onClick = { viewModel.applyTemplate(template) }
                    )
                }
            }

            // Skill name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("技能名称 *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage != null && uiState.name.isBlank()
            )

            // Category selection
            Text(
                text = "分类",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkillCategory.entries.forEach { category ->
                    FilterChip(
                        selected = uiState.category == category,
                        onClick = { viewModel.updateCategory(category) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(category.color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(category.displayName)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = category.color.copy(alpha = 0.2f),
                            selectedLabelColor = category.color
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = category.color,
                            enabled = true,
                            selected = uiState.category == category
                        )
                    )
                }
            }

            // Bound attribute selection
            Text(
                text = "绑定属性",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column {
                BoundAttribute.entries.forEach { attribute ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateBoundAttribute(attribute) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.boundAttribute == attribute,
                            onClick = { viewModel.updateBoundAttribute(attribute) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = attribute.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Custom thresholds toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "自定义升级阈值",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = uiState.customThresholdsEnabled,
                    onCheckedChange = { viewModel.updateCustomThresholdsEnabled(it) }
                )
            }

            if (uiState.customThresholdsEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "升级所需小时数",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ThresholdInput("LV2", uiState.thresholdLv2) { viewModel.updateThreshold(2, it) }
                        ThresholdInput("LV3", uiState.thresholdLv3) { viewModel.updateThreshold(3, it) }
                        ThresholdInput("LV4", uiState.thresholdLv4) { viewModel.updateThreshold(4, it) }
                        ThresholdInput("LV5", uiState.thresholdLv5) { viewModel.updateThreshold(5, it) }
                    }
                }
            }

            // Energy cost info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.energy < 2f) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "创建技能消耗: 2 能量",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.energy < 2f) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(当前: ${uiState.energy.toInt()})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Error message
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Create button
            Button(
                onClick = { viewModel.createSkill() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = uiState.canCreate && !uiState.isCreating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                )
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("创建技能")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = CardDefaults.outlinedCardBorder(enabled = true),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ThresholdInput(
    level: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = level,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(36.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = value.toString(),
            onValueChange = {
                val parsed = it.toIntOrNull()
                if (parsed != null && parsed >= 0) {
                    onValueChange(parsed)
                } else if (it.isBlank()) {
                    onValueChange(0)
                }
            },
            singleLine = true,
            modifier = Modifier.weight(1f),
            suffix = { Text("小时") }
        )
    }
}
