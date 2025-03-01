package com.medicinereminder.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.medicinereminder.presentation.medicines.MedicineListScreen
import com.medicinereminder.presentation.medicines.add.AddMedicineScreen
import com.medicinereminder.presentation.medicines.details.MedicineDetailsScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.MedicineList.route
    ) {
        composable(route = Screen.MedicineList.route) {
            MedicineListScreen(
                onNavigateToAddMedicine = {
                    navController.navigate(Screen.AddMedicine.route)
                },
                onNavigateToMedicineDetails = { medicineId ->
                    navController.navigate(Screen.MedicineDetails.route + "/$medicineId")
                }
            )
        }

        composable(route = Screen.AddMedicine.route) {
            AddMedicineScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.MedicineDetails.route + "/{medicineId}",
            arguments = listOf(
                navArgument("medicineId") {
                    type = NavType.StringType
                }
            )
        ) {
            MedicineDetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { medicineId ->
                    // TODO: Navigate to edit screen when implemented
                    // Will be implemented in the next feature
                }
            )
        }
    }
}