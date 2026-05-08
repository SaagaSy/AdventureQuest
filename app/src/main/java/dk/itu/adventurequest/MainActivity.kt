package dk.itu.adventurequest

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dk.itu.adventurequest.ui.theme.AdventureQuestTheme
import android.location.Location
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // use jetpack compose + custom theme
            AdventureQuestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = colorScheme.background,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home", modifier = Modifier.size(32.dp)) },
                    label = { Text("Home", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    selected = currentRoute == "landing_screen",
                    alwaysShowLabel = true,
                    onClick = {
                        navController.navigate("landing_screen") {
                            popUpTo("landing_screen")
                            launchSingleTop = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorScheme.surface,
                        indicatorColor = colorScheme.primary,
                        selectedTextColor = colorScheme.primary,
                        unselectedIconColor = colorScheme.secondary,
                        unselectedTextColor = colorScheme.secondary
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.LocationOn, contentDescription = "Map", modifier = Modifier.size(32.dp)) },
                    label = { Text("Map", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    selected = currentRoute == "map_screen",
                    alwaysShowLabel = true,
                    onClick = {
                        navController.navigate("map_screen") {
                            popUpTo("landing_screen")
                            launchSingleTop = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorScheme.surface,
                        indicatorColor = colorScheme.primary,
                        selectedTextColor = colorScheme.primary,
                        unselectedIconColor = colorScheme.secondary,
                        unselectedTextColor = colorScheme.secondary
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "landing_screen",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("landing_screen") {
                LandingScreen(navController)
            }
            composable("map_screen") {
                AdventureQuestMap(navController)
            }
            composable("conversation_screen/{locationName}") {
                backStackEntry ->
                val locationName = backStackEntry.arguments?.getString("locationName")
                ConversationScreen(navController, locationName)
            }
        }
    }
}

// Helpers
fun calculateDistance(userLat: Double, userLng: Double,pinLat: Double, pinLng: Double): Float {
    val results = FloatArray(1)
    Location.distanceBetween(userLat, userLng, pinLat, pinLng, results)
    return results[0] // distance in meters
}
