package com.example.iotapp.ui.slideshow

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.iotapp.R
import com.example.iotapp.databinding.FragmentSlideshowBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.time.LocalDate
import java.time.LocalDateTime


class SlideshowFragment : Fragment() {

    var barChart: LineChart? = null
    private var _binding: FragmentSlideshowBinding? = null
    val timeData = ArrayList<String>()
    val tmpData = ArrayList<String>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("lifecycle","SlideShowFragment "+lifecycle.currentState.toString())
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setChartView(binding.root)


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("lifecycle","SlideShowFragment "+lifecycle.currentState.toString())
        _binding = null
    }


    private fun setChartView(view: View) {
        var chartWeek = view.findViewById<LineChart>(R.id.chart_week)
        setWeek(chartWeek)
    }

    private fun initBarDataSet(lineDataSet: LineDataSet) {
        // Changing the color of the bar
        lineDataSet.color = Color.parseColor("#304567")
        // Setting the size of the form in the legend
        lineDataSet.formSize = 15f
        // showing the value of the bar, default true if not set
        lineDataSet.setDrawValues(true)
        // setting the text size of the value of the bar
        lineDataSet.valueTextSize = 15f
        // 데이터 투명하게
        lineDataSet.setDrawValues(false)
        // 두께
        lineDataSet.lineWidth = 8f


    }

    class MyXAxisFormatter : ValueFormatter() {
        var currentDateTime: LocalDateTime = LocalDateTime.now()
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            var currentTime = currentDateTime.toString().substring(11,13).toInt()
            val tmpDays = ArrayList<String>()
            for (i in 0..24){
                if(currentTime+i>=24){
                    tmpDays.add((currentTime + i-24).toString()+"시")
                }else {
                    tmpDays.add((currentTime + i).toString()+"시")
                }
            }
            val days:Array<String> = tmpDays.toTypedArray()

            return days.getOrNull(value.toInt()) ?: value.toString()
        }
    }

    private fun setWeek(lineChart: LineChart) {
        initLineChart(lineChart)

        lineChart.setScaleEnabled(false) //Zoom In/Out
        // 처음 텍스트 값 변경
        lineChart.setNoDataText("그래프 로딩중 ...");
        lineChart.setNoDataTextColor(R.color.black);
        val valueList = ArrayList<Int>()
        val entries: ArrayList<Entry> = ArrayList()
        val title = "소음 값(dB)"

        ///
        val db = Firebase.firestore
        db.collection("timeData")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("시간", "${document.id} => ${document.data}")
                    val tmp = document.data["value"] as String
                    Log.d("시간", LocalDate.now().toString().substring(8, 10))
                    Log.d("시간", document.id.toString().substring(8, 10))

//                    Log.d("시간", document.data.toString().length.toString())
//                    timeData.add(document.data.toString().length.toString())
//                    Log.d("시간", timeData.toString())

                    if (LocalDate.now().toString().substring(8, 10)
                            .toInt() - document.id.toString().substring(8, 10).toInt() <= 1
                    ) {
                        Log.d("시간", "${document.data["value"]}")

                        timeData.add(Gson().fromJson(document.data["value"].toString(), Sensor::class.java).sound.toString())
                    }
                }
                makeTimeData()
                Log.d("시간", timeData.toString())
                Log.d("시간", tmpData.toString())
                ///
                //input data
                for (i in 0..23) {
                    if(i>=timeData.size){
                        valueList.add(timeData[timeData.size-1].toInt())
                    }
                    valueList.add(timeData[i].toInt())
                }

                //fit the data into a bar
                for (i in 0 until valueList.size) {
                    val lineEntry = Entry(i.toFloat(), valueList[i].toFloat())
                    entries.add(lineEntry)
                }
                val lineDataSet = LineDataSet(entries, title)
                val data = LineData(lineDataSet)
                // initBarDataSet
                initBarDataSet(lineDataSet)
                // xAxis data formatter
                lineChart.xAxis.valueFormatter = MyXAxisFormatter()
                // put data & invalidate
                lineChart.data = data
                lineChart.invalidate()
            }
            .addOnFailureListener { exception ->
                Log.d("시간", "Error getting documents: ", exception)
            }
        ///

//        //input data
//        for (i in 0..24) {
//            if(i>=timeData.size){
//                valueList.add(timeData[timeData.size-1].toDouble())
//            }
//            valueList.add(timeData[i].toDouble())
//        }
//
//        //fit the data into a bar
//        for (i in 0 until valueList.size) {
//            val barEntry = BarEntry(i.toFloat(), valueList[i].toFloat())
//            entries.add(barEntry)
//        }
//        val barDataSet = BarDataSet(entries, title)
//        val data = BarData(barDataSet)
//        ///
//        initBarDataSet(barDataSet)
//        ///
//        barChart.data = data
//        barChart.invalidate()
    }

    private fun makeTimeData() {
        var count=0
        var tmp=-999
        for(i: Int in 0 until timeData.size){
            if(tmp<timeData[i].toInt()){
                tmp=timeData[i].toInt()
            }
            count+=1
            if(count==12){
                tmpData.add(tmp.toString());
                tmp=-999
                count=0
            }
        }
    }

    class Sensor {
        var sound: String? = null
    }

    private fun initLineChart(lineChart: LineChart) {

        //hiding the grey background of the chart, default false if not set
        lineChart.setDrawGridBackground(false)
        //remove the bar shadow, default false if not set
//        lineChart.setDrawBarShadow(false)
        //remove border of the chart, default false if not set
        lineChart.setDrawBorders(false)

        //remove the description label text located at the lower right corner
        val description = Description()
        description.setEnabled(false)
        lineChart.setDescription(description)

        //X, Y 바의 애니메이션 효과
        lineChart.animateY(1000)
        lineChart.animateX(1000)


        //바텀 좌표 값
        val xAxis: XAxis = lineChart.getXAxis()
        //change the position of x-axis to the bottom
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //set the horizontal distance of the grid line
        xAxis.granularity = 1f
        xAxis.textColor = Color.BLACK
        //hiding the x-axis line, default true if not set
        xAxis.setDrawAxisLine(false)
        //hiding the vertical grid lines, default true if not set
        xAxis.setDrawGridLines(false)


        //좌측 값 hiding the left y-axis line, default true if not set
        val leftAxis: YAxis = lineChart.getAxisLeft()
        leftAxis.setDrawAxisLine(true)
        leftAxis.textColor = Color.BLACK


        //우측 값 hiding the right y-axis line, default true if not set
        val rightAxis: YAxis = lineChart.getAxisRight()
        rightAxis.setDrawAxisLine(false)
        rightAxis.textColor = Color.BLACK


        //바차트의 타이틀
        val legend: Legend = lineChart.getLegend()
        //setting the shape of the legend form to line, default square shape
        legend.form = Legend.LegendForm.SQUARE
        //setting the text size of the legend
        legend.textSize = 15f
        legend.textColor = Color.BLACK
        //setting the alignment of legend toward the chart
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        //setting the stacking direction of legend
        legend.orientation = Legend.LegendOrientation.VERTICAL
        //setting the location of legend outside the chart, default false if not set
        legend.setDrawInside(false)
    }
}