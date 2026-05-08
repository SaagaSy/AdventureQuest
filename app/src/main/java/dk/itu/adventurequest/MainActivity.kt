package dk.itu.adventurequest

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import android.content.Intent
import android.net.Uri
import dk.itu.adventurequest.BuildConfig
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.clustering.Clustering
import dk.itu.adventurequest.ui.theme.AdventureQuestTheme
import android.location.Location
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.core.net.toUri

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
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home", fontWeight = FontWeight.Bold) },
                    selected = currentRoute == "landing_screen",
                    onClick = {
                        navController.navigate("landing_screen") {
                            popUpTo("landing_screen")
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.LocationOn, contentDescription = "Map") },
                    label = { Text("Map", fontWeight = FontWeight.Bold) },
                    selected = currentRoute == "map_screen",
                    onClick = {
                        navController.navigate("map_screen") {
                            popUpTo("landing_screen")
                            launchSingleTop = true
                        }
                    }
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

@SuppressLint("MissingPermission")  // check permissions manually
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureQuestMap(navController: NavController) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current    // phone's vibration

    // get Google's location engine for later use for GPS
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var selectedPin by remember { mutableStateOf<PinItem?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // track and ask permissions
    var hasLocationPermission by remember { mutableStateOf(false) }

    //store user's location to draw route
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    val mapItems = remember {
        listOf(
            PinItem(LatLng(55.7949, 12.5714), "Eremitagen", "Advanced Route - Nature Trails", "Conversation"),
            PinItem(LatLng(55.6811, 12.5404), "Landbohøjskolens Have", "Accessible Route - Flat & Paved", "Conversation"),
            PinItem(LatLng(55.7005, 12.5448), "Superkilen Park", "Accessible Route - Flat & Paved", "Conversation"),
            PinItem(LatLng(55.6418, 12.6543), "Kastrup Søbad", "Accessible Route - Wooden Decking", "Conversation"),
            PinItem(LatLng(55.6534, 12.5932), "Grønjordssøen Udkigspunkt", "Advanced Route - Uneven Ground", "Conversation"),
            PinItem(LatLng(55.6111, 12.5256), "Kalvebod Fælled Tower", "Advanced Route - Nature Trails", "Conversation"),
            PinItem(LatLng(55.6433, 12.5516), "Naturpark Amager", "Accessible Route - Flat Paths", "Conversation")
        )
    }

    // run event listener after getting permission:
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(55.6761, 12.5683), 10.5f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            // cluster pinItems if zoomed out
            Clustering(
                items = mapItems,
                onClusterItemClick = { item ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)  // trigger a vibration when pin is tapped
                    selectedPin = item
                    showBottomSheet = true
                    userLocation = null
                    false
                }
            )

            // draw route
            if (userLocation != null && selectedPin != null) {
                Polyline(
                    points = listOf(userLocation!!, selectedPin!!.position),
                    color = MaterialTheme.colorScheme.primary,
                    width = 12f // thick line
                )
            }
        }

        if (showBottomSheet && selectedPin != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = selectedPin!!.title ?: "Discovery Point",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = selectedPin!!.snippet ?: "",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val uri =
                                "google.navigation:q=${selectedPin!!.position.latitude},${selectedPin!!.position.longitude}&mode=w".toUri()
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Get Street Directions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                            if (!hasLocationPermission) {
                            // ask for GPS permissions if not yet available
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                        // get GPS coordinates
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                // save location for Polyline to draw on the map
                                userLocation = LatLng(location.latitude, location.longitude)

                                // calculate distance
                                val distanceInMeters = calculateDistance(
                                   location.latitude, location.longitude,
                                    selectedPin!!.position.latitude,
                                   selectedPin!!.position.longitude
                                )

                                // if user is within 50 meters of the discovery point
                                if (distanceInMeters <= 50f) {
                                    showBottomSheet = false
                                    val safeName = selectedPin!!.title ?: "Location"
                                    navController.navigate("conversation_screen/$safeName")
                                    Toast.makeText(context, "Task Unlocked", Toast.LENGTH_LONG)
                                        .show()
                                } else {
                                    // round to whole number
                                    val distanceRounded = distanceInMeters.toInt()
                                    Toast.makeText(
                                        context,
                                        "You are $distanceRounded meters away. Almost there!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Searching for signal...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                  },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (hasLocationPermission) "Show route & check arrival" else "Enable GPS to start",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
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
