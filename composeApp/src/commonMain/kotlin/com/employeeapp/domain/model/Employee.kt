package com.employeeapp.domain.model

import kotlinx.datetime.LocalDate

data class Employee(
    val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val normalizedPhone: String,
    val address: String,
    val gender: Gender,
    val department: Department,
    val skills: List<Skill>,
    val employmentType: EmploymentType,
    val isActive: Boolean,
    val joiningDate: LocalDate,
    val salary: Double,
    val profileImagePath: String? = null,
    val resumePath: String? = null,
    val resumeName: String? = null,
    val resumeSize: Long? = null,
    val resumeMimeType: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    PREFER_NOT_TO_SAY("Prefer not to say")
}

enum class Department(val displayName: String) {
    ENGINEERING("Engineering"),
    HR("HR"),
    SALES("Sales"),
    FINANCE("Finance"),
    DESIGN("Design"),
    OPS("Ops")
}

enum class Skill(val displayName: String) {
    KOTLIN("Kotlin"),
    JAVA("Java"),
    SWIFT("Swift"),
    PYTHON("Python"),
    JAVASCRIPT("JavaScript"),
    TYPESCRIPT("TypeScript"),
    REACT("React"),
    FLUTTER("Flutter"),
    COMPOSE("Compose"),
    SQL("SQL"),
    DOCKER("Docker"),
    AWS("AWS"),
    FIGMA("Figma"),
    MACHINE_LEARNING("Machine Learning"),
    DEVOPS("DevOps")
}

enum class EmploymentType(val displayName: String) {
    FULL_TIME("Full-Time"),
    PART_TIME("Part-Time"),
    CONTRACT("Contract")
}

sealed class SortOption(val label: String) {
    data object NameAZ : SortOption("Name A → Z")
    data object NameZA : SortOption("Name Z → A")
    data object DateNewest : SortOption("Joining Date: Newest")
    data object DateOldest : SortOption("Joining Date: Oldest")
    data object SalaryHigh : SortOption("Salary: High → Low")
    data object SalaryLow : SortOption("Salary: Low → High")
}

data class FilterState(
    val departments: Set<Department> = emptySet(),
    val employmentTypes: Set<EmploymentType> = emptySet(),
    val isActiveFilter: Boolean? = null
) {
    val activeFilterCount: Int
        get() = departments.size +
                employmentTypes.size +
                if (isActiveFilter != null) 1 else 0

    val hasFilters: Boolean get() = activeFilterCount > 0
}

data class DocumentInfo(
    val path: String,
    val name: String,
    val size: Long,
    val mimeType: String
)
