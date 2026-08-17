package com.exmaple.taskmanagement.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exmaple.taskmanagement.model.Task
import com.exmaple.taskmanagement.model.User
import com.exmaple.taskmanagement.ui.components.UserAvatar
import com.exmaple.taskmanagement.ui.theme.StatusCompletedBg
import com.exmaple.taskmanagement.ui.theme.StatusCompletedText
import com.exmaple.taskmanagement.ui.theme.StatusInProgressBg
import com.exmaple.taskmanagement.ui.theme.StatusInProgressText
import com.exmaple.taskmanagement.ui.theme.StatusOverdueBg
import com.exmaple.taskmanagement.ui.theme.StatusOverdueText
import com.exmaple.taskmanagement.ui.components.KineticTaskCard
import com.exmaple.taskmanagement.viewmodel.AuthViewModel
import com.exmaple.taskmanagement.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AllTasksScreen(
    taskViewModel: TaskViewModel,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onTaskClick: (String) -> Unit = {}
) {
    val filteredTasks by taskViewModel.filteredAllTasks.collectAsStateWithLifecycle()
    val employees by taskViewModel.employees.collectAsStateWithLifecycle()
    val statusFilter by taskViewModel.statusFilter.collectAsStateWithLifecycle()
    val priorityFilter by taskViewModel.priorityFilter.collectAsStateWithLifecycle()
    val categoryFilter by taskViewModel.categoryFilter.collectAsStateWithLifecycle()
    val dateFilter by taskViewModel.dateFilter.collectAsStateWithLifecycle()
    val userProfile by authViewModel.currentUserProfile.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val filterDateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val datePickerDialog = remember {
        val cal = Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                taskViewModel.setDateFilter(selectedCal.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val categories = listOf("All", "Engineering", "Sales", "Inventory", "Marketing", "Support", "Operations")

    val currentAdminId = authViewModel.getCurrentUserId() ?: ""
    val notificationsFlow = remember(currentAdminId) { taskViewModel.getMyNotifications(currentAdminId) }
    val notifications by notificationsFlow.collectAsStateWithLifecycle(emptyList())
    val unreadCount = notifications.count { !it.isRead }

    LaunchedEffect(currentAdminId) {
        if (currentAdminId.isNotBlank()) {
            taskViewModel.loadAdminTasks(currentAdminId)
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val currentTimeMillis = System.currentTimeMillis()

    val displayedTasks = filteredTasks.filter { task ->
        if (searchQuery.isBlank()) true
        else task.title.contains(searchQuery, ignoreCase = true) || task.description.contains(searchQuery, ignoreCase = true)
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = false,
                    onClick = onBackClick,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
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
            Box(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Title
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text(
                            text = "All Tasks",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Manage and track workforce assignments.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Search Bar & Filter Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search tasks...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (priorityFilter != "All" || categoryFilter != "All" || dateFilter != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                IconButton(onClick = { showFilterSheet = true }) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = if (priorityFilter != "All" || categoryFilter != "All" || dateFilter != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    // Filter Chips Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Pending", "In_Progress", "Completed").forEach { statusOption ->
                            val isSelected = statusFilter.equals(statusOption, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { taskViewModel.setStatusFilter(statusOption) },
                                label = { Text(statusOption.replace("_", " ")) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }

                    // Task List View
                    if (displayedTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tasks found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(displayedTasks, key = { it.id }) { task ->
                                val isOverdue = task.status != "completed" && task.deadline != null && task.deadline.toDate().time < currentTimeMillis
                                val firstAssignedId = task.assignedTo.firstOrNull() ?: ""
                                val assignedEmp = employees.find { it.uid == firstAssignedId }

                                KineticTaskCard(
                                    task = task,
                                    assignedUser = assignedEmp,
                                    isOverdue = isOverdue,
                                    dateFormatter = dateFormatter,
                                    onClick = { onTaskClick(task.id) }
                                )
                            }
                        }
                    }
                }
            }

            if (showFilterSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterSheet = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
                    ) {
                        Text("Filters", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Priority", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All", "Low", "Medium", "High").forEach { p ->
                                FilterChip(
                                    selected = priorityFilter.equals(p, ignoreCase = true),
                                    onClick = { taskViewModel.setPriorityFilter(p) },
                                    label = { Text(p) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Category", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = categoryFilter.equals(cat, ignoreCase = true),
                                    onClick = { taskViewModel.setCategoryFilter(cat) },
                                    label = { Text(cat) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Date Assigned", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { datePickerDialog.show() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (dateFilter != null) filterDateFormatter.format(Date(dateFilter!!)) else "Select Date")
                            }

                            if (dateFilter != null) {
                                IconButton(onClick = { taskViewModel.setDateFilter(null) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear Date")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { 
                                scope.launch { sheetState.hide() }.invokeOnCompletion { showFilterSheet = false }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply Filters")
                        }

                        TextButton(
                            onClick = { 
                                taskViewModel.setPriorityFilter("All")
                                taskViewModel.setCategoryFilter("All")
                                taskViewModel.setDateFilter(null)
                                scope.launch { sheetState.hide() }.invokeOnCompletion { showFilterSheet = false }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset All")
                        }
                    }
                }
            }
        }
    }
}
