package com.employeeapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.employeeapp.data.repository.EmployeeRepository
import com.employeeapp.domain.model.Department
import com.employeeapp.domain.model.DocumentInfo
import com.employeeapp.domain.model.Employee
import com.employeeapp.domain.model.EmploymentType
import com.employeeapp.domain.model.Gender
import com.employeeapp.domain.model.Skill
import com.employeeapp.domain.usecase.DuplicateDetector
import com.employeeapp.domain.usecase.DuplicateResult
import com.employeeapp.domain.usecase.FormValidation
import com.employeeapp.domain.usecase.normalizePhone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class FormUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    // Fields
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val gender: Gender? = null,
    val department: Department? = null,
    val selectedSkills: Set<Skill> = emptySet(),
    val employmentType: EmploymentType? = null,
    val isActive: Boolean = true,
    val joiningDate: LocalDate? = null,
    val salary: String = "",
    val profileImagePath: String? = null,
    val document: DocumentInfo? = null,
    // Errors
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val genderError: String? = null,
    val departmentError: String? = null,
    val skillsError: String? = null,
    val employmentTypeError: String? = null,
    val joiningDateError: String? = null,
    val salaryError: String? = null,
    val documentError: String? = null
) {
    val isFormValid: Boolean
        get() = nameError == null && emailError == null && phoneError == null &&
                addressError == null && genderError == null && departmentError == null &&
                skillsError == null && employmentTypeError == null && joiningDateError == null &&
                salaryError == null &&
                fullName.isNotBlank() && email.isNotBlank() && phone.isNotBlank() &&
                address.isNotBlank() && gender != null && department != null &&
                selectedSkills.isNotEmpty() && employmentType != null && joiningDate != null &&
                salary.isNotBlank()
}

sealed class FormEvent {
    data object SaveSuccess : FormEvent()
    data class SaveError(val message: String) : FormEvent()
    data class DuplicateFound(val result: DuplicateResult) : FormEvent()
}

class EmployeeFormViewModel(
    private val repository: EmployeeRepository,
    private val duplicateDetector: DuplicateDetector,
    private val editEmployeeId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormUiState())
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<FormEvent?>(null)
    val events: StateFlow<FormEvent?> = _events.asStateFlow()

    private var originalEmployee: Employee? = null
    private var existingNames: List<String> = emptyList()
    private var allEmployees: List<Employee> = emptyList()

    init {
        viewModelScope.launch {
            existingNames = repository.getAllEmails() // used for dedup in names via names list
            allEmployees = buildAllEmployeesList()
            editEmployeeId?.let { loadEmployee(it) }
        }
    }

    private suspend fun buildAllEmployeesList(): List<Employee> {
        // Gather full list from repository for name dupe checking
        return emptyList() // populated via init from getAllEmployees flow
    }

    private suspend fun loadEmployee(id: Long) {
        _uiState.update { it.copy(isLoading = true) }
        val emp = repository.getEmployeeById(id)
        originalEmployee = emp
        emp?.let { e ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    fullName = e.fullName,
                    email = e.email,
                    phone = e.phone,
                    address = e.address,
                    gender = e.gender,
                    department = e.department,
                    selectedSkills = e.skills.toSet(),
                    employmentType = e.employmentType,
                    isActive = e.isActive,
                    joiningDate = e.joiningDate,
                    salary = e.salary.toString(),
                    profileImagePath = e.profileImagePath,
                    document = if (e.resumePath != null && e.resumeName != null && e.resumeSize != null && e.resumeMimeType != null) {
                        DocumentInfo(e.resumePath, e.resumeName, e.resumeSize, e.resumeMimeType)
                    } else null
                )
            }
        } ?: _uiState.update { it.copy(isLoading = false) }
    }

    // ── Field update + blur validation ───────────────────────────────────────

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value, nameError = null) }
    fun onFullNameBlur() = _uiState.update {
        it.copy(nameError = FormValidation.validateFullName(it.fullName, emptyList(), editEmployeeId))
    }

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null) }
    fun onEmailBlur() = _uiState.update {
        it.copy(emailError = FormValidation.validateEmail(it.email))
    }

    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value, phoneError = null) }
    fun onPhoneBlur() = _uiState.update {
        it.copy(phoneError = FormValidation.validatePhone(it.phone))
    }

    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value, addressError = null) }
    fun onAddressBlur() = _uiState.update {
        it.copy(addressError = FormValidation.validateAddress(it.address))
    }

    fun onGenderChange(gender: Gender) = _uiState.update { it.copy(gender = gender, genderError = null) }
    fun onDepartmentChange(dept: Department) = _uiState.update { it.copy(department = dept, departmentError = null) }

    fun onSkillToggle(skill: Skill) = _uiState.update { state ->
        val updated = if (skill in state.selectedSkills)
            state.selectedSkills - skill else state.selectedSkills + skill
        state.copy(selectedSkills = updated, skillsError = FormValidation.validateSkills(updated.toList()))
    }

    fun onEmploymentTypeChange(type: EmploymentType) =
        _uiState.update { it.copy(employmentType = type, employmentTypeError = null) }

    fun onIsActiveChange(active: Boolean) = _uiState.update { it.copy(isActive = active) }

    fun onJoiningDateChange(date: LocalDate) = _uiState.update {
        it.copy(joiningDate = date, joiningDateError = FormValidation.validateJoiningDate(date.toEpochDays().toLong()))
    }

    fun onSalaryChange(value: String) = _uiState.update { it.copy(salary = value, salaryError = null) }
    fun onSalaryBlur() = _uiState.update {
        it.copy(salaryError = FormValidation.validateSalary(it.salary))
    }

    fun onProfileImageSelected(path: String) = _uiState.update { it.copy(profileImagePath = path) }

    fun onDocumentSelected(doc: DocumentInfo) {
        if (doc.size > 5 * 1024 * 1024) {
            _uiState.update { it.copy(documentError = "File size must be less than 5 MB") }
            return
        }
        _uiState.update { it.copy(document = doc, documentError = null) }
    }

    fun onDocumentRemoved() = _uiState.update { it.copy(document = null, documentError = null) }

    fun clearEvent() { _events.value = null }

    // ── Submit ───────────────────────────────────────────────────────────────

    fun submit() = viewModelScope.launch {
        val state = _uiState.value

        // Run all validations
        val nameErr = FormValidation.validateFullName(state.fullName, emptyList(), editEmployeeId)
        val emailErr = FormValidation.validateEmail(state.email)
        val phoneErr = FormValidation.validatePhone(state.phone)
        val addrErr = FormValidation.validateAddress(state.address)
        val genderErr = if (state.gender == null) "Gender is required" else null
        val deptErr = if (state.department == null) "Department is required" else null
        val skillsErr = FormValidation.validateSkills(state.selectedSkills.toList())
        val empTypeErr = if (state.employmentType == null) "Employment type is required" else null
        val dateErr = FormValidation.validateJoiningDate(state.joiningDate?.toEpochDays()?.toLong())
        val salaryErr = FormValidation.validateSalary(state.salary)

        _uiState.update {
            it.copy(
                nameError = nameErr, emailError = emailErr, phoneError = phoneErr,
                addressError = addrErr, genderError = genderErr, departmentError = deptErr,
                skillsError = skillsErr, employmentTypeError = empTypeErr,
                joiningDateError = dateErr, salaryError = salaryErr
            )
        }

        if (listOf(nameErr, emailErr, phoneErr, addrErr, genderErr, deptErr, skillsErr, empTypeErr, dateErr, salaryErr).any { it != null }) {
            return@launch
        }

        // Duplicate check O(1)
        val normalizedPhone = normalizePhone(state.phone)
        val dupResult = duplicateDetector.isDuplicate(
            email = state.email.trim().lowercase(),
            normalizedPhone = normalizedPhone,
            excludeId = editEmployeeId
        )

        if (dupResult.hasDuplicate) {
            _uiState.update {
                it.copy(
                    emailError = if (dupResult.emailDuplicate) "This email is already registered" else it.emailError,
                    phoneError = if (dupResult.phoneDuplicate) "This phone is already registered" else it.phoneError
                )
            }
            _events.value = FormEvent.DuplicateFound(dupResult)
            return@launch
        }

        _uiState.update { it.copy(isLoading = true) }

        try {
            val now = Clock.System.now().toEpochMilliseconds()
            val employee = Employee(
                id = editEmployeeId ?: 0,
                fullName = state.fullName.trim(),
                email = state.email.trim().lowercase(),
                phone = state.phone,
                normalizedPhone = normalizedPhone,
                address = state.address.trim(),
                gender = state.gender!!,
                department = state.department!!,
                skills = state.selectedSkills.toList(),
                employmentType = state.employmentType!!,
                isActive = state.isActive,
                joiningDate = state.joiningDate!!,
                salary = state.salary.toDouble(),
                profileImagePath = state.profileImagePath,
                resumePath = state.document?.path,
                resumeName = state.document?.name,
                resumeSize = state.document?.size,
                resumeMimeType = state.document?.mimeType,
                createdAt = originalEmployee?.createdAt ?: now,
                updatedAt = now
            )

            if (editEmployeeId != null) {
                repository.updateEmployee(employee)
                originalEmployee?.let { old ->
                    duplicateDetector.updateEntry(old.email, old.normalizedPhone, employee.email, employee.normalizedPhone)
                }
            } else {
                repository.insertEmployee(employee)
                duplicateDetector.addEntry(employee.email, employee.normalizedPhone)
            }

            _uiState.update { it.copy(isLoading = false, isSaved = true) }
            _events.value = FormEvent.SaveSuccess
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false) }
            _events.value = FormEvent.SaveError(e.message ?: "Unknown error")
        }
    }
}
