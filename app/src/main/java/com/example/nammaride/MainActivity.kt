package com.example.nammaride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth

// Import our newly organized files!
import com.example.nammaride.theme.DarkThemeColors
import com.example.nammaride.theme.LightThemeColors
import com.example.nammaride.theme.LocalNammaColors
import com.example.nammaride.auth.PhoneAuthScreen
import com.example.nammaride.home.MainAppScreen

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            // Theme Engine
            var isDarkMode by remember { mutableStateOf(true) }
            val currentColors = if (isDarkMode) DarkThemeColors else LightThemeColors

            CompositionLocalProvider(LocalNammaColors provides currentColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = currentColors.background) {

                    // Top-Level Application Router
                    var isAuthenticated by remember { mutableStateOf(false) }

                    if (isAuthenticated) {
                        MainAppScreen(
                            isDarkMode = isDarkMode,
                            onThemeChange = { isDarkMode = it },
                            onLogout = {
                                auth.signOut() // Kill Firebase Session
                                isAuthenticated = false // Send back to login
                            }
                        )
                    } else {
                        PhoneAuthScreen(
                            auth = auth,
                            onLoginSuccess = {
                                isAuthenticated = true // Trigger the jump to Dashboard
                            }
                        )
                    }
                }
            }
        }
    }
}