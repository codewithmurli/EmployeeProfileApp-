package com.employeeapp.data.repository

import com.employeeapp.data.db.AppDatabase
import com.employeeapp.data.db.toDomain
import com.employeeapp.data.db.toEntity
import com.employeeapp.domain.model.Employee
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EmployeeRepositoryImpl(
    private val db: AppDatabase
) : EmployeeRepository {

    private val dao = db.employeeDao()

    override fun getAllEmployees(): Flow<List<Employee>> =
        dao.getAllEmployees().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getEmployeeById(id: Long): Employee? =
        dao.getEmployeeById(id)?.toDomain()

    override suspend fun insertEmployee(employee: Employee): Long =
        dao.insertEmployee(employee.toEntity())

    override suspend fun updateEmployee(employee: Employee) =
        dao.updateEmployee(employee.toEntity())

    override suspend fun deleteEmployee(employee: Employee) =
        dao.deleteEmployee(employee.toEntity())

    override suspend fun getAllEmails(): List<String> =
        dao.getAllEmails()

    override suspend fun getAllNormalizedPhones(): List<String> =
        dao.getAllNormalizedPhones()
}
