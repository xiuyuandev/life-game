package com.lifeup.app.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeup.app.data.SeedData
import com.lifeup.app.data.db.entity.TimeSessionEntity
import com.lifeup.app.ui.components.*
import com.lifeup.app.ui.theme.PixelColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionHistoryScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val sessions by viewModel.sessionHistory.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelColors.Background)
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
                    text = "📜 历史记录",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PixelColors.SecondaryVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (sessions.isEmpty()) {
            EmptyStateView(
                icon = "📜",
                title = "暂无记录",
                subtitle = "开始你的第一次计时吧"
            )
        } else {
            LazyColumn {
                items(sessions) { session ->
                    SessionHistoryItem(session = session)
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryItem(session: TimeSessionEntity) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val startDate = Date(session.startTime)
    val timeStr = dateFormat.format(startDate)
    val hours = session.durationMinutes / 60
    val mins = session.durationMinutes % 60
    val durationStr = if (hours > 0) "${hours}小时${mins}分钟" else "${mins}分钟"
    val isInvestment = session.isInvestment
    val accentColor = if (isInvestment) PixelColors.AccentGreen else PixelColors.AccentRed

    GlassCard(
        glowColor = accentColor.copy(alpha = 0.08f),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            accentColor.copy(alpha = 0.15f),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            accentColor.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isInvestment) "📈" else "📉",
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixelColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = PixelColors.TextMuted
                    )
                }
                StatusBadge(
                    text = durationStr,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SeedData.activityNames[session.category] ?: session.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
                Row {
                    if (session.totalExp > 0) {
                        Text(
                            text = "⭐ +${session.totalExp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelColors.ExpBar,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (session.goldEarned > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💰 +${session.goldEarned}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelColors.AccentGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
