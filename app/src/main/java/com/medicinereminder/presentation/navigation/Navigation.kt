package com.medicinereminder.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.medicinereminder.presentation.medicines.list.MedicineListScreen
import com.medicinereminder.presentation.medicines.add.AddMedicineScreen
import com.medicinereminder.presentation.medicines.details.MedicineDetailsScreen
import com.medicinereminder.presentation.settings.SettingsScreen

@Composable
fun Navigation(
    navController: NavHostController,
    startDestination: String = Screen.MedicineList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.MedicineList.route) {
            MedicineListScreen(
                onNavigateToAdd = { navController.navigate(Screen.AddMedicine.route) },
                onNavigateToDetails = { medicineId -> 
                    navController.navigate("medicine_details/$medicineId")
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(route = Screen.AddMedicine.route) {
            AddMedicineScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "medicine_details/{medicineId}",
            arguments = listOf(
                navArgument("medicineId") { type = NavType.StringType }
            )
        ) { entry ->
            val id = entry.arguments?.getString("medicineId") ?: ""
            MedicineDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { medicineId -> 
                    navController.navigate(Screen.AddMedicine.route)
                }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen()
        }
    }
}