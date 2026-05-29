package com.employeeapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.employeeapp.data.repository.EmployeeRepository
import com.employeeapp.domain.model.Department
import com.employeeapp.domain.model.Employee
import com.employeeapp.domain.model.FilterState
import com.employeeapp.domain.model.SortOption
import com.employeeapp.domain.usecase.DuplicateDetector
import com.employeeapp.domain.usecase.UndoDeleteStack
import com.employeeapp.domain.usecase.topNBySalary
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ListUiState {
    data object Loading : ListUiState()
    data class Success(val employees: List<Employee>) : ListUiState()
    data class Error(val message: String) : ListUiState()
    data object Empty : ListUiState()
}

data class UndoEvent(val employee: Employee, val message: String)

@OptIn(FlowPreview::class)
class EmployeeListViewModel(
    private val repository: EmployeeRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val filterState = MutableStateFlow(FilterState())
    val sortOption  = MutableStateFlow<SortOption>(SortOption.NameAZ)
    val topNCount   = MutableStateFlow(5)

    // Cursor-based pagination — page size = 20
    val visiblePage = MutableStateFlow(1)

    private val undoStack = UndoDeleteStack()
    val undoEvent = MutableStateFlow<UndoEvent?>(null)

    val duplicateDetector = DuplicateDetector()

    private val _allEmployees = repository.getAllEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Reactive: combine raw list + search + filter + sort
    val listUiState: StateFlow<ListUiState> = combine(
        _allEmployees,
        searchQuery.debounce(300),
        filterState,
        sortOption
    ) { employees, query, filters, sort ->
        duplicateDetector.initialise(employees)
        val filtered = employees
            .filter { matchesSearch(it, query) }
            .filter { matchesFilter(it, filters) }
            .sortedWith(sortComparator(sort))

        when {
            employees.isEmpty() -> ListUiState.Empty
            else -> ListUiState.Success(filtered)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListUiState.Loading)

    val topEarners: StateFlow<List<Employee>> = combine(_allEmployees, topNCount) { emps, n ->
        topNBySalary(emps, n)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val departmentSummary: StateFlow<Map<Department, Int>> = combine(
        _allEmployees,
        MutableStateFlow(Unit)
    ) { emps, _ ->
        emps.groupingBy { it.department }.eachCount()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // ── Pagination ────────────────────────────────────────────────────────────
    /** Called when LazyColumn detects the user is near the bottom */
    fun loadNextPage() {
        visiblePage.value = visiblePage.value + 1
    }

    /** Reset pagination when filter/search changes */
    init {
        viewModelScope.launch {
            combine(searchQuery, filterState, sortOption) { _, _, _ -> Unit }
                .collect { visiblePage.value = 1 }
        }
    }

    // ── Delete + Undo ─────────────────────────────────────────────────────────
    fun deleteEmployee(employee: Employee) = viewModelScope.launch {
        try {
            repository.deleteEmployee(employee)
            undoStack.push(employee)
            duplicateDetector.removeEntry(employee.email, employee.normalizedPhone)
            undoEvent.value = UndoEvent(employee, "${employee.fullName} deleted")
        } catch (_: Exception) { }
    }

    fun undoDelete() = viewModelScope.launch {
        val emp = undoStack.pop() ?: return@launch
        try {
            repository.insertEmployee(emp.copy(id = 0))
            undoEvent.value = null
        } catch (_: Exception) { }
    }

    fun dismissUndo() { undoEvent.value = null }

    // ── State updates ─────────────────────────────────────────────────────────
    fun updateSearchQuery(query: String) { searchQuery.value = query }
    fun updateFilter(filter: FilterState) { filterState.value = filter }
    fun updateSort(sort: SortOption) { sortOption.value = sort }
    fun updateTopN(n: Int) { topNCount.value = n.coerceIn(1, 10) }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun matchesSearch(emp: Employee, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.lowercase()
        return emp.fullName.lowercase().contains(q) ||
                emp.email.lowercase().contains(q) ||
                emp.department.displayName.lowercase().contains(q)
    }

    private fun matchesFilter(emp: Employee, filters: FilterState): Boolean {
        if (!filters.hasFilters) return true
        val deptOk   = filters.departments.isEmpty() || emp.department in filters.departments
        val typeOk   = filters.employmentTypes.isEmpty() || emp.employmentType in filters.employmentTypes
        val activeOk = filters.isActiveFilter == null || emp.isActive == filters.isActiveFilter
        return deptOk && typeOk && activeOk
    }

    private fun sortComparator(sort: SortOption): Comparator<Employee> = when (sort) {
        SortOption.NameAZ     -> compareBy { it.fullName.lowercase() }
        SortOption.NameZA     -> compareByDescending { it.fullName.lowercase() }
        SortOption.DateNewest -> compareByDescending { it.joiningDate }
        SortOption.DateOldest -> compareBy { it.joiningDate }
        SortOption.SalaryHigh -> compareByDescending { it.salary }
        SortOption.SalaryLow  -> compareBy { it.salary }
    }
}
