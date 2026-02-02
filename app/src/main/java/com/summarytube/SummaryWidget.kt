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

            // 1. Ação para abrir o INPUT (Clicar na barra ou logo)
            val inputIntent = Intent(context, OverlayService::class.java).apply {
                action = "ACTION_OPEN_INPUT"
            }
            val pInput = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(context, 1, inputIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getService(context, 1, inputIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
            //views.setOnClickPendingIntent(R.id.widget_main_container, pInput)
            //views.setOnClickPendingIntent(R.id.iv_widget_logo, pInput)

            // 2. Ação para PASTE
            val pasteIntent = Intent(context, OverlayService::class.java).apply {
                action = "ACTION_PASTE_AND_SUMMARY"
            }
            val pPaste = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(context, 2, pasteIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getService(context, 2, pasteIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
            //views.setOnClickPendingIntent(R.id.btn_widget_paste, pPaste)
            //views.setOnClickPendingIntent(R.id.btn_widget_send, pPaste)

            // Ação do botão Enviar: Inicia o serviço de Overlay passando a URL
            //val intent = Intent(context, OverlayService::class.java).apply {
            //    action = "ACTION_START_FROM_WIDGET"
                // Aqui passamos o link que está na barra para o Overlay
            //    putExtra("VIDEO_URL", "LINK_DO_EDIT_TEXT_AQUI")
            //}
            // Nota: Em um widget real, pegar o texto do EditText exige uma Activity de configuração
            // ou o uso de BroadCast. Para simplificar e funcionar como barra de busca:
            //val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            //views.setOnClickPendingIntent(R.id.btn_widget_send, pendingIntent)

            // Aplicando os cliques nos IDs corretos do seu XML:
            views.setOnClickPendingIntent(R.id.widget_main_container, pInput) // Clicar na barra
            views.setOnClickPendingIntent(R.id.iv_widget_logo, pInput)        // Clicar no logo
            views.setOnClickPendingIntent(R.id.btn_widget_send, pInput)      // Send abre o input se clicado no widget
            views.setOnClickPendingIntent(R.id.btn_widget_paste, pPaste)     // Paste resume direto

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
