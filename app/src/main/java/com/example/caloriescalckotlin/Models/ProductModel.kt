package com.example.caloriescalckotlin.Models

import com.example.caloriescalckotlin.Models.Product
import com.google.firebase.database.*

class ProductModel {

    private var db: FirebaseDatabase? = null
    private var products: DatabaseReference? = null

    init {
        db = FirebaseDatabase.getInstance()
        products = db!!.getReference("Products")
    }

    fun fetchProducts(callback: (List<Product>) -> Unit, errorCallback: (String) -> Unit) {
        val productList = mutableListOf<Product>()
        products!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (productSnapshot in snapshot.children) {
                    val prod = Product()
                    prod.calories = productSnapshot.child("calories").value.toString().toInt()
                    prod.name = productSnapshot.child("name").value.toString()
                    prod.carbohydrates = productSnapshot.child("carbohydrates").value.toString().toFloat().toInt()
                    prod.proteins = productSnapshot.child("proteins").value.toString().toFloat().toInt()
                    prod.fats = productSnapshot.child("fats").value.toString().toFloat().toInt()
                    productList.add(prod)
                }
                callback.invoke(productList)
            }

            override fun onCancelled(error: DatabaseError) {
                errorCallback.invoke("Failed to read products.")
            }
        })
    }

    fun fetchYourProducts(callback: (List<Product>) -> Unit, errorCallback: (String) -> Unit) {
        val productList = mutableListOf<Product>()
        products!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (productSnapshot in snapshot.children) {
                    val prod = Product()
                    prod.calories = productSnapshot.child("calories").value.toString().toInt()
                    prod.name = productSnapshot.child("name").value.toString()
                    prod.carbohydrates = productSnapshot.child("carbohydrates").value.toString().toFloat().toInt()
                    prod.proteins = productSnapshot.child("proteins").value.toString().toFloat().toInt()
                    prod.fats = productSnapshot.child("fats").value.toString().toFloat().toInt()
                    productList.add(prod)
                }
                callback.invoke(productList)
            }

            override fun onCancelled(error: DatabaseError) {
                errorCallback.invoke("Failed to read products.")
            }
        })
    }
}