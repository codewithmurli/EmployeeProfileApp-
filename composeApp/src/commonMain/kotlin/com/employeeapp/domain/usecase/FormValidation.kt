package com.employeeapp.domain.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object FormValidation {

    fun validateFullName(name: String, existingNames: List<String> = emptyList(), currentId: Long? = null): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> "Full name is required"
            trimmed.length < 3 -> "Name must be at least 3 characters"
            existingNames.any { it.equals(trimmed, ignoreCase = true) } &&
                    currentId == null -> "Employee with this name already exists"
            else -> null
        }
    }

    fun validateEmail(email: String): String? {
        val trimmed = email.trim().lowercase()
        val emailRegex = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
        return when {
            trimmed.isBlank() -> "Email is required"
            !emailRegex.matches(trimmed) -> "Enter a valid email address"
            else -> null
        }
    }

    fun validatePhone(phone: String): String? {
        val digits = phone.replace(Regex("[^0-9]"), "")
        return when {
            phone.isBlank() -> "Phone number is required"
            digits.length != 10 -> "Phone must be exactly 10 digits"
            else -> null
        }
    }

    fun validateAddress(address: String): String? {
        return when {
            address.isBlank() -> "Address is required"
            address.trim().length < 6 -> "Address must be at least 6 characters"
            else -> null
        }
    }

    fun validateSalary(salary: String): String? {
        val value = salary.toDoubleOrNull()
        return when {
            salary.isBlank() -> "Salary is required"
            value == null -> "Enter a valid salary"
            value <= 0 -> "Salary must be positive"
            else -> null
        }
    }

    fun validateSkills(skills: List<*>): String? =
        if (skills.isEmpty()) "Select at least one skill" else null

    fun validateJoiningDate(dateEpochDay: Long?): String? {
        if (dateEpochDay == null) return "Joining date is required"
        // Fix: use Clock.System.now().toLocalDateTime() instead of deprecated todayIn
        val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayEpochDay = todayDate.toEpochDays().toLong()
        return if (dateEpochDay > todayEpochDay) "Joining date cannot be in the future" else null
    }
}
