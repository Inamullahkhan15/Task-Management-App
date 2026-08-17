package com.exmaple.taskmanagement.repository

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

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTaskStatus(taskId: String, status: String): Result<Unit> {
        return try {
            val taskDoc = db.collection("tasks").document(taskId).get().await()
            val taskTitle = taskDoc.getString("title") ?: "Task"
            val adminId = taskDoc.getString("assignedBy") ?: ""

            db.collection("tasks").document(taskId).update(
                mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
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
}