package com.medicinereminder.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.medicinereminder.presentation.medicines.MedicineListScreen

@Composable
fun Navigation(navController: NavHostController) {
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
                    navController.navigate(Screen.MedicineDetails.createRoute(medicineId))
                }
            )
        }

        composable(route = Screen.AddMedicine.route) {
            // TODO: Implement AddMedicineScreen
        }

        composable(
            route = Screen.MedicineDetails.route,
            arguments = listOf(
                navArgument("medicineId") {
                    type = NavType.StringType
                }
            )
        ) {
            // TODO: Implement MedicineDetailsScreen
        }
    }
}