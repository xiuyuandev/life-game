package com.lifeup.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeup.app.service.TimerManager
import com.lifeup.app.ui.theme.MonospaceFontFamily
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSheet(
    timerManager: TimerManager,
    onStop: (durationMinutes: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var elapsedSeconds by remember { mutableStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var skillName by remember { mutableStateOf("") }
    var recordTypeIndex by remember { mutableIntStateOf(0) } // 0 = 投资性, 1 = 消耗性

    // Collect timer state every second
    LaunchedEffect(Unit) {
        snapshotFlow { timerManager.elapsedSeconds.value }
            .distinctUntilChanged()
            .collect { elapsedSeconds = it }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { timerManager.isPaused.value }
            .distinctUntilChanged()
            .collect { isPaused = it }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { timerManager.isRunning.value }
            .distinctUntilChanged()
            .collect { isRunning = it }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { timerManager.currentSkillName.value }
            .distinctUntilChanged()
            .collect { skillName = it }
    }

    ModalBottomSheet(
        onDismissRequest = { /* Don't dismiss while running */ },
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Skill name
            Text(
                text = skillName.ifBlank { "技能练习" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Elapsed time in large monospace font
            Text(
                text = timerManager.formatElapsedTime(elapsedSeconds),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = MonospaceFontFamily,
                    fontSize = 56.sp,
                    lineHeight = 64.sp
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Record type toggle
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = recordTypeIndex == 0,
                    onClick = { recordTypeIndex = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("投资性")
                }
                SegmentedButton(
                    selected = recordTypeIndex == 1,
                    onClick = { recordTypeIndex = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("消耗性")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume button
                OutlinedButton(
                    onClick = {
                        if (isPaused) {
                            timerManager.resumeTimer()
                        } else {
                            timerManager.pauseTimer()
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "继续" else "暂停",
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Stop button
                Button(
                    onClick = {
                        val durationSeconds = timerManager.stopTimer()
                        val durationMinutes = timerManager.toDurationMinutes(durationSeconds)
                        onStop(durationMinutes)
                    },
                    modifier = Modifier.size(64.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "停止",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
