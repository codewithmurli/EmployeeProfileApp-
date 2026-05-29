package com.employeeapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.datetime.Clock

@Entity(tableName = "employees")
@TypeConverters(Converters::class)
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val normalizedPhone: String,
    val address: String,
    val gender: String,
    val department: String,
    val skills: String,          // comma-separated
    val employmentType: String,
    val isActive: Boolean,
    val joiningDateEpochDay: Long,
    val salary: Double,
    val profileImagePath: String? = null,
    val resumePath: String? = null,
    val resumeName: String? = null,
    val resumeSize: Long? = null,
    val resumeMimeType: String? = null,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
)

class Converters {
    /**
     * Converts a List<String> to a comma-separated String for DB storage.
     * Time complexity: O(n) where n = number of skills
     */
    @TypeConverter
    fun fromSkillList(skills: List<String>): String = skills.joinToString(",")

    /**
     * Converts a comma-separated String back to List<String>.
     * Time complexity: O(n)
     */
    @TypeConverter
    fun toSkillList(skills: String): List<String> =
        if (skills.isBlank()) emptyList() else skills.split(",").map { it.trim() }
}
