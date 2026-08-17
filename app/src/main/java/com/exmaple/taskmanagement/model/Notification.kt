package com.exmaple.taskmanagement.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Notification(
    val id: String = "",
    val userId: String = "",
    val taskId: String = "",
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: Timestamp? = null
)