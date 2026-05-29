package com.employeeapp.di

import com.employeeapp.data.repository.EmployeeRepository
import com.employeeapp.data.repository.EmployeeRepositoryImpl
import com.employeeapp.domain.usecase.DuplicateDetector
import com.employeeapp.presentation.viewmodel.EmployeeFormViewModel
import com.employeeapp.presentation.viewmodel.EmployeeListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val repositoryModule = module {
    single<EmployeeRepository> { EmployeeRepositoryImpl(get()) }
}

val domainModule = module {
    // DuplicateDetector is singleton — shared across ViewModels
    single { DuplicateDetector() }
}

val viewModelModule = module {
    viewModelOf(::EmployeeListViewModel)

    // EmployeeFormViewModel takes optional editEmployeeId param
    viewModel { params ->
        EmployeeFormViewModel(
            repository = get(),
            duplicateDetector = get(),
            editEmployeeId = params.getOrNull()
        )
    }
}

// All common modules bundled together
val commonModules = listOf(repositoryModule, domainModule, viewModelModule)
