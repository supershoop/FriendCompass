package com.example.friendcompass4

import android.Manifest
import android.R.attr.onClick
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.friendcompass4.ui.theme.FriendCompass4Theme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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
                locationViewModel.azimuth.value =
                    Math.toDegrees(azimuthRadians.toDouble()).toFloat()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }


    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //

/*
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
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
        sensorManager.registerListener(
            listener,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_UI
        )

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
        //
*/



        setContent {
            val navController = rememberNavController() // create here
            FriendCompass4Theme(true) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("register1") { Register(navController) }
                    composable("register2") { Register2(navController) }
                    composable("home") { HomeScreen(locationViewModel, navController) }
                    composable("addFriend") { AddFriend(navController) }
                }

            }
        }
    }
}

@Composable
fun HomeScreen(locationViewModel: LocationViewModel, n : NavController) {
    Scaffold {
        padding -> Box(
        Modifier
            .background(Color.Black)
            .fillMaxSize()
            .padding(padding)
        ) {
            val azimuth by locationViewModel.azimuth.collectAsState()
            val tracking by locationViewModel.tracking.collectAsState()
            val friends by locationViewModel.friends.collectAsState()
            val location by locationViewModel.location.collectAsState()

            DrawBG("hi", "main", rotation = -azimuth.toFloat())


            Column {
                val loc by locationViewModel.location.collectAsState()

                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column {
                        Button(onClick={n.navigate("addFriend")}) {
                            Icon(Icons.Default.Face, "")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add a Friend")
                        }

                        val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
                        val index = ((azimuth + 22.5) % 360 / 45).toInt()
                        Text(
                            "Direction: " + (azimuth*10).roundToInt()/10 + "° " + directions[index],
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                CompassScreen(friends, loc, azimuth.toDouble(), locationViewModel)


            }

            if (tracking != "0"){
                var targetPerson = Person("", "", "", Location("dummyProvider"))
                Log.e("", friends.size.toString())
                for (friend in friends){
                    Log.e("", friend.phone)
                    Log.e("", tracking)
                    if (friend.phone == tracking){
                        targetPerson = friend
                    }
                }

                DrawTrack("hi", "hello")

                Text(
                    text = "TRACKING:",
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.offset(x = 10.dp, y = 670.dp),
                    color = Color.Black,
                )
                Text(
                    //name
                    text = "jufhbwuif" + (targetPerson.firstName + " " + targetPerson.lastName.take(1)+".").uppercase(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.offset(x = 20.dp, y = 730.dp),
                    color = Color.Black,
                    letterSpacing = 0.2.sp
                )

                Text(
                    //distance
                    text = "-------------------------\nDISTANCE: " + (targetPerson.location.distanceTo(location)*10).roundToInt() / 10 + "m",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.offset(x = 20.dp, y = 770.dp),
                    color = Color.Black,
                    letterSpacing = 0.2.sp
                )

                Box(
                    modifier = Modifier.fillMaxSize().padding(20.dp)  // fills the screen
                ) {
                    FloatingActionButton(onClick = { locationViewModel.tracking.value = "0"}, modifier = Modifier.align ( Alignment.BottomEnd ).offset(x = 0.dp, y = -10.dp)) {
                        Icon(Icons.Default.Close, "")
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
fun CompassScreen(friends: List<Person>, loc: Location, azimuth: Double, locationViewModel: LocationViewModel) {
    val tracking by locationViewModel.tracking.collectAsState()


    val accent = MaterialTheme.colorScheme.primary
    var x = Location("dummyProvider")
    x.longitude  = -80.5402155.toDouble()
    x.latitude = 43.4726362.toDouble()
    val sample = listOf(Person("1","O","W", x))
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
        DrawCompass("hi", "main", rotation = -azimuth.toFloat())
        Text(
            text = "YOU",
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Black,
            modifier = Modifier.offset(x = 0.dp, y = 45.dp)
        )
        friends.forEach { friend ->
            var angle = loc.bearingTo(friend.location) - azimuth - 90
            val rad = Math.toRadians(angle)
            val magnitude = loc.distanceTo(friend.location)
            // raw x/y based on circle around center
            val X = cos(rad).toFloat() * magnitude
            val Y = sin(rad).toFloat() * magnitude
            Log.d("Distance", magnitude.toString())

            val rawX = (X/40f) * (maxHeight/2)
            val rawY = (Y/40f) * (maxHeight/2)

            val (clampedX, clampedY) = clampToScreen(
                rawX.value,
                rawY.value,
                maxWidth.value / 2,
                maxHeight.value / 2,
                16f
            )

            //drawing friend
            val friendImg = painterResource(R.drawable.friend)
            val track = false
            Image(
                painter = friendImg,
                contentDescription = "riend image",
                modifier = Modifier.size((25 + 15 * Math.pow(2.toDouble(), (-friend.location.distanceTo(locationViewModel.location.value))/80.toDouble())).dp).offset(clampedX.dp, clampedY.dp).clip(CircleShape).clickable (
                    //tracking this one
                    onClick = { locationViewModel.tracking.value = friend.phone }
                )
            )
            if (friend.phone == tracking) {
                DrawHalo("","", x = clampedX, y = clampedY)
            }

            Text(
                text = friend.firstName.take(1)  + friend.lastName.take(1),
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(x = (0 + clampedX).dp, y = (30 + clampedY).dp)
            )

            /*
            Box(
                modifier = Modifier
                    .offset(clampedX.dp, clampedY.dp)
                    .size(50.dp)
                    .background(Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center

            ) {
                FriendMarker(friend.firstName.take(1)  + friend.lastName.take(1),
                    angle, 32.dp, magnitude.toDouble())
                //DrawFriend("hi", "main", x = , y = )
                    angle, 32.dp, friend.location.distanceTo(loc).toDouble())
            }

             */
        }
    }


}