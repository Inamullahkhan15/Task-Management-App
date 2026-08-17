package com.exmaple.taskmanagement.ui.screens.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exmaple.taskmanagement.ui.components.KineticEmployeeTaskCard
import com.exmaple.taskmanagement.ui.components.UserAvatar
import com.exmaple.taskmanagement.viewmodel.AuthViewModel
import com.exmaple.taskmanagement.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EmployeeAllTasksScreen(
    taskViewModel: TaskViewModel,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onTaskClick: (String) -> Unit
) {
    val userId = authViewModel.getCurrentUserId() ?: ""
    val userProfile by authViewModel.currentUserProfile.collectAsStateWithLifecycle()
    
    val myTasksFlow = remember(userId) { taskViewModel.getMyTasks(userId) }
    val myTasks by myTasksFlow.collectAsStateWithLifecycle(emptyList())

    val notificationsFlow = remember(userId) { taskViewModel.getMyNotifications(userId) }
    val notifications by notificationsFlow.collectAsStateWithLifecycle(emptyList())
    val unreadCount = notifications.count { !it.isRead }

    val statusFilter by taskViewModel.statusFilter.collectAsStateWithLifecycle()
    val priorityFilter by taskViewModel.priorityFilter.collectAsStateWithLifecycle()
    val categoryFilter by taskViewModel.categoryFilter.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    val currentTimeMillis = System.currentTimeMillis()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }

    val categories = listOf("All", "Engineering", "Sales", "Inventory", "Marketing", "Support", "Operations")

    val displayedTasks = myTasks.filter { task ->
        val matchesSearch = if (searchQuery.isBlank()) true
        else task.title.contains(searchQuery, ignoreCase = true) || task.description.contains(searchQuery, ignoreCase = true)
        
        val matchesStatus = if (statusFilter == "All") true else task.status.equals(statusFilter, ignoreCase = true)
        val matchesPriority = if (priorityFilter == "All") true else task.priority.equals(priorityFilter, ignoreCase = true)
        val matchesCategory = if (categoryFilter == "All") true else task.category.equals(categoryFilter, ignoreCase = true)

        matchesSearch && matchesStatus && matchesPriority && matchesCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(name = userProfile?.name ?: "Employee")
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Task Management", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = false, onClick = onBackClick, icon = { Icon(Icons.Default.Home, contentDescription = "Home") }, label = { Text("Home") })
                NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") }, label = { Text("Tasks") })
                NavigationBarItem(selected = false, onClick = onHistoryClick, icon = { Icon(Icons.Default.History, contentDescription = "History") }, label = { Text("History") })
                NavigationBarItem(selected = false, onClick = onProfileClick, icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }, label = { Text("Profile") })
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text("My Assignments", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Search and filter all your tasks.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Search & Filter Icon
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery, onValueChange = { searchQuery = it },
                        placeholder = { Text("Search tasks...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (priorityFilter != "All" || categoryFilter != "All") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.size(52.dp)
                    ) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                }

                // Quick Status Chips
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Pending", "In_Progress", "Completed").forEach { statusOption ->
                        FilterChip(
                            selected = statusFilter.equals(statusOption, ignoreCase = true),
                            onClick = { taskViewModel.setStatusFilter(statusOption) },
                            label = { Text(statusOption.replace("_", " ")) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                if (displayedTasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tasks match your criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(displayedTasks, key = { it.id }) { task ->
                            val isOverdue = task.status != "completed" && task.deadline?.let { it.toDate().time < currentTimeMillis } == true
                            KineticEmployeeTaskCard(task = task, isOverdue = isOverdue, dateFormatter = dateFormatter, onTaskClick = { onTaskClick(task.id) })
                        }
                    }
                }
            }

            if (showFilterSheet) {
                ModalBottomSheet(onDismissRequest = { showFilterSheet = false }, sheetState = sheetState) {
                    Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 48.dp)) {
                        Text("Refine Tasks", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Priority", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("All", "Low", "Medium", "High").forEach { p ->
                                FilterChip(selected = priorityFilter.equals(p, ignoreCase = true), onClick = { taskViewModel.setPriorityFilter(p) }, label = { Text(p) })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Category", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.forEach { cat ->
                                FilterChip(selected = categoryFilter.equals(cat, ignoreCase = true), onClick = { taskViewModel.setCategoryFilter(cat) }, label = { Text(cat) })
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { showFilterSheet = false } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Apply") }
                    }
                }
            }
        }
    }
}
