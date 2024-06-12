package com.example.caloriescalckotlin.Models

class CaloriesStatistic {
    var calories = 0f
    var fats = 0f
    var proteins = 0f
    var carbohydrates = 0f

    constructor() {}
    constructor(calories: Float, fats: Float, carbohydrates: Float, proteins: Float) {
        this.calories = calories
        this.carbohydrates = carbohydrates
        this.fats = fats
        this.proteins = proteins
    }
}