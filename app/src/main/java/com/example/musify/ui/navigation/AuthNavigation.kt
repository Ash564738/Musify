package com.example.musify.ui.navigation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musify.viewmodels.AuthViewModel
import com.example.musify.ui.screens.authscreen.AuthScreen
import com.example.musify.ui.screens.authscreen.LoginScreen
import com.example.musify.ui.screens.authscreen.LogupScreen
@Composable
fun AuthNavigation(onLoginSuccess: () -> Unit) {
    val navController = rememberNavController()
    val viewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "auth_options"
    ) {
        composable("auth_options") {
            AuthScreen(
                onLoginClick = { navController.navigate("login") },
                onSignupClick = { navController.navigate("logup") }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onBackClick = { navController.popBackStack() },
                onSignupClick = { navController.navigate("logup") }
            )
        }

        composable("logup") {
            LogupScreen(
                onLogupSuccess = onLoginSuccess,
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate("login") }
            )
        }
    }
}