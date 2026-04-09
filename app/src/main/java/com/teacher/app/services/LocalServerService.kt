package com.teacher.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.net.ServerSocket

class LocalServerService : Service() {
    
    private var serverThread: Thread? = null
    private var serverSocket: ServerSocket? = null
    
    companion object {
        private const val PORT = 8080
        private const val TAG = "LocalServer"
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startServer()
        return START_STICKY
    }
    
    private fun startServer() {
        serverThread = Thread {
            try {
                serverSocket = ServerSocket(PORT)
                Log.d(TAG, "✅ سيرفر المعلم يعمل على المنفذ $PORT")
                while (!Thread.currentThread().isInterrupted) {
                    val client = serverSocket?.accept()
                    client?.let {
                        Log.d(TAG, "👤 طالب متصل: ${it.inetAddress.hostAddress}")
                        it.close()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ خطأ في السيرفر: ${e.message}")
            }
        }
        serverThread?.start()
    }
    
    private fun stopServer() {
        serverThread?.interrupt()
        serverSocket?.close()
        serverThread = null
        serverSocket = null
    }
    
    override fun onDestroy() {
        stopServer()
        super.onDestroy()
        Log.d(TAG, "🛑 تم إيقاف السيرفر")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
