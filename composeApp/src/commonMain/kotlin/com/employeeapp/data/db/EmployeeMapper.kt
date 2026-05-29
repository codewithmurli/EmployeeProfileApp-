package com.employeeapp.data.db

import com.employeeapp.domain.model.Department
import com.employeeapp.domain.model.Employee
import com.employeeapp.domain.model.EmploymentType
import com.employeeapp.domain.model.Gender
import com.employeeapp.domain.model.Skill
import kotlinx.datetime.LocalDate

fun EmployeeEntity.toDomain(): Employee = Employee(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    normalizedPhone = normalizedPhone,
    address = address,
    gender = Gender.valueOf(gender),
    department = Department.valueOf(department),
    skills = skills.split(",").filter { it.isNotBlank() }.map { Skill.valueOf(it.trim()) },
    employmentType = EmploymentType.valueOf(employmentType),
    isActive = isActive,
    joiningDate = LocalDate.fromEpochDays(joiningDateEpochDay.toInt()),
    salary = salary,
    profileImagePath = profileImagePath,
    resumePath = resumePath,
    resumeName = resumeName,
    resumeSize = resumeSize,
    resumeMimeType = resumeMimeType,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Employee.toEntity(): EmployeeEntity = EmployeeEntity(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    normalizedPhone = normalizedPhone,
    address = address,
    gender = gender.name,
    department = department.name,
    skills = skills.joinToString(",") { it.name },
    employmentType = employmentType.name,
    isActive = isActive,
    joiningDateEpochDay = joiningDate.toEpochDays().toLong(),
    salary = salary,
    profileImagePath = profileImagePath,
    resumePath = resumePath,
    resumeName = resumeName,
    resumeSize = resumeSize,
    resumeMimeType = resumeMimeType,
    createdAt = createdAt,
    updatedAt = updatedAt
)
