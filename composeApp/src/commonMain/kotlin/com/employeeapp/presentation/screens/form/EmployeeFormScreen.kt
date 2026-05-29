package com.employeeapp.presentation.screens.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.employeeapp.domain.model.Department
import com.employeeapp.domain.model.DocumentInfo
import com.employeeapp.domain.model.EmploymentType
import com.employeeapp.domain.model.Gender
import com.employeeapp.domain.model.Skill
import com.employeeapp.platform.DocumentPickerButton
import com.employeeapp.platform.ImagePickerBottomSheet
import com.employeeapp.presentation.components.EmployeeAvatar
import com.employeeapp.presentation.components.formatDate
import com.employeeapp.presentation.viewmodel.EmployeeFormViewModel
import com.employeeapp.presentation.viewmodel.FormEvent
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EmployeeFormScreen(
    editEmployeeId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: EmployeeFormViewModel = koinViewModel { parametersOf(editEmployeeId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val events by viewModel.events.collectAsState()

    // ── Dialog / sheet visibility flags ─────────────────────────────────────
    var showDatePicker     by remember { mutableStateOf(false) }
    var showImagePicker    by remember { mutableStateOf(false) }
    var showDeptDropdown   by remember { mutableStateOf(false) }
    var showTypeDropdown   by remember { mutableStateOf(false) }

    // Navigate back on save success
    LaunchedEffect(events) {
        if (events is FormEvent.SaveSuccess) {
            viewModel.clearEvent()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editEmployeeId != null) "Edit Employee" else "New Employee",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Profile Image Avatar ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    EmployeeAvatar(
                        name = uiState.fullName.ifBlank { "?" },
                        imagePath = uiState.profileImagePath,
                        department = uiState.department?.displayName ?: "Engineering",
                        size = 88
                    )
                    // Camera overlay button
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .clip(CircleShape)
                            .clickable { showImagePicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Change photo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            val fieldModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)

            // ════════════════════════════════════════════════════════════════
            // SECTION: Personal Information
            // ════════════════════════════════════════════════════════════════
            SectionHeader("Personal Information")

            // Full Name
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = { Text("Full Name *") },
                isError = uiState.nameError != null,
                supportingText = {
                    uiState.nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = fieldModifier.onFocusChanged { if (!it.isFocused) viewModel.onFullNameBlur() }
            )
            Spacer(Modifier.height(8.dp))

            // Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email *") },
                isError = uiState.emailError != null,
                supportingText = {
                    uiState.emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = fieldModifier.onFocusChanged { if (!it.isFocused) viewModel.onEmailBlur() }
            )
            Spacer(Modifier.height(8.dp))

            // Phone
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Phone Number *") },
                isError = uiState.phoneError != null,
                supportingText = {
                    uiState.phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                prefix = { Text("+91 ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                modifier = fieldModifier.onFocusChanged { if (!it.isFocused) viewModel.onPhoneBlur() }
            )
            Spacer(Modifier.height(8.dp))

            // Address
            OutlinedTextField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Address *") },
                isError = uiState.addressError != null,
                supportingText = {
                    uiState.addressError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                minLines = 4,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = fieldModifier.onFocusChanged { if (!it.isFocused) viewModel.onAddressBlur() }
            )

            // ════════════════════════════════════════════════════════════════
            // SECTION: Employment Details
            // ════════════════════════════════════════════════════════════════
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
            SectionHeader("Employment Details")

            // Gender Radio Group
            Column(modifier = fieldModifier) {
                Text(
                    "Gender *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Gender.entries.forEach { gender ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onGenderChange(gender) }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = uiState.gender == gender,
                            onClick = { viewModel.onGenderChange(gender) }
                        )
                        Text(gender.displayName, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                uiState.genderError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))

            // Department ExposedDropdown
            ExposedDropdownMenuBox(
                expanded = showDeptDropdown,
                onExpandedChange = { showDeptDropdown = it },
                modifier = fieldModifier
            ) {
                OutlinedTextField(
                    value = uiState.department?.displayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Department *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showDeptDropdown) },
                    isError = uiState.departmentError != null,
                    supportingText = {
                        uiState.departmentError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showDeptDropdown,
                    onDismissRequest = { showDeptDropdown = false }
                ) {
                    Department.entries.forEach { dept ->
                        DropdownMenuItem(
                            text = { Text(dept.displayName) },
                            onClick = {
                                viewModel.onDepartmentChange(dept)
                                showDeptDropdown = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            // Employment Type ExposedDropdown
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = it },
                modifier = fieldModifier
            ) {
                OutlinedTextField(
                    value = uiState.employmentType?.displayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Employment Type *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showTypeDropdown) },
                    isError = uiState.employmentTypeError != null,
                    supportingText = {
                        uiState.employmentTypeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    EmploymentType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                viewModel.onEmploymentTypeChange(type)
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Skills Multi-select Chips
            Column(modifier = fieldModifier) {
                Text(
                    "Skills * (select at least 1)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Skill.entries.forEach { skill ->
                        FilterChip(
                            selected = skill in uiState.selectedSkills,
                            onClick = { viewModel.onSkillToggle(skill) },
                            label = { Text(skill.displayName) }
                        )
                    }
                }
                uiState.skillsError?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))

            // Is Active Toggle
            Row(
                modifier = fieldModifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Active Employee",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        if (uiState.isActive) "Currently active" else "Currently inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isActive,
                    onCheckedChange = viewModel::onIsActiveChange
                )
            }

            // ════════════════════════════════════════════════════════════════
            // SECTION: Compensation & Dates
            // ════════════════════════════════════════════════════════════════
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
            SectionHeader("Compensation & Dates")

            // Salary
            OutlinedTextField(
                value = uiState.salary,
                onValueChange = viewModel::onSalaryChange,
                label = { Text("Annual Salary *") },
                prefix = { Text("₹ ") },
                isError = uiState.salaryError != null,
                supportingText = {
                    uiState.salaryError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                modifier = fieldModifier.onFocusChanged { if (!it.isFocused) viewModel.onSalaryBlur() }
            )
            Spacer(Modifier.height(8.dp))

            // Joining Date
            // FIX: OutlinedTextField(readOnly) intercepts all touches internally.
            // Solution: disable the field (so it never steals clicks) then overlay
            // a transparent Box that reliably catches every tap and opens the dialog.
            Box(modifier = fieldModifier) {
                OutlinedTextField(
                    value = uiState.joiningDate?.let { formatDate(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Joining Date *") },
                    placeholder = { Text("DD MMM YYYY") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Pick date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = uiState.joiningDateError != null,
                    supportingText = {
                        uiState.joiningDateError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (uiState.joiningDateError != null)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outline,
                        disabledLabelColor = if (uiState.joiningDateError != null)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                // Transparent overlay — reliably captures every tap on the whole field area
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            // ════════════════════════════════════════════════════════════════
            // SECTION: Documents
            // ════════════════════════════════════════════════════════════════
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
            SectionHeader("Documents")

            DocumentUploadSection(
                document = uiState.document,
                error = uiState.documentError,
                onDocumentSelected = viewModel::onDocumentSelected,
                onDocumentRemoved = viewModel::onDocumentRemoved,
                modifier = fieldModifier
            )

            // ════════════════════════════════════════════════════════════════
            // Submit Button
            // ════════════════════════════════════════════════════════════════
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::submit,
                modifier = fieldModifier.height(56.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (editEmployeeId != null) "Update Employee" else "Add Employee",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    // ── Date Picker Dialog ───────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.joiningDate?.let {
                it.toEpochDays().toLong() * 86_400_000L
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val date = instant.toLocalDateTime(TimeZone.UTC).date
                        viewModel.onJoiningDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── Image Picker Bottom Sheet ────────────────────────────────────────────
    if (showImagePicker) {
        ImagePickerBottomSheet(
            onImageSelected = { path ->
                viewModel.onProfileImageSelected(path)
                showImagePicker = false
            },
            onDismiss = { showImagePicker = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Document Upload Section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DocumentUploadSection(
    document: DocumentInfo?,
    error: String?,
    onDocumentSelected: (DocumentInfo) -> Unit,
    onDocumentRemoved: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Resume (PDF / DOC / DOCX)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        if (document != null) {
            // Show selected document card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${document.size / 1024} KB • ${document.mimeType.substringAfterLast("/")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDocumentRemoved) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove document",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            // Show platform-specific document picker button
            DocumentPickerButton(onDocumentSelected = onDocumentSelected)
        }

        error?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
