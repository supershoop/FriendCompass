package com.example.friendcompass4

import android.Manifest
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Button
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
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : ComponentActivity() {
    val locationViewModel: LocationViewModel by viewModels()
    private lateinit var sensorManager: SensorManager
    private lateinit var rotationVectorSensor: Sensor

    private lateinit var locationManager: LocationManager
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
    val locationListener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            locationViewModel.location.value = loc
            Log.i("", "ASKJDL:KSA")
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }




    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            1001
        )
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationViewModel.location.value = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)!!
        sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0.01f, locationListener)

        locationViewModel.startLocationUpdates()

        setContent {

            FriendCompass4Theme(true) {
                Box(
                    Modifier
                        .background(Color.Black)
                        .fillMaxSize()
                ) {
                    Column {
                        val friends by locationViewModel.friends.collectAsState()
                        val azimuth by locationViewModel.azimuth.collectAsState()
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
fun FriendMarker(name: String, angle: Double, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.secondary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(name, fontSize = 14.sp)

        // Tip pointing outwards
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val tipLength = 12.dp.toPx()

            // angle in radians
            val rad = Math.toRadians(angle.toDouble())
            val tipX = center.x + cos(rad).toFloat() * tipLength
            val tipY = center.y + sin(rad).toFloat() * tipLength

            val path = Path().apply {
                moveTo(center.x, center.y)
                lineTo(tipX, tipY)
            }

            drawPath(
                path = path,
                color = Color.Red
            )
        }
    }

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
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(accent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "You",
                fontSize = 30.sp
            )
        }
        friends.forEach { friend ->
            val angle = loc.bearingTo(friend.location) - azimuth
            val rad = Math.toRadians(angle)
            // raw x/y based on circle around center
            val rawX = cos(rad).toFloat() * maxX.value
            val rawY = sin(rad).toFloat() * maxY.value

            // clamp to edges
            val clampedX = rawX.coerceIn(-maxX.value + 32, maxX.value - 32)
            val clampedY = rawY.coerceIn(-maxY.value + 32, maxY.value - 32)

            Box(
                modifier = Modifier
                    .offset(clampedX.dp, clampedY.dp)
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                FriendMarker(friend.firstName.take(1)  + friend.lastName.take(1),
                    angle, 32.dp)
            }
        }
    }
}
