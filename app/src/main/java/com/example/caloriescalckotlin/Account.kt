package com.example.caloriescalckotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import MenuController
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import com.example.caloriescalckotlin.Models.CaloriesStatistic
import com.example.caloriescalckotlin.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.*
import com.google.firebase.database.*
import java.util.*

class Account : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    var loging = Loging()
    var db: FirebaseDatabase? = null
    var users: DatabaseReference? = null
    var userEmail: TextView? = null
    var enterBoimetric: Button? = null
    var enterBiometricLayout: View? = null
    var biometricLayout: View? = null
    var showWeight: TextView? = null
    var showAge: TextView? = null
    var showHeight: TextView? = null
    var showGender: TextView? = null
    var showTarget: TextView? = null
    var showActivity: TextView? = null
    var age: EditText? = null
    var weight: EditText? = null
    var height: EditText? = null
    var gender: Spinner? = null
    var target: Spinner? = null
    var activity: Spinner? = null

    private lateinit var menuController: MenuController
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        menuController = MenuController(this)
        val discrAge: String
        mAuth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        enterBiometricLayout = findViewById(R.id.enterBiometricLayout)
        biometricLayout = findViewById(R.id.biometricLayout)
        enterBiometricLayout?.let { layout -> layout.visibility = View.INVISIBLE }
        enterBoimetric = findViewById(R.id.enterBoimetric)
        showWeight = findViewById(R.id.showWeight)
        showAge = findViewById(R.id.showAge)
        showHeight = findViewById(R.id.showHeight)
        showGender = findViewById(R.id.showGender)
        showTarget = findViewById(R.id.showTarget)
        showActivity = findViewById(R.id.showActivity)
        userEmail = findViewById(R.id.userEmail)

        age = findViewById(R.id.editAge)
        weight = findViewById(R.id.editWeight)
        height = findViewById(R.id.editHeight)
        gender = findViewById(R.id.spinnerGender)
        target = findViewById(R.id.spinnerTarget)
        activity = findViewById(R.id.spinnerActivity)

        val adapterGender = ArrayAdapter.createFromResource(this, R.array.sex, R.layout.spinner_item)
        gender?.adapter = adapterGender

        val adapterTarget = ArrayAdapter.createFromResource(this, R.array.target, R.layout.spinner_item)
        target?.adapter = adapterTarget

        val adapterActivity = ArrayAdapter.createFromResource(this, R.array.activityKoeff, R.layout.spinner_item)
        activity?.adapter = adapterActivity

        val currentUser = mAuth!!.currentUser
        val uid = currentUser!!.uid
        userEmail?.let {it.text = currentUser.email}
        db = FirebaseDatabase.getInstance()
        users = db!!.getReference("Users")
        users!!.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val discrAge = snapshot.child("age").value.toString()
                    showAge?.let {it.text = discrAge}
                    age?.let {it.text = Editable.Factory.getInstance().newEditable(discrAge)}
                    val discrWeight = snapshot.child("weight").value.toString()
                    showWeight?.let {it.text = discrWeight }
                    weight?.let {it.text = Editable.Factory.getInstance().newEditable(discrWeight)}
                    val discrGender = snapshot.child("gender").value.toString()
                    showGender?.let {it.text = discrGender}
                    val genderArray = resources.getStringArray(R.array.sex)
                    val indexGender = genderArray.indexOf(discrGender)
                    gender?.setSelection(indexGender)
                    var discrTarget = snapshot.child("target").value.toString()
                    if (discrTarget == "null" || discrTarget == "") {
                        discrTarget = "Нет данных"
                    }
                    showTarget?.let {it.text = discrTarget}
                    val targetArray = resources.getStringArray(R.array.target)
                    val indexTarget = targetArray.indexOf(discrTarget)
                    target?.setSelection(indexTarget)
                    var discrActivity = snapshot.child("activity").value.toString()
                    if (discrActivity == "null" || discrActivity == "") {
                        discrActivity = "Нет данных"
                    }
                    showActivity?.let {it.text = discrActivity}
                    val activityArray = resources.getStringArray(R.array.activityKoeff)
                    val indexActivity = activityArray.indexOf(discrActivity)
                    activity?.setSelection(indexActivity)
                    val discrHeight = snapshot.child("height").value.toString()
                    showHeight?.let {it.text = discrHeight}
                    height?.let {it.text = Editable.Factory.getInstance().newEditable(discrHeight)}
                } else {
                    showWeight?.let {it.text = "Нет данных" }
                    showAge?.let {it.text = "Нет данных" }
                    showGender?.let {it.text = "Нет данных" }
                    showTarget?.let {it.text = "Нет данных" }
                    showActivity?.let {it.text = "Нет данных" }
                    showHeight?.let {it.text = "Нет данных" }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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

    fun onClickEnterBio(view: View?) {
        enterBoimetric!!.visibility = View.INVISIBLE
        enterBiometricLayout!!.visibility = View.VISIBLE
        biometricLayout!!.visibility = View.INVISIBLE
    }

    fun onClickDone(view: View?) {
        val age = findViewById<EditText>(R.id.editAge)
        val weight = findViewById<EditText>(R.id.editWeight)
        val height = findViewById<EditText>(R.id.editHeight)
        val gender = findViewById<Spinner>(R.id.spinnerGender)
        val target = findViewById<Spinner>(R.id.spinnerTarget)
        val activity = findViewById<Spinner>(R.id.spinnerActivity)
        val check = addBiometricToBase(age, weight, height, gender, target, activity)
        if (check) {
            enterBoimetric!!.visibility = View.VISIBLE
            enterBiometricLayout!!.visibility = View.INVISIBLE
            biometricLayout!!.visibility = View.VISIBLE
        }
    }

    private fun addBiometricToBase(age: EditText, weight: EditText, height: EditText, gender: Spinner, target: Spinner, activity: Spinner): Boolean {
        if (age.text.toString() == "" || height.text.toString() == "" || weight.text.toString() == "") {
            Toast.makeText(this, "Остались пустые поля", Toast.LENGTH_LONG).show()
            return false
        }

        val updates = hashMapOf<String, Any>(
            "age" to age.text.toString(),
            "gender" to gender.selectedItem.toString(),
            "target" to target.selectedItem.toString(),
            "activity" to activity.selectedItem.toString(),
            "height" to height.text.toString(),
            "weight" to weight.text.toString(),
            "uid" to Loging.signedInAccountId!!
        )

        val currentUser = mAuth!!.currentUser
        currentUser?.let {
            users!!.child(it.uid).updateChildren(updates)
        }

        return true
    }
}