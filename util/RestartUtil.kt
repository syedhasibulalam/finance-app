package com.achievemeaalk.freedjf.util

import android.content.Context
import android.content.Intent
import com.achievemeaalk.freedjf.MainActivity

object RestartUtil {
    fun restartApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }
}
