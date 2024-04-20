package com.example.myaccelerometer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.room.Room
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate


class MainActivity3 : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var recordEntryDao: RecordEntryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Room database access
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app-db").build()
        recordEntryDao = db.recordEntryDao()

        setContent {
           DisplayCharts()
        }

    }

    @Composable
    fun DisplayCharts() {
        val recordedData = remember { mutableStateOf<List<RecordEntry>>(emptyList()) }

        // Fetch data from the database when the activity is created
        LaunchedEffect(Unit) {
            recordedData.value = recordEntryDao.getAllEntries()
        }
        Log.d("RecordedData", "Printing Recorded Data:")
        val recordedDataState = remember { mutableStateOf<List<RecordEntry>>(emptyList()) }
        val isLoading = remember { mutableStateOf(true) }
        val error = remember { mutableStateOf<String?>(null) }

        // Fetch data from the database when the activity is created
        LaunchedEffect(Unit) {
            try {
                recordedDataState.value = recordEntryDao.getAllEntries()
                isLoading.value = false
            } catch (e: Exception) {
                error.value = "Error fetching data: ${e.message}"
                isLoading.value = false
            }
        }

        // Show loading indicator if data is being fetched
        if (isLoading.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Show error message if data fetching failed
            error.value?.let { errorMessage ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = Color.Red, textAlign = TextAlign.Center)
                }
                return
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Graph Plots",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    PlotX(recordedData.value)
                    PlotY(recordedData.value)
                    PlotZ(recordedData.value)
                }
            }

        }
    }


    @Composable
    fun PlotX(dataPoints: List<RecordEntry>) {
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            AndroidView(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .align(Alignment.BottomCenter)
                    .aspectRatio(1f),
                factory = { context ->
                    LineChart(context).apply {
                        setNoDataText("No data available")
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(true)
                        setPinchZoom(true)
                        legend.textColor = Color.Black.toArgb()
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.valueFormatter =
                            IndexAxisValueFormatter(dataPoints.map { it.time.toString() })
                        print(xAxis.valueFormatter)
                        xAxis.granularity = 1f
                        xAxis.apply {
                            // Set text color for X-axis labels
                            textColor = Color.Black.toArgb()
                            axisLineWidth = 2f
                            // Set grid line width
                            gridLineWidth = 1.5f
                            axisLineColor = Color.Black.toArgb()
                            gridColor = Color.Black.toArgb()
                        }
                        legend.apply {
                            // Set legend text color to Black
                            textColor = Color.Black.toArgb()
                            // Increase legend text size
                            textSize = 10f // Adjust the size as needed
                        }
                        axisLeft.apply {
                            // Set text color for Y-axis labels
                            textColor = Color.Black.toArgb()
                            axisLineWidth = 2f
                            // Set grid line width
                            gridLineWidth = 1.5f
                            axisLineColor = Color.Black.toArgb()
                            gridColor = Color.Black.toArgb()
                        }
                        // Set the x-axis description label
                        description.text = "Time (seconds)"
                        description.textColor = Color.Black.toArgb()


                        axisRight.isEnabled = false

                        val leftAxis: YAxis = axisLeft
                        leftAxis.setDrawGridLines(true)

                        // Find the minimum and maximum values for x and y axes
                        val xMin = dataPoints.minByOrNull { it.time }?.time ?: 0f
                        val xMax = dataPoints.maxByOrNull { it.time }?.time ?: 0f
                        val yMin = dataPoints.minOf { it.xAngle }
                        val yMax = dataPoints.maxOf { it.xAngle }

                        // Set minimum and maximum values for the axes with some padding
                        axisLeft.axisMinimum = yMin - 1f
                        axisLeft.axisMaximum = yMax + 1f
                        xAxis.axisMinimum = 0f
                        xAxis.axisMaximum = xMax + 1f

                        val xValues = ArrayList<Entry>()

                        dataPoints.forEach { dataPoint ->
                            xValues.add(Entry(dataPoint.time, dataPoint.xAngle))
                        }

                        val set1 = LineDataSet(xValues, "X Values").apply {
                            //setCircleColor(ColorTemplate.getHoloBlue())
                            lineWidth = 2f
                            circleRadius = 3f
                            setDrawCircleHole(false)
                            valueTextSize = 9f
                            valueTextColor = Color.Black.toArgb()
                            setDrawFilled(true)
                            //fillColor = ColorTemplate.getHoloBlue()
                            // Set the color of the legend text to Black
                            setDrawValues(true)
                            setValueTextColor(Color.Black.toArgb())
                        }

                        var data = LineData(set1)
                        setData(data)
                        dataPoints.forEachIndexed { index, recordedEntry ->
                            Log.d(
                                "RecordedEntry",
                                "Index: $index, Time: ${recordedEntry.time}, X Angle: ${recordedEntry.xAngle}"
                            )
                        }
                    }
                }
            )
        }
    }


    @Composable
    fun PlotY(dataPoints: List<RecordEntry>) {
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            AndroidView(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .align(Alignment.BottomCenter)
                    .aspectRatio(1f),
                factory = { context ->
                    LineChart(context).apply {
                        setNoDataText("No data available")
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(true)
                        setPinchZoom(true)

                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.valueFormatter =
                            IndexAxisValueFormatter(dataPoints.map { it.time.toString() })
                        print(xAxis.valueFormatter)
                        xAxis.apply {
                            // Set text color for X-axis labels
                            textColor = Color.Black.toArgb()
                            axisLineWidth = 2f
                            // Set grid line width
                            gridLineWidth = 1.5f
                            axisLineColor = Color.Black.toArgb()
                            gridColor = Color.Black.toArgb()
                        }
                        legend.apply {
                            // Set legend text color to Black
                            textColor = Color.Black.toArgb()
                            // Increase legend text size
                            textSize = 10f // Adjust the size as needed
                        }
                        axisLeft.apply {
                            // Set text color for Y-axis labels
                            textColor = Color.Black.toArgb()
                            axisLineWidth = 2f
                            // Set grid line width
                            gridLineWidth = 1.5f
                            axisLineColor = Color.Black.toArgb()
                            gridColor = Color.Black.toArgb()
                        }
                        xAxis.granularity = 1f
                        // Set the x-axis description label
                        description.text = "Time (seconds)"
                        description.textColor = Color.Black.toArgb()

                        axisRight.isEnabled = false

                        val leftAxis: YAxis = axisLeft
                        leftAxis.setDrawGridLines(true)

                        // Find the minimum and maximum values for x and y axes
                        val xMin = dataPoints.minByOrNull { it.time }?.time ?: 0f
                        val xMax = dataPoints.maxByOrNull { it.time }?.time ?: 0f
                        val yMin = dataPoints.minOf { it.yAngle }
                        val yMax = dataPoints.maxOf { it.yAngle }

                        // Set minimum and maximum values for the axes with some padding
                        axisLeft.axisMinimum = yMin - 1f
                        axisLeft.axisMaximum = yMax + 1f
                        xAxis.axisMinimum = 0f
                        xAxis.axisMaximum = xMax + 1f

                        val xValues = ArrayList<Entry>()

                        dataPoints.forEach { dataPoint ->
                            xValues.add(Entry(dataPoint.time, dataPoint.yAngle))
                        }

                        val set1 = LineDataSet(xValues, "Y Values").apply {
                            color = ColorTemplate.VORDIPLOM_COLORS[1]
                            setCircleColor(ColorTemplate.VORDIPLOM_COLORS[1])
                            lineWidth = 2f
                            circleRadius = 3f
                            setDrawCircleHole(false)
                            valueTextColor = Color.Black.toArgb()
                            valueTextSize = 9f
                            setDrawFilled(true)
                            fillColor = ColorTemplate.VORDIPLOM_COLORS[1]

                        }

                        val data = LineData(set1)
                        setData(data)
                        dataPoints.forEachIndexed { index, recordedEntry ->
                            Log.d(
                                "RecordedEntry",
                                "Index: $index, Time: ${recordedEntry.time}, Y Angle: ${recordedEntry.yAngle}"
                            )
                        }
                    }
                }
            )
        }
    }

    @Composable
    fun PlotZ(dataPoints: List<RecordEntry>) {
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            AndroidView(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .align(Alignment.BottomCenter)
                    .aspectRatio(1f),
                factory = { context ->
                    LineChart(context).apply {
                        setNoDataText("No data available")
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(true)
                        setPinchZoom(true)
                        legend.apply {
                            // Set legend text color to Black
                            textColor = Color.Black.toArgb()
                            // Increase legend text size
                            textSize = 10f // Adjust the size as needed
                        }
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.valueFormatter =
                            IndexAxisValueFormatter(dataPoints.map { it.time.toString() })
                        print(xAxis.valueFormatter)
                        xAxis.apply {
                            // Set text color for X-axis labels
                            textColor = Color.Black.toArgb()
                            axisLineWidth = 2f
                            // Set grid line width
                            gridLineWidth = 1.5f
                            axisLineColor = Color.Black.toArgb()
                            gridColor = Color.Black.toArgb()
                        }
                        axisLeft.apply {
                            // Set text color for Y-axis labels
                            textColor = Color.Black.toArgb()
                            axisLineWidth = 2f
                            // Set grid line width
                            gridLineWidth = 1.5f
                            axisLineColor = Color.Black.toArgb()
                            gridColor = Color.Black.toArgb()
                        }
                        xAxis.granularity = 1f
                        // Set the x-axis description label
                        description.text = "Time (seconds)"
                        description.textColor = Color.Black.toArgb()

                        axisRight.isEnabled = false

                        val leftAxis: YAxis = axisLeft
                        leftAxis.setDrawGridLines(true)

                        // Find the minimum and maximum values for x and y axes
                        val xMin = dataPoints.minByOrNull { it.time }?.time ?: 0f
                        val xMax = dataPoints.maxByOrNull { it.time }?.time ?: 0f
                        val yMin = dataPoints.minOf { it.zAngle }
                        val yMax = dataPoints.maxOf { it.zAngle }

                        // Set minimum and maximum values for the axes with some padding
                        axisLeft.axisMinimum = yMin - 1f
                        axisLeft.axisMaximum = yMax + 1f
                        xAxis.axisMinimum = 0f
                        xAxis.axisMaximum = xMax + 1f

                        val xValues = ArrayList<Entry>()

                        dataPoints.forEach { dataPoint ->
                            xValues.add(Entry(dataPoint.time, dataPoint.zAngle))
                        }

                        val set1 = LineDataSet(xValues, "Z Values").apply {
                            color = ColorTemplate.VORDIPLOM_COLORS[2]
                            setCircleColor(ColorTemplate.VORDIPLOM_COLORS[2])
                            lineWidth = 2f
                            circleRadius = 3f
                            setDrawCircleHole(false)
                            valueTextColor = Color.Black.toArgb()
                            valueTextSize = 9f
                            setDrawFilled(true)
                            fillColor = ColorTemplate.VORDIPLOM_COLORS[2]
                        }

                        val data = LineData(set1)
                        setData(data)
                        dataPoints.forEachIndexed { index, recordedEntry ->
                            Log.d(
                                "RecordedEntry",
                                "Index: $index, Time: ${recordedEntry.time}, Z Angle: ${recordedEntry.zAngle}"
                            )
                        }
                    }
                }
            )
        }
    }

}


