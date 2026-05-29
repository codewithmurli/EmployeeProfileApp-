package com.employeeapp.presentation.screens.employees

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.employeeapp.domain.model.Employee
import com.employeeapp.domain.model.FilterState
import com.employeeapp.presentation.components.EmployeeCard
import com.employeeapp.presentation.components.EmployeeListShimmer
import com.employeeapp.presentation.components.EmptyState
import com.employeeapp.presentation.components.ErrorState
import com.employeeapp.presentation.components.NoSearchResultsState
import com.employeeapp.presentation.viewmodel.EmployeeListViewModel
import com.employeeapp.presentation.viewmodel.ListUiState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.compose.viewmodel.koinViewModel

private const val PAGE_SIZE = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    onAddEmployee: () -> Unit,
    onEditEmployee: (Long) -> Unit,
    onViewEmployee: (Long) -> Unit,
    onTopEarners: () -> Unit,
    viewModel: EmployeeListViewModel = koinViewModel()
) {
    val uiState     by viewModel.listUiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val sortOption  by viewModel.sortOption.collectAsState()
    val undoEvent   by viewModel.undoEvent.collectAsState()
    val visiblePage by viewModel.visiblePage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet   by remember { mutableStateOf(false) }
    var deleteTarget    by remember { mutableStateOf<Employee?>(null) }

    // Undo snackbar
    LaunchedEffect(undoEvent) {
        undoEvent?.let { event ->
            val result = snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
            else viewModel.dismissUndo()
        }
    }

    // Lazy list state for pagination
    val listState = rememberLazyListState()

    // Detect when user is near bottom → load next page
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { info ->
                val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                val total = info.totalItemsCount
                total > 0 && lastVisible >= total - 5
            }
            .distinctUntilChanged()
            .collect { nearBottom ->
                if (nearBottom) viewModel.loadNextPage()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Employees",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        val count = (uiState as? ListUiState.Success)?.employees?.size ?: 0
                        Text(
                            "$count members",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onTopEarners) {
                        Icon(Icons.Default.WorkspacePremium, "Top Earners",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(Icons.Default.Sort, "Sort")
                    }
                    BadgedBox(badge = {
                        if (filterState.activeFilterCount > 0) {
                            Badge { Text("${filterState.activeFilterCount}") }
                        }
                    }) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, "Filter")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEmployee,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Employee",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Search bar
            DockedSearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = viewModel::updateSearchQuery,
                        onSearch = { /* dismiss keyboard */ },
                        expanded = false,           // always false — no overlay
                        onExpandedChange = {},      // ignore expand requests
                        placeholder = { Text("Search name, email, department…") },
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                },
                expanded = false,                   // never expand
                onExpandedChange = {},              // ignore
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                content = {}
            )

            // Active filter chips
            AnimatedVisibility(visible = filterState.hasFilters) {
                ActiveFiltersRow(filterState = filterState) {
                    viewModel.updateFilter(FilterState())
                }
            }

            // Content
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "list_content"
            ) { state ->
                when (state) {
                    is ListUiState.Loading -> EmployeeListShimmer()
                    is ListUiState.Empty   -> EmptyState(onAction = onAddEmployee)
                    is ListUiState.Error   -> ErrorState(state.message)
                    is ListUiState.Success -> {
                        if (state.employees.isEmpty()) {
                            NoSearchResultsState(searchQuery)
                        } else {
                            // ── Cursor-based pagination: show only first PAGE_SIZE * visiblePage items
                            val paginated = state.employees.take(PAGE_SIZE * visiblePage)
                            val hasMore   = paginated.size < state.employees.size

                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(
                                    items = paginated,
                                    key = { it.id }
                                ) { emp ->
                                    EmployeeCard(
                                        employee = emp,
                                        searchQuery = searchQuery,
                                        onEdit = { onEditEmployee(emp.id) },
                                        onDelete = { deleteTarget = emp },
                                        onViewDetails = { onViewEmployee(emp.id) }
                                    )
                                }

                                // ── Pagination loading indicator at bottom ──
                                if (hasMore) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    deleteTarget?.let { emp ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Employee") },
            text = { Text("Are you sure you want to delete ${emp.fullName}? This can be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteEmployee(emp); deleteTarget = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filterState = filterState,
            onFilterChange = viewModel::updateFilter,
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showSortSheet) {
        SortBottomSheet(
            currentSort = sortOption,
            onSortChange = { viewModel.updateSort(it); showSortSheet = false },
            onDismiss = { showSortSheet = false }
        )
    }
}
