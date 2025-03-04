package com.medicinereminder.presentation.navigation

sealed class Screen(val route: String) {
    object MedicineList : Screen("medicine_list")
    object AddMedicine : Screen("add_medicine")
    object MedicineDetails : Screen("medicine_details/{medicineId}") {
        fun createRoute(medicineId: String) = "medicine_details/$medicineId"
    }
} 