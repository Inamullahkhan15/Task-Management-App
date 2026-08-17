package com.exmaple.taskmanagement.ui.screens.admin

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exmaple.taskmanagement.model.User
import com.exmaple.taskmanagement.viewmodel.ActionState
import com.exmaple.taskmanagement.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTaskScreen(
    taskViewModel: TaskViewModel,
    onBackClick: () -> Unit,
    onTaskCreated: () -> Unit
) {
    val employees by taskViewModel.employees.collectAsStateWithLifecycle()
    val createTaskState by taskViewModel.createTaskState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var selectedEmployees by remember { mutableStateOf(emptyList<User>()) }
    var employeeDropdownExpanded by remember { mutableStateOf(false) }

    var selectedPriority by remember { mutableStateOf("medium") }

    val calendar = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) } }
    var selectedDateMillis by remember { mutableLongStateOf(calendar.timeInMillis) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) }

    val categories = listOf("Engineering", "Sales", "Inventory", "Marketing", "Support", "Operations")

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 23, 59, 59)
                }
                selectedDateMillis = selectedCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    LaunchedEffect(createTaskState) {
        if (createTaskState is ActionState.Success) {
            taskViewModel.resetCreateTaskState()
            onTaskCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Task", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (selectedEmployees.isNotEmpty()) {
                                    taskViewModel.createTask(
                                        title = title,
                                        description = description,
                                        assignedTo = selectedEmployees.map { it.uid },
                                        deadlineDateMillis = selectedDateMillis,
                                        priority = selectedPriority,
                                        category = selectedCategory
                                    )
                                }
                            },
                            enabled = selectedEmployees.isNotEmpty() && title.isNotBlank() && description.isNotBlank() && createTaskState !is ActionState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (createTaskState is ActionState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Task", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
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
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Task Title Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Task Title",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("e.g. Design new landing page") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Description Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Provide detailed information about the task...") },
                            minLines = 4,
                            maxLines = 6,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Assign to Employee Dropdown
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Assign to",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = employeeDropdownExpanded,
                            onExpandedChange = { employeeDropdownExpanded = !employeeDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = if (selectedEmployees.isEmpty()) "" else "${selectedEmployees.size} person(s) selected",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Select people") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = employeeDropdownExpanded) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = employeeDropdownExpanded,
                                onDismissRequest = { employeeDropdownExpanded = false }
                            ) {
                                if (employees.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No registered users found") },
                                        onClick = { employeeDropdownExpanded = false }
                                    )
                                } else {
                                    employees.forEach { emp ->
                                        val isSelected = selectedEmployees.any { it.uid == emp.uid }
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(
                                                        checked = isSelected,
                                                        onCheckedChange = null // Click handled by parent item
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(emp.name, fontWeight = FontWeight.Bold)
                                                            if (emp.role.equals("admin", ignoreCase = true)) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = "(Admin)",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                        Text(emp.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedEmployees = if (isSelected) {
                                                    selectedEmployees.filter { it.uid != emp.uid }
                                                } else {
                                                    selectedEmployees + emp
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedEmployees.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedEmployees.forEach { emp ->
                                    InputChip(
                                        selected = true,
                                        onClick = { },
                                        label = { Text(emp.name, style = MaterialTheme.typography.labelSmall) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        selectedEmployees = selectedEmployees.filter { it.uid != emp.uid }
                                                    }
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Deadline Date Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Deadline",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = dateFormatter.format(Date(selectedDateMillis)),
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("mm/dd/yyyy") },
                            trailingIcon = {
                                IconButton(onClick = { datePickerDialog.show() }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() }
                        )
                    }

                    // Priority Segmented Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Priority",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("low", "medium", "high").forEach { pOption ->
                                    val isSelected = selectedPriority == pOption
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedPriority = pOption },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shadowElevation = if (isSelected) 2.dp else 0.dp
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = pOption.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Category Dropdown
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory.ifBlank { "Select category" },
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Error Display Banner
                    AnimatedVisibility(visible = createTaskState is ActionState.Error) {
                        if (createTaskState is ActionState.Error) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (createTaskState as ActionState.Error).message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}