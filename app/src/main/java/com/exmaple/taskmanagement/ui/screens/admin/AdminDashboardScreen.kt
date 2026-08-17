package com.exmaple.taskmanagement.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exmaple.taskmanagement.model.Task
import com.exmaple.taskmanagement.ui.components.ShimmerTaskCard
import com.exmaple.taskmanagement.ui.components.UserAvatar
import com.exmaple.taskmanagement.ui.theme.StatusCompletedBg
import com.exmaple.taskmanagement.ui.theme.StatusCompletedText
import com.exmaple.taskmanagement.ui.theme.StatusInProgressBg
import com.exmaple.taskmanagement.ui.theme.StatusInProgressText
import com.exmaple.taskmanagement.ui.theme.StatusOverdueBg
import com.exmaple.taskmanagement.ui.theme.StatusOverdueText
import com.exmaple.taskmanagement.ui.theme.StatusPendingBg
import com.exmaple.taskmanagement.ui.theme.StatusPendingText
import com.exmaple.taskmanagement.viewmodel.AuthViewModel
import com.exmaple.taskmanagement.viewmodel.TaskViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    taskViewModel: TaskViewModel,
    authViewModel: AuthViewModel,
    onCreateTaskClick: () -> Unit,
    onViewAllTasksClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onViewMyTasksClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onTaskClick: (String) -> Unit = {}
) {
    val allTasks by taskViewModel.allTasks.collectAsStateWithLifecycle()
    val employees by taskViewModel.employees.collectAsStateWithLifecycle()
    val userProfile by authViewModel.currentUserProfile.collectAsStateWithLifecycle()

    val currentAdminId = authViewModel.getCurrentUserId() ?: ""
    val notificationsFlow = remember(currentAdminId) { taskViewModel.getMyNotifications(currentAdminId) }
    val notifications by notificationsFlow.collectAsStateWithLifecycle(emptyList())
    val unreadCount = notifications.count { !it.isRead }

    LaunchedEffect(currentAdminId) {
        if (currentAdminId.isNotBlank()) {
            taskViewModel.loadAdminTasks(currentAdminId)
        }
    }

    val currentTimeMillis = System.currentTimeMillis()
    val totalEmployees = employees.size
    val activeTasks = allTasks.count { it.status == "pending" || it.status == "in_progress" }
    val pendingTasks = allTasks.count { it.status == "pending" }
    val overdueTasks = allTasks.count { task ->
        val deadlineTime = try { task.deadline?.toDate()?.time } catch (_: Exception) { null }
        task.status != "completed" && deadlineTime != null && deadlineTime < currentTimeMillis
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(name = userProfile?.name ?: "Admin")
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Task Management",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge { Text(unreadCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = onNotificationsClick) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogoutClick()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onViewAllTasksClick,
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") },
                    label = { Text("Tasks") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onHistoryClick,
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            // Adaptive Container for wide devices / tablets
            Box(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    // Welcome Header
                    item {
                        Column {
                            Text(
                                text = "Good morning, ${userProfile?.name ?: "Admin"}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Here is an overview of your workforce today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Stat Cards Grid 2x2
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                KineticStatCard(
                                    count = totalEmployees.toString(),
                                    label = "TOTAL EMPLOYEES",
                                    icon = Icons.Default.Group,
                                    badgeBg = Color(0xFFECEBFF),
                                    badgeIconTint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                KineticStatCard(
                                    count = activeTasks.toString(),
                                    label = "ACTIVE TASKS",
                                    icon = Icons.Default.Assignment,
                                    badgeBg = Color(0xFFECEBFF),
                                    badgeIconTint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                KineticStatCard(
                                    count = pendingTasks.toString(),
                                    label = "PENDING",
                                    icon = Icons.Default.Pending,
                                    badgeBg = Color(0xFFFEE2E2).copy(alpha = 0.5f),
                                    badgeIconTint = Color(0xFFD97706),
                                    modifier = Modifier.weight(1f)
                                )
                                KineticStatCard(
                                    count = overdueTasks.toString(),
                                    label = "OVERDUE",
                                    icon = Icons.Default.Warning,
                                    badgeBg = StatusOverdueBg,
                                    badgeIconTint = StatusOverdueText,
                                    countColor = StatusOverdueText,
                                    borderColor = StatusOverdueBg,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Assign New Task Primary Button
                    item {
                        Button(
                            onClick = onCreateTaskClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Assign new task",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // Secondary button — lets Admin view their own personal task list
                    // (same screen an Employee sees)
                    item {
                        OutlinedButton(
                            onClick = onViewMyTasksClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View my tasks",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    // Recent Activity Title
                    item {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Live Activity Feed
                    if (allTasks.isEmpty()) {
                        items(3) {
                            ShimmerTaskCard()
                        }
                    } else {
                        items(allTasks.take(8), key = { it.id }) { task ->
                            val firstAssignedId = task.assignedTo.firstOrNull() ?: ""
                            val assignedEmp = employees.find { it.uid == firstAssignedId }
                            val empName = if (task.assignedTo.size > 1) {
                                "${assignedEmp?.name ?: "User"} + ${task.assignedTo.size - 1} more"
                            } else {
                                assignedEmp?.name ?: "User"
                            }

                            val deadlineTime = try { task.deadline?.toDate()?.time } catch (_: Exception) { null }
                            val isOverdue = task.status != "completed" && deadlineTime != null && deadlineTime < currentTimeMillis

                            ActivityItemCard(
                                task = task,
                                employeeName = empName,
                                isOverdue = isOverdue,
                                onClick = { onTaskClick(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KineticStatCard(
    count: String,
    label: String,
    icon: ImageVector,
    badgeBg: Color,
    badgeIconTint: Color,
    countColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.then(
            if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(16.dp)) else Modifier
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = badgeBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = badgeIconTint, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = countColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActivityItemCard(
    task: Task,
    employeeName: String,
    isOverdue: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (iconBg, iconTint, iconVector) = when {
                isOverdue -> Triple(StatusOverdueBg, StatusOverdueText, Icons.Default.Warning)
                task.status == "completed" -> Triple(StatusCompletedBg, StatusCompletedText, Icons.Default.CheckCircle)
                task.status == "in_progress" -> Triple(StatusInProgressBg, StatusInProgressText, Icons.Default.Assignment)
                else -> Triple(StatusPendingBg, StatusPendingText, Icons.Default.Pending)
            }

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(iconVector, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                val actionText = when {
                    isOverdue -> "Task '${task.title}' is overdue"
                    task.status == "completed" -> "$employeeName completed '${task.title}'"
                    task.status == "in_progress" -> "$employeeName started '${task.title}'"
                    else -> "New task assigned to $employeeName"
                }

                Text(
                    text = actionText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isOverdue) StatusOverdueText else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Priority: ${task.priority.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} • ${task.status.replace("_", " ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}