package com.employeeapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Delete
    suspend fun deleteEmployee(employee: EmployeeEntity)

    @Query("SELECT * FROM employees ORDER BY fullName ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Long): EmployeeEntity?

    @Query("SELECT * FROM employees WHERE id = :id")
    fun getEmployeeByIdFlow(id: Long): Flow<EmployeeEntity?>

    @Query("""
        SELECT * FROM employees 
        WHERE (fullName LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR department LIKE '%' || :query || '%')
    """)
    fun searchEmployees(query: String): Flow<List<EmployeeEntity>>

    @Query("SELECT email FROM employees")
    suspend fun getAllEmails(): List<String>

    @Query("SELECT normalizedPhone FROM employees")
    suspend fun getAllNormalizedPhones(): List<String>

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int

    @Query("SELECT * FROM employees WHERE department = :department")
    fun getByDepartment(department: String): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees ORDER BY salary DESC LIMIT :limit")
    suspend fun getTopSalaries(limit: Int): List<EmployeeEntity>
}
