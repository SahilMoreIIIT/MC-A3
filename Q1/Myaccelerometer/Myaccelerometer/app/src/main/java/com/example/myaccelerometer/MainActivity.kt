package com.example.myaccelerometer

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
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
            Image(
                painter= painterResource(id = R.drawable.background1),
                contentDescription = null,
                contentScale= ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Accelerometer",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally) // Center align the text horizontally
                    .padding(top = 16.dp) // Add some padding at the top
            )
            Spacer(modifier = Modifier.height(25.dp))

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(300.dp)
                    .background(color = Color.Black.copy(alpha = 0.5f))
                    .border(2.dp, Color.White)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center)
                )  {
                    Text("X Angle: $xAngle",color = Color.White,fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = FontFamily.Serif)
                    Text("Y Angle: $yAngle",color = Color.White,fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = FontFamily.Serif)
                    Text("Z Angle: $zAngle",color = Color.White,fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = FontFamily.Serif)
                }

            }

                Button(
                    onClick = {
                        startActivity(Intent(context, SecondActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth() ,
                    colors = ButtonDefaults.buttonColors(Color.Black.copy(alpha = 0.7f)) // Set button background color to black with 50% transparency// Match the width of the parent Box
                ) {
                    Text(
                        "Go to Second Activity",
                        color = Color.White ,fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = FontFamily.Serif// Adjust text color
                    )
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
