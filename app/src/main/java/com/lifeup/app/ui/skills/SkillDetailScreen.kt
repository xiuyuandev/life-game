package com.lifeup.app.ui.skills

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.BoundAttribute
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.data.db.SkillStatus
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.model.TimeRecord
import com.lifeup.app.ui.theme.CategoryArt
import com.lifeup.app.ui.theme.CategoryLanguage
import com.lifeup.app.ui.theme.CategoryLife
import com.lifeup.app.ui.theme.CategoryLivelihood
import com.lifeup.app.ui.theme.CategoryMental
import com.lifeup.app.ui.theme.CategoryPhysical
import com.lifeup.app.ui.theme.CategorySocial
import java.text.SimpleDateFormat
import java.util.Locale

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

private val levelBorderColor: (Int) -> Color = { level ->
    when (level) {
        1 -> Color(0xFF9E9E9E)
        2 -> Color(0xFFCD7F32)
        3 -> Color(0xFFC0C0C0)
        4 -> Color(0xFFFFD700)
        else -> Color(0xFF9C27B0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillDetailScreen(
    skillId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToTimer: (Long) -> Unit,
    viewModel: SkillDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("技能详情") },
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val skill = uiState.skill
            if (skill == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "技能不存在",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SkillHeader(skill = skill)
                    SkillProgressSection(skill = skill)
                    SkillActionButtons(
                        skill = skill,
                        onStartTimer = { onNavigateToTimer(skill.id) },
                        onPause = { viewModel.pauseSkill() },
                        onArchive = { viewModel.archiveSkill() },
                        onResume = { viewModel.resumeSkill() }
                    )
                    StatsSection(
                        weeklyFrequency = uiState.weeklyFrequency,
                        growthCurve = uiState.growthCurve,
                        categoryColor = skill.category.color
                    )
                    TimeRecordsSection(timeRecords = uiState.timeRecords)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SkillHeader(skill: Skill) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = levelBorderColor(skill.level),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .background(
                            color = levelBorderColor(skill.level).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LV${skill.level}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = levelBorderColor(skill.level)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(skill.category.color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = skill.category.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "绑定属性: ${skill.boundAttribute.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (skill.masteryStars > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "精通 " + "★".repeat(skill.masteryStars),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SkillProgressSection(skill: Skill) {
    val totalHours = skill.totalMinutes / 60
    val totalMinutesRemainder = skill.totalMinutes % 60
    val progress = skill.getProgressToNextLevel()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "进度",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = skill.category.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "距下一等级 ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "总时长: ${totalHours}h ${totalMinutesRemainder}m",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SkillActionButtons(
    skill: Skill,
    onStartTimer: () -> Unit,
    onPause: () -> Unit,
    onArchive: () -> Unit,
    onResume: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onStartTimer,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("开始计时")
        }

        if (skill.status == SkillStatus.PAUSED) {
            OutlinedButton(
                onClick = onResume,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("恢复")
            }
        } else {
            OutlinedButton(
                onClick = onPause,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("暂停技能")
            }
        }

        OutlinedButton(
            onClick = onArchive,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("归档")
        }
    }
}

// ─── Stats Section with Canvas Charts ───────────────────────────────────────

@Composable
private fun StatsSection(
    weeklyFrequency: List<DailyFrequency>,
    growthCurve: List<GrowthPoint>,
    categoryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "统计",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "7日频率",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            WeeklyFrequencyChart(
                data = weeklyFrequency,
                barColor = categoryColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "成长曲线",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            GrowthCurveChart(
                data = growthCurve,
                lineColor = categoryColor
            )
        }
    }
}

@Composable
private fun WeeklyFrequencyChart(
    data: List<DailyFrequency>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val maxMinutes = data.maxOfOrNull { it.totalMinutes }?.coerceAtLeast(1) ?: 1

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barCount = data.size.coerceAtLeast(1)
        val labelHeight = 28f
        val chartHeight = canvasHeight - labelHeight
        val barSpacing = 8f
        val totalSpacing = barSpacing * (barCount + 1)
        val barWidth = (canvasWidth - totalSpacing) / barCount

        // Draw bars
        data.forEachIndexed { index, item ->
            val x = barSpacing + index * (barWidth + barSpacing)
            val barHeight = if (maxMinutes > 0) {
                (item.totalMinutes.toFloat() / maxMinutes) * (chartHeight - 20f)
            } else {
                0f
            }
            val y = chartHeight - barHeight

            // Bar
            drawRoundRect(
                color = barColor.copy(alpha = 0.85f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Value label on top
            if (item.totalMinutes > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${item.totalMinutes}m",
                    x + barWidth / 2f,
                    y - 4f,
                    android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 20f
                        color = barColor.toArgb()
                        isAntiAlias = true
                    }
                )
            }

            // Date label at bottom
            drawContext.canvas.nativeCanvas.drawText(
                item.date,
                x + barWidth / 2f,
                canvasHeight,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 20f
                    color = android.graphics.Color.GRAY
                    isAntiAlias = true
                }
            )
        }
    }
}

@Composable
private fun GrowthCurveChart(
    data: List<GrowthPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无成长数据",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        return
    }

    val maxCumulative = data.maxOfOrNull { it.cumulativeMinutes }?.coerceAtLeast(1) ?: 1

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val labelHeight = 28f
        val chartHeight = canvasHeight - labelHeight
        val paddingLeft = 4f
        val paddingRight = 4f
        val chartWidth = canvasWidth - paddingLeft - paddingRight

        // Calculate points
        val points = data.mapIndexed { index, point ->
            val x = if (data.size == 1) {
                paddingLeft + chartWidth / 2f
            } else {
                paddingLeft + (index.toFloat() / (data.size - 1)) * chartWidth
            }
            val y = chartHeight - (point.cumulativeMinutes.toFloat() / maxCumulative) * (chartHeight - 20f)
            Offset(x, y)
        }

        // Draw filled area
        if (points.size >= 2) {
            val areaPath = Path().apply {
                moveTo(points.first().x, chartHeight)
                for (point in points) {
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, chartHeight)
                close()
            }

            clipPath(areaPath) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.3f),
                            lineColor.copy(alpha = 0.05f)
                        ),
                        startY = 0f,
                        endY = chartHeight
                    )
                )
            }

            // Draw line
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }

        // Draw dots
        for (point in points) {
            drawCircle(
                color = lineColor,
                radius = 4f,
                center = point
            )
        }

        // Current total label
        val lastPoint = points.last()
        val totalHours = data.last().cumulativeMinutes / 60.0
        drawContext.canvas.nativeCanvas.drawText(
            String.format("%.1fh", totalHours),
            lastPoint.x,
            lastPoint.y - 10f,
            android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 22f
                color = lineColor.toArgb()
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
        )

        // Month labels at bottom
        val labelCount = data.size.coerceAtMost(6)
        val step = if (data.size <= labelCount) 1 else (data.size - 1) / (labelCount - 1)
        for (i in data.indices) {
            if (data.size <= labelCount || i % step == 0 || i == data.size - 1) {
                val x = if (data.size == 1) {
                    paddingLeft + chartWidth / 2f
                } else {
                    paddingLeft + (i.toFloat() / (data.size - 1)) * chartWidth
                }
                drawContext.canvas.nativeCanvas.drawText(
                    data[i].month,
                    x,
                    canvasHeight,
                    android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 18f
                        color = android.graphics.Color.GRAY
                        isAntiAlias = true
                    }
                )
            }
        }
    }
}

// ─── Time Records Section ────────────────────────────────────────────────────

@Composable
private fun TimeRecordsSection(timeRecords: List<TimeRecord>) {
    var expanded by remember { mutableStateOf(true) }
    var showAll by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "时间记录",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.animateContentSize()
                ) {
                    if (timeRecords.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无时间记录",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        val displayedRecords = if (showAll) timeRecords else timeRecords.take(20)
                        val grouped = groupRecordsByDate(displayedRecords)

                        grouped.forEach { (date, records) ->
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )

                            records.forEach { record ->
                                TimeRecordItem(record = record)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        if (timeRecords.size > 20 && !showAll) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "查看更多 (${timeRecords.size - 20}条)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { showAll = true }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeRecordItem(record: TimeRecord) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val startTime = timeFormat.format(record.startTime)
    val endTime = timeFormat.format(record.endTime)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$startTime - $endTime",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "${record.durationMinutes}分钟",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.weight(1f))

        // Record type badge
        val badgeColor = when (record.recordType) {
            RecordType.INVESTMENT -> Color(0xFF4CAF50)
            RecordType.CONSUMPTION -> Color(0xFFFF9800)
        }
        val badgeText = when (record.recordType) {
            RecordType.INVESTMENT -> "投资"
            RecordType.CONSUMPTION -> "消耗"
        }
        Box(
            modifier = Modifier
                .background(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        }
    }
}

private fun groupRecordsByDate(records: List<TimeRecord>): Map<String, List<TimeRecord>> {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    return records
        .sortedByDescending { it.startTime }
        .groupBy { dateFormat.format(it.startTime) }
}
