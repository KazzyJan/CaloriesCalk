package com.example.caloriescalckotlin.Models

import android.os.Parcel
import android.os.Parcelable

class Product : Parcelable {
    var name: String? = null
    var calories = 0
    var fats = 0
    var proteins = 0
    var carbohydrates = 0

    constructor() {}

    constructor(name: String?, calories: Int, fats: Int, proteins: Int, carbohydrates: Int) {
        this.name = name
        this.calories = calories
        this.carbohydrates = carbohydrates
        this.fats = fats
        this.proteins = proteins
    }

    constructor(parcel: Parcel) {
        name = parcel.readString()
        calories = parcel.readInt()
        fats = parcel.readInt()
        proteins = parcel.readInt()
        carbohydrates = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(calories)
        parcel.writeInt(fats)
        parcel.writeInt(proteins)
        parcel.writeInt(carbohydrates)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}
