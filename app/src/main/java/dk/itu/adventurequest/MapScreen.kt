package dk.itu.adventurequest

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState

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

    val mapItems = remember { LocationData.discoveryPoints }

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
                                "https://www.google.com/maps/dir/?api=1&destination=${selectedPin!!.position.latitude},${selectedPin!!.position.longitude}&travelmode=walking".toUri()
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