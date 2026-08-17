package com.exmaple.taskmanagement.ui.screens.employee

import android.app.Notification
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exmaple.taskmanagement.ui.components.EmployeeStatBadge
import com.exmaple.taskmanagement.ui.components.KineticEmployeeTaskCard
import com.exmaple.taskmanagement.ui.components.UserAvatar
import com.exmaple.taskmanagement.viewmodel.AuthViewModel
import com.exmaple.taskmanagement.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeHomeScreen(
    taskViewModel: TaskViewModel,
    authViewModel: AuthViewModel,
    onTaskClick: (taskId: String) -> Unit,
    onLogoutClick: () -> Unit,
    onViewAllClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    isAdminView: Boolean = false,
    onBackToAdminClick: () -> Unit = {}
) {
    val userId = authViewModel.getCurrentUserId() ?: ""
    val userProfile by authViewModel.currentUserProfile.collectAsStateWithLifecycle()
    val myTasksFlow = remember(userId) { taskViewModel.getMyTasks(userId) }
    val myTasks by myTasksFlow.collectAsStateWithLifecycle()

    val notificationsFlow = remember(userId) { taskViewModel.getMyNotifications(userId) }
    val notifications by notificationsFlow.collectAsStateWithLifecycle(emptyList())
    val unreadCount = notifications.count { !it.isRead }

    val currentTimeMillis = System.currentTimeMillis()
    val pendingCount = myTasks.count { it.status == "pending" }
    val inProgressCount = myTasks.count { it.status == "in_progress" }
    val completedCount = myTasks.count { it.status == "completed" }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(name = userProfile?.name ?: "Employee")
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Task Management",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (isAdminView) {
                            Text(
                                text = "Viewing as Admin",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 46.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (isAdminView) {
                        IconButton(onClick = onBackToAdminClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to Admin Dashboard")
                        }
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
            if (!isAdminView) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onViewAllClick,
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
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
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EmployeeStatBadge(
                                count = pendingCount.toString(),
                                label = "PENDING",
                                isActive = false,
                                modifier = Modifier.weight(1f)
                            )
                            EmployeeStatBadge(
                                count = inProgressCount.toString(),
                                label = "IN PROGRESS",
                                isActive = true,
                                modifier = Modifier.weight(1f)
                            )
                            EmployeeStatBadge(
                                count = completedCount.toString(),
                                label = "COMPLETED",
                                isActive = false,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "My Tasks",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onViewAllClick() }
                            )
                        }
                    }

                    if (myTasks.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No tasks assigned to you right now.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(myTasks, key = { it.id }) { task ->
                            val deadlineTime = try { task.deadline?.toDate()?.time } catch (_: Exception) { null }
                            val isOverdue = task.status != "completed" && deadlineTime != null && deadlineTime < currentTimeMillis

                            KineticEmployeeTaskCard(
                                task = task,
                                isOverdue = isOverdue,
                                dateFormatter = dateFormatter,
                                onTaskClick = { onTaskClick(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}