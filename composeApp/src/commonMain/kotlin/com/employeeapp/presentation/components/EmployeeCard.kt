package com.employeeapp.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.employeeapp.domain.model.Employee
import com.employeeapp.presentation.theme.DepartmentColors
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmployeeCard(
    employee: Employee,
    searchQuery: String = "",
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showContextMenu by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .combinedClickable(
                    onClick = onViewDetails,
                    onLongClick = { showContextMenu = true }
                ),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, hoveredElevation = 6.dp)
        ) {
            Box {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    EmployeeAvatar(
                        name = employee.fullName,
                        imagePath = employee.profileImagePath,
                        department = employee.department.displayName,
                        size = 52
                    )

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Name with search highlight
                        Text(
                            text = highlightText(employee.fullName, searchQuery),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = highlightText(employee.email, searchQuery),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DepartmentBadge(
                                department = employee.department.displayName,
                                query = searchQuery
                            )
                            EmploymentChip(label = employee.employmentType.displayName)
                            if (employee.isActive) {
                                ActiveIndicator(active = true)
                            } else {
                                ActiveIndicator(active = false)
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "₹${"%,.0f".format(employee.salary)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatDate(employee.joiningDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Context Menu
                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View Details") },
                        leadingIcon = { Icon(Icons.Default.Visibility, null) },
                        onClick = { showContextMenu = false; onViewDetails() }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { showContextMenu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = { showContextMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
fun EmployeeAvatar(
    name: String,
    imagePath: String?,
    department: String,
    size: Int = 52
) {
    val deptColor = DepartmentColors[department] ?: Color(0xFF1E6FFF)
    val initials = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (imagePath != null) {
            AsyncImage(
                model = imagePath,
                contentDescription = "Profile image of $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size.dp).clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier.size(size.dp).background(deptColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.ifEmpty { "?" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (size / 3).sp,
                        fontWeight = FontWeight.Bold,
                        color = deptColor
                    )
                )
            }
        }
    }
}

@Composable
fun DepartmentBadge(department: String, query: String = "") {
    val color = DepartmentColors[department] ?: Color(0xFF1E6FFF)
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(
            text = highlightText(department, query),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmploymentChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ActiveIndicator(active: Boolean) {
    val color = if (active) Color(0xFF10B981) else Color(0xFF94A3B8)
    val label = if (active) "Active" else "Inactive"
    Surface(color = color.copy(alpha = 0.12f), shape = MaterialTheme.shapes.extraLarge) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    return buildAnnotatedString {
        val lower = text.lowercase()
        val q = query.lowercase()
        var start = 0
        var idx = lower.indexOf(q, start)
        while (idx != -1) {
            append(text.substring(start, idx))
            withStyle(SpanStyle(background = Color(0xFFFFD700), fontWeight = FontWeight.Bold)) {
                append(text.substring(idx, idx + q.length))
            }
            start = idx + q.length
            idx = lower.indexOf(q, start)
        }
        append(text.substring(start))
    }
}

fun formatDate(date: LocalDate): String {
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    return "${date.dayOfMonth} ${months[date.monthNumber - 1]} ${date.year}"
}
