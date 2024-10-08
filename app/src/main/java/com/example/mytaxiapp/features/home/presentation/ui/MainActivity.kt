package com.example.mytaxiapp.features.home.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.lifecycle.lifecycleScope
import com.example.mytaxiapp.R
import com.example.mytaxiapp.common.LocationService
import com.example.mytaxiapp.features.home.presentation.viewModel.UserLocationVM
import com.google.android.gms.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private var mapboxMap: MapboxMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel: UserLocationVM by viewModels()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var latLng: LatLng? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token)) // Initialize Mapbox
        requestLocationPermissions()
        // Initialize FusedLocationProviderClient
        fusedLocationClient = FusedLocationProviderClient(this)
        // Initialize MapView and request permissions if needed
        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)

        setContent {
            MapScreen()
        }
    }
    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions are already granted, start the service
            startLocationService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, start the service
                startLocationService()
            } else {
                // Permissions denied, show a message to the user
                Toast.makeText(this, "Location permissions are required", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    @SuppressLint("CoroutineCreationDuringComposition")
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
        //val locationState by viewModel.state.collectAsState()
        val mapboxMapState = remember { mutableStateOf<MapboxMap?>(null) }

        val scope = rememberCoroutineScope()
        LaunchedEffect(key1 = true) {
            scope.launch {
                viewModel.state.collectLatest {
                    val icon = com.mapbox.mapboxsdk.annotations.IconFactory.getInstance(context).fromResource(R.drawable.ic_car)
                     latLng = it.location?.let { it1 -> LatLng(it1.latitude, it.location.longitude) }

                    mapboxMapState.value?.addMarker(
                        MarkerOptions().position(LatLng(latLng))
                            .icon(icon))

                }
            }
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
                                    mapboxMapState.value = mapboxMap
                                    mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                                       // startLocationUpdates()
                                        viewModel.setMapboxMap(mapboxMap)
                                        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng!!, 18.0))

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
                            ClickableImage(R.drawable.ic_zoom_in, isTransparent = true, "Zoom in") {
                                zoomIn(mapboxMapState.value) }
                            Spacer(modifier = Modifier.height(8.dp))
                            ClickableImage(R.drawable.ic_zoom_out, isTransparent = true, "Zoom out") {
                                zoomOut(mapboxMapState.value) }
                            Spacer(modifier = Modifier.height(8.dp))
                            ClickableImage(R.drawable.ic_reset, isTransparent = true, "Reset") {
                                reset(mapboxMapState.value) }
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
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    @SuppressLint("ResourceAsColor")
    @Composable
    fun BottomSheetContent() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
                .background(
                    Color(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.background_secondary
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .height(200.dp) // Adjust the height as needed
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp) // Adjust the height as needed
                    .background(
                        Color(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.background_secondary
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
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

    private fun zoomIn(mapboxMap: MapboxMap?) {
        mapboxMap?.let {
            val currentZoom = it.cameraPosition.zoom
            if (currentZoom < it.maxZoomLevel) {
                it.animateCamera(CameraUpdateFactory.zoomIn())
            }
        }
    }

    private fun zoomOut(mapboxMap: MapboxMap?) {
        mapboxMap?.let {
            val currentZoom = it.cameraPosition.zoom
            if (currentZoom > it.minZoomLevel) {
                it.animateCamera(CameraUpdateFactory.zoomOut())
            }
        }
    }

    private fun reset(mapboxMap: MapboxMap?) {
        latLng?.let { CameraUpdateFactory.newLatLng(it) }
            ?.let { mapboxMap?.animateCamera(it) }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
