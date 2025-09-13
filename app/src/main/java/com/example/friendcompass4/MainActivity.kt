package com.example.friendcompass4
import com.example.friendcompass4.R

import android.Manifest
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.friendcompass4.ui.theme.FriendCompass4Theme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin



class MainActivity : ComponentActivity() {
    val locationViewModel: LocationViewModel by viewModels()
    private lateinit var sensorManager: SensorManager
    private lateinit var rotationVectorSensor: Sensor

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                val azimuthRadians = orientation[0]          // rotation around Z-axis
                locationViewModel.azimuth.value = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }




    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            1001
        )
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && location.hasAccuracy()) {
                locationViewModel.location.value = location
            }
        }

        Log.i("", locationViewModel.location.value.toString());
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)!!
        sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)

        val locationRequest = LocationRequest.Builder(
            1000L // interval in milliseconds
        ).setMinUpdateDistanceMeters(0f).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (result.lastLocation != null && result.lastLocation!!.hasAccuracy()) {
                    locationViewModel.location.value = result.lastLocation!!
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)

        locationViewModel.startLocationUpdates()



        setContent {

            FriendCompass4Theme(true) {
                Box(
                    Modifier
                        .background(Color.Black)
                        .fillMaxSize()
                ) {
                    val azimuth by locationViewModel.azimuth.collectAsState()

                    DrawBG("hi", "main", rotation = -azimuth.toFloat())

                    Column {
                        val friends by locationViewModel.friends.collectAsState()
                        val loc by locationViewModel.location.collectAsState()

                        Row {
                            Button(onClick={}) {
                                Text("Add Friend")
                            }
                        }
                        CompassScreen(friends, loc, azimuth.toDouble())
                    }
                }

                }

        }
    }
}

@Composable
fun FriendMarker(name: String, angle: Double, size: Dp, distance: Double) {
    Box(
        modifier = Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.secondary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(distance.toString(), fontSize = 14.sp)

    }

}


fun clampToScreen(x: Float, y: Float, maxX: Float, maxY: Float, radius: Float): Pair<Float, Float> {
    // shrink maxX/Y by radius so circle stays fully visible
    val clampedMaxX = maxX - radius
    val clampedMaxY = maxY - radius

    // compute scale factor to fit within screen bounds
    val scaleX = if (x.absoluteValue > clampedMaxX) clampedMaxX / x.absoluteValue else 1f
    val scaleY = if (y.absoluteValue > clampedMaxY) clampedMaxY / y.absoluteValue else 1f

    val scale = minOf(scaleX, scaleY)
    return x * scale to y * scale
}

@Composable
fun CompassScreen(friends: List<Person>, loc: Location, azimuth: Double) {
    val accent = MaterialTheme.colorScheme.primary
    BoxWithConstraints (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val maxX = maxWidth / 2
        val maxY = maxHeight / 2

        // Compass circle
        DrawFace("hi", "main")
        Text(
            text = "You",
            fontSize = 30.sp
        )
        friends.forEach { friend ->
            var angle = loc.bearingTo(friend.location) - azimuth - 90
            val rad = Math.toRadians(angle)
            // raw x/y based on circle around center
            val rawX = cos(rad).toFloat() * maxWidth.value / 2
            val rawY = sin(rad).toFloat() * maxHeight.value / 2

            val (clampedX, clampedY) = clampToScreen(
                rawX,
                rawY,
                maxWidth.value / 2,
                maxHeight.value / 2,
                16f
            )

            Box(
                modifier = Modifier
                    .offset(clampedX.dp, clampedY.dp)
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FriendMarker(friend.firstName.take(1)  + friend.lastName.take(1),
                    angle, 32.dp, friend.location.distanceTo(loc).toDouble())
            }
        }
    }
}
