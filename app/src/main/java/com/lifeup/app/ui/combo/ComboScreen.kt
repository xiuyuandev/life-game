package com.lifeup.app.ui.combo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.lifeup.app.ui.components.ScreenScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.lifeup.app.data.db.SkillCategory

private val categoryColorMap: Map<SkillCategory, Color> = mapOf(
    SkillCategory.LIVELIHOOD to Color(0xFFFF8D6E),
    SkillCategory.SOCIAL to Color(0xFFFF7043),
    SkillCategory.LANGUAGE to Color(0xFF42A5F5),
    SkillCategory.LIFE to Color(0xFF66BB6A),
    SkillCategory.PHYSICAL to Color(0xFFEF5350),
    SkillCategory.MENTAL to Color(0xFFAB47BC),
    SkillCategory.ART to Color(0xFFFFA726)
)

private val categoryLabelMap: Map<SkillCategory, String> = mapOf(
    SkillCategory.LIVELIHOOD to "谋生",
    SkillCategory.SOCIAL to "社交",
    SkillCategory.LANGUAGE to "语言",
    SkillCategory.LIFE to "生活",
    SkillCategory.PHYSICAL to "体能",
    SkillCategory.MENTAL to "心智",
    SkillCategory.ART to "艺术"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComboScreen(
    onNavigateBack: () -> Unit,
    viewModel: ComboViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = "🔗 技能组合",
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { viewModel.showCreateDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "创建组合")
            }
        }
    ) {
        // Active Combos section
        item {
            Text(
                text = "🔗 我的组合",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (uiState.combos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔗 还没有组合，从推荐中创建吧",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            items(
                items = uiState.combos,
                key = { it.id }
            ) { combo ->
                val primarySkill = uiState.skills.find { it.id == combo.primarySkillId }
                val secondarySkill = uiState.skills.find { it.id == combo.secondarySkillId }

                ComboCard(
                    combo = combo,
                    primarySkill = primarySkill,
                    secondarySkill = secondarySkill,
                    onToggleActive = { viewModel.toggleComboActive(combo.id) },
                    onDelete = { viewModel.deleteCombo(combo.id) }
                )
            }
        }

        // Recommended Combos section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "💡 推荐组合",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(
            items = uiState.recommendedCombos,
            key = { it.name }
        ) { recommended ->
            RecommendedComboCard(
                recommended = recommended,
                skills = uiState.skills,
                onCreateClick = { viewModel.showCreateDialogWithRecommendation(recommended) }
            )
        }
    }

    // Create combo dialog
    if (uiState.showCreateDialog) {
        CreateComboDialog(
            uiState = uiState,
            onDismiss = { viewModel.dismissCreateDialog() },
            onSelectPrimary = { viewModel.selectPrimarySkill(it) },
            onSelectSecondary = { viewModel.selectSecondarySkill(it) },
            onNameChange = { viewModel.updateComboName(it) },
            onConfirm = { viewModel.createCombo() }
        )
    }
}

@Composable
private fun ComboCard(
    combo: com.lifeup.app.domain.model.Combo,
    primarySkill: com.lifeup.app.domain.model.Skill?,
    secondarySkill: com.lifeup.app.domain.model.Skill?,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val primaryColor = primarySkill?.let { categoryColorMap[it.category] } ?: Color.Gray
    val secondaryColor = secondarySkill?.let { categoryColorMap[it.category] } ?: Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (combo.isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = combo.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (combo.isActive) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Primary skill
                    if (primarySkill != null) {
                        CategoryDot(primaryColor)
                        Text(
                            text = primarySkill.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (combo.isActive) primaryColor else primaryColor.copy(alpha = 0.5f)
                        )
                    } else {
                        Text(
                            text = "未知技能",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // Secondary skill
                    if (secondarySkill != null) {
                        CategoryDot(secondaryColor)
                        Text(
                            text = secondarySkill.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (combo.isActive) secondaryColor else secondaryColor.copy(alpha = 0.5f)
                        )
                    } else {
                        Text(
                            text = "未知技能",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "经验加成 ×${String.format("%.2f", combo.expBonus)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (combo.isActive) Color(0xFFFFB300) else Color(0xFFFFB300).copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )
            }

            Switch(
                checked = combo.isActive,
                onCheckedChange = { onToggleActive() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Color(0xFFFFB300),
                    checkedThumbColor = Color.White
                ),
                modifier = Modifier.padding(end = 8.dp)
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
}

@Composable
private fun RecommendedComboCard(
    recommended: RecommendedCombo,
    skills: List<com.lifeup.app.domain.model.Skill>,
    onCreateClick: () -> Unit
) {
    val primaryColor = categoryColorMap[recommended.primaryCategory] ?: Color.Gray
    val secondaryColor = categoryColorMap[recommended.secondaryCategory] ?: Color.Gray
    val primaryLabel = categoryLabelMap[recommended.primaryCategory] ?: ""
    val secondaryLabel = categoryLabelMap[recommended.secondaryCategory] ?: ""

    val hasPrimarySkill = skills.any { it.category == recommended.primaryCategory }
    val hasSecondarySkill = skills.any { it.category == recommended.secondaryCategory }
    val canCreate = hasPrimarySkill && hasSecondarySkill

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommended.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryDot(primaryColor)
                    Text(
                        text = primaryLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CategoryDot(secondaryColor)
                    Text(
                        text = secondaryLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = recommended.suggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "经验加成 ×${String.format("%.2f", recommended.expBonus)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFB300),
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onCreateClick,
                enabled = canCreate,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp,
                    vertical = 4.dp
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "创建",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateComboDialog(
    uiState: ComboUiState,
    onDismiss: () -> Unit,
    onSelectPrimary: (Long) -> Unit,
    onSelectSecondary: (Long) -> Unit,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    var primaryExpanded by remember { mutableStateOf(false) }
    var secondaryExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        try { focusRequester.requestFocus() } catch (_: Exception) { }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "创建技能组合", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Primary skill selector
                ExposedDropdownMenuBox(
                    expanded = primaryExpanded,
                    onExpandedChange = { primaryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.skills.find { it.id == uiState.selectedPrimarySkillId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("主技能") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = primaryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = primaryExpanded,
                        onDismissRequest = { primaryExpanded = false }
                    ) {
                        uiState.skills.forEach { skill ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryDot(categoryColorMap[skill.category] ?: Color.Gray)
                                        Text(skill.name)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = categoryLabelMap[skill.category] ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectPrimary(skill.id)
                                    primaryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Secondary skill selector
                ExposedDropdownMenuBox(
                    expanded = secondaryExpanded,
                    onExpandedChange = { secondaryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.skills.find { it.id == uiState.selectedSecondarySkillId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("副技能") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = secondaryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = secondaryExpanded,
                        onDismissRequest = { secondaryExpanded = false }
                    ) {
                        uiState.skills.forEach { skill ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryDot(categoryColorMap[skill.category] ?: Color.Gray)
                                        Text(skill.name)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = categoryLabelMap[skill.category] ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectSecondary(skill.id)
                                    secondaryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Combo name
                OutlinedTextField(
                    value = uiState.comboName,
                    onValueChange = onNameChange,
                    label = { Text("组合名称（可选）") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onConfirm()
                        }
                    )
                )

                // Auto-calculated exp bonus preview
                val primarySkill = uiState.skills.find { it.id == uiState.selectedPrimarySkillId }
                val secondarySkill = uiState.skills.find { it.id == uiState.selectedSecondarySkillId }
                if (primarySkill != null && secondarySkill != null) {
                    val bonus = RECOMMENDED_COMBOS.find {
                        (it.primaryCategory == primarySkill.category && it.secondaryCategory == secondarySkill.category) ||
                        (it.primaryCategory == secondarySkill.category && it.secondaryCategory == primarySkill.category)
                    }?.expBonus ?: 1.05f

                    Text(
                        text = "经验加成：×${String.format("%.2f", bonus)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFB300),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Error message
                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
