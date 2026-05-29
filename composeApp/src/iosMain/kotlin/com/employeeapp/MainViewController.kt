package com.employeeapp

import androidx.compose.ui.window.ComposeUIViewController
import com.employeeapp.di.commonModules
import com.employeeapp.di.iosModule
import com.employeeapp.presentation.AppNavigation
import com.employeeapp.presentation.theme.EmployeeAppTheme
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    startKoin {
        modules(commonModules + iosModule)
    }
    return ComposeUIViewController {
        EmployeeAppTheme {
            AppNavigation()
        }
    }
}
