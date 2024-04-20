package com.example.myaccelerometer
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var coroutineScope: CoroutineScope? = null
    private var xAngle by mutableStateOf(0f)
    private var yAngle by mutableStateOf(0f)
    private var zAngle by mutableStateOf(0f)
    private lateinit var db: AppDatabase
    private lateinit var recordEntryDao: RecordEntryDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app-db").build()
        recordEntryDao = db.recordEntryDao()

        coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope?.launch {
            recordEntryDao.deleteAllEntries()
        }

        setContent {
            OrientationDisplay()
        }
    }

    @Composable
    fun OrientationDisplay() {
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            sensorManager.registerListener(this@MainActivity, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Accelerometer Future",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
            )
            Spacer(modifier = Modifier.height(25.dp))

            Column()
            {
                Text("X Angle: $xAngle", color = Color.Black, fontSize = 20.sp)
                Text("Y Angle: $yAngle", color = Color.Black, fontSize = 20.sp)
                Text("Z Angle: $zAngle", color = Color.Black, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp)) // Add space between buttons
                Button(
                    enabled = true,
                    onClick = {
                        startActivity(Intent(context, SecondActivity::class.java))
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = Color.Gray
                    ),
                    border = BorderStroke(2.dp, Color.DarkGray),
                    shape = ButtonDefaults.elevatedShape,
                    modifier = Modifier.size(100.dp, 30.dp) // Adjust size here
                )
                {
                    Text(
                        text = "Start",
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp // Decrease font size to fit the text
                    )
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this example
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                xAngle = x
                yAngle = y
                zAngle = z
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}
