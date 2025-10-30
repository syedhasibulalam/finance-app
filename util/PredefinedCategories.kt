package com.achievemeaalk.freedjf.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.achievemeaalk.freedjf.data.model.Category
import java.io.IOException

fun getPredefinedCategories(context: Context): List<Category> {
    val jsonString: String
    try {
        jsonString = context.assets.open("categories.json").bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return emptyList()
    }

    val listType = object : TypeToken<List<Category>>() {}.type
    return Gson().fromJson(jsonString, listType)
} 