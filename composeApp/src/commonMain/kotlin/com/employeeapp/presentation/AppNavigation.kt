package com.employeeapp.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.employeeapp.presentation.screens.employees.EmployeeDetailScreen
import com.employeeapp.presentation.screens.employees.EmployeeListScreen
import com.employeeapp.presentation.screens.form.EmployeeFormScreen
import com.employeeapp.presentation.screens.topearners.TopEarnersScreen

sealed class Screen(val route: String) {
    data object EmployeeList : Screen("employee_list")
    data object AddEmployee  : Screen("add_employee")
    data object EditEmployee : Screen("edit_employee/{employeeId}") {
        fun createRoute(id: Long) = "edit_employee/$id"
    }
    data object EmployeeDetail : Screen("employee_detail/{employeeId}") {
        fun createRoute(id: Long) = "employee_detail/$id"
    }
    data object TopEarners : Screen("top_earners")
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // SharedTransitionLayout enables shared element transitions between screens
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Screen.EmployeeList.route
        ) {
            composable(
                route = Screen.EmployeeList.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                EmployeeListScreen(
                    onAddEmployee  = { navController.navigate(Screen.AddEmployee.route) },
                    onEditEmployee = { id -> navController.navigate(Screen.EditEmployee.createRoute(id)) },
                    onViewEmployee = { id -> navController.navigate(Screen.EmployeeDetail.createRoute(id)) },
                    onTopEarners   = { navController.navigate(Screen.TopEarners.route) }
                )
            }

            composable(
                route = Screen.AddEmployee.route,
                enterTransition = { slideInHorizontally { it } + fadeIn() },
                exitTransition  = { slideOutHorizontally { it } + fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition  = { slideOutHorizontally { it } + fadeOut() }
            ) {
                EmployeeFormScreen(
                    editEmployeeId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditEmployee.route,
                arguments = listOf(navArgument("employeeId") { type = NavType.LongType }),
                enterTransition = { slideInHorizontally { it } + fadeIn() },
                exitTransition  = { slideOutHorizontally { it } + fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition  = { slideOutHorizontally { it } + fadeOut() }
            ) { back ->
                val id = back.arguments?.getLong("employeeId")
                EmployeeFormScreen(
                    editEmployeeId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EmployeeDetail.route,
                arguments = listOf(navArgument("employeeId") { type = NavType.LongType }),
                // Shared element transition: card slides up and fades in
                enterTransition = { slideInHorizontally { it / 2 } + fadeIn() },
                exitTransition  = { slideOutHorizontally { it / 2 } + fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition  = { slideOutHorizontally { it } + fadeOut() }
            ) { back ->
                val id = back.arguments?.getLong("employeeId") ?: return@composable
                EmployeeDetailScreen(
                    employeeId     = id,
                    onNavigateBack = { navController.popBackStack() },
                    onEdit         = { empId -> navController.navigate(Screen.EditEmployee.createRoute(empId)) },
                    animatedVisibilityScope = this
                )
            }

            composable(
                route = Screen.TopEarners.route,
                enterTransition = { slideInHorizontally { it } + fadeIn() },
                exitTransition  = { slideOutHorizontally { it } + fadeOut() }
            ) {
                TopEarnersScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
