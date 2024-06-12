package com.example.caloriescalckotlin

import MenuController
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriescalckotlin.Models.Product
import com.example.caloriescalckotlin.Models.ProductModel
import com.example.caloriescalckotlin.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.math.log

class ProductList : AppCompatActivity() {

    private var arrayAdapter: CustomAdapter? = null
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var users: DatabaseReference? = null
    private var thisUser = User()
    private var selectedProduct: Product? = null
    private var selectedProductYour: Product? = null
    private var selectedProductYourID: String? = null
    private var addLayout: View? = null
    private var listProd: ListView? = null
    private var listYourProd: ListView? = null
    private var calories: TextView? = null
    private var fats: TextView? = null
    private var carbohydrates: TextView? = null
    private var proteins: TextView? = null
    private var caloriesYour: TextView? = null
    private var fatsYour: TextView? = null
    private var carbohydratesYour: TextView? = null
    private var proteinsYour: TextView? = null
    private var searchEditText: EditText? = null
    private var weightEditText: EditText? = null
    private var addButtonFromList: Button? = null
    private var addButtonFromYourList: Button? = null
    private var delButtonInListYour: Button? = null
    private var infoLayout: View? = null
    private var searchImageView: ImageView? = null
    private var enterDataLayout: View? = null
    private var skrollView: View? = null

    private var productListYour: List<Product>? = null
    private var productListYourID: MutableList<String> = mutableListOf()

    private lateinit var productModel: ProductModel

    private lateinit var menuController: MenuController

    private var isUserProduct: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.currentUser
        val uid = currentUser!!.uid
        thisUser.uid = uid
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)
        menuController = MenuController(this)

        infoLayout = findViewById(R.id.infoLayout)
        searchImageView = findViewById(R.id.searchImageView)
        listProd = findViewById(R.id.listProd)
        listYourProd = findViewById(R.id.listYourProd)
        calories = findViewById(R.id.calories)
        fats = findViewById(R.id.fats)
        carbohydrates = findViewById(R.id.carbohydrates)
        proteins = findViewById(R.id.proteins)
        caloriesYour = findViewById(R.id.caloriesYour)
        fatsYour = findViewById(R.id.fatsYour)
        carbohydratesYour = findViewById(R.id.carbohydratesYour)
        proteinsYour = findViewById(R.id.proteinsYour)
        searchEditText = findViewById(R.id.searchEditText)
        addButtonFromList = findViewById(R.id.addButtonFromList)
        addButtonFromYourList = findViewById(R.id.addButtonFromYourList)
        addLayout = findViewById(R.id.addLayout)
        weightEditText = findViewById(R.id.weightEditText)
        enterDataLayout = findViewById(R.id.enterDataLayout)
        skrollView = findViewById(R.id.skrollView)
        delButtonInListYour = findViewById(R.id.delButtonInListYour)

        enterDataLayout!!.visibility = View.GONE
        skrollView!!.visibility = View.VISIBLE
        infoLayout!!.visibility = View.VISIBLE
        searchImageView!!.visibility = View.VISIBLE
        searchEditText!!.visibility = View.VISIBLE

//        listProd!!.isNestedScrollingEnabled = false
        db = FirebaseDatabase.getInstance()
        users = db!!.getReference("Users")

        listProd?.setOnTouchListener(object : View.OnTouchListener {
            private var initialY = 0f
            private var lastY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Сохраняем начальную позицию по Y
                        initialY = event.y
                        lastY = event.y
                        // Запрещаем ScrollView перехватывать события касания
                        v.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val currentY = event.y
                        val dy = currentY - lastY

                        val listView = listProd
                        if (listView != null) {
                            // Проверяем, достигнут ли конец или начало списка
                            val canScrollDown = listView.canScrollVertically(1)
                            val canScrollUp = listView.canScrollVertically(-1)

                            if (dy > 0 && !canScrollUp) {
                                // Если скроллим вниз и не можем скроллировать вверх, разрешаем ScrollView перехватывать события касания
                                v.parent.requestDisallowInterceptTouchEvent(false)
                            } else if (dy < 0 && !canScrollDown) {
                                // Если скроллим вверх и не можем скроллировать вниз, разрешаем ScrollView перехватывать события касания
                                v.parent.requestDisallowInterceptTouchEvent(false)
                            } else {
                                // В остальных случаях запрещаем ScrollView перехватывать события касания
                                v.parent.requestDisallowInterceptTouchEvent(true)
                            }

                            lastY = currentY
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Разрешаем ScrollView перехватывать события касания при отпускании
                        v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                // Обрабатываем события касания ListView
                v.onTouchEvent(event)
                return true
            }
        })

        listYourProd?.setOnTouchListener(object : View.OnTouchListener {
            private var initialY = 0f
            private var lastY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Сохраняем начальную позицию по Y
                        initialY = event.y
                        lastY = event.y
                        // Запрещаем ScrollView перехватывать события касания
                        v.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val currentY = event.y
                        val dy = currentY - lastY

                        val listView = listYourProd
                        if (listView != null) {
                            // Проверяем, достигнут ли конец или начало списка
                            val canScrollDown = listView.canScrollVertically(1)
                            val canScrollUp = listView.canScrollVertically(-1)

                            if (dy > 0 && !canScrollUp) {
                                // Если скроллим вниз и не можем скроллировать вверх, разрешаем ScrollView перехватывать события касания
                                v.parent.requestDisallowInterceptTouchEvent(false)
                            } else if (dy < 0 && !canScrollDown) {
                                // Если скроллим вверх и не можем скроллировать вниз, разрешаем ScrollView перехватывать события касания
                                v.parent.requestDisallowInterceptTouchEvent(false)
                            } else {
                                // В остальных случаях запрещаем ScrollView перехватывать события касания
                                v.parent.requestDisallowInterceptTouchEvent(true)
                            }

                            lastY = currentY
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Разрешаем ScrollView перехватывать события касания при отпускании
                        v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                // Обрабатываем события касания ListView
                v.onTouchEvent(event)
                return true
            }
        })

        readUserProducts()
        productModel = ProductModel()
        lateinit var productList: List<Product>

        productModel.fetchProducts({ fetchedProductList ->
            productList = fetchedProductList
            val productNames = productList.map { it.name }
            arrayAdapter = CustomAdapter(this, productNames as ArrayList<String?>)
            listProd?.adapter = arrayAdapter
        }, { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        })

        listProd?.setOnItemClickListener { adapterView, view, i, l ->
            // Обработка нажатия на продукт в списке
            val filteredItems = (arrayAdapter as? CustomAdapter)?.filteredItems
            if (filteredItems != null && i < filteredItems.size) {
                val selectedProductName = filteredItems[i]

                // Найти объект Product с соответствующим именем в исходном списке продуктов
                val selectedProduct = productList.firstOrNull { it.name == selectedProductName }

                // Если найден продукт, установить значения полей TextView
                selectedProduct?.let {
                    calories?.text = "Калории: ${it.calories}"
                    fats?.text = "Жиры: ${it.fats}"
                    carbohydrates?.text = "Углеводы: ${it.carbohydrates}"
                    proteins?.text = "Белки: ${it.proteins}"
                    this.selectedProduct = it
                    hideKeyboard(this@ProductList)
                    addButtonFromList?.isEnabled = true
                }
            }
        }

        listYourProd?.setOnItemClickListener { adapterView, view, i, l ->
            // Обработка нажатия на продукт в списке
            val selectedProductYour = productListYour?.get(i)
            val selectedProductYourID = productListYourID[i]
            caloriesYour?.text = "Калории: ${selectedProductYour!!.calories}"
            fatsYour?.text = "Жиры: ${selectedProductYour.fats}"
            carbohydratesYour?.text = "Углеводы: ${selectedProductYour.carbohydrates}"
            proteinsYour?.text = "Белки: ${selectedProductYour.proteins}"
            this.selectedProductYour = selectedProductYour
            this.selectedProductYourID = selectedProductYourID
            hideKeyboard(this@ProductList)
            addButtonFromYourList?.isEnabled = true
            delButtonInListYour?.isEnabled = true
        }

        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val searchText = charSequence.toString()
                arrayAdapter?.filter?.filter(searchText)
                val filteredItems = (arrayAdapter as? CustomAdapter)?.filteredItems
                Log.d("Элементы списка", filteredItems.toString())
                Log.d("текст в поиске", searchText)
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        addButtonFromList?.isEnabled = selectedProduct != null
        addButtonFromYourList?.isEnabled = selectedProductYour != null
        delButtonInListYour?.isEnabled = selectedProductYour != null
    }

    fun onClickAddFromYourList (view: View?){
        isUserProduct = true
        onClickAddFromList(view)
    }

    fun onClickAddFromList(view: View?) {
        addButtonFromList!!.isEnabled = false
        addButtonFromYourList!!.isEnabled = false
        delButtonInListYour!!.isEnabled = false
        addLayout!!.visibility = View.VISIBLE
        skrollView!!.visibility = View.GONE
//        listProd!!.visibility = View.INVISIBLE
//        infoLayout!!.visibility = View.INVISIBLE
//        searchImageView!!.visibility = View.INVISIBLE
//        searchEditText!!.visibility = View.INVISIBLE
    }

    fun onClickConfirmAdd(view: View?) {
        if (weightEditText!!.text.toString() == "") {
            Toast.makeText(this, "Остались пустые поля", Toast.LENGTH_LONG).show()
        } else {
            if (isUserProduct) {
                selectedProduct = selectedProductYour
                Log.d("selectedProductYour: ", selectedProductYour.toString())
            }
            Calculate.Companion.addFromBase(selectedProduct, weightEditText!!.text.toString())
            addLayout!!.visibility = View.GONE
            skrollView!!.visibility = View.VISIBLE
//            listProd!!.visibility = View.VISIBLE
//            infoLayout!!.visibility = View.VISIBLE
//            searchImageView!!.visibility = View.VISIBLE
//            searchEditText!!.visibility = View.VISIBLE
            val intent = Intent(this, Calculate::class.java)
            startActivity(intent)
            finish()
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

    fun onClickAdd (view: View) {
        enterDataLayout!!.visibility = View.VISIBLE
        skrollView!!.visibility = View.GONE
    }

    fun recalculateProductIDs() {
        for (i in productListYourID.indices) {
            productListYourID[i] = (i + 1).toString() // Преобразуем индекс в строку
        }
    }

    fun onClickDell (view: View?){
        if (selectedProductYourID != null) {
            deleteUserProduct(selectedProductYourID!!)
            Log.d("ИД удаляемого продукта", selectedProductYourID!!)
        }
        caloriesYour?.text = "Калории"
        fatsYour?.text = "Жиры"
        carbohydratesYour?.text = "Углеводы"
        proteinsYour?.text = "Белки"

        addButtonFromYourList?.isEnabled = false
        delButtonInListYour?.isEnabled = false

    }

    private fun deleteUserProduct(productId: String) {
        val currentUser = mAuth!!.currentUser
        currentUser?.let {
            val userId = it.uid
            val database = FirebaseDatabase.getInstance()
            val userProductsRef = database.getReference("Users").child(userId).child("products").child(productId)

            userProductsRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Продукт удален", Toast.LENGTH_LONG).show()
                    // Обновите список продуктов, если необходимо
                    readUserProducts()
                    // Пересчитайте идентификаторы после успешного удаления

                } else {
                    Toast.makeText(this, "Ошибка при удалении продукта", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    fun onClickAddEnteredData(view: View?) {
        val nameEnteredProd = findViewById<EditText>(R.id.nameEnteredProd)
        val calEnteredProd = findViewById<EditText>(R.id.calEnteredProd)
        val chEnteredProd = findViewById<EditText>(R.id.chEnteredProd)
        val fatsEnteredProd = findViewById<EditText>(R.id.fatsEnteredProd)
        val prEnteredProd = findViewById<EditText>(R.id.prEnteredProd)
        val check = addEnteredProd(nameEnteredProd, calEnteredProd, chEnteredProd, fatsEnteredProd, prEnteredProd)
        if (check) {
            enterDataLayout!!.visibility = View.GONE
            skrollView!!.visibility = View.VISIBLE
        }
    }

    private fun addEnteredProd(nameEnteredProd: EditText, calEnteredProd: EditText, chEnteredProd: EditText, fatsEnteredProd: EditText, prEnteredProd: EditText): Boolean {
        val product = Product()
        if (nameEnteredProd.text.toString() == "" || calEnteredProd.text.toString() == "" ||
            chEnteredProd.text.toString() =="" || fatsEnteredProd.text.toString() =="" || prEnteredProd.text.toString() =="") {
            Toast.makeText(this, "Остались пустые поля", Toast.LENGTH_LONG).show()
            return false
        }
        if (productListYour!!.count() >= 20) {
            Toast.makeText(this, "Максимальное количество продуктов уже добавлено.", Toast.LENGTH_LONG).show()
            return false
        }
        product.name = nameEnteredProd.text.toString()
        product.calories = calEnteredProd.text.toString().toInt()
        product.fats = fatsEnteredProd.text.toString().toInt()
        product.carbohydrates = chEnteredProd.text.toString().toInt()
        product.proteins = prEnteredProd.text.toString().toInt()

        val currentUser = mAuth!!.currentUser
        currentUser?.let {
            val userId = it.uid
            val database = FirebaseDatabase.getInstance()
            val userProductsRef = database.getReference("Users").child(userId).child("products")

            val productId = userProductsRef.push().key // Генерируем уникальный ключ для нового продукта

            productId?.let { id ->
                userProductsRef.child(id).setValue(product).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Продукт добавлен", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Ошибка при добавлении продукта", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        return true
    }

    private fun readUserProducts() {
        val currentUser = mAuth!!.currentUser
        currentUser?.let {
            val userId = it.uid
            val database = FirebaseDatabase.getInstance()
            val userProductsRef = database.getReference("Users").child(userId).child("products")

            userProductsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val productList = mutableListOf<Product>()
                    productListYourID.clear()
                    for (productSnapshot in dataSnapshot.children) {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let {
                            val prod = Product(
                                name = productSnapshot.child("name").value.toString(),
                                calories = productSnapshot.child("calories").value.toString().toInt(),
                                carbohydrates = productSnapshot.child("carbohydrates").value.toString().toFloat().toInt(),
                                proteins = productSnapshot.child("proteins").value.toString().toFloat().toInt(),
                                fats = productSnapshot.child("fats").value.toString().toFloat().toInt()
                            )
                            productList.add(prod)
                        }
                        productListYourID.add(productSnapshot.key.toString())
                        Log.d("Список идишников", productListYourID.toString())
                    }

                    // Установка вашего кастомного адаптера после загрузки данных
                    productListYour = productList
                    val productNames = productList.map { it.name } // Создаем список имен продуктов
                    val customAdapter = CustomAdapter(this@ProductList, ArrayList(productNames)) // Создаем адаптер с использованием списка имен продуктов
                    listYourProd!!.adapter = customAdapter // Устанавливаем адаптер для вашего списка
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Обработка ошибок чтения данных, если такие возникли
                    Log.e(TAG, "Ошибка при чтении продуктов из базы данных", databaseError.toException())
                }
            })
        }
    }



    companion object {
        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}