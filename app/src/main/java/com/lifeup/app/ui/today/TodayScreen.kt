package com.lifeup.app.ui.today

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.Priority
import com.lifeup.app.ui.components.EnergyBar
import com.lifeup.app.ui.components.TodoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onNavigateToTimer: (Long) -> Unit,
    onNavigateToCreateSkill: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var addAsHabit by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    addAsHabit = false
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加待办"
                )
            }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top bar: title + date + streak
                item {
                    TopBar(
                        todayDate = uiState.todayDate,
                        streakCount = uiState.streakCount
                    )
                }

                // Energy bar
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        EnergyBar(
                            current = uiState.energy,
                            cap = uiState.energyCap,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Habit section
                item {
                    SectionHeader(
                        title = "习惯打卡",
                        onAddClick = {
                            addAsHabit = true
                            showAddDialog = true
                        }
                    )
                }

                if (uiState.habits.isEmpty()) {
                    item {
                        EmptyStateMessage(text = "还没有习惯，添加一个吧")
                    }
                } else {
                    items(
                        items = uiState.habits,
                        key = { "habit-${it.id}" }
                    ) { habit ->
                        TodoItem(
                            todo = habit,
                            onToggle = { viewModel.toggleTodo(habit.id) },
                            onDelete = { viewModel.deleteTodo(habit.id) }
                        )
                    }
                }

                // Spacer between sections
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Todo section
                item {
                    SectionHeader(
                        title = "今日待办",
                        onAddClick = {
                            addAsHabit = false
                            showAddDialog = true
                        }
                    )
                }

                if (uiState.todos.isEmpty()) {
                    item {
                        EmptyStateMessage(text = "今天没有待办")
                    }
                } else {
                    items(
                        items = uiState.todos,
                        key = { "todo-${it.id}" }
                    ) { todo ->
                        TodoItem(
                            todo = todo,
                            onToggle = { viewModel.toggleTodo(todo.id) },
                            onDelete = { viewModel.deleteTodo(todo.id) }
                        )
                    }
                }

                // Quick actions row
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    QuickActionsRow(
                        onRetroactiveClick = { /* TODO: Navigate to retroactive entry */ },
                        onStartTimerClick = { onNavigateToTimer(0L) }
                    )
                }

                // Bottom spacing for FAB
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    // Add todo/habit dialog
    if (showAddDialog) {
        AddTodoSheet(
            isHabit = addAsHabit,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, priority, linkedSkillId ->
                viewModel.addTodo(
                    title = title,
                    isHabit = addAsHabit,
                    priority = priority,
                    linkedSkillId = linkedSkillId
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TopBar(
    todayDate: String,
    streakCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "今日",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = todayDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (streakCount > 0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF6D00).copy(alpha = 0.12f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF6D00),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streakCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6D00)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text("添加")
        }
    }
}

@Composable
private fun EmptyStateMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun QuickActionsRow(
    onRetroactiveClick: () -> Unit,
    onStartTimerClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onRetroactiveClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("补录时间")
        }

        Button(
            onClick = onStartTimerClick,
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTodoSheet(
    isHabit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, priority: Priority, linkedSkillId: Long?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var title by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.NONE) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isHabit) "添加习惯" else "添加待办",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(if (isHabit) "习惯名称" else "待办名称")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Priority selector
            Text(
                text = "优先级",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Priority.entries.forEach { priority ->
                    val isSelected = selectedPriority == priority
                    val color = when (priority) {
                        Priority.HIGH -> Color(0xFFFF5252)
                        Priority.MEDIUM -> Color(0xFFFFD740)
                        Priority.LOW -> Color(0xFF66BB6A)
                        Priority.NONE -> Color(0xFFBDBDBD)
                    }
                    val label = when (priority) {
                        Priority.HIGH -> "高"
                        Priority.MEDIUM -> "中"
                        Priority.LOW -> "低"
                        Priority.NONE -> "无"
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPriority = priority },
                        label = { Text(label) },
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onConfirm(title, selectedPriority, null)
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("确定")
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    color: Color
) {
    val containerColor = if (selected) color.copy(alpha = 0.2f) else Color.Transparent
    val borderColor = if (selected) color else MaterialTheme.colorScheme.outlineVariant
    val contentColor = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant

    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
    ) {
        label()
    }
}
