package com.employeeapp.data.repository

import com.employeeapp.domain.model.Employee
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {
    fun getAllEmployees(): Flow<List<Employee>>
    suspend fun getEmployeeById(id: Long): Employee?
    suspend fun insertEmployee(employee: Employee): Long
    suspend fun updateEmployee(employee: Employee)
    suspend fun deleteEmployee(employee: Employee)
    suspend fun getAllEmails(): List<String>
    suspend fun getAllNormalizedPhones(): List<String>
}
