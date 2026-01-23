package com.summarytube

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class SummaryWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Ação do botão Enviar: Inicia o serviço de Overlay passando a URL
            val intent = Intent(context, OverlayService::class.java).apply {
                action = "SHOW_POPUP"
            }
            // Nota: Em um widget real, pegar o texto do EditText exige uma Activity de configuração
            // ou o uso de BroadCast. Para simplificar e funcionar como barra de busca:
            val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btn_send_widget, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
