package com.lifeup.app.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeup.app.data.SeedData
import com.lifeup.app.data.db.entity.EquipmentEntity
import com.lifeup.app.ui.components.*
import com.lifeup.app.ui.theme.PixelColors

@Composable
fun EquipmentScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val equipment by viewModel.equipment.collectAsState()
    val character by viewModel.character.collectAsState()
    val scrollState = rememberScrollState()
    var selectedEquipment by remember { mutableStateOf<EquipmentEntity?>(null) }

    val slots = listOf("habit" to "习惯", "tool" to "工具", "mindset" to "心态", "environment" to "环境")
    val slotColors = mapOf(
        "habit" to PixelColors.SlotHabit,
        "tool" to PixelColors.SlotTool,
        "mindset" to PixelColors.SlotMindset,
        "environment" to PixelColors.SlotEnvironment
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelColors.Background)
            .verticalScroll(scrollState)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PixelColors.GradientHero)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = PixelColors.TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🛡️ 装备管理",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PixelColors.AccentBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Equipped Section
        GlassCard(glowColor = PixelColors.AccentBlue.copy(alpha = 0.1f)) {
            Column {
                Text(
                    text = "已装备",
                    style = MaterialTheme.typography.titleMedium,
                    color = PixelColors.AccentBlue,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                slots.forEach { (slot, slotName) ->
                    val equipped = equipment.find { it.slot == slot && it.active }
                    val slotColor = slotColors[slot] ?: PixelColors.TextMuted

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(
                                if (equipped != null) PixelColors.SurfaceElevated.copy(alpha = 0.4f)
                                else PixelColors.Surface.copy(alpha = 0.2f),
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                if (equipped != null) slotColor.copy(alpha = 0.3f)
                                else PixelColors.Border,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Slot indicator
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    slotColor.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    slotColor.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = equipped?.icon ?: "+",
                                fontSize = if (equipped != null) 22.sp else 18.sp,
                                color = if (equipped == null) PixelColors.TextMuted else Color.Unspecified
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = slotName,
                                style = MaterialTheme.typography.labelSmall,
                                color = slotColor
                            )
                            if (equipped != null) {
                                Text(
                                    text = equipped.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PixelColors.TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = equipped.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PixelColors.TextMuted
                                )
                            } else {
                                Text(
                                    text = "未装备",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PixelColors.TextMuted
                                )
                            }
                        }

                        if (equipped != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                GlowProgressBar(
                                    progress = equipped.currentDurability.toFloat() / equipped.maxDurability.toFloat(),
                                    progressBrush = Brush.horizontalGradient(
                                        listOf(
                                            if (equipped.currentDurability > 10) PixelColors.AccentGreen else PixelColors.AccentRed,
                                            if (equipped.currentDurability > 10) PixelColors.TertiaryVariant else PixelColors.Primary
                                        )
                                    ),
                                    glowColor = if (equipped.currentDurability > 10) PixelColors.InvestmentGlow
                                    else Color(0x40FF4757),
                                    height = 6.dp,
                                    label = null,
                                    modifier = Modifier.width(70.dp),
                                    animate = false
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${equipped.currentDurability}/${equipped.maxDurability}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (equipped.currentDurability > 10) PixelColors.TextMuted else PixelColors.AccentRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Owned Equipment
        val ownedEquipment = equipment.filter { it.owned && !it.active }
        if (ownedEquipment.isNotEmpty()) {
            SectionHeader(title = "已拥有", accentColor = PixelColors.AccentGreen)
            ownedEquipment.forEach { equip ->
                EquipmentListItem(equipment = equip, onClick = { selectedEquipment = equip })
            }
        }

        // Shop
        val shopEquipment = equipment.filter { !it.owned && it.price > 0 }
        if (shopEquipment.isNotEmpty()) {
            SectionHeader(title = "🛒 商店", accentColor = PixelColors.AccentGold)
            shopEquipment.forEach { equip ->
                EquipmentShopItem(
                    equipment = equip,
                    gold = character?.gold ?: 0,
                    onPurchase = { viewModel.purchaseEquipment(equip.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    selectedEquipment?.let { equip ->
        EquipmentDetailDialog(
            equipment = equip,
            onEquip = {
                viewModel.equip(equip.id)
                selectedEquipment = null
            },
            onUnequip = {
                viewModel.unequip(equip.id)
                selectedEquipment = null
            },
            onDismiss = { selectedEquipment = null }
        )
    }
}

@Composable
private fun EquipmentListItem(
    equipment: EquipmentEntity,
    onClick: () -> Unit
) {
    val slotColor = when (equipment.slot) {
        "habit" -> PixelColors.SlotHabit
        "tool" -> PixelColors.SlotTool
        "mindset" -> PixelColors.SlotMindset
        "environment" -> PixelColors.SlotEnvironment
        else -> PixelColors.TextMuted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(PixelColors.SurfaceElevated.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .border(1.dp, PixelColors.Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(slotColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(1.dp, slotColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = equipment.icon, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = equipment.name,
                style = MaterialTheme.typography.bodyMedium,
                color = PixelColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = equipment.description,
                style = MaterialTheme.typography.labelSmall,
                color = PixelColors.TextMuted
            )
        }
        Box(
            modifier = Modifier
                .background(slotColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .border(1.dp, slotColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = when (equipment.slot) {
                    "habit" -> "习惯"
                    "tool" -> "工具"
                    "mindset" -> "心态"
                    "environment" -> "环境"
                    else -> equipment.slot
                },
                style = MaterialTheme.typography.labelSmall,
                color = slotColor
            )
        }
    }
}

@Composable
private fun EquipmentShopItem(
    equipment: EquipmentEntity,
    gold: Long,
    onPurchase: () -> Unit
) {
    val canAfford = gold >= equipment.price
    val slotColor = when (equipment.slot) {
        "habit" -> PixelColors.SlotHabit
        "tool" -> PixelColors.SlotTool
        "mindset" -> PixelColors.SlotMindset
        "environment" -> PixelColors.SlotEnvironment
        else -> PixelColors.TextMuted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(PixelColors.SurfaceElevated.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .border(1.dp, PixelColors.Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(slotColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(1.dp, slotColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = equipment.icon, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = equipment.name,
                style = MaterialTheme.typography.bodyMedium,
                color = PixelColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = equipment.description,
                style = MaterialTheme.typography.labelSmall,
                color = PixelColors.TextMuted
            )
        }
        GlowButton(
            text = "💰 ${equipment.price}",
            onClick = onPurchase,
            brush = if (canAfford) Brush.horizontalGradient(
                listOf(PixelColors.AccentGold, PixelColors.AccentOrange)
            ) else Brush.horizontalGradient(
                listOf(PixelColors.SurfaceVariant, PixelColors.SurfaceVariant)
            ),
            glowColor = if (canAfford) PixelColors.AccentGoldGlow else Color.Transparent
        )
    }
}

@Composable
private fun EquipmentDetailDialog(
    equipment: EquipmentEntity,
    onEquip: () -> Unit,
    onUnequip: () -> Unit,
    onDismiss: () -> Unit
) {
    val slotColor = when (equipment.slot) {
        "habit" -> PixelColors.SlotHabit
        "tool" -> PixelColors.SlotTool
        "mindset" -> PixelColors.SlotMindset
        "environment" -> PixelColors.SlotEnvironment
        else -> PixelColors.TextMuted
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PixelColors.Surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(slotColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .border(1.dp, slotColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = equipment.icon, fontSize = 26.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = equipment.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = PixelColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = equipment.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = PixelColors.TextSecondary
                    )
                }
            }
        },
        text = {
            Column {
                DetailRow(label = "槽位", value = equipment.slot, color = slotColor)
                DetailRow(
                    label = "耐久",
                    value = "${equipment.currentDurability}/${equipment.maxDurability}",
                    color = if (equipment.currentDurability > 10) PixelColors.AccentGreen else PixelColors.AccentRed
                )
                DetailRow(
                    label = "维护",
                    value = SeedData.activityNames[equipment.maintenanceActivity] ?: equipment.maintenanceActivity,
                    color = PixelColors.TextSecondary
                )
                DetailRow(
                    label = "效果",
                    value = when (equipment.effectType) {
                        "exp_multiplier" -> "经验 +${(equipment.effectValue * 100).toInt()}%"
                        "gold_multiplier" -> "金币 +${(equipment.effectValue * 100).toInt()}%"
                        "first_daily_bonus" -> "首次 +${(equipment.effectValue * 100).toInt()}%"
                        "long_session_bonus" -> "长时段 +${(equipment.effectValue * 100).toInt()}%"
                        "streak_bonus" -> "连续加成"
                        "sp_recovery" -> "精力 +${equipment.effectValue.toInt()}"
                        else -> equipment.effectType
                    },
                    color = PixelColors.AccentGold
                )
            }
        },
        confirmButton = {
            if (equipment.active) {
                OutlinedButton(
                    onClick = onUnequip,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PixelColors.AccentRed),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(PixelColors.AccentRed.copy(alpha = 0.3f), PixelColors.AccentRed.copy(alpha = 0.1f)))
                    )
                ) {
                    Text("卸下")
                }
            } else {
                GlowButton(
                    text = "装备",
                    onClick = onEquip,
                    brush = Brush.horizontalGradient(listOf(slotColor, slotColor.copy(alpha = 0.7f)))
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = PixelColors.TextMuted) }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = PixelColors.TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
