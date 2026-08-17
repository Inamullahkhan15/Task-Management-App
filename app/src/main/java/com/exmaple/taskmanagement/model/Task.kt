package com.exmaple.taskmanagement.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val assignedTo: List<String> = emptyList(),
    val assignedBy: String = "",
    val deadline: Timestamp? = null,
    val priority: String = "medium",
    val category: String? = null,
    val status: String = "pending",
    val employeeStatuses: Map<String, String> = emptyMap(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
