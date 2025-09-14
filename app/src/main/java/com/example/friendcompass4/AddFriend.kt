package com.example.friendcompass4

import android.Manifest
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.friendcompass4.LocationViewModel.constants.endpoint
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerScreen(onResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }


    if (ContextCompat.checkSelfPermission(LocalContext.current, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(LocalActivity.current, arrayOf(Manifest.permission.CAMERA), 0)
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize()) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val scanner = BarcodeScanning.getClient()
            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                barcode.rawValue?.let { onResult(it) }
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(context))
    }
}


@Composable
fun AddFriend(nav: NavController) {
    var done = false
    val model : UserViewModel = viewModel()
    val ctx = LocalContext.current
    val act = LocalActivity.current
    Scaffold { padding -> Box(Modifier.padding(padding)) {
        Box(Modifier.padding(12.dp)) {
            Column {
                Text("Add A Friend", fontSize = 36.sp, modifier=Modifier.padding(0.dp, 12.dp))
                Text("Scan a HackTheNorth QR code", fontSize = 18.sp, modifier=Modifier.padding(0.dp, 6.dp))

                QRScannerScreen { result ->
                    if (!done) {
                        nav.popBackStack()
                        done = true
                        if (result.contains("hackthenorth.com")) {
                            val code = result.substringAfterLast('/');
                            var text= ""
                            if (code == act!!.getPreferences(MODE_PRIVATE).getString("key", ""))
                                text="You cannot friend yourself!"
                            else {
                                model.friend(code)
                                text = "Friend added!"
                            }
                            val duration = Toast.LENGTH_SHORT

                            val toast = Toast.makeText(ctx, text, duration) // in Activity
                            toast.show()
                        } else {
                            val text = "Sorry, only HackTheNorth lanyard codes can be scanned"
                            val duration = Toast.LENGTH_SHORT

                            val toast = Toast.makeText(ctx, text, duration) // in Activity
                            toast.show()
                        }
                    }
                }

            }

        }
    }
    }
}

var key=""
@Composable
fun Register(nav: NavController) {
    val sharedPref = LocalActivity.current!!.getPreferences(MODE_PRIVATE)
    if (sharedPref.contains("key")) {
        nav.navigate("Home")
    }
    Scaffold {
        padding ->
            Box (Modifier.padding(padding)) {
                Box(Modifier.padding(12.dp)) {
                    Column {
                        Text("Register", fontSize = 36.sp, modifier=Modifier.padding(0.dp, 12.dp))
                        Text("Scan your HackTheNorth QR Code", fontSize = 18.sp, modifier=Modifier.padding(0.dp, 6.dp))
                        var done = false
                        QRScannerScreen { result ->
                            if (!done) {
                                nav.navigate("register2")
                                key=result.substringAfterLast('/')
                                done = true
                            }
                        }

                    }
                }

            }
    }

}

class UserViewModel(application: Application) : AndroidViewModel(application) {
    val state = MutableStateFlow(0)
    fun register(first: String, last: String, code: String) {
        viewModelScope.launch {
            state.value=1
            SmsManager.getDefault().sendTextMessage(endpoint, "",
                String.format("r;%s;%s;%s", first, last, code), null, null);

            var lastUpdate = 0.toLong()
            for (i in 1..10) {
                val cursor = getApplication<Application>().contentResolver.query(
                    "content://sms/inbox".toUri(),
                    arrayOf("address", "date", "body"), // columns
                    null,
                    null,
                    "date DESC" // sort order
                )

                cursor?.use {
                    val addressIdx = it.getColumnIndexOrThrow("address")
                    val bodyIdx = it.getColumnIndexOrThrow("body")
                    val dateIdx = cursor.getColumnIndexOrThrow("date")

                    while (it.moveToNext()) {
                        val sender = it.getString(addressIdx)
                        if (PhoneNumberUtils.compare(sender, endpoint)) {
                            val message = it.getString(bodyIdx)
                            val timestamp = cursor.getLong(dateIdx)
                            if (lastUpdate >= timestamp) break;
                            lastUpdate = timestamp
                            if (message.contains("success")) {
                                state.value=2
                            }
                        }
                    }
                }


                delay(1000);
            }
            state.value=3
        }
    }

    fun friend(code: String) {
        viewModelScope.launch {
            SmsManager.getDefault().sendTextMessage(endpoint, "", String.format("fa;%s", code), null, null)
        }
    }
}

@Composable
fun Register2(nav: NavController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val model : UserViewModel = viewModel()
    val state by model.state.collectAsState()
    fun submit() {
        if (firstName.isBlank() || lastName.isBlank()) return;
        model.register(firstName, lastName, key)
    }
    Scaffold {
            padding ->
        Box (Modifier.padding(padding)) {
            Box(Modifier.padding(12.dp)) {
                Column {
                    Text("Register", fontSize = 36.sp, modifier=Modifier.padding(0.dp, 12.dp))
                    Text(key, fontSize= 12.sp, modifier= Modifier.padding(0.dp, 8.dp), fontFamily = FontFamily.Monospace)
                    Text("Enter your details", fontSize = 18.sp, modifier=Modifier.padding(0.dp, 6.dp))
                    TextField(label = { Text("First Name")}, onValueChange = { x:String -> if (x.length <= 10) firstName = x }, value=firstName)
                    TextField(label = { Text("Last Name")}, onValueChange = { x: String -> if (x.length <= 10) lastName = x }, value=lastName)
                    Button(modifier=Modifier.padding(18.dp), onClick = { submit() }) {
                        if (state==0 || state == 3) Text("Submit")
                        if (state==1) CircularProgressIndicator(color=MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                    }
                    if (state==3) Text("Something went wrong, please try again.", color = Color.Red)
                    if (state==2) {
                        val sharedPref = LocalActivity.current!!.getPreferences(MODE_PRIVATE)
                        sharedPref.edit().putString("key", key).apply();
                        nav.navigate("home")
                    }
                }
            }
        }
    }

}