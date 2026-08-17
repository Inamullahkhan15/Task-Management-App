package com.exmaple.taskmanagement.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.exmaple.taskmanagement.ui.notification.NotificationsScreen
import com.exmaple.taskmanagement.ui.screens.admin.AdminDashboardScreen
import com.exmaple.taskmanagement.ui.screens.admin.AllTasksScreen
import com.exmaple.taskmanagement.ui.screens.admin.CreateTaskScreen
import com.exmaple.taskmanagement.ui.screens.employee.EmployeeAllTasksScreen
import com.exmaple.taskmanagement.ui.screens.employee.EmployeeHomeScreen
import com.exmaple.taskmanagement.ui.screens.employee.TaskDetailScreen
import com.exmaple.taskmanagement.ui.screens.history.HistoryScreen
import com.exmaple.taskmanagement.ui.screens.login.LoginScreen
import com.exmaple.taskmanagement.ui.screens.profile.ProfileScreen
import com.exmaple.taskmanagement.ui.screens.signup.SignUpScreen
import com.exmaple.taskmanagement.viewmodel.AuthViewModel
import com.exmaple.taskmanagement.viewmodel.TaskViewModel

object Routes {
    const val SplashCheck = "splash_check"
    const val Login = "login"
    const val SignUp = "signup"
    const val AdminDashboard = "admin_dashboard"
    const val CreateTask = "create_task"
    const val AllTasks = "all_tasks"
    const val EmployeeHome = "employee_home"
    const val EmployeeAllTasks = "employee_all_tasks"
    const val Notifications = "notifications"
    const val Profile = "profile"
    const val History = "history"
    const val TaskDetail = "task_detail/{taskId}"

    fun buildTaskDetailRoute(taskId: String): String = "task_detail/$taskId"
}

// Guards a screen so only an "admin" role can see it. Anyone else
// (or role not yet loaded) gets redirected away safely.
@Composable
private fun RequireAdmin(
    authViewModel: AuthViewModel,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val role by authViewModel.currentRole.collectAsStateWithLifecycle()

    LaunchedEffect(role) {
        if (role != null && !role.equals("admin", ignoreCase = true)) {
            try {
                navController.navigate(Routes.EmployeeHome) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            } catch (_: Exception) {}
        }
    }

    if (role.equals("admin", ignoreCase = true)) {
        content()
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SplashCheck
    ) {
        // Initial Session Check
        composable(Routes.SplashCheck) {
            LaunchedEffect(Unit) {
                authViewModel.checkInitialSession { role, _ ->
                    val destination = when (role?.lowercase()) {
                        "admin" -> Routes.AdminDashboard
                        "employee" -> Routes.EmployeeHome
                        else -> Routes.Login
                    }
                    try {
                        navController.navigate(destination) {
                            popUpTo(Routes.SplashCheck) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (_: Exception) {}
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Login Screen
        composable(Routes.Login) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { role, _ ->
                    val destination = if (role.equals("admin", ignoreCase = true)) {
                        Routes.AdminDashboard
                    } else {
                        Routes.EmployeeHome
                    }
                    try {
                        navController.navigate(destination) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (_: Exception) {}
                },
                onNavigateToSignUp = {
                    try { navController.navigate(Routes.SignUp) } catch (_: Exception) {}
                }
            )
        }

        // Sign Up Screen
        composable(Routes.SignUp) {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = {
                    try { navController.popBackStack() } catch (_: Exception) {}
                },
                onBackToLogin = {
                    try { navController.popBackStack() } catch (_: Exception) {}
                }
            )
        }

        // Admin Dashboard — guarded, employee cannot reach this
        composable(Routes.AdminDashboard) {
            RequireAdmin(authViewModel, navController) {
                AdminDashboardScreen(
                    taskViewModel = taskViewModel,
                    authViewModel = authViewModel,
                    onCreateTaskClick = {
                        try { navController.navigate(Routes.CreateTask) } catch (_: Exception) {}
                    },
                    onViewAllTasksClick = {
                        try { navController.navigate(Routes.AllTasks) } catch (_: Exception) {}
                    },
                    onViewMyTasksClick = {
                        try { navController.navigate(Routes.EmployeeHome) } catch (_: Exception) {}
                    },
                    onLogoutClick = {
                        try {
                            navController.navigate(Routes.Login) {
                                popUpTo(0) { inclusive = true }
                            }
                        } catch (_: Exception) {}
                    },
                    onNotificationsClick = {
                        try { navController.navigate(Routes.Notifications) } catch (_: Exception) {}
                    },
                    onProfileClick = {
                        try { navController.navigate(Routes.Profile) } catch (_: Exception) {}
                    },
                    onHistoryClick = {
                        try { navController.navigate(Routes.History) } catch (_: Exception) {}
                    },
                    onTaskClick = { taskId ->
                        try { navController.navigate(Routes.buildTaskDetailRoute(taskId)) } catch (_: Exception) {}
                    }
                )
            }
        }

        // Create Task Screen — guarded, employee cannot reach this
        composable(Routes.CreateTask) {
            RequireAdmin(authViewModel, navController) {
                CreateTaskScreen(
                    taskViewModel = taskViewModel,
                    onBackClick = { try { navController.popBackStack() } catch (_: Exception) {} },
                    onTaskCreated = { try { navController.popBackStack() } catch (_: Exception) {} }
                )
            }
        }

        // All Tasks Screen — guarded, employee cannot reach this
        composable(Routes.AllTasks) {
            RequireAdmin(authViewModel, navController) {
                AllTasksScreen(
                    taskViewModel = taskViewModel,
                    authViewModel = authViewModel,
                    onBackClick = { try { navController.popBackStack() } catch (_: Exception) {} },
                    onNotificationsClick = {
                        try { navController.navigate(Routes.Notifications) } catch (_: Exception) {}
                    },
                    onProfileClick = {
                        try { navController.navigate(Routes.Profile) } catch (_: Exception) {}
                    },
                    onHistoryClick = {
                        try { navController.navigate(Routes.History) } catch (_: Exception) {}
                    },
                    onTaskClick = { taskId ->
                        try { navController.navigate(Routes.buildTaskDetailRoute(taskId)) } catch (_: Exception) {}
                    }
                )
            }
        }

        // Employee Home — reachable by BOTH employee and admin (admin can
        // check their own personal tasks here too, with a back button to
        // return to the Admin Dashboard)
        composable(Routes.EmployeeHome) {
            val role by authViewModel.currentRole.collectAsStateWithLifecycle()
            val isAdminViewing = role.equals("admin", ignoreCase = true)

            EmployeeHomeScreen(
                taskViewModel = taskViewModel,
                authViewModel = authViewModel,
                isAdminView = isAdminViewing,
                onBackToAdminClick = {
                    try {
                        navController.navigate(Routes.AdminDashboard) { launchSingleTop = true }
                    } catch (_: Exception) {}
                },
                onNotificationsClick = {
                    try { navController.navigate(Routes.Notifications) } catch (_: Exception) {}
                },
                onProfileClick = {
                    try { navController.navigate(Routes.Profile) } catch (_: Exception) {}
                },
                onHistoryClick = {
                    try { navController.navigate(Routes.History) } catch (_: Exception) {}
                },
                onTaskClick = { taskId ->
                    try { navController.navigate(Routes.buildTaskDetailRoute(taskId)) } catch (_: Exception) {}
                },
                onViewAllClick = {
                    try { navController.navigate(Routes.EmployeeAllTasks) } catch (_: Exception) {}
                },
                onLogoutClick = {
                    try {
                        navController.navigate(Routes.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    } catch (_: Exception) {}
                }
            )
        }

        // Employee All Tasks Screen
        composable(Routes.EmployeeAllTasks) {
            EmployeeAllTasksScreen(
                taskViewModel = taskViewModel,
                authViewModel = authViewModel,
                onBackClick = { try { navController.popBackStack() } catch (_: Exception) {} },
                onNotificationsClick = { try { navController.navigate(Routes.Notifications) } catch (_: Exception) {} },
                onProfileClick = { try { navController.navigate(Routes.Profile) } catch (_: Exception) {} },
                onHistoryClick = { try { navController.navigate(Routes.History) } catch (_: Exception) {} },
                onTaskClick = { taskId -> try { navController.navigate(Routes.buildTaskDetailRoute(taskId)) } catch (_: Exception) {} }
            )
        }

        // Notifications Screen
        composable(Routes.Notifications) {
            NotificationsScreen(
                taskViewModel = taskViewModel,
                authViewModel = authViewModel,
                onBackClick = { try { navController.popBackStack() } catch (_: Exception) {} },
                onNotificationClick = { taskId ->
                    try { navController.navigate(Routes.buildTaskDetailRoute(taskId)) } catch (_: Exception) {}
                }
            )
        }

        // Profile Screen
        composable(Routes.Profile) {
            val role by authViewModel.currentRole.collectAsStateWithLifecycle()
            val isAdmin = role.equals("admin", ignoreCase = true)
            
            ProfileScreen(
                authViewModel = authViewModel,
                onBackClick = { try { navController.popBackStack() } catch (_: Exception) {} },
                onHomeClick = {
                    val dest = if (isAdmin) Routes.AdminDashboard else Routes.EmployeeHome
                    try { navController.navigate(dest) { popUpTo(0) } } catch (_: Exception) {}
                },
                onTasksClick = {
                    val dest = if (isAdmin) Routes.AllTasks else Routes.EmployeeAllTasks
                    try { navController.navigate(dest) { launchSingleTop = true } } catch (_: Exception) {}
                },
                onHistoryClick = {
                    try { navController.navigate(Routes.History) { launchSingleTop = true } } catch (_: Exception) {}
                },
                onLogoutClick = {
                    try {
                        navController.navigate(Routes.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    } catch (_: Exception) {}
                }
            )
        }

        // History Screen
        composable(Routes.History) {
            val role by authViewModel.currentRole.collectAsStateWithLifecycle()
            val isAdmin = role.equals("admin", ignoreCase = true)
            
            HistoryScreen(
                taskViewModel = taskViewModel,
                authViewModel = authViewModel,
                onBackClick = { try { navController.popBackStack() } catch (_: Exception) {} },
                onHomeClick = {
                    val dest = if (isAdmin) Routes.AdminDashboard else Routes.EmployeeHome
                    try { navController.navigate(dest) { popUpTo(0) } } catch (_: Exception) {}
                },
                onTasksClick = {
                    val dest = if (isAdmin) Routes.AllTasks else Routes.EmployeeAllTasks
                    try { navController.navigate(dest) { launchSingleTop = true } } catch (_: Exception) {}
                },
                onProfileClick = {
                    try { navController.navigate(Routes.Profile) { launchSingleTop = true } } catch (_: Exception) {}
                },
                onTaskClick = { taskId ->
                    try { navController.navigate(Routes.buildTaskDetailRoute(taskId)) } catch (_: Exception) {}
                }
            )
        }

        // Task Detail Screen
        composable(
            route = Routes.TaskDetail,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                taskId = taskId,
                taskViewModel = taskViewModel,
                authViewModel = authViewModel,
                onBackClick = { try { navController.popBackStack() } catch (_: Exception) {} }
            )
        }
    }
}