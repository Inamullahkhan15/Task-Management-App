package com.exmaple.taskmanagement.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UserAvatar(
    name: String,
    size: Dp = 36.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    val initials = getInitials(name)
    
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.4).sp
                ),
                color = contentColor
            )
        }
    }
}

private fun getInitials(name: String): String {
    val parts = name.trim().split(" ")
    return if (parts.size >= 2) {
        "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
    } else if (parts.isNotEmpty() && parts[0].isNotEmpty()) {
        parts[0].take(1).uppercase()
    } else {
        "?"
    }
}
