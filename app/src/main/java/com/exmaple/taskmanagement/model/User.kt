package com.exmaple.taskmanagement.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "employee",
    val department: String? = null,
    val fcmToken: String? = null,
    val profilePictureUrl: String? = null
)
