package com.example.caloriescalckotlin.Models

import java.util.*

class User {
    var weight: String? = null
    var age: String? = null
    var height: String? = null
    var gender: String? = null
    var target: String? = null
    var activity: String? = null
    var uid: String? = null
    var statList: TreeMap<String, CaloriesStatistic>? = null

    constructor() {}
    constructor(weight: String?, age: String?, height: String?, gender: String?, target: String?, activity:String?, uid: String?, statList: TreeMap<String, CaloriesStatistic>?) {
        this.weight = weight
        this.age = age
        this.height = height
        this.gender = gender
        this.activity = activity
        this.target = target
        this.uid = uid
        this.statList = statList
    }

    fun addStatList(stat: CaloriesStatistic, date: String) {
        statList!!.put(date, stat)
    }

    val statListToday: CaloriesStatistic?
        get() = statList?.values?.lastOrNull()

    fun getStatThisDay(date: String): CaloriesStatistic? {
        return statList?.get(date)
    }
}