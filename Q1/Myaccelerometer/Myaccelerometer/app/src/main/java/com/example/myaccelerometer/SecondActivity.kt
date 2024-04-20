package com.example.myaccelerometer
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import kotlin.math.floor


import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.withContext
import java.io.File


class SecondActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var coroutineScope: CoroutineScope? = null
    private var xAngle by mutableStateOf(0f)
    private var yAngle by mutableStateOf(0f)
    private var zAngle by mutableStateOf(0f)
    private val recordingIntervalMillis = 100L // Record every 100 milliseconds
    //private val recordedData = mutableListOf<Triple<Float, Float, Float>>()
    private val recordedData = mutableListOf(
        RecordEntry(time = 0f, xAngle = 0f, yAngle = 0f, zAngle = 0f)
    )
    private var isRecording by mutableStateOf(false)
    private var saveCounter = 0
    private var startTimeMillis: Long = 0L
    private var elapsedTimeSeconds = 0
    private lateinit var db: AppDatabase
    private lateinit var recordEntryDao: RecordEntryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app-db").build()
        recordEntryDao = db.recordEntryDao()
         //Delete all existing entries at the start
//        coroutineScope?.launch(Dispatchers.IO) {
//            recordEntryDao.deleteAllEntries()
//        }
        setContent {
            Image(
                painter= painterResource(id = R.drawable.background1),
                contentDescription = null,
                contentScale= ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
            Column {
                OrientationDisplay(recordedData)
            }

        }
        //startTimeMillis = System.currentTimeMillis()
        coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope?.launch {
            repeat(400) { // Save for 20 seconds
                 // Delay for 1 second (1000 milliseconds)
                if (saveCounter < 400) {
                    saveRecordedData(xAngle, yAngle, zAngle, elapsedTimeSeconds.toFloat())
                    delay(1000)
                    elapsedTimeSeconds++
                }
            }
        }
    }

    private fun saveRecordedData(xAngle: Float, yAngle: Float, zAngle: Float, elapsedTimeMillis: Float) {
        println("X: $xAngle, Y: $yAngle, Z: $zAngle")
        val recordedEntry = RecordEntry(time = elapsedTimeMillis, xAngle = xAngle, yAngle = yAngle, zAngle = zAngle)

        coroutineScope?.launch(Dispatchers.IO) {

            recordEntryDao.insert(recordedEntry)
        }
        saveCounter++
    }



    @Composable
    fun OrientationDisplay(recordedData: List<RecordEntry>) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            sensorManager.registerListener(
                this@SecondActivity,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

            Column(
                modifier = Modifier.padding(16.dp)) {
                Text(
                    "Accelerometer",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally) // Center align the text horizontally
                        .padding(top = 16.dp) // Add some padding at the top
                )
                Spacer(modifier = Modifier.height(25.dp))
                DatabaseContent(recordEntryDao)

                Button(
                    onClick = {
                        val intent = Intent(context, MainActivity3::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Open Third Activity")
                }

                Button(
                    onClick = {
                        coroutineScope?.launch {
                            exportDatabaseAsCSV(context)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Export Database to CSV")
                }
            }
    }

    @Composable
    fun DatabaseContent(recordEntryDao: RecordEntryDao) {
        val context = LocalContext.current
        var recordedData by remember { mutableStateOf(emptyList<RecordEntry>()) }

        LaunchedEffect(Unit) {
            val flow = recordEntryDao.getAll()
            flow.collect { entries ->
                recordedData = entries
            }
        }
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(500.dp)
                .background(color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                .border(2.dp, androidx.compose.ui.graphics.Color.White)
        ) {
            LazyColumn(
                modifier = Modifier.height(500.dp).fillMaxWidth()
            ) {
                items(recordedData) { entry ->
                    Text(
                        text = "Time: ${entry.time}, X-Angle: ${entry.xAngle}, Y-Angle: ${entry.yAngle}, Z-Angle: ${entry.zAngle}",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
    private suspend fun exportDatabaseAsCSV(context: Context) {
        val recordedData = try {
            recordEntryDao.getAllEntries()
        } catch (e: Exception) {
            emptyList()
        }

        val csvContent = StringBuilder()
        csvContent.append("Time,X-Angle,Y-Angle,Z-Angle\n")
        recordedData.forEach { entry ->
            csvContent.append("${entry.time},${entry.xAngle},${entry.yAngle},${entry.zAngle}\n")
        }

        try {
            val fileDir = context.filesDir
            val file = File(fileDir, "database_export.csv")
            file.writeText(csvContent.toString())
            println("saved to csv")
            println(csvContent.toString())
            // Inform the user that export is successful
            // Toast.makeText(context, "Database exported to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle file writing error
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

                val xAngle = x //Math.atan2(y.toDouble(), z.toDouble()).toFloat() * (180 / Math.PI)
                val yAngle = y //Math.atan2(-x.toDouble(), Math.sqrt(y.toDouble() * y.toDouble() + z.toDouble() * z.toDouble())).toFloat() * (180 / Math.PI)
                val zAngle = z //Math.atan2(z.toDouble(), Math.sqrt(x.toDouble() * x.toDouble() + y.toDouble() * y.toDouble())).toFloat() * (180 / Math.PI)
// Not used in this example

                xAngle.takeIf { !it.isNaN() }?.let { xAngle ->
                    yAngle.takeIf { !it.isNaN() }?.let { yAngle ->
                        zAngle.takeIf { !it.isNaN() }?.let { zAngle ->
                            this@SecondActivity.xAngle = x
                            this@SecondActivity.yAngle = y
                            this@SecondActivity.zAngle = z
                        }
                    }
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

}
