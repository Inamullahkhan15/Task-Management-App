package com.exmaple.taskmanagement.repository

import com.exmaple.taskmanagement.model.JoinRequest
import com.exmaple.taskmanagement.model.Notification
import com.exmaple.taskmanagement.model.Task
import com.exmaple.taskmanagement.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun getAllTasks(): Flow<List<Task>> = callbackFlow {
        val registration = db.collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Task::class.java)?.copy(id = doc.id)
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()

                val sortedTasks = tasks.sortedByDescending { task ->
                    task.updatedAt?.seconds ?: (task.createdAt?.seconds ?: 0L)
                }
                trySend(sortedTasks)
            }
        awaitClose { registration.remove() }
    }

    fun getTasksByAdmin(adminId: String): Flow<List<Task>> = callbackFlow {
        val registration = db.collection("tasks")
            .whereEqualTo("assignedBy", adminId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Task::class.java)?.copy(id = doc.id)
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()

                val sortedTasks = tasks.sortedByDescending { task ->
                    task.updatedAt?.seconds ?: (task.createdAt?.seconds ?: 0L)
                }
                trySend(sortedTasks)
            }
        awaitClose { registration.remove() }
    }

    fun getRecentTasksByAdmin(adminId: String, limit: Long): Flow<List<Task>> = callbackFlow {
        val registration = db.collection("tasks")
            .whereEqualTo("assignedBy", adminId)
            .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Task::class.java)?.copy(id = doc.id)
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(tasks)
            }
        awaitClose { registration.remove() }
    }

    fun getCompletedTasksByAdmin(adminId: String): Flow<List<Task>> = callbackFlow {
        val registration = db.collection("tasks")
            .whereEqualTo("assignedBy", adminId)
            .whereEqualTo("status", "completed")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Task::class.java)?.copy(id = doc.id)
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()

                val sortedTasks = tasks.sortedByDescending { it.updatedAt?.seconds ?: 0L }
                trySend(sortedTasks)
            }
        awaitClose { registration.remove() }
    }

    fun getCompletedTasksByEmployee(userId: String): Flow<List<Task>> = callbackFlow {
        val registration = db.collection("tasks")
            .whereArrayContains("assignedTo", userId)
            .whereEqualTo("status", "completed")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Task::class.java)?.copy(id = doc.id)
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()

                val sortedTasks = tasks.sortedByDescending { it.updatedAt?.seconds ?: 0L }
                trySend(sortedTasks)
            }
        awaitClose { registration.remove() }
    }

    fun getMyTasks(userId: String): Flow<List<Task>> = callbackFlow {
        val registration = db.collection("tasks")
            .whereArrayContains("assignedTo", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Task::class.java)?.copy(id = doc.id)
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()

                val sortedTasks = tasks.sortedBy { task ->
                    task.deadline?.seconds ?: Long.MAX_VALUE
                }
                trySend(sortedTasks)
            }
        awaitClose { registration.remove() }
    }

    fun getTaskById(taskId: String): Flow<Task?> = callbackFlow {
        val registration = db.collection("tasks").document(taskId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val task = try {
                    snapshot?.toObject(Task::class.java)?.copy(id = snapshot.id)
                } catch (_: Exception) {
                    null
                }
                trySend(task)
            }
        awaitClose { registration.remove() }
    }

    fun getEmployees(): Flow<List<User>> = callbackFlow {
        val registration = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(User::class.java)?.copy(uid = doc.id)
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { registration.remove() }
    }

    suspend fun getUserById(uid: String): User? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)?.copy(uid = doc.id)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun createTask(
        title: String,
        description: String,
        assignedTo: List<String>,
        assignedBy: String,
        deadline: Timestamp,
        priority: String,
        category: String?
    ): Result<Unit> {
        return try {
            val taskData = hashMapOf(
                "title" to title,
                "description" to description,
                "assignedTo" to assignedTo,
                "assignedBy" to assignedBy,
                "deadline" to deadline,
                "priority" to priority,
                "category" to category,
                "status" to "pending",
                "employeeStatuses" to assignedTo.associateWith { "pending" },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val taskRef = db.collection("tasks").add(taskData).await()

            assignedTo.forEach { userId ->
                val notificationData = hashMapOf(
                    "userId" to userId,
                    "taskId" to taskRef.id,
                    "title" to "New task assigned",
                    "message" to "You've been assigned: $title",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("notifications").add(notificationData).await()
            }

            // Save category if it's new
            if (!category.isNullOrBlank()) {
                saveCategory(category)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(
        taskId: String,
        title: String,
        description: String,
        assignedTo: List<String>,
        deadline: Timestamp,
        priority: String,
        category: String?
    ): Result<Unit> {
        return try {
            val updateData = hashMapOf(
                "title" to title,
                "description" to description,
                "assignedTo" to assignedTo,
                "deadline" to deadline,
                "priority" to priority,
                "category" to category,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("tasks").document(taskId).update(updateData as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            // 1. Delete the task first (Admin definitely has permission for this)
            db.collection("tasks").document(taskId).delete().await()
            
            // 2. Try to cleanup related notifications
            // Wrap in inner try-catch so permission issues here don't break the whole flow
            try {
                val notifs = db.collection("notifications").whereEqualTo("taskId", taskId).get().await()
                if (!notifs.isEmpty) {
                    val batch = db.batch()
                    for (doc in notifs.documents) {
                        batch.delete(doc.reference)
                    }
                    batch.commit().await()
                }
            } catch (e: Exception) {
                // Log and ignore: If we can't delete notifications, 
                // the task is still gone, which is the main goal.
                android.util.Log.w("TaskRepository", "Notification cleanup failed: ${e.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveCategory(categoryName: String) {
        try {
            val normalized = categoryName.trim()
            val query = db.collection("categories")
                .whereEqualTo("name", normalized)
                .get().await()

            if (query.isEmpty) {
                db.collection("categories").add(mapOf("name" to normalized)).await()
            }
        } catch (_: Exception) {}
    }

    fun getCustomCategories(): Flow<List<String>> = callbackFlow {
        val registration = db.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val cats = snapshot?.documents?.mapNotNull { it.getString("name") } ?: emptyList()
                trySend(cats.sorted())
            }
        awaitClose { registration.remove() }
    }

    suspend fun updateTaskStatus(taskId: String, status: String): Result<Unit> {
        return try {
            val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val taskDoc = db.collection("tasks").document(taskId).get().await()
            val taskTitle = taskDoc.getString("title") ?: "Task"
            val adminId = taskDoc.getString("assignedBy") ?: ""

            // Update individual status in the map
            db.collection("tasks").document(taskId).update(
                mapOf(
                    "employeeStatuses.$currentUid" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (adminId.isNotBlank() && adminId != currentUid) {
                val userDoc = db.collection("users").document(currentUid).get().await()
                val updaterName = userDoc.getString("name") ?: "An employee"

                val notificationData = hashMapOf(
                    "userId" to adminId,
                    "taskId" to taskId,
                    "title" to "Task Status Updated",
                    "message" to "$updaterName updated '$taskTitle' to ${status.replace("_", " ")}",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("notifications").add(notificationData).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMyNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val registration = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Notification::class.java)?.copy(id = doc.id)
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()

                val sorted = notifications.sortedByDescending { it.createdAt?.seconds ?: 0L }
                trySend(sorted)
            }
        awaitClose { registration.remove() }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        return try {
            db.collection("notifications").document(notificationId)
                .update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllNotifications(userId: String): Result<Unit> {
        return try {
            val snapshots = db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get().await()
            
            val batch = db.batch()
            for (doc in snapshots.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun inviteEmployee(name: String, email: String, adminId: String, password: String): Result<Unit> {
        return try {
            val normalizedEmail = email.trim().lowercase()
            
            // 1. Check if already invited
            val existing = db.collection("invited_users")
                .whereEqualTo("email", normalizedEmail)
                .get().await()
            
            if (!existing.isEmpty) {
                return Result.failure(Exception("This email is already invited."))
            }

            // 2. Add to invited_users
            val inviteData = hashMapOf(
                "name" to name.trim(),
                "email" to normalizedEmail,
                "password" to password.trim(), // Save the Admin-set password
                "role" to "employee",
                "invitedBy" to adminId,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("invited_users").add(inviteData).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
// Employee Submit Request
    suspend fun submitJoinRequest(name: String, email: String, password: String): Result<Unit>{
        return try{
            val requestData = hashMapOf(
                "name" to name.trim(),
                "email" to email.trim().lowercase(),
                "password" to password.trim(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("join_requests").add(requestData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPendingRequests(): Flow<List<JoinRequest>> = callbackFlow {
        val registration = db.collection("join_requests")
        .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener {snapshot, error ->
                if (error != null){
                    trySend(emptyList())
                    return@addSnapshotListener

                }
                val requests = snapshot?.documents?.mapNotNull {doc ->
                    doc.toObject(JoinRequest::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(requests)
            }
        awaitClose { registration.remove()}
    }

    suspend fun approveJoinRequest(request: JoinRequest, adminId: String): Result<Unit> {
        return try {
            val inviteResult = inviteEmployee(request.name, request.email, adminId, request.password)
            if (inviteResult.isSuccess) {
            db.collection("join_requests").document(request.id).delete().await()
            
            // Notify the Admin (Self-feedback sound)
            val notificationData = hashMapOf(
                "userId" to adminId,
                "taskId" to "",
                "title" to "Member Approved",
                "message" to "You successfully approved ${request.name}.",
                "isRead" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("notifications").add(notificationData).await()
            
            Result.success(Unit)
        } else {
                Result.failure(inviteResult.exceptionOrNull() ?: Exception("Approval failed"))
            }
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun rejectJoinRequest(requestId: String): Result<Unit> {
        return try {
            val doc = db.collection("join_requests").document(requestId).get().await()
            val name = doc.getString("name") ?: "User"
            
            db.collection("join_requests").document(requestId).delete().await()
            
            // Notify the Admin (Self-feedback sound)
            val currentAdminId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (currentAdminId.isNotBlank()) {
                val notificationData = hashMapOf(
                    "userId" to currentAdminId,
                    "taskId" to "",
                    "title" to "Request Rejected",
                    "message" to "You rejected the request from $name.",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("notifications").add(notificationData).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUnapprovedUsers(): Flow<List<User>> = callbackFlow {
        val registration = db.collection("users")
            .whereEqualTo("isApproved", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(uid = doc.id)
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { registration.remove() }
    }

    suspend fun approveUser(uid: String): Result<Unit> {
        return try {
            db.collection("users").document(uid).update("isApproved", true).await()
            
            // Notify the User that they are approved
            val notificationData = hashMapOf(
                "userId" to uid,
                "taskId" to "",
                "title" to "Account Approved",
                "message" to "Your account has been approved by the Admin. You can now access all features.",
                "isRead" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("notifications").add(notificationData).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}