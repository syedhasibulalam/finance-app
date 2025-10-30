package com.achievemeaalk.freedjf.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.achievemeaalk.freedjf.data.model.Category

@Composable
fun Category.localizedName(): String {
    val context = LocalContext.current
    return if (nameResId != null) {
        val resId = context.resources.getIdentifier(nameResId, "string", context.packageName)
        if (resId != 0) {
            context.getString(resId)
        } else {
            name
        }
    } else {
        name
    }
}

@Composable
fun Category.localizedDescription(): String {
    val context = LocalContext.current
    return if (descriptionResId != null) {
        val resId = context.resources.getIdentifier(descriptionResId, "string", context.packageName)
        if (resId != 0) {
            context.getString(resId)
        } else {
            description ?: ""
        }
    } else {
        description ?: ""
    }
}
