package com.lifeup.app.ui.demon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lifeup.app.domain.model.DemonTemplate
import com.lifeup.app.domain.model.DemonId
import com.lifeup.app.domain.repository.DemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class DemonEntryUiState(
    val defeated: Int = 0,
    val total: Int = 12,
    val totalHpSum: Int = 0,
    val currentHpSum: Int = 0
)

@HiltViewModel
class DemonEntryViewModel @Inject constructor(
    private val demonRepository: DemonRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DemonEntryUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val all = demonRepository.observeAllProgress().first()
                val defeated = all.count { it.isDefeated }
                val totalHp = DemonTemplate.STANDARD.sumOf { it.totalHp }
                val currentHp = all.filter { it.demonId != DemonId.MIRROR_OF_SELF.key }.sumOf { it.currentHp }
                _state.value = DemonEntryUiState(defeated, total = 12, totalHpSum = totalHp, currentHpSum = currentHp)
            } catch (_: Exception) {}
        }
    }
}

/**
 * "心魔试炼"快捷入口卡片，用于放在今日 / 角色馆页面。
 */
@Composable
fun DemonEntryCard(
    onClick: () -> Unit,
    viewModel: DemonEntryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fraction = if (state.totalHpSum <= 0) 0f else 1f - state.currentHpSum.toFloat() / state.totalHpSum
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = Color(0xFF455A64).copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF263238),
                            Color(0xFF37474F)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFF8A65), Color(0xFF263238))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "心魔试炼",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "已伏诛 ${state.defeated} / ${state.total}  ·  总进度 ${(fraction * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "进入",
                    tint = Color.White
                )
            }
        }
    }
}
