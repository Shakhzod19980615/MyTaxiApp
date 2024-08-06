package com.example.mytaxiapp.features

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mytaxiapp.R
import com.example.mytaxiapp.features.viewModel.FragmentHomeViewModel
import com.google.android.gms.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : AppCompatActivity(), PermissionsListener {
    private lateinit var mapView: MapView
    private var mapboxMap: MapboxMap? = null
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel: FragmentHomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token)) // Initialize Mapbox

        // Initialize FusedLocationProviderClient
        fusedLocationClient = FusedLocationProviderClient(this)
        // Initialize MapView and request permissions if needed
        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            startLocationUpdates()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
        setContent {
            MapScreen()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MapScreen() {
        val context = LocalContext.current
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
        )
        LaunchedEffect(bottomSheetScaffoldState.bottomSheetState.currentValue) {
            viewModel.setBottomSheetState(bottomSheetScaffoldState.bottomSheetState.currentValue)
        }

        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetContent = {
                BottomSheetContent()
            },
            sheetPeekHeight = 150.dp, // Adjust this height based on your requirement
            content = {
                Box(modifier = Modifier.fillMaxSize()) {
                    val mapView = remember { MapView(context) }

                    AndroidView(
                        factory = {
                            mapView.apply {
                                getMapAsync { mapboxMap ->
                                    this@MainActivity.mapboxMap = mapboxMap
                                    mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                                        enableLocationComponent(style)
                                        startLocationUpdates()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ClickableImage(R.drawable.ic_menu, isTransparent = false, "Menu") { /* handle menu click */ }
                        SwitcherButton()
                        SpeedDisplay()
                    }
                    val bottomSheetState by viewModel.bottomSheetState.collectAsState()
                    if(bottomSheetState==SheetValue.PartiallyExpanded) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                        ) {
                            ClickableImage(R.drawable.ic_zoom_in, isTransparent = true, "Zoom in") { zoomIn() }
                            Spacer(modifier = Modifier.height(8.dp))
                            ClickableImage(R.drawable.ic_zoom_out, isTransparent = true, "Zoom out") { zoomOut() }
                            Spacer(modifier = Modifier.height(8.dp))
                            ClickableImage(R.drawable.ic_reset, isTransparent = true, "Reset") { reset() }
                        }
                    }


                    DisposableEffect(mapView) {
                        onDispose {
                            mapView.onStop()
                            mapView.onDestroy()
                        }
                    }
                }
            }
        )
    }

    @SuppressLint("ResourceAsColor")
    @Composable
    fun BottomSheetContent() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
                .background(Color(ContextCompat.getColor(this@MainActivity,
                    R.color.background_secondary
                )),
                    shape = RoundedCornerShape(16.dp))
                .height(200.dp) // Adjust the height as needed
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp) // Adjust the height as needed
                    .background(Color(ContextCompat.getColor(this@MainActivity,
                        R.color.background_secondary
                    )),
                        shape = RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BottomSheetItems(
                    icon = R.drawable.ic_tariff,
                    title = "Tariff",
                    number = "6/8",
                    onClick = {}
                    )
                Divider(thickness = 2.dp,
                    color = Color(ContextCompat.getColor(this@MainActivity, R.color.lineColor)))
                BottomSheetItems(
                    icon = R.drawable.ic_order,
                    title = "Orders",
                    number = "0",
                    onClick = {}
                )
                Divider(thickness = 2.dp,
                    color = Color(ContextCompat.getColor(this@MainActivity, R.color.lineColor)))
                BottomSheetItems(
                    icon = R.drawable.ic_rocket,
                    title = "Tariff",
                    number = "",
                    onClick = {}
                )
            }
        }

    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MapScreen()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "Permission needed", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponentOptions = LocationComponentOptions.builder(this)
                .foregroundDrawable(R.drawable.ic_car) // Update drawable resource
                .build()
            val locationComponent = mapboxMap?.locationComponent
            locationComponent?.apply {
                activateLocationComponent(
                    LocationComponentActivationOptions.builder(this@MainActivity, loadedMapStyle)
                        .locationComponentOptions(locationComponentOptions)
                        .build()
                )
                isLocationComponentEnabled = true
                cameraMode = com.mapbox.mapboxsdk.location.modes.CameraMode.TRACKING
                renderMode = com.mapbox.mapboxsdk.location.modes.RenderMode.COMPASS
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Update interval in milliseconds
            fastestInterval = 5000 // Fastest update interval in milliseconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                val location = locationResult.lastLocation
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    val cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(15.0)
                        .tilt(20.0)
                        .build()
                    mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000)
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @Composable
    fun MenuButton(onClick: () -> Unit) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = "Menu",
                tint = Color.Black
            )
        }
    }
    @Composable
    fun BottomSheetItems(
        icon: Int,
        title: String,
        number: String? = null,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = 16.dp, end = 16.dp)
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // Center the items vertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically // Center the items vertically
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = "Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically // Center the items vertically
            ) {
                if (number != null) {
                    Text(
                        text = number,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_right_arrow),
                    contentDescription = "Right Arrow",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    @Composable
    fun SpeedDisplay() {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, shape = RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier
                .size(52.dp)
                .background(Color(0xFF80ED99), shape = RoundedCornerShape(14.dp)),
            )
            Text(
                text = "95",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 24.sp),
                color = Color.Black,
                modifier = Modifier.padding(8.dp)
            )
        }
    }

    @Composable
    fun ClickableImage(
        imageResId: Int,
        isTransparent: Boolean,
        contentDescription: String? = null,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isTransparent) Color.White.copy(alpha = 0.7f) else Color.White,
                    shape = RoundedCornerShape(10.dp)
                ) // Set the size of the image container
                .clickable(onClick = onClick)
                .clip(RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center // Set corner radius if needed
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp), // Make the image fill the container
                contentScale = ContentScale.Crop // Adjust the content scale as needed
            )
        }
    }

    @Composable
    fun SwitcherButton() {
        var isActive by remember { mutableStateOf(true) }

        Row(
            modifier = Modifier
                .width(192.dp) // 192px width
                .height(56.dp) // 56px height
                .clip(RoundedCornerShape(14.dp)) // 14px radius for all corners
                .background(Color.White)
                .padding(all = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeColor = Color(0xFF80ED99)
            val inactiveColor = Color(0xFFe0465f)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isActive) activeColor else Color.White,
                        shape = RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        )
                    )
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .fillMaxWidth()
                    .clickable { isActive = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Active",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp, bottom = 15.dp),
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (!isActive) inactiveColor else Color.White,
                        shape = RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        )
                    )
                    .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp))
                    .fillMaxWidth()
                    .clickable { isActive = false },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Busy",
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp, bottom = 15.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    private fun zoomIn() {
        mapboxMap?.animateCamera(CameraUpdateFactory.zoomIn())
    }

    private fun zoomOut() {
        mapboxMap?.animateCamera(CameraUpdateFactory.zoomOut())
    }

    private fun reset() {
        mapboxMap?.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.DEFAULT))
    }

    @Composable
    fun ZoomInButton(onClick: () -> Unit) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_zoom_in),
                contentDescription = "Zoom Out",
            )
        }
    }

    @Composable
    fun ZoomOutButton(onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_zoom_out),
                contentDescription = "Localized description",
                tint = Color.Black,
            )
        }
    }

    @Composable
    fun ResetButton(onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.navigation),
                contentDescription = "Localized description",
                tint = Color.Black,
            )
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
