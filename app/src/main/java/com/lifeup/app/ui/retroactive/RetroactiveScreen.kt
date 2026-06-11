package com.lifeup.app.ui.retroactive

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.domain.model.Skill
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetroactiveScreen(
    onNavigateBack: () -> Unit,
    viewModel: RetroactiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Navigate back on save success
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("补录时间") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Skill selector
                SkillSelector(
                    skills = uiState.skills,
                    selectedSkillId = uiState.selectedSkillId,
                    onSkillSelected = { viewModel.selectSkill(it) }
                )

                // Date picker
                DatePickerField(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { viewModel.selectDate(it) }
                )

                // Start time picker
                TimePickerField(
                    hour = uiState.startHour,
                    minute = uiState.startMinute,
                    onTimeSelected = { h, m -> viewModel.setStartTime(h, m) }
                )

                // Duration slider
                DurationSlider(
                    durationMinutes = uiState.durationMinutes,
                    onDurationChanged = { viewModel.setDuration(it) }
                )

                // Record type toggle
                RecordTypeToggle(
                    recordType = uiState.recordType,
                    onToggle = { viewModel.toggleRecordType() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save button
                Button(
                    onClick = { viewModel.saveRetroactive() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isSaving && uiState.selectedSkillId != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(20.dp)
                                .width(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("保存")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkillSelector(
    skills: List<Skill>,
    selectedSkillId: Long?,
    onSkillSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedSkill = skills.find { it.id == selectedSkillId }

    Column {
        Text(
            text = "选择技能",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedSkill?.name ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                placeholder = { Text("请选择技能") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                skills.forEach { skill ->
                    DropdownMenuItem(
                        text = { Text(skill.name) },
                        onClick = {
                            onSkillSelected(skill.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DatePickerField(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val displayFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    val storageFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Column {
        Text(
            text = "选择日期",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = {
                val parsedDate = try {
                    storageFormat.parse(selectedDate)
                } catch (e: Exception) {
                    null
                }
                val cal = Calendar.getInstance().apply {
                    if (parsedDate != null) time = parsedDate
                }
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val newCal = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth)
                        }
                        onDateSelected(storageFormat.format(newCal.time))
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            val displayText = try {
                val date = storageFormat.parse(selectedDate)
                if (date != null) displayFormat.format(date) else selectedDate
            } catch (e: Exception) {
                selectedDate
            }
            Text(displayText)
        }
    }
}

@Composable
private fun TimePickerField(
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    val context = LocalContext.current

    Column {
        Text(
            text = "开始时间",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, h, m -> onTimeSelected(h, m) },
                    hour,
                    minute,
                    true
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(String.format("%02d:%02d", hour, minute))
        }
    }
}

@Composable
private fun DurationSlider(
    durationMinutes: Int,
    onDurationChanged: (Int) -> Unit
) {
    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60
    val displayText = buildString {
        if (hours > 0) append("${hours}h")
        if (minutes > 0) append("${minutes}m")
        if (isEmpty()) append("0m")
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "时长",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = durationMinutes.toFloat(),
            onValueChange = { onDurationChanged(it.toInt()) },
            valueRange = 5f..480f,
            steps = (480 - 5) / 5 - 1,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "5分钟",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "8小时",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecordTypeToggle(
    recordType: RecordType,
    onToggle: () -> Unit
) {
    Column {
        Text(
            text = "记录类型",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = recordType == RecordType.INVESTMENT,
                onClick = {
                    if (recordType != RecordType.INVESTMENT) onToggle()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("投资性")
            }
            SegmentedButton(
                selected = recordType == RecordType.CONSUMPTION,
                onClick = {
                    if (recordType != RecordType.CONSUMPTION) onToggle()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("消耗性")
            }
        }
    }
}
