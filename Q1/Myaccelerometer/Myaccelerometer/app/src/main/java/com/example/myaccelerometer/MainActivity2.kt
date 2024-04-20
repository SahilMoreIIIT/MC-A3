package com.example.myaccelerometer

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myaccelerometer.ui.theme.MyaccelerometerTheme

import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity2 : ComponentActivity() , SensorEventListener{
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private val orientationData = mutableListOf<Triple<Float, Float, Float>>() // Store orientation data (x, y, z)
    private var isRecording = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            OrientationDisplay()
        }
    }
    @Composable
    fun OrientationDisplay() {
        val context = LocalContext.current
        Text(text = "hello vivek ")
        LaunchedEffect(Unit) {
            sensorManager.registerListener(this@MainActivity2, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            startRecordingData()
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(orientationData) { index, orientation ->
                Text(
                    text = "Orientation Data $index: ${orientation.first}, ${orientation.second}, ${orientation.third}"
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this example
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (isRecording) {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    // Store orientation data with timestamp
                    orientationData.add(Triple(x, y, z))
                }
            }
        }
    }

    private fun startRecordingData() {
        isRecording = true
        // Use coroutine to stop recording after 5 seconds
        val durationMillis = 5000L
        val startTime = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while (System.currentTimeMillis() - startTime < durationMillis) {
                delay(100) // Check every 100 milliseconds
            }
            stopRecordingData()
        }
    }

    private fun stopRecordingData() {
        isRecording = false
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecordingData()
    }

}



