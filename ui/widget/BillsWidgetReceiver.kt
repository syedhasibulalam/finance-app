package com.achievemeaalk.freedjf.ui.widget

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.achievemeaalk.freedjf.MainActivity
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import dagger.hilt.EntryPoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BillsWidget()

    companion object {
        const val ACTION_MARK_AS_PAID = "com.achievemeaalk.freedjf.ui.widget.ACTION_MARK_AS_PAID"
        const val EXTRA_TRANSACTION_ID = "com.achievemeaalk.freedjf.ui.widget.EXTRA_TRANSACTION_ID"

        fun getOpenAppIntent(context: Context): Action {
            return actionStartActivity(ComponentName(context, MainActivity::class.java))
        }

        fun getMarkAsPaidIntent(context: Context, transactionId: Int): Intent {
            return Intent(context, BillsWidgetReceiver::class.java).apply {
                action = ACTION_MARK_AS_PAID
                putExtra(EXTRA_TRANSACTION_ID, transactionId)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_MARK_AS_PAID) {
            val viewModel = EntryPoints.get(context.applicationContext, WidgetViewModel.Provider::class.java).widgetViewModel()
            val transactionId = intent.getIntExtra(EXTRA_TRANSACTION_ID, -1)
            if (transactionId != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.markBillAsPaid(transactionId)
                }
            }
        }
    }
}
