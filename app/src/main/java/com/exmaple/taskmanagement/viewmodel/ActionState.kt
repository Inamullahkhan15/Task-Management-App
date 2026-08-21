package com.exmaple.taskmanagement.viewmodel

sealed interface ActionState {
    object Idle : ActionState
    object Loading : ActionState
    object Success : ActionState
    data class Error(val message: String) : ActionState
}