package com.exmaple.taskmanagement.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exmaple.taskmanagement.model.Task
import com.exmaple.taskmanagement.model.User
import com.exmaple.taskmanagement.ui.theme.StatusCompletedBg
import com.exmaple.taskmanagement.ui.theme.StatusCompletedText
import com.exmaple.taskmanagement.ui.theme.StatusInProgressBg
import com.exmaple.taskmanagement.ui.theme.StatusInProgressText
import com.exmaple.taskmanagement.ui.theme.StatusOverdueBg
import com.exmaple.taskmanagement.ui.theme.StatusOverdueText
import com.exmaple.taskmanagement.ui.theme.StatusPendingBg
import com.exmaple.taskmanagement.ui.theme.StatusPendingText
import java.text.SimpleDateFormat

@Composable
fun KineticTaskCard(
    task: Task,
    assignedUser: User?,
    isOverdue: Boolean,
    dateFormatter: SimpleDateFormat,
    onClick: (() -> Unit)? = null
) {
    val formattedDate = try {
        task.deadline?.toDate()?.let { dateFormatter.format(it) } ?: "No date"
    } catch (_: Exception) {
        "No date"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))

                val (badgeBg, badgeText, label) = when {
                    isOverdue -> Triple(StatusOverdueBg, StatusOverdueText, "Overdue")
                    task.status == "completed" -> Triple(StatusCompletedBg, StatusCompletedText, "Completed")
                    task.status == "in_progress" -> Triple(StatusInProgressBg, StatusInProgressText, "In Progress")
                    else -> Triple(StatusPendingBg, StatusPendingText, "Pending")
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = label,
                        color = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val initials = assignedUser?.name?.take(2)?.uppercase() ?: "EM"
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = assignedUser?.name ?: "Assigned Employee",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (isOverdue) StatusOverdueText else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) StatusOverdueText else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun EmployeeStatBadge(
    count: String,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun KineticEmployeeTaskCard(
    task: Task,
    isOverdue: Boolean,
    dateFormatter: SimpleDateFormat,
    onTaskClick: () -> Unit
) {
    val formattedDate = try {
        task.deadline?.toDate()?.let { dateFormatter.format(it) } ?: "No deadline"
    } catch (_: Exception) {
        "No deadline"
    }

    val leftBorderColor = when {
        isOverdue -> StatusOverdueText
        task.status == "in_progress" -> MaterialTheme.colorScheme.primary
        task.status == "completed" -> StatusCompletedText
        else -> StatusPendingText
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(leftBorderColor)
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (isOverdue) StatusOverdueText else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Due: $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) StatusOverdueText else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val (badgeBg, badgeText, label) = when {
                    isOverdue -> Triple(StatusOverdueBg, StatusOverdueText, "Overdue")
                    task.status == "completed" -> Triple(StatusCompletedBg, StatusCompletedText, "Completed")
                    task.status == "in_progress" -> Triple(StatusInProgressBg, StatusInProgressText, "In Progress")
                    else -> Triple(StatusPendingBg, StatusPendingText, "Pending")
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = label,
                        color = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
