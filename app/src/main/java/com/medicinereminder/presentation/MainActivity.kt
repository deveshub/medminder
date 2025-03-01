package com.medicinereminder.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.medicinereminder.presentation.navigation.Navigation
import com.medicinereminder.presentation.theme.MedicineReminderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedicineReminderTheme {
                val navController = rememberNavController()
                Navigation(navController = navController)
            }
        }
    }
} 