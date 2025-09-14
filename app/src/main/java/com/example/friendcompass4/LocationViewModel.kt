package com.example.friendcompass4

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.location.Location
import android.net.Uri
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.compose.material3.PermanentDrawerSheet
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.update

class LocationViewModel(app: Application) : AndroidViewModel(app) {
    object constants {
        val endpoint = "+1 986 253 0688" // replace with your endpoint
    }
    val sms = SmsManager.getDefault();
    val azimuth = MutableStateFlow(0f)
    val location = MutableStateFlow(Location("dummyprovider"))
    val friends = MutableStateFlow<List<Person>>(listOf())
    val firstName = MutableStateFlow("")
    val lastName = MutableStateFlow("")

    var tracking = MutableStateFlow("0")

    @SuppressLint("DefaultLocale")
    fun startLocationUpdates() {
        viewModelScope.launch {
            var lastUpdate = 0.toLong()
            while (true) {
                sms.sendTextMessage(constants.endpoint, "",
                    String.format("l;%s;%s;%f;%f", firstName.value, lastName.value, location.value.longitude, location.value.latitude), null, null);



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
                        if (PhoneNumberUtils.compare(sender, constants.endpoint)) {
                            val message = it.getString(bodyIdx)
                            val timestamp = cursor.getLong(dateIdx)
                            if (lastUpdate >= timestamp) break;
                            lastUpdate = timestamp
                            process(message)
                        }
                    }
                }


                delay(30000000000);
            }
        }
    }
    fun process(msg: String) {
        val _friends = arrayListOf<Person>()
        try {
            val lines = msg.split("\n")
            if (!lines[0].contains("f")) return
            for (i in 1 until lines.size) {
                val fields = lines[i].split(";")
                val loc = Location("dummyprovider")
                loc.longitude = fields[3].toDouble()
                loc.latitude = fields[4].toDouble()
                _friends.add(Person(fields[0], fields[1], fields[2], loc))
            }
            friends.value = _friends
        } catch (e: Exception) {
            Log.e("Err", e.toString())
        }
    }
}