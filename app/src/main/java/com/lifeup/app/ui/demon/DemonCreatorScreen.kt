package com.lifeup.app.ui.demon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.ui.theme.SecondaryOrange

/**
 * 自定义心魔编辑器：玩家可以创造自己专属的"敌人"。
 *
 * 字段：
 *  - 名称 / 描述
 *  - 颜色（4 选 1）
 *  - 弱点分类（多选）
 *  - 抵抗分类（多选）
 *  - 7 个部位 HP（每个 50~200，滑块）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemonCreatorScreen(
    onNavigateBack: () -> Unit,
    onCreated: () -> Unit = {},
    viewModel: DemonCreatorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "创造你的心魔",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PreviewCard(
                    name = state.name.ifBlank { "未命名心魔" },
                    description = state.description.ifBlank { "写下你给它的故事……" },
                    colorHex = state.colorHex,
                    weekHps = state.weekHps
                )
            }
            item {
                SectionTitle("基本信息")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("心魔名字") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("故事 / 描述") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            item {
                SectionTitle("颜色")
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DemonCreatorViewModel.PALETTE.forEach { hex ->
                            val selected = state.colorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(parseColorOrNull(hex) ?: Color.Gray)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.onColorChange(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "已选",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
            item {
                SectionTitle("弱点分类（命中时伤害 ×1.6）")
                Spacer(modifier = Modifier.height(8.dp))
                CategoryChips(
                    selected = state.weakCategories,
                    onToggle = viewModel::toggleWeak
                )
            }
            item {
                SectionTitle("抵抗分类（命中时伤害 ×0.55）")
                Spacer(modifier = Modifier.height(8.dp))
                CategoryChips(
                    selected = state.resistCategories,
                    onToggle = viewModel::toggleResist
                )
            }
            item {
                SectionTitle("七日之躯（每个部位 HP 50~200）")
                Spacer(modifier = Modifier.height(8.dp))
                HpSliders(
                    hps = state.weekHps,
                    onChange = viewModel::setPartHp
                )
            }
            item {
                Button(
                    onClick = {
                        viewModel.save()
                        onCreated()
                    },
                    enabled = state.canSave,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("把它写入你的心魔册", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PreviewCard(name: String, description: String, colorHex: String, weekHps: List<Int>) {
    val color = parseColorOrNull(colorHex) ?: Color(0xFF455A64)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.6f))))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "总 HP: ${weekHps.sum()}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun CategoryChips(selected: Set<SkillCategory>, onToggle: (SkillCategory) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(SkillCategory.values().toList()) { cat ->
            val isSelected = cat in selected
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(cat) },
                label = { Text(categoryName(cat)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun HpSliders(hps: List<Int>, onChange: (Int, Int) -> Unit) {
    val partNames = com.lifeup.app.domain.model.DemonTemplate.PART_NAMES
    Column {
        hps.forEachIndexed { idx, hp ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${partNames[idx]} (Day ${idx + 1})",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(120.dp)
                )
                androidx.compose.material3.Slider(
                    value = hp.toFloat(),
                    onValueChange = { onChange(idx, it.toInt()) },
                    valueRange = 50f..200f,
                    steps = 14,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$hp",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(40.dp)
                )
            }
        }
    }
}

private fun categoryName(c: SkillCategory): String = when (c) {
    SkillCategory.LIVELIHOOD -> "谋生"
    SkillCategory.SOCIAL -> "社交"
    SkillCategory.LANGUAGE -> "语言"
    SkillCategory.LIFE -> "生活"
    SkillCategory.PHYSICAL -> "体能"
    SkillCategory.MENTAL -> "心智"
    SkillCategory.ART -> "艺术"
}

/** 解析颜色（容忍 #RGB / #RRGGBB / #AARRGGBB） */
private fun parseColorOrNull(hex: String): Color? = try {
    com.lifeup.app.domain.model.InnerDemon.parseColor(hex)
} catch (_: Exception) {
    null
}
