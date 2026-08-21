package com.exmaple.taskmanagement.model

import com.google.firebase.Timestamp

data class JoinRequest(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val createdAt: Timestamp? = null
)
