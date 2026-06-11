package com.lifeup.app.ui.character

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.ItemTier
import com.lifeup.app.data.db.SlotType
import com.lifeup.app.data.preferences.OutfitPreset
import com.lifeup.app.domain.model.Item
import com.lifeup.app.ui.components.AnimatedCounter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterScreen(
    onNavigateToAchievement: () -> Unit = {},
    onNavigateToShop: () -> Unit = {},
    viewModel: CharacterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedSlot by remember { mutableStateOf<SlotType?>(null) }
    var unequipTarget by remember { mutableStateOf<Item?>(null) }
    var showOutfitSheet by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CharacterHeader(
                    characterLevel = uiState.characterLevel,
                    totalExp = uiState.totalExp,
                    title = uiState.title,
                    outfitName = uiState.outfitName,
                    totalAttributeBonus = uiState.totalAttributeBonus,
                    onOutfitClick = { showOutfitSheet = true }
                )
            }

            item {
                AttributesSection(attributes = uiState.attributes)
            }

            item {
                ItemsSection(
                    equippedItems = uiState.equippedItems,
                    backpackItems = uiState.backpackItems,
                    onEquip = { viewModel.equipItem(it) },
                    onUnequip = { unequipTarget = it },
                    onSlotTap = { slot -> selectedSlot = slot }
                )
            }

            item {
                OutlinedButton(
                    onClick = onNavigateToShop,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("金币商店")
                }
            }

            item {
                AchievementWallPreview(
                    onClick = onNavigateToAchievement
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        selectedSlot?.let { slot ->
            val availableItems = uiState.backpackItems.filter { it.slotType == slot }
            SlotSelectionBottomSheet(
                slotType = slot,
                availableItems = availableItems,
                onSelect = { item ->
                    viewModel.equipItem(item)
                    selectedSlot = null
                },
                onDismiss = { selectedSlot = null }
            )
        }

        unequipTarget?.let { item ->
            AlertDialog(
                onDismissRequest = { unequipTarget = null },
                title = { Text("卸下装备") },
                text = { Text("确定要卸下「${item.name}」吗？") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.unequipItem(item)
                        unequipTarget = null
                    }) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { unequipTarget = null }) {
                        Text("取消")
                    }
                }
            )
        }

        if (showOutfitSheet) {
            OutfitPresetBottomSheet(
                presets = uiState.outfitPresets,
                onApply = { preset ->
                    viewModel.applyPreset(preset)
                    showOutfitSheet = false
                },
                onDelete = { preset ->
                    viewModel.deletePreset(preset.id)
                },
                onSave = {
                    showSavePresetDialog = true
                },
                onDismiss = { showOutfitSheet = false }
            )
        }

        if (showSavePresetDialog) {
            SavePresetDialog(
                onConfirm = { name ->
                    viewModel.savePreset(name)
                    showSavePresetDialog = false
                },
                onDismiss = { showSavePresetDialog = false }
            )
        }
    }
}

@Composable
private fun CharacterHeader(
    characterLevel: Int,
    totalExp: Long,
    title: String,
    outfitName: String,
    totalAttributeBonus: Int,
    onOutfitClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Character avatar with ring
                val infiniteTransition = rememberInfiniteTransition(label = "avatarPulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "avatarPulseScale"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.scale(pulseScale)
                ) {
                    // Outer ring
                    Canvas(modifier = Modifier.size(108.dp)) {
                        val strokeWidth = 3.dp.toPx()
                        drawCircle(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            radius = (size.minDimension - strokeWidth) / 2f,
                            style = Stroke(width = strokeWidth)
                        )
                    }
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title with glow effect
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Lv.",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AnimatedCounter(
                        count = characterLevel,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Exp progress bar
                val nextLevelExp = ((characterLevel + 1) * (characterLevel + 1) * 1000L)
                val currentLevelExp = (characterLevel * characterLevel * 1000L)
                val progress = if (nextLevelExp > currentLevelExp) {
                    ((totalExp - currentLevelExp).toFloat() / (nextLevelExp - currentLevelExp)).coerceIn(0f, 1f)
                } else 0f
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(600),
                    label = "expProgress"
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        // Track
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.06f),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                        // Fill
                        val fillWidth = size.width * animatedProgress
                        if (fillWidth > 0f) {
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.primary
                                    ),
                                    startX = 0f,
                                    endX = fillWidth
                                ),
                                topLeft = Offset.Zero,
                                size = Size(fillWidth, size.height),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$totalExp / $nextLevelExp EXP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Outfit name and change button
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = outfitName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = onOutfitClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "换装",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                if (totalAttributeBonus > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "装备加成 +$totalAttributeBonus",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AttributesSection(
    attributes: Map<String, Int>
) {
    val attributeLabels = mapOf(
        "STRENGTH" to "力量",
        "INTELLIGENCE" to "智力",
        "CHARISMA" to "魅力",
        "PERCEPTION" to "感知",
        "CREATIVITY" to "创造力",
        "WILLPOWER" to "意志力",
        "DEXTERITY" to "灵巧",
        "ENDURANCE" to "耐力",
        "LUCK" to "幸运"
    )

    val attributeColors = mapOf(
        "STRENGTH" to Color(0xFFFF5252),
        "INTELLIGENCE" to Color(0xFF448AFF),
        "CHARISMA" to Color(0xFFFF8A50),
        "PERCEPTION" to Color(0xFF66BB6A),
        "CREATIVITY" to Color(0xFFC9A87C),
        "WILLPOWER" to Color(0xFF7E57C2),
        "DEXTERITY" to Color(0xFF00BFA5),
        "ENDURANCE" to Color(0xFFFFB300),
        "LUCK" to Color(0xFFE040FB)
    )

    val attributeEmojis = mapOf(
        "STRENGTH" to "💪",
        "INTELLIGENCE" to "🧠",
        "CHARISMA" to "✨",
        "PERCEPTION" to "👁",
        "CREATIVITY" to "🎨",
        "WILLPOWER" to "🔥",
        "DEXTERITY" to "🎯",
        "ENDURANCE" to "🛡",
        "LUCK" to "🍀"
    )

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "属性面板",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                attributeLabels.forEach { (key, label) ->
                    val value = attributes[key] ?: 0
                    val maxValue = 50
                    val progress = (value.toFloat() / maxValue).coerceIn(0f, 1f)
                    val color = attributeColors[key] ?: MaterialTheme.colorScheme.primary
                    val emoji = attributeEmojis[key] ?: ""
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(600),
                        label = "attr_$key"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji + Label
                        Text(
                            text = emoji,
                            fontSize = 14.sp,
                            modifier = Modifier.width(20.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(48.dp)
                        )

                        // Custom progress bar with gradient
                        Canvas(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            // Track
                            drawRoundRect(
                                color = Color.Black.copy(alpha = 0.05f),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                            // Fill
                            val fillWidth = size.width * animatedProgress
                            if (fillWidth > 0f) {
                                drawRoundRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            color.copy(alpha = 0.5f),
                                            color
                                        ),
                                        startX = 0f,
                                        endX = fillWidth
                                    ),
                                    topLeft = Offset.Zero,
                                    size = Size(fillWidth, size.height),
                                    cornerRadius = CornerRadius(4.dp.toPx())
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "$value",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemsSection(
    equippedItems: List<Item>,
    backpackItems: List<Item>,
    onEquip: (Item) -> Unit,
    onUnequip: (Item) -> Unit,
    onSlotTap: (SlotType) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("道具", "背包")

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚔",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "装备与道具",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> EquippedItemsSection(
                equippedItems = equippedItems,
                onUnequip = onUnequip,
                onSlotTap = onSlotTap
            )
            1 -> BackpackSection(
                backpackItems = backpackItems,
                onEquip = onEquip
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EquippedItemsSection(
    equippedItems: List<Item>,
    onUnequip: (Item) -> Unit,
    onSlotTap: (SlotType) -> Unit
) {
    val slotLabels = mapOf(
        SlotType.HEAD to "头部",
        SlotType.BODY to "身体",
        SlotType.HANDS to "手部",
        SlotType.FEET to "脚部",
        SlotType.ACCESSORY to "饰品"
    )

    val slotIcons: Map<SlotType, ImageVector> = mapOf(
        SlotType.HEAD to Icons.Default.Person,
        SlotType.BODY to Icons.Default.Checkroom,
        SlotType.HANDS to Icons.Default.Shield,
        SlotType.FEET to Icons.Default.Person,
        SlotType.ACCESSORY to Icons.Default.EmojiEvents
    )

    val slotEmojis = mapOf(
        SlotType.HEAD to "👑",
        SlotType.BODY to "🧥",
        SlotType.HANDS to "🧤",
        SlotType.FEET to "👢",
        SlotType.ACCESSORY to "💍"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SlotType.entries.forEach { slot ->
            val label = slotLabels[slot] ?: slot.name
            val icon = slotIcons[slot] ?: Icons.Default.Person
            val emoji = slotEmojis[slot] ?: ""
            val equipped = equippedItems.find { it.slotType == slot }

            if (equipped != null) {
                val tierBorderColor = tierColor(equipped.itemTier)
                Card(
                    onClick = { onUnequip(equipped) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = tierBorderColor.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = tierBorderColor.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = equipped.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (equipped.attributeBonus > 0) {
                                Text(
                                    text = "+${equipped.attributeBonus} 属性",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tierBorderColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = "点击卸下",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                val dashColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                Card(
                    onClick = { onSlotTap(slot) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val stroke = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(10f, 8f),
                                    0f
                                )
                            )
                            drawRoundRect(
                                color = dashColor,
                                cornerRadius = CornerRadius(14.dp.toPx()),
                                style = stroke
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "未装备",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }

                        Text(
                            text = "点击装备",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BackpackSection(
    backpackItems: List<Item>,
    onEquip: (Item) -> Unit
) {
    if (backpackItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎒 背包空空如也",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            backpackItems.forEach { item ->
                Card(
                    onClick = { onEquip(item) },
                    colors = CardDefaults.cardColors(
                        containerColor = tierColor(item.itemTier).copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.width(160.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(enabled = false)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = slotLabel(item.slotType),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.attributeBonus > 0) {
                            Text(
                                text = "+${item.attributeBonus} 属性",
                                style = MaterialTheme.typography.bodySmall,
                                color = tierColor(item.itemTier),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AchievementWallPreview(
    onClick: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏆",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "成就墙",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(8) { _ ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击查看全部成就",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotSelectionBottomSheet(
    slotType: SlotType,
    availableItems: List<Item>,
    onSelect: (Item) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "选择${slotLabel(slotType)}装备",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (availableItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "没有可装备的${slotLabel(slotType)}道具",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                availableItems.forEach { item ->
                    Card(
                        onClick = { onSelect(item) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = tierColor(item.itemTier).copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (item.attributeBonus > 0) {
                                    Text(
                                        text = "+${item.attributeBonus} 属性",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tierColor(item.itemTier)
                                    )
                                }
                            }

                            val tierLabel = when (item.itemTier) {
                                ItemTier.COMMON -> "普通"
                                ItemTier.FINE -> "精良"
                                ItemTier.RARE -> "稀有"
                                ItemTier.EPIC -> "史诗"
                                ItemTier.LEGENDARY -> "传说"
                            }
                            Text(
                                text = tierLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = tierColor(item.itemTier),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutfitPresetBottomSheet(
    presets: List<OutfitPreset>,
    onApply: (OutfitPreset) -> Unit,
    onDelete: (OutfitPreset) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "装束预设",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                if (presets.size < 5) {
                    IconButton(onClick = onSave) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "保存预设"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "最多保存5个预设（当前${presets.size}/5）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (presets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "还没有保存的预设",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                presets.forEach { preset ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                val equippedCount = listOfNotNull(
                                    preset.headItemId,
                                    preset.bodyItemId,
                                    preset.handsItemId,
                                    preset.feetItemId,
                                    preset.accessoryItemId
                                ).size
                                Text(
                                    text = "$equippedCount 件装备",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalButton(
                                onClick = { onApply(preset) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("应用", style = MaterialTheme.typography.labelMedium)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = { onDelete(preset) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除预设",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SavePresetDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保存装束预设") },
        text = {
            OutlinedTextField(
                value = presetName,
                onValueChange = { presetName = it },
                label = { Text("预设名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (presetName.isNotBlank()) onConfirm(presetName.trim()) },
                enabled = presetName.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun tierColor(tier: ItemTier): Color {
    return when (tier) {
        ItemTier.COMMON -> Color(0xFF9E9E9E)
        ItemTier.FINE -> Color(0xFF66BB6A)
        ItemTier.RARE -> Color(0xFF448AFF)
        ItemTier.EPIC -> Color(0xFF7E57C2)
        ItemTier.LEGENDARY -> Color(0xFFFFB300)
    }
}

private fun slotLabel(slotType: SlotType): String {
    return when (slotType) {
        SlotType.HEAD -> "头部"
        SlotType.BODY -> "身体"
        SlotType.HANDS -> "手部"
        SlotType.FEET -> "脚部"
        SlotType.ACCESSORY -> "饰品"
    }
}
