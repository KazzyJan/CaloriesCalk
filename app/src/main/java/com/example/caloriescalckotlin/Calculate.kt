package com.example.caloriescalckotlin

import MenuController
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriescalckotlin.Models.CaloriesStatistic
import com.example.caloriescalckotlin.Models.Product
import com.example.caloriescalckotlin.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class Calculate : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    var db: FirebaseDatabase? = null
    var users: DatabaseReference? = null
    var thisUser = User()
    var nestedScrollView: View? = null
    var progressBar: ProgressBar? = null
    var enterDataLayout: View? = null
    var head1: TextView? = null
    var dailyCaloriesNorm: TextView? = null
    var calInDayTextView: TextView? = null
    var statList = TreeMap<String, CaloriesStatistic>()
    var endDataRead = false
    private lateinit var menuController: MenuController
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        menuController = MenuController(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculate)
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.currentUser
        val uid = currentUser!!.uid
        db = FirebaseDatabase.getInstance()
        users = db!!.getReference("Users")
        thisUser.uid = uid
        thisUser.statList = statList
        dailyCaloriesNorm = findViewById(R.id.dailyCaloriesNorm)
        calcListProd = findViewById(R.id.calcListProd)
        nestedScrollView = findViewById(R.id.nestedScrollView)
        enterDataLayout = findViewById(R.id.enterDataLayout)
        progressBar = findViewById(R.id.progressBar)
        calInDayTextView = findViewById(R.id.calInDayTextView)
        head1 = findViewById(R.id.head1)
        enterDataLayout?.let {layout -> layout.visibility = View.GONE}
        nestedScrollView?.let {layout -> layout.visibility = View.VISIBLE}
        users!!.child(uid).addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val discrAge = snapshot.child("age").value.toString()
                    thisUser.age = discrAge
                    val discrWeight = snapshot.child("weight").value.toString()
                    thisUser.weight = discrWeight
                    val discrGender = snapshot.child("gender").value.toString()
                    thisUser.gender = discrGender
                    val discrHeight = snapshot.child("height").value.toString()
                    thisUser.height = discrHeight
                    val discrActivity = snapshot.child("activity").value.toString().split(" ")[0]
                    thisUser.activity = discrActivity
                    val discrTarget = snapshot.child("target").value.toString()
                    thisUser.target = discrTarget
                    for (statSnapshot in snapshot.child("statList").children) {
                        val thisStat = CaloriesStatistic()
                        val thisStatDate = statSnapshot.key.toString()
                        thisStat.calories = statSnapshot
                                .child("calories").value.toString().toFloat()
                        thisUser.addStatList(thisStat, thisStatDate)
                    }
                    endDataRead = true
                    dailyCalories = calculateNorm(thisUser)
                    val paramsPrim = head1?.layoutParams
                    val params = dailyCaloriesNorm?.layoutParams
                    params?.width = paramsPrim?.width?.div(2)
                    dailyCaloriesNorm?.layoutParams = params
                    dailyCaloriesNorm?.text = String.format("%.0f", dailyCalories)
                    dailyCaloriesNorm?.textSize = 40f

                    val date = getTimestamp().toString()
                    if (thisUser.getStatThisDay(date) != null) {
                        val stat = thisUser.getStatThisDay(date)
                        caloriesInThisDay = stat?.calories?.toFloat() ?: 0f
                        val calories = thisUser.getStatThisDay(date)?.calories.toString()
                        Toast.makeText(this@Calculate, calories, Toast.LENGTH_LONG).show()

                    } else {
                        Toast.makeText(this@Calculate, "Запись не найдена", Toast.LENGTH_LONG).show()
                    }

                    calInDayTextView?.text = String.format("%.0f", caloriesInThisDay)
                    calInDayTextView?.textSize = 40F

                    val progressBarView = progressBar
                    progressBarView?.progress = ((caloriesInThisDay / dailyCalories * 100).toInt())

                    progressBarView?.let { progressBar ->
                        if (caloriesInThisDay > dailyCalories) {
                            val progressDrawable = progressBar.progressDrawable.mutate()
                            progressDrawable.setColorFilter(-0x10000, PorterDuff.Mode.MULTIPLY)
                            progressBar.progressDrawable = progressDrawable
                        }
                    }
                } else {
                    thisUser.uid = null
                    head1?.let { textViewHead1 ->
                        val paramsPrim = textViewHead1.layoutParams
                        val params = dailyCaloriesNorm?.layoutParams
                        params?.width = paramsPrim.width
                        dailyCaloriesNorm?.layoutParams = params
                        dailyCaloriesNorm?.gravity = 10
                        dailyCaloriesNorm?.text = "Введите данные о себе в настройках аккаунта, чтобы видеть прогресс."
                    }
                    calInDayTextView?.text = "0"
                    calInDayTextView?.textSize = 40f
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        val arrayAdapter = CustomAdapter(this, productNames)
        calcListProd?.adapter = arrayAdapter
        calcListProd?.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            arrayAdapter?.remove(productNames[i])
            selectedProducts.remove(selectedProducts[i])
            weightList.remove(weightList[i])
        }
    }

    fun onClickAdd(view: View?) {
        val intent = Intent(this, ProductList::class.java)
        this.startActivity(intent)

        finish()
//        nestedScrollView!!.visibility = View.GONE
//        enterDataLayout!!.visibility = View.VISIBLE
    }

    fun onClickAddFromBase(view: View?) {
        val intent = Intent(this, ProductList::class.java)
        startActivity(intent)
    }

//    fun onClickAddEnteredData(view: View?) {
//        val nameEnteredProd = findViewById<EditText>(R.id.nameEnteredProd)
//        val calEnteredProd = findViewById<EditText>(R.id.calEnteredProd)
//        val chEnteredProd = findViewById<EditText>(R.id.chEnteredProd)
//        val fatsEnteredProd = findViewById<EditText>(R.id.fatsEnteredProd)
//        val prEnteredProd = findViewById<EditText>(R.id.prEnteredProd)
//        val weigthEnteredProd = findViewById<EditText>(R.id.weigthEnteredProd)
//        val check = addEnteredProd(nameEnteredProd, calEnteredProd, weigthEnteredProd, chEnteredProd, fatsEnteredProd, prEnteredProd)
//        if (check) {
//            enterDataLayout!!.visibility = View.GONE
//            nestedScrollView!!.visibility = View.VISIBLE
//            val arrayAdapter = CustomAdapter(this, productNames)
//            calcListProd!!.adapter = arrayAdapter
//        }
//    }

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

    fun onClickResult(view: View?) {
        if (thisUser.uid == null) {
            calculate()
            calInDayTextView!!.text = String.format("%.0f", caloriesInThisDay)
            caloriesInThisDay = 0f
            selectedProducts.clear()
            weightList.clear()
            productNames.clear()
            arrayAdapter = ArrayAdapter<String?>(this, android.R.layout.simple_list_item_1, productNames)
            calcListProd!!.adapter = arrayAdapter
        } else {
            calculate()
            selectedProducts.clear()
            weightList.clear()
            productNames.clear()
            arrayAdapter = ArrayAdapter<String?>(this, android.R.layout.simple_list_item_1, productNames)
            calcListProd!!.adapter = arrayAdapter
            saveStatOnBase()
            progressBar!!.progress = (caloriesInThisDay / dailyCalories * 100).toInt()
            if (caloriesInThisDay > dailyCalories) {
                val progressDrawable = progressBar!!.progressDrawable.mutate()
                progressDrawable.setColorFilter(-0x10000, PorterDuff.Mode.MULTIPLY)
                progressBar!!.progressDrawable = progressDrawable
            }
            calInDayTextView!!.text = String.format("%.0f", caloriesInThisDay)
        }
    }

    private fun saveStatOnBase() {
        // Получаем временную метку для начала текущего дня
        val timestamp = getTimestamp()
        val stat = CaloriesStatistic(caloriesInThisDay, fatsInThisDay, carbohydratesInThisDay, proteinsInThisDay)
        thisUser.addStatList(stat, timestamp.toString())

        thisUser.uid?.let { uid ->
            users!!.child(uid).child("statList").child(timestamp.toString())
                .setValue(thisUser.statListToday)
        }

        val todayStat = thisUser.getStatThisDay(timestamp.toString())
        todayStat?.apply {
            this.calories = caloriesInThisDay
            this.fats = fatsInThisDay
            this.proteins = proteinsInThisDay
            this.carbohydrates = carbohydratesInThisDay
        }
    }

//    private fun addEnteredProd(nameEnteredProd: EditText, calEnteredProd: EditText, weigthEnteredProd: EditText, chEnteredProd: EditText, fatsEnteredProd: EditText, prEnteredProd: EditText): Boolean {
//        val product = Product()
//        if (nameEnteredProd.text.toString() == "" || calEnteredProd.text.toString() == "" || weigthEnteredProd.text.toString() == "" ||
//            chEnteredProd.text.toString() =="" || fatsEnteredProd.text.toString() =="" || prEnteredProd.text.toString() =="") {
//            Toast.makeText(this, "Остались пустые поля", Toast.LENGTH_LONG).show()
//            return false
//        }
//        product.name = nameEnteredProd.text.toString()
//        product.calories = calEnteredProd.text.toString().toInt()
//        product.fats = fatsEnteredProd.text.toString().toInt()
//        product.carbohydrates = chEnteredProd.text.toString().toInt()
//        product.proteins = prEnteredProd.text.toString().toInt()
//        selectedProducts.add(product)
//        weightList.add(weigthEnteredProd.text.toString().toFloat())
//        productNames.add("""${product.name}
//${weigthEnteredProd.text} грамм""")
//        return true
//    }

    private fun calculate() {
        for (i in selectedProducts.indices) {
            caloriesInThisDay += selectedProducts[i]!!.calories / 100f * weightList[i]
            fatsInThisDay += selectedProducts[i]!!.fats / 100f * weightList[i]
            proteinsInThisDay += selectedProducts[i]!!.proteins / 100f * weightList[i]
            carbohydratesInThisDay += selectedProducts[i]!!.carbohydrates / 100f * weightList[i]
        }
    }

    companion object {
        private var arrayAdapter: ArrayAdapter<String?>? = null
        var calcListProd: ListView? = null
        var selectedProducts = ArrayList<Product?>()
        var productNames = ArrayList<String?>()
        var weightList = ArrayList<Float>()
        var caloriesInThisDay = 0f
        var fatsInThisDay = 0f
        var proteinsInThisDay = 0f
        var carbohydratesInThisDay = 0f
        var dailyCalories = 0.0
        fun addFromBase(selectedProduct: Product?, weight: String) {
            selectedProducts.add(selectedProduct)
            weightList.add(weight.toFloat())
            productNames.add("""${selectedProduct!!.name}
$weight грамм""")
        }

        fun getTimestamp(): Long {
            val calendar = Calendar.getInstance()

            // Устанавливаем время на начало текущего дня (00:00:00)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Получаем временную метку для начала текущего дня
            val timestamp = calendar.timeInMillis
            return timestamp
        }

        /*Чтобы рассчитать свою потребность в калораже, нужно измерить свой вес.
 Затем измерить свой рост и определить суточную норму калорий по формуле:
 1. Qж = (рост в см х1,8) — (возраст в годах х 4,7) + (вес тела в кг х 9,6) + 655,
 где Qж — количество суточной нормы калорий для женщин.
 2. Qм = (рост в см х 5) — (возраст в годах х 6,8) + (вес тела в кг х13,7) + 66,
 где Qм -количество суточной нормы калорий для мужчин.
*/

//    Мужчины - [9.99 x вес (кг)] + [6.25 x рост (см)] - [4.92 x возраст (в годах)] + 5;
//
//    Женщины - [9.99 x вес (кг)] + [6.25 x рост (см)] - [4.92 x возраст (в годах)] -161.

        fun calculateNorm(thisUser: User): Double {
            var dailyCalories: Double?
            dailyCalories = 2500.0
            val activityString = thisUser.activity?.toString()?.replace(",", ".")
            val activity = if (!activityString.isNullOrEmpty() && activityString != "null") {
                try {
                    activityString.toFloat()
                } catch (e: NumberFormatException) {
                    // Логируем ошибку и устанавливаем значение по умолчанию
                    Log.e("CalculateNorm", "Некорректный формат числа для activity: $activityString")
                    1.375f // Значение по умолчанию в случае исключения
                }
            } else {
                1.375f // Значение по умолчанию, если строка пуста или равна "null"
            }
            if (thisUser.uid != null) {
                if (thisUser.gender == "Мужской") {
                    dailyCalories = (thisUser.height!!.toFloat() * 6.25 - thisUser.age!!.toFloat() * 4.92 + thisUser.weight!!.toFloat() * 9.99 + 5) * activity
                } else if (thisUser.gender == "Женский") {
                    dailyCalories = (thisUser.height!!.toFloat() * 6.25 - thisUser.age!!.toFloat() * 4.92 + thisUser.weight!!.toFloat() * 9.99 - 161) * activity
                }
            }
            when (thisUser.target) {
                "Похудение" -> {
                    dailyCalories -= 500;
                }
                "Набор массы" -> {
                    dailyCalories += 500;
                }
            }
            return dailyCalories
        }
    }
}