package com.lifeup.app.ui.skill

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeup.app.data.SeedData
import com.lifeup.app.data.db.entity.SkillEntity
import com.lifeup.app.ui.components.*
import com.lifeup.app.ui.theme.PixelColors

@Composable
fun SkillMapScreen(
    viewModel: SkillViewModel = hiltViewModel(),
    onSkillClick: (Long) -> Unit
) {
    val skills by viewModel.skills.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var filterCategory by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val skillsByCategory = if (filterCategory != null) {
        mapOf(filterCategory to skills.filter { it.category == filterCategory })
    } else {
        skills.groupBy { it.category }
    }

    Box(modifier = Modifier.fillMaxSize().background(PixelColors.Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PixelColors.GradientHero)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🗺️ 技能地图",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PixelColors.AccentGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "已解锁 ${skills.count { it.unlocked }} / ${skills.size} 技能",
                            style = MaterialTheme.typography.labelMedium,
                            color = PixelColors.TextMuted
                        )
                    }
                    CategoryFilter(
                        selected = filterCategory,
                        onSelect = { filterCategory = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Skills by category
            skillsByCategory.forEach { (category, categorySkills) ->
                val categoryName = SeedData.categoryNames[category] ?: category
                val rootSkills = categorySkills.filter { it.parentSkillId == null }

                if (rootSkills.isNotEmpty()) {
                    GlassCard(glowColor = getCategoryColor(category).copy(alpha = 0.1f)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(getCategoryColor(category))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = categoryName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = getCategoryColor(category),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            rootSkills.forEach { skill ->
                                SkillTreeNode(
                                    skill = skill,
                                    allSkills = skills,
                                    onSkillClick = onSkillClick
                                )
                                if (skill != rootSkills.last()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = PixelColors.Primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加技能")
        }
    }

    if (showAddDialog) {
        AddSkillDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, category, icon, desc ->
                viewModel.addSkill(name, category, icon, desc)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SkillTreeNode(
    skill: SkillEntity,
    allSkills: List<SkillEntity>,
    level: Int = 0,
    onSkillClick: (Long) -> Unit
) {
    val childSkills = allSkills.filter { it.parentSkillId == skill.id }
    val progress = if (skill.expToNext > 0) skill.exp.toFloat() / skill.expToNext.toFloat() else 0f
    val categoryColor = getCategoryColor(skill.category)
    val indent = level * 20

    Column {
        // Skill Card
        val isUnlocked = skill.unlocked
        val bgColor = if (isUnlocked) PixelColors.SurfaceElevated.copy(alpha = 0.5f)
        else PixelColors.Surface.copy(alpha = 0.3f)
        val borderColor = if (isUnlocked) categoryColor.copy(alpha = 0.3f)
        else PixelColors.Border

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = indent.dp)
                .background(bgColor, RoundedCornerShape(14.dp))
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .clickable(enabled = isUnlocked) { onSkillClick(skill.id) }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isUnlocked) categoryColor.copy(alpha = 0.15f)
                        else PixelColors.SurfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (isUnlocked) categoryColor.copy(alpha = 0.3f)
                        else PixelColors.Border,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isUnlocked) skill.icon else "🔒",
                    fontSize = if (isUnlocked) 22.sp else 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isUnlocked) PixelColors.TextPrimary else PixelColors.TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!isUnlocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusBadge(
                            text = "未解锁",
                            color = PixelColors.TextMuted
                        )
                    }
                }

                if (isUnlocked) {
                    Spacer(modifier = Modifier.height(6.dp))
                    GlowProgressBar(
                        progress = progress,
                        progressBrush = Brush.horizontalGradient(
                            listOf(categoryColor, categoryColor.copy(alpha = 0.7f))
                        ),
                        glowColor = categoryColor.copy(alpha = 0.2f),
                        height = 6.dp,
                        label = null,
                        animate = false
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${skill.exp}/${skill.expToNext} EXP  ·  ${skill.totalMinutesInvested}分钟投入",
                        style = MaterialTheme.typography.labelSmall,
                        color = PixelColors.TextMuted
                    )
                } else {
                    val parent = allSkills.find { it.id == skill.parentSkillId }
                    Text(
                        text = "需: ${parent?.name ?: ""} Lv.${skill.parentLevelRequired}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PixelColors.Warning
                    )
                }
            }

            // Level badge
            if (isUnlocked) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Lv.${skill.level}",
                        style = MaterialTheme.typography.titleSmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold
                    )
                    if (skill.level >= skill.maxLevel) {
                        Text(
                            text = "MAX",
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelColors.AccentGold
                        )
                    }
                }
            }
        }

        // Connector line + children
        if (childSkills.isNotEmpty()) {
            Box(modifier = Modifier.padding(start = (indent + 28).dp)) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height((childSkills.size * 8).dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(categoryColor.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )
            }
            childSkills.forEach { child ->
                Spacer(modifier = Modifier.height(4.dp))
                SkillTreeNode(
                    skill = child,
                    allSkills = allSkills,
                    level = level + 1,
                    onSkillClick = onSkillClick
                )
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .background(PixelColors.SurfaceElevated, RoundedCornerShape(10.dp))
                .border(1.dp, PixelColors.Border, RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (selected != null) SeedData.categoryNames[selected] ?: selected else "全部",
                style = MaterialTheme.typography.labelMedium,
                color = PixelColors.TextSecondary
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(PixelColors.Surface)
        ) {
            DropdownMenuItem(
                text = { Text("全部", color = PixelColors.TextPrimary) },
                onClick = { onSelect(null); expanded = false }
            )
            SeedData.categoryNames.forEach { (key, name) ->
                DropdownMenuItem(
                    text = { Text(name, color = PixelColors.TextPrimary) },
                    onClick = { onSelect(key); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddSkillDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("professional") }
    var selectedIcon by remember { mutableStateOf("⭐") }

    val icons = listOf("⭐", "💻", "📚", "🗣️", "🏃", "💪", "🎨", "✍️", "🧘", "🍳", "📊", "🌐", "🎵", "🔬", "💡")
    val categories = SeedData.categoryNames.toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PixelColors.Surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text("添加新技能", color = PixelColors.AccentGold, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("技能名称", color = PixelColors.TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PixelColors.TextPrimary,
                        unfocusedTextColor = PixelColors.TextSecondary,
                        focusedBorderColor = PixelColors.Primary,
                        unfocusedBorderColor = PixelColors.Border
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述", color = PixelColors.TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PixelColors.TextPrimary,
                        unfocusedTextColor = PixelColors.TextSecondary,
                        focusedBorderColor = PixelColors.Primary,
                        unfocusedBorderColor = PixelColors.Border
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("类别", style = MaterialTheme.typography.labelLarge, color = PixelColors.TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { (key, catName) ->
                        val isSelected = selectedCategory == key
                        val catColor = getCategoryColor(key)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) catColor.copy(alpha = 0.2f)
                                    else PixelColors.SurfaceElevated,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) catColor.copy(alpha = 0.5f)
                                    else PixelColors.Border,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedCategory = key }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                catName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) catColor else PixelColors.TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("图标", style = MaterialTheme.typography.labelLarge, color = PixelColors.TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    icons.forEach { icon ->
                        val isSelected = selectedIcon == icon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (isSelected) PixelColors.Primary.copy(alpha = 0.2f)
                                    else PixelColors.SurfaceElevated,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) PixelColors.Primary else PixelColors.Border,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(icon, fontSize = 20.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            GlowButton(
                text = "添加",
                onClick = { onAdd(name, selectedCategory, selectedIcon, description) },
                brush = Brush.horizontalGradient(listOf(PixelColors.Primary, PixelColors.PrimaryVariant))
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = PixelColors.TextMuted) }
        }
    )
}

@Composable
private fun getCategoryColor(category: String): Color = when (category) {
    "professional" -> PixelColors.AccentBlue
    "language" -> PixelColors.AccentCyan
    "sport" -> PixelColors.AccentGreen
    "art" -> PixelColors.AccentPink
    "life" -> PixelColors.AccentOrange
    "social" -> PixelColors.AccentPurple
    else -> PixelColors.Secondary
}