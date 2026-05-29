package com.employeeapp.presentation.screens.employees

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.employeeapp.domain.model.Department
import com.employeeapp.domain.model.EmploymentType
import com.employeeapp.domain.model.FilterState
import com.employeeapp.domain.model.SortOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var localFilter by remember { mutableStateOf(filterState) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filter Employees", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row {
                    TextButton(onClick = { localFilter = FilterState(); onFilterChange(FilterState()) }) {
                        Text("Clear All")
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // Department
            Text("Department", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Department.entries.forEach { dept ->
                    FilterChip(
                        selected = dept in localFilter.departments,
                        onClick = {
                            val newDepts = if (dept in localFilter.departments)
                                localFilter.departments - dept else localFilter.departments + dept
                            localFilter = localFilter.copy(departments = newDepts)
                            onFilterChange(localFilter)
                        },
                        label = { Text(dept.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Employment Type
            Text("Employment Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EmploymentType.entries.forEach { type ->
                    FilterChip(
                        selected = type in localFilter.employmentTypes,
                        onClick = {
                            val newTypes = if (type in localFilter.employmentTypes)
                                localFilter.employmentTypes - type else localFilter.employmentTypes + type
                            localFilter = localFilter.copy(employmentTypes = newTypes)
                            onFilterChange(localFilter)
                        },
                        label = { Text(type.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Status
            Text("Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null, true, false).forEach { active ->
                    val label = when (active) { null -> "All"; true -> "Active"; false -> "Inactive" }
                    FilterChip(
                        selected = localFilter.isActiveFilter == active,
                        onClick = {
                            localFilter = localFilter.copy(isActiveFilter = active)
                            onFilterChange(localFilter)
                        },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    val sortOptions = listOf(
        SortOption.NameAZ, SortOption.NameZA,
        SortOption.DateNewest, SortOption.DateOldest,
        SortOption.SalaryHigh, SortOption.SalaryLow
    )

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Sort By", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            sortOptions.forEach { option ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSort == option,
                        onClick = { onSortChange(option) }
                    )
                    Text(option.label, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveFiltersRow(filterState: FilterState, onClearAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filterState.departments.forEach { dept ->
                FilterChip(selected = true, onClick = {}, label = { Text(dept.displayName) })
            }
            filterState.employmentTypes.forEach { type ->
                FilterChip(selected = true, onClick = {}, label = { Text(type.displayName) })
            }
            filterState.isActiveFilter?.let { active ->
                FilterChip(selected = true, onClick = {}, label = { Text(if (active) "Active" else "Inactive") })
            }
        }
        TextButton(onClick = onClearAll) { Text("Clear") }
    }
}
