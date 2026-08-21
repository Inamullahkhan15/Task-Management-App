package com.exmaple.taskmanagement.repository

import com.exmaple.taskmanagement.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class EmailNotVerifiedException :
    Exception("Your email is not verified yet. Please check your inbox for the verification link.")

class AuthRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Create account, save profile as "employee", send verification email, sign out immediately
    suspend fun signUp(name: String, email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Sign up failed"))

            val profile = hashMapOf(
                "name" to name,
                "email" to email,
                "role" to "employee",
                "isApproved" to false
            )
            db.collection("users").document(user.uid).set(profile).await()

            // Notify Admins about new registration
            val admins = db.collection("users").whereEqualTo("role", "admin").get().await()
            for (adminDoc in admins.documents) {
                val notificationData = hashMapOf(
                    "userId" to adminDoc.id,
                    "taskId" to "",
                    "title" to "New Join Request",
                    "message" to "$name has registered and is waiting for approval.",
                    "isRead" to false,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                db.collection("notifications").add(notificationData)
            }

            user.sendEmailVerification().await()
            auth.signOut()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Login failed: invalid account"))

            if (!user.isEmailVerified) {
                auth.signOut()
                return Result.failure(EmailNotVerifiedException())
            }

            // Check if account is approved by Admin
            val userDoc = db.collection("users").document(user.uid).get().await()
            val isApproved = userDoc.getBoolean("isApproved") ?: false
            val role = userDoc.getString("role") ?: "employee"

            if (!isApproved && role != "admin") {
                auth.signOut()
                return Result.failure(Exception("Your account is pending Admin approval. Please try again later."))
            }

            // Fetch and update FCM Token (ASYNCHRONOUSLY)
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    // Run update in a global way since login is about to return
                    db.collection("users").document(user.uid).update("fcmToken", token)
                }
            }

            Result.success(user.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        try {
            db.collection("users").document(uid).update("fcmToken", token).await()
        } catch (_: Exception) {}
    }

    suspend fun resendVerificationEmail(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Account not found"))
            user.sendEmailVerification().await()
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRole(uid: String): String? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) doc.getString("role")?.takeIf { it.isNotBlank() } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)?.copy(uid = doc.id)
        } catch (_: Exception) {
            null
        }
    }

    fun logout() {
        try { auth.signOut() } catch (_: Exception) {}
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}