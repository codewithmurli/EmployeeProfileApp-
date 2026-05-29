package com.employeeapp.presentation.screens.employees

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.employeeapp.data.repository.EmployeeRepository
import com.employeeapp.domain.model.Employee
import com.employeeapp.presentation.components.ActiveIndicator
import com.employeeapp.presentation.components.DepartmentBadge
import com.employeeapp.presentation.components.EmployeeAvatar
import com.employeeapp.presentation.components.EmploymentChip
import com.employeeapp.presentation.components.formatDate
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.EmployeeDetailScreen(
    employeeId: Long,
    onNavigateBack: () -> Unit,
    onEdit: (Long) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    repository: EmployeeRepository = koinInject()
) {
    var employee  by remember { mutableStateOf<Employee?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(employeeId) {
        scope.launch {
            employee  = repository.getEmployeeById(employeeId)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        employee?.fullName ?: "Employee Detail",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            employee?.let {
                FloatingActionButton(onClick = { onEdit(it.id) }) {
                    Icon(Icons.Default.Edit, "Edit")
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val emp = employee ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Employee not found")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header with shared element transition key on avatar ───────────
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Shared element: avatar transitions from list card to detail
                    Box(
                        modifier = Modifier.sharedElement(
                            state = rememberSharedContentState(key = "avatar-${emp.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    ) {
                        EmployeeAvatar(
                            emp.fullName,
                            emp.profileImagePath,
                            emp.department.displayName,
                            88
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        emp.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.sharedElement(
                            state = rememberSharedContentState(key = "name-${emp.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    )
                    Text(
                        emp.department.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DepartmentBadge(emp.department.displayName)
                        EmploymentChip(emp.employmentType.displayName)
                        ActiveIndicator(emp.isActive)
                    }
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Salary highlight
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Annual Salary", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "₹${"%,.0f".format(emp.salary)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider()

                DetailRow({ Icon(Icons.Default.Email, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) }, "Email", emp.email)
                DetailRow({ Icon(Icons.Default.Phone, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) }, "Phone", emp.phone)
                DetailRow({ Icon(Icons.Default.LocationOn, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) }, "Address", emp.address)

                HorizontalDivider()

                InfoGrid(listOf(
                    "Gender"       to emp.gender.displayName,
                    "Joining Date" to formatDate(emp.joiningDate),
                    "Employment"   to emp.employmentType.displayName,
                    "Status"       to if (emp.isActive) "Active" else "Inactive"
                ))

                HorizontalDivider()

                Text("Skills", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    emp.skills.forEach { skill ->
                        AssistChip(onClick = {}, label = { Text(skill.displayName) })
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(icon: @Composable () -> Unit, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        icon()
        Spacer(Modifier.size(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun InfoGrid(items: List<Pair<String, String>>) {
    items.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth()) {
            pair.forEach { (label, value) ->
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
