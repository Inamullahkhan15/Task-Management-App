package com.exmaple.taskmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmaple.taskmanagement.model.Notification
import com.exmaple.taskmanagement.model.Task
import com.exmaple.taskmanagement.model.User
import com.exmaple.taskmanagement.repository.AuthRepository
import com.exmaple.taskmanagement.repository.TaskRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ActionState {
    object Idle : ActionState
    object Loading : ActionState
    object Success : ActionState
    data class Error(val message: String) : ActionState
}

class TaskViewModel(
    private val taskRepository: TaskRepository = TaskRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _adminTasks = MutableStateFlow<List<Task>>(emptyList())
    val adminTasks: StateFlow<List<Task>> = _adminTasks.asStateFlow()

    private val _historyTasks = MutableStateFlow<List<Task>>(emptyList())
    val historyTasks: StateFlow<List<Task>> = _historyTasks.asStateFlow()

    val allTasks: StateFlow<List<Task>> = adminTasks

    val employees: StateFlow<List<User>> = taskRepository.getEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _priorityFilter = MutableStateFlow("All")
    val priorityFilter: StateFlow<String> = _priorityFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow("All")
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    val filteredAllTasks: StateFlow<List<Task>> = combine(
        adminTasks,
        statusFilter,
        priorityFilter,
        categoryFilter
    ) { tasks, status, priority, category ->
        tasks.filter { task ->
            val matchesStatus = if (status == "All") true else task.status.equals(status, ignoreCase = true)
            val matchesPriority = if (priority == "All") true else task.priority.equals(priority, ignoreCase = true)
            val matchesCategory = if (category == "All") true else task.category.equals(category, ignoreCase = true)
            matchesStatus && matchesPriority && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _createTaskState = MutableStateFlow<ActionState>(ActionState.Idle)
    val createTaskState: StateFlow<ActionState> = _createTaskState.asStateFlow()

    private val _updateStatusState = MutableStateFlow<ActionState>(ActionState.Idle)
    val updateStatusState: StateFlow<ActionState> = _updateStatusState.asStateFlow()

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    private val _assignedByName = MutableStateFlow("Admin")
    val assignedByName: StateFlow<String> = _assignedByName.asStateFlow()

    fun getMyTasks(userId: String): StateFlow<List<Task>> {
        return taskRepository.getMyTasks(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun setPriorityFilter(priority: String) {
        _priorityFilter.value = priority
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun loadAdminTasks(adminId: String) {
        viewModelScope.launch {
            taskRepository.getTasksByAdmin(adminId).collect { tasks ->
                _adminTasks.value = tasks
            }
        }
    }

    fun loadHistoryTasks(userId: String, role: String) {
        viewModelScope.launch {
            val flow = if (role.equals("admin", ignoreCase = true)) {
                taskRepository.getCompletedTasksByAdmin(userId)
            } else {
                taskRepository.getCompletedTasksByEmployee(userId)
            }
            flow.collect { tasks ->
                _historyTasks.value = tasks
            }
        }
    }

    fun loadTaskDetails(taskId: String) {
        _selectedTask.value = null // Reset before loading new
        viewModelScope.launch {
            taskRepository.getTaskById(taskId).collect { task ->
                _selectedTask.value = task
                task?.assignedBy?.takeIf { it.isNotBlank() }?.let { adminUid ->
                    val adminUser = taskRepository.getUserById(adminUid)
                    _assignedByName.value = adminUser?.name ?: "Admin"
                }
            }
        }
    }

    fun createTask(
        title: String,
        description: String,
        assignedTo: List<String>,
        deadlineDateMillis: Long,
        priority: String,
        category: String?
    ) {
        if (title.isBlank() || description.isBlank() || assignedTo.isEmpty()) {
            _createTaskState.value = ActionState.Error("Title, description, and at least one assigned employee are required.")
            return
        }

        val currentUserId = authRepository.getCurrentUserId() ?: ""
        val deadlineTimestamp = Timestamp(deadlineDateMillis / 1000, 0)

        viewModelScope.launch {
            _createTaskState.value = ActionState.Loading
            val result = taskRepository.createTask(
                title = title.trim(),
                description = description.trim(),
                assignedTo = assignedTo,
                assignedBy = currentUserId,
                deadline = deadlineTimestamp,
                priority = priority.lowercase(),
                category = category?.trim()?.ifBlank { null }
            )

            result.fold(
                onSuccess = {
                    _createTaskState.value = ActionState.Success
                },
                onFailure = { error ->
                    _createTaskState.value = ActionState.Error(error.localizedMessage ?: "Failed to create task")
                }
            )
        }
    }

    fun updateTaskStatus(taskId: String, newStatus: String) {
        viewModelScope.launch {
            _updateStatusState.value = ActionState.Loading
            val result = taskRepository.updateTaskStatus(taskId, newStatus)
            result.fold(
                onSuccess = {
                    _updateStatusState.value = ActionState.Success
                },
                onFailure = { error ->
                    _updateStatusState.value = ActionState.Error(error.localizedMessage ?: "Failed to update status")
                }
            )
        }
    }

    fun resetCreateTaskState() {
        _createTaskState.value = ActionState.Idle
    }

    fun resetUpdateStatusState() {
        _updateStatusState.value = ActionState.Idle
    }

    // Notifications
    fun getMyNotifications(userId: String): StateFlow<List<Notification>> {
        return taskRepository.getMyNotifications(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            taskRepository.markNotificationRead(notificationId)
        }
    }

    fun clearAllNotifications(userId: String) {
        viewModelScope.launch {
            taskRepository.clearAllNotifications(userId)
        }
    }
}