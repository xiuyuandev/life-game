package com.lifeup.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeup.app.data.backup.DataBackupManager
import com.lifeup.app.data.db.entity.EquipmentEntity
import com.lifeup.app.ui.components.*
import com.lifeup.app.ui.theme.PixelColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToEquipment: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onResetCharacter: () -> Unit
) {
    val context = LocalContext.current
    val character by viewModel.character.collectAsState()
    val equipment by viewModel.equipment.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val unlockedCount by viewModel.unlockedCount.collectAsState()

    // Preferences
    val soundEnabled by viewModel.soundEnabled.collectAsState(initial = true)
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState(initial = true)
    val dailyReminderEnabled by viewModel.dailyReminderEnabled.collectAsState(initial = false)

    // Backup result snackbar
    val backupResult by viewModel.backupResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showEditName by remember { mutableStateOf(false) }

    val activeEquipment = equipment.filter { it.active }
    val totalAchievements = achievements.size

    // Backup file pickers
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

    // Show snackbar for backup result
    LaunchedEffect(backupResult) {
        backupResult?.let { result ->
            val message = if (result.success) {
                "${result.message} (${result.recordCount} 条记录)"
            } else {
                result.message
            }
            snackbarHostState.showSnackbar(message)
            viewModel.clearBackupResult()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = PixelColors.SurfaceElevated,
                    contentColor = PixelColors.TextPrimary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PixelColors.Background)
                .verticalScroll(scrollState)
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PixelColors.GradientHero)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "⚙️ 设置",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PixelColors.AccentGold,
                    fontWeight = FontWeight.Bold
                )
            }

            // Character Section
            GlassCard(glowColor = PixelColors.PrimaryGlow) {
                Column {
                    Text(
                        text = "👤 角色",
                        style = MaterialTheme.typography.titleMedium,
                        color = PixelColors.PrimaryVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlowAvatar(
                            emoji = "🧙",
                            size = 56.dp,
                            glowColor = PixelColors.AccentGold
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = character?.name ?: "冒险者",
                                style = MaterialTheme.typography.titleLarge,
                                color = PixelColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(
                                    text = "Lv.${character?.level ?: 1}",
                                    color = PixelColors.AccentGold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "冒险者",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PixelColors.TextMuted
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditName = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PixelColors.TextSecondary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(
                                    listOf(PixelColors.Border, PixelColors.BorderStrong)
                                )
                            )
                        ) {
                            Text("编辑角色", fontWeight = FontWeight.Medium)
                        }
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PixelColors.AccentRed
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(
                                    listOf(PixelColors.AccentRed.copy(alpha = 0.3f), PixelColors.AccentRed.copy(alpha = 0.1f))
                                )
                            )
                        ) {
                            Text("重新开始", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Equipment Section
            GlassCard(
                modifier = Modifier.clickable { onNavigateToEquipment() },
                glowColor = PixelColors.AccentBlue.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🛡️ 装备",
                            style = MaterialTheme.typography.titleMedium,
                            color = PixelColors.AccentBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        EquipmentSlotsPreview(activeEquipment)
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "进入",
                        tint = PixelColors.TextMuted
                    )
                }
            }

            // Achievements Section
            GlassCard(
                modifier = Modifier.clickable { onNavigateToAchievements() },
                glowColor = PixelColors.AccentGold.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🏆 成就",
                            style = MaterialTheme.typography.titleMedium,
                            color = PixelColors.AccentGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$unlockedCount",
                                style = MaterialTheme.typography.headlineSmall,
                                color = PixelColors.AccentGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " / $totalAchievements",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PixelColors.TextMuted
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            GlowProgressBar(
                                progress = if (totalAchievements > 0) unlockedCount.toFloat() / totalAchievements else 0f,
                                progressBrush = PixelColors.GradientExp,
                                glowColor = PixelColors.AccentGoldGlow,
                                height = 6.dp,
                                label = null,
                                modifier = Modifier.width(100.dp)
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "进入",
                        tint = PixelColors.TextMuted
                    )
                }
            }

            // Data Section
            GlassCard(glowColor = PixelColors.SecondaryGlow) {
                Column {
                    Text(
                        text = "💾 数据",
                        style = MaterialTheme.typography.titleMedium,
                        color = PixelColors.SecondaryVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsButton(text = "📜 历史记录", onClick = onNavigateToHistory)
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsButton(
                        text = "💾 导出备份",
                        onClick = {
                            val fileName = DataBackupManager.generateBackupFileName()
                            exportLauncher.launch(fileName)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsButton(
                        text = "📥 导入备份",
                        onClick = {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    )
                }
            }

            // Preferences Section
            GlassCard {
                Column {
                    Text(
                        text = "🔧 偏好",
                        style = MaterialTheme.typography.titleMedium,
                        color = PixelColors.TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SwitchPreferenceItem(
                        label = "音效",
                        icon = "🔊",
                        checked = soundEnabled,
                        onCheckedChange = { viewModel.setSoundEnabled(it) }
                    )
                    GradientDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SwitchPreferenceItem(
                        label = "震动",
                        icon = "📳",
                        checked = vibrationEnabled,
                        onCheckedChange = { viewModel.setVibrationEnabled(it) }
                    )
                    GradientDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SwitchPreferenceItem(
                        label = "每日提醒",
                        icon = "🔔",
                        checked = dailyReminderEnabled,
                        onCheckedChange = { viewModel.setDailyReminderEnabled(it) }
                    )
                }
            }

            // Version
            Text(
                text = "LifeUp v2.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = PixelColors.TextMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    // Dialogs
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = PixelColors.Surface,
            shape = RoundedCornerShape(24.dp),
            title = { Text("确认重置？", color = PixelColors.AccentRed, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "这将删除所有数据，包括角色、技能、装备和成就记录。此操作不可撤销！",
                    color = PixelColors.TextSecondary
                )
            },
            confirmButton = {
                GlowButton(
                    text = "确认重置",
                    onClick = {
                        viewModel.resetAllData()
                        showResetDialog = false
                        onResetCharacter()
                    },
                    brush = Brush.horizontalGradient(
                        listOf(PixelColors.AccentRed, PixelColors.Primary)
                    )
                )
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("取消", color = PixelColors.TextMuted)
                }
            }
        )
    }

    if (showEditName) {
        var newName by remember { mutableStateOf(character?.name ?: "") }
        AlertDialog(
            onDismissRequest = { showEditName = false },
            containerColor = PixelColors.Surface,
            shape = RoundedCornerShape(24.dp),
            title = { Text("编辑角色名称", color = PixelColors.AccentGold, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("名称", color = PixelColors.TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PixelColors.TextPrimary,
                        unfocusedTextColor = PixelColors.TextSecondary,
                        focusedBorderColor = PixelColors.Primary,
                        unfocusedBorderColor = PixelColors.Border
                    )
                )
            },
            confirmButton = {
                GlowButton(
                    text = "保存",
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.createCharacter(newName)
                            showEditName = false
                        }
                    },
                    brush = PixelColors.GradientPrimary
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditName = false }) {
                    Text("取消", color = PixelColors.TextMuted)
                }
            }
        )
    }
}

@Composable
private fun SwitchPreferenceItem(
    label: String,
    icon: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = PixelColors.TextPrimary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PixelColors.AccentGreen,
                checkedTrackColor = PixelColors.AccentGreen.copy(alpha = 0.4f),
                uncheckedThumbColor = PixelColors.TextMuted,
                uncheckedTrackColor = PixelColors.SurfaceVariant
            )
        )
    }
}

@Composable
private fun EquipmentSlotsPreview(activeEquipment: List<EquipmentEntity>) {
    val slots = listOf(
        "habit" to "习惯" to PixelColors.SlotHabit,
        "tool" to "工具" to PixelColors.SlotTool,
        "mindset" to "心态" to PixelColors.SlotMindset,
        "environment" to "环境" to PixelColors.SlotEnvironment
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        slots.forEach { (slotData, color) ->
            val (slot, slotName) = slotData
            val equip = activeEquipment.find { it.slot == slot }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (equip != null) color.copy(alpha = 0.15f)
                            else PixelColors.SurfaceVariant,
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            1.5.dp,
                            if (equip != null) color.copy(alpha = 0.4f)
                            else PixelColors.Border,
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = equip?.icon ?: "+",
                        fontSize = if (equip != null) 22.sp else 18.sp,
                        color = if (equip == null) PixelColors.TextMuted else Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = slotName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (equip != null) color else PixelColors.TextMuted
                )
                if (equip != null) {
                    Text(
                        text = "${equip.currentDurability}/${equip.maxDurability}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (equip.currentDurability > 10) PixelColors.TextMuted else PixelColors.AccentRed
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PixelColors.SurfaceElevated, RoundedCornerShape(12.dp))
            .border(1.dp, PixelColors.Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = PixelColors.TextSecondary
        )
    }
}
