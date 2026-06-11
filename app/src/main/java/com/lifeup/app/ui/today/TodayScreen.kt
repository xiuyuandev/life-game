package com.lifeup.app.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeup.app.ui.components.doneKeyboardActions
import com.lifeup.app.ui.components.doneKeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.lifeup.app.data.db.Priority
import com.lifeup.app.ui.components.DismissBackground
import com.lifeup.app.ui.components.EnergyBar
import com.lifeup.app.ui.components.ErrorState
import com.lifeup.app.ui.components.HapticFeedbackHelper
import com.lifeup.app.ui.components.ScrollToTopButton
import com.lifeup.app.ui.components.TodoItem
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onNavigateToTimer: (Long) -> Unit,
    onNavigateToCreateSkill: () -> Unit,
    onNavigateToRetroactive: () -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var addAsHabit by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    HapticFeedbackHelper.performLightClick(context)
                    addAsHabit = false
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    hoveredElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加待办",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                ErrorState(
                    message = uiState.error,
                    onRetry = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top bar: greeting + date + streak
                item {
                    TopBar(
                        todayDate = uiState.todayDate,
                        streakCount = uiState.streakCount
                    )
                }

                // Tip card
                AnimatedVisibility(
                    visible = uiState.tip != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    if (uiState.tip != null) {
                        TipCard(
                            tip = uiState.tip,
                            onDismiss = { viewModel.dismissTip() }
                        )
                    }
                }

                // Energy bar card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                            MaterialTheme.colorScheme.surface
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            EnergyBar(
                                current = uiState.energy,
                                cap = uiState.energyCap
                            )
                        }
                    }
                }

                // Habit section
                item {
                    SectionHeader(
                        title = "习惯打卡",
                        icon = "🔄",
                        onAddClick = {
                            addAsHabit = true
                            showAddDialog = true
                        }
                    )
                }

                if (uiState.habits.isEmpty()) {
                    item {
                        EmptyStateMessage(text = "还没有习惯，添加一个吧", icon = "🌱")
                    }
                } else {
                    items(
                        items = uiState.habits,
                        key = { "habit-${it.id}" }
                    ) { habit ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteTodo(habit.id)
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = { DismissBackground(dismissState) },
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            TodoItem(
                                todo = habit,
                                onToggle = {
                                    HapticFeedbackHelper.performTick(context)
                                    viewModel.toggleTodo(habit.id)
                                },
                                onDelete = { viewModel.deleteTodo(habit.id) }
                            )
                        }
                    }
                }

                // Spacer between sections
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Todo section
                item {
                    SectionHeader(
                        title = "今日待办",
                        icon = "📋",
                        onAddClick = {
                            addAsHabit = false
                            showAddDialog = true
                        }
                    )
                }

                if (uiState.todos.isEmpty()) {
                    item {
                        EmptyStateMessage(text = "今天没有待办", icon = "✨")
                    }
                } else {
                    items(
                        items = uiState.todos,
                        key = { "todo-${it.id}" }
                    ) { todo ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteTodo(todo.id)
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = { DismissBackground(dismissState) },
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            TodoItem(
                                todo = todo,
                                onToggle = {
                                    HapticFeedbackHelper.performTick(context)
                                    viewModel.toggleTodo(todo.id)
                                },
                                onDelete = { viewModel.deleteTodo(todo.id) }
                            )
                        }
                    }
                }

                // Quick actions row
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    QuickActionsRow(
                        onRetroactiveClick = onNavigateToRetroactive,
                        onStartTimerClick = { onNavigateToTimer(0L) }
                    )
                }

                // Bottom spacing for FAB
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
            }

            ScrollToTopButton(
                listState = listState,
                onClick = {
                    viewModel.viewModelScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            )
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
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 6 -> "夜深了"
        hour < 9 -> "早安"
        hour < 12 -> "上午好"
        hour < 14 -> "中午好"
        hour < 18 -> "下午好"
        hour < 22 -> "晚上好"
        else -> "夜深了"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
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
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(1.dp, Color(0xFFFF6D00).copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF6D00).copy(alpha = 0.08f),
                                    Color(0xFFFF8A50).copy(alpha = 0.12f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF6D00),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$streakCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6D00)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "天连续",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF6D00).copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: String = "",
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon.isNotEmpty()) {
                Text(text = icon, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        TextButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text("添加", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun EmptyStateMessage(text: String, icon: String = "") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon.isNotEmpty()) {
                Text(text = icon, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
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
            shape = RoundedCornerShape(12.dp),
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
            shape = RoundedCornerShape(12.dp),
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

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .imePadding()
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
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = doneKeyboardOptions(),
                keyboardActions = doneKeyboardActions(focusManager) {
                    if (title.isNotBlank()) {
                        onConfirm(title, selectedPriority, null)
                    }
                }
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
                        Priority.MEDIUM -> Color(0xFFFFB300)
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
                        color = color
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
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
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
    val containerColor = if (selected) color.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (selected) color else MaterialTheme.colorScheme.outlineVariant
    val contentColor = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant

    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        label()
    }
}
