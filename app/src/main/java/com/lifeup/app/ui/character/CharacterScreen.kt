package com.lifeup.app.ui.character

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.ItemTier
import com.lifeup.app.data.db.SlotType
import com.lifeup.app.domain.model.Item

@Composable
fun CharacterScreen(
    viewModel: CharacterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            // Section 1: Character image area
            item {
                CharacterHeader(
                    characterLevel = uiState.characterLevel,
                    totalExp = uiState.totalExp,
                    title = uiState.title,
                    equippedItems = uiState.equippedItems
                )
            }

            // Section 2: Attributes
            item {
                AttributesSection(attributes = uiState.attributes)
            }

            // Section 3: Items & Backpack tabs
            item {
                ItemsSection(
                    equippedItems = uiState.equippedItems,
                    backpackItems = uiState.backpackItems,
                    onEquip = { viewModel.equipItem(it) },
                    onUnequip = { viewModel.unequipItem(it) }
                )
            }

            // Section 4: Achievement wall placeholder
            item {
                AchievementWallPlaceholder()
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun CharacterHeader(
    characterLevel: Int,
    totalExp: Long,
    title: String,
    equippedItems: List<Item>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Character avatar placeholder
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Level
            Text(
                text = "Lv.$characterLevel",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Exp progress bar
            val nextLevelExp = ((characterLevel + 1) * (characterLevel + 1) * 1000L)
            val currentLevelExp = (characterLevel * characterLevel * 1000L)
            val progress = if (nextLevelExp > currentLevelExp) {
                ((totalExp - currentLevelExp).toFloat() / (nextLevelExp - currentLevelExp)).coerceIn(0f, 1f)
            } else 0f

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small),
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$totalExp / $nextLevelExp EXP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Outfit name
            val outfitName = if (equippedItems.isNotEmpty()) {
                equippedItems.joinToString(" + ") { it.name }
            } else {
                "默认装束"
            }
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

    Column {
        Text(
            text = "属性面板",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            )
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(56.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(color)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "$value",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.width(32.dp),
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
    onUnequip: (Item) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("道具", "背包")

    Column {
        Text(
            text = "装备与道具",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

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
                onUnequip = onUnequip
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
    onUnequip: (Item) -> Unit
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SlotType.entries.forEach { slot ->
            val label = slotLabels[slot] ?: slot.name
            val icon = slotIcons[slot] ?: Icons.Default.Person
            val equipped = equippedItems.find { it.slotType == slot }

            Card(
                onClick = {
                    if (equipped != null) onUnequip(equipped)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (equipped != null) {
                        tierColor(equipped.itemTier).copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (equipped != null) {
                            tierColor(equipped.itemTier)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (equipped != null) {
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
                                    color = tierColor(equipped.itemTier)
                                )
                            }
                        } else {
                            Text(
                                text = "空",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
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
                text = "背包空空如也",
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
                        containerColor = tierColor(item.itemTier).copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.width(160.dp)
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
                                color = tierColor(item.itemTier)
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
private fun AchievementWallPlaceholder() {
    Column {
        Text(
            text = "成就墙",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            )
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(8) { index ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
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
