package com.example.caloriescalckotlin

import MenuController
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.example.caloriescalckotlin.Models.CaloriesStatistic
import com.example.caloriescalckotlin.Calculate.Companion
import com.example.caloriescalckotlin.Models.User
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class Statistic : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var users: DatabaseReference
    var statList = TreeMap<String, CaloriesStatistic>()
    var daylyCalTextViewS: TextView? = null
    var calInDayTextViewS2: TextView? = null
    var fatsInDayTextViewS: TextView? = null
    var chInDayTextViewS: TextView? = null
    var prInDayTextViewS: TextView? = null
    var time: Spinner? = null
    var thisUser = User()
    var progressBarS: ProgressBar? = null
    var selectedTime: String? = null
    var currentTime: Int? = 30

    private var calories: Float = 0f // Переменная для хранения данных из Firebase
    private lateinit var menuController: MenuController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        menuController = MenuController(this)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        users = db.getReference("Users")

        daylyCalTextViewS = findViewById(R.id.daylyCalTextViewS)
        calInDayTextViewS2 = findViewById(R.id.calInDayTextViewS2)
        fatsInDayTextViewS = findViewById(R.id.fatsInDayTextViewS)
        chInDayTextViewS = findViewById(R.id.chInDayTextViewS)
        prInDayTextViewS = findViewById(R.id.prInDayTextViewS)
        progressBarS = findViewById(R.id.progressBarS)
        time = findViewById(R.id.spinnerTime)

        val adapter = ArrayAdapter.createFromResource(this, R.array.time, R.layout.spinner_time_item)
        time?.adapter = adapter

        // Получение данных из Firebase
        val currentUser = mAuth.currentUser
        val uid = currentUser?.uid
        thisUser.uid = uid
        thisUser.statList = statList
        if (uid != null) {
            users.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        thisUser.gender = snapshot.child("gender").value.toString()
                        thisUser.height = snapshot.child("height").value.toString()
                        thisUser.age = snapshot.child("age").value.toString()
                        thisUser.weight = snapshot.child("weight").value.toString()
                        thisUser.activity = snapshot.child("activity").value.toString().split(" ")[0]
                        thisUser.target = snapshot.child("target").value.toString()
                        for (statSnapshot in snapshot.child("statList").children) {
                            val thisStatDay = statSnapshot.key.toString()
                            val thisStat = CaloriesStatistic()
                            thisStat.calories = statSnapshot.child("calories").value.toString().toFloat()
                            thisStat.fats = statSnapshot.child("fats").value.toString().toFloat()
                            thisStat.carbohydrates = statSnapshot.child("carbohydrates").value.toString().toFloat()
                            thisStat.proteins = statSnapshot.child("proteins").value.toString().toFloat()
                            thisUser.addStatList(thisStat, thisStatDay)
                        }
//                        calInDayTextViewS2!!.text = thisUser.statList?.values?.lastOrNull()?.calories.toString()

                        var thisDayCalories = 0.1f
                        var thisDayFats = 0.1f
                        var thisDayProteins =0.1f
                        var thisDayCarbohydrates = 0.1f
                        val timeStamp = Companion.getTimestamp()
                        if (thisUser.getStatThisDay(timeStamp.toString()) != null) {
                            thisDayCalories =
                                thisUser.getStatThisDay(timeStamp.toString())?.calories!!
                            thisDayFats = thisUser.getStatThisDay(timeStamp.toString())?.fats!!
                            thisDayProteins =
                                thisUser.getStatThisDay(timeStamp.toString())?.proteins!!
                            thisDayCarbohydrates =
                                thisUser.getStatThisDay(timeStamp.toString())?.carbohydrates!!
                        }

                        fatsInDayTextViewS!!.text = thisDayFats.toInt().toString()
                        chInDayTextViewS!!.text = thisDayCarbohydrates.toInt().toString()
                        prInDayTextViewS!!.text = thisDayProteins.toInt().toString()

                        createKZHBUChart (
                            thisDayFats,
                            thisDayProteins,
                            thisDayCarbohydrates
//                            thisUser.statList?.values?.lastOrNull()?.calories!!,
//                            thisUser.statList?.values?.lastOrNull()?.fats!!,
//                            thisUser.statList?.values?.lastOrNull()?.proteins!!,
//                            thisUser.statList?.values?.lastOrNull()?.carbohydrates!!
                        )
                        createProgressBar(thisUser)
                        createCaloriesChart(thisUser, currentTime)
                    } else {
                        // Обработка случая, когда нет данных в базе
                        Log.d("Calories", "No data available")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Обработка ошибок при чтении данных из Firebase
                    Log.d("Calories", "Failed to read value.", error.toException())
                    Toast.makeText(this@Statistic, "Failed to read value.", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
        val timeArray = resources.getStringArray(R.array.time)
        val index = timeArray.indexOf("Месяц")
        time?.setSelection(index)

        time?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTime = parent?.getItemAtPosition(position).toString()
                if (selectedTime == "3 Месяца") {
                    currentTime = 90
                }
                else if (selectedTime == "Месяц"){
                    currentTime = 30
                }
                else if (selectedTime == "Неделя"){
                    currentTime = 7
                }
                createCaloriesChart(thisUser, currentTime)
                // Теперь selectedValue содержит выбранный элемент
                // Вы можете использовать его далее в вашем коде
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Обработка события, если ничего не выбрано
            }
        }
    }

    fun onMenuItemClicked(view: View) {
        // Получаем id кнопки, на которую нажали
        val menuItemId = view.id

        // Определяем, какая кнопка была нажата
        val menuItem: String = when (menuItemId) {
            R.id.button_calculate -> "calculate"
            R.id.button_account -> "account"
            R.id.button_product_list -> "productList"
            R.id.button_statistic -> "statistic"
            R.id.button_signOut -> "signOut"
            else -> ""
        }

        // Вызываем метод MenuController с информацией о нажатой кнопке
        menuController.onMenuItemClicked(menuItem)

        finish()
    }

    fun createCaloriesChart(thisUser: User, time: Int?) {
        val lineChart = findViewById<LineChart>(R.id.lineChart)

        val entries = ArrayList<Entry>()

        val currentDate = Calendar.getInstance()

        // Устанавливаем время на начало текущего дня (00:00:00)
        currentDate.add(Calendar.DAY_OF_MONTH, - (time!! - 1)) // Вычитаем time-1 дней
        currentDate.set(Calendar.HOUR_OF_DAY, 0)
        currentDate.set(Calendar.MINUTE, 0)
        currentDate.set(Calendar.SECOND, 0)
        currentDate.set(Calendar.MILLISECOND, 0)

        // Получаем временную метку для начала текущего дня
        val last30DaysTimestamp = currentDate.timeInMillis

//        Log.d("last30DaysTimestamp", last30DaysTimestamp.toString())
        // Составляем список последних 30 дней в формате timestamp
        val lastDaysList = mutableListOf<Long>()
        for (i in 0 until time!!) {
            val dayTimestamp = last30DaysTimestamp + i * 24 * 60 * 60 * 1000L // Заменяем 1000 на 1000L для обозначения типа Long
            lastDaysList.add(dayTimestamp)
        }


// Проверяем наличие записи в базе данных для каждого дня
        for ((index, dayTimestamp) in lastDaysList.withIndex()) {
            val dayStat = thisUser.statList?.get(dayTimestamp.toString())
            if (dayStat != null) {
                entries.add(Entry(index.toFloat(), dayStat.calories))
            } else {
                entries.add(Entry(index.toFloat(), 0f)) // Если нет записи в базе данных, добавляем 0
            }
        }

        val dataSet = LineDataSet(entries, "Статистика употреблённых калорий за $time дней")
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSet.valueTextSize = 12f
        dataSet.color = Color.parseColor("#009D92")
        dataSet.lineWidth = 4f

        val data = LineData(dataSet)
        lineChart.data = data

        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return (value + 1).toInt().toString()
            }
        }

        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = (time-1).toFloat()

        xAxis.axisLineColor = Color.BLACK

        val yAxis = lineChart.axisLeft
        yAxis.setDrawGridLines(false)
        yAxis.axisLineColor = Color.BLACK

        lineChart.axisRight.isEnabled = false

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val dayNumber = value.toInt() + 1 // Добавляем 1, так как индексация начинается с 0
                return dayNumber.toString()
            }
        }
        if (time > 7) {
            dataSet.setDrawValues(false)
        }

        lineChart.invalidate()

    }

    fun createProgressBar(thisUser: User) {
        val progressBarView = progressBarS as? ProgressBar
        val stat = thisUser.getStatThisDay(Companion.getTimestamp().toString())
        var caloriesInThisDay = stat?.calories?.toInt()
        Log.d("uid", thisUser.uid.toString())
        Log.d("gender", thisUser.gender.toString())
        val dailyCalories = Companion.calculateNorm(thisUser)
        Log.d("dailyCalories", dailyCalories.toString())
        if (caloriesInThisDay == null) {
            caloriesInThisDay = 0;
        }

        calInDayTextViewS2?.text = caloriesInThisDay.toString()

        daylyCalTextViewS?.text = String.format("%.0f", dailyCalories)
        Log.d("caloriesInThisDay", caloriesInThisDay.toString())
        Log.d("TimeStamp", Companion.getTimestamp().toString())
        if (caloriesInThisDay != null) {
            val progress = (caloriesInThisDay.toFloat() / dailyCalories.toFloat() * 100).toInt()

            progressBarView?.progress = progress
            Log.d("Норма калорий", dailyCalories.toString())
            if (caloriesInThisDay > dailyCalories) {
                val progressDrawable = progressBarView!!.progressDrawable.mutate()
                progressDrawable.setColorFilter(-0x10000, PorterDuff.Mode.MULTIPLY)
                progressBarView!!.progressDrawable = progressDrawable
            }
        }
    }

    fun createKZHBUChart(fats: Float, proteins: Float, carbohydrates: Float) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val entries = listOf(
            PieEntry(fats, "Жиры"),
            PieEntry(proteins, "Белки"),
            PieEntry(carbohydrates, "Углеводы")
        )

        val dataSet = PieDataSet(entries, "КЖБУ")
        dataSet.valueTextSize = 18f
        // Adding colors
        val colors = listOf(
            Color.rgb(93, 206, 198),
            Color.rgb(255, 176, 115),
            Color.rgb(255, 111, 0)
        )
        dataSet.colors = colors

        val data = PieData(dataSet)
        pieChart.data = data

        pieChart.setDrawEntryLabels(false)
        // Other chart settings
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setTransparentCircleAlpha(0)
        pieChart.setHoleRadius(40f)
        pieChart.legend.isEnabled = false
        pieChart.setTransparentCircleRadius(25f)
        pieChart.animateY(1400)

        pieChart.invalidate()
    }

}
