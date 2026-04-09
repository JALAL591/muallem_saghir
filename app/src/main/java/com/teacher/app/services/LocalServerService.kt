package com.teacher.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

class LocalServerService : Service() {
    
    private var serverThread: ServerThread? = null
    private val clients = CopyOnWriteArrayList<ClientHandler>()
    
    companion object {
        private const val PORT = 8080
        private const val TAG = "LocalServer"
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (serverThread == null || serverThread?.isAlive == false) {
            serverThread = ServerThread()
            serverThread?.start()
            Log.d(TAG, "✅ سيرفر المعلم يعمل على المنفذ $PORT")
        }
        return START_STICKY
    }
    
    inner class ServerThread : Thread() {
        private var serverSocket: ServerSocket? = null
        
        override fun run() {
            try {
                serverSocket = ServerSocket(PORT)
                while (!Thread.currentThread().isInterrupted) {
                    val clientSocket = serverSocket?.accept()
                    if (clientSocket != null) {
                        Log.d(TAG, "👤 طالب جديد متصل: ${clientSocket.inetAddress.hostAddress}")
                        val clientHandler = ClientHandler(clientSocket)
                        clients.add(clientHandler)
                        clientHandler.start()
                        updateConnectedClients()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ خطأ في السيرفر: ${e.message}")
            } finally {
                serverSocket?.close()
            }
        }
        
        fun stopServer() {
            interrupt()
            serverSocket?.close()
            clients.forEach { it.stopClient() }
            clients.clear()
        }
    }
    
    inner class ClientHandler(private val socket: Socket) : Thread() {
        private var reader: BufferedReader? = null
        private var writer: PrintWriter? = null
        private var isRunning = true
        
        override fun run() {
            try {
                reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                writer = PrintWriter(socket.getOutputStream(), true)
                
                while (isRunning) {
                    val message = reader?.readLine()
                    if (message == null) break
                    Log.d(TAG, "📨 رسالة من طالب: $message")
                    handleMessage(message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "خطأ في معالجة العميل: ${e.message}")
            } finally {
                stopClient()
            }
        }
        
        private fun handleMessage(message: String) {
            when {
                message.contains("GET_LESSONS") -> {
                    // إرسال الدروس المحفوظة
                    sendMessage("LESSONS_DATA|[]")
                }
                message.contains("SUBMIT_HOMEWORK") -> {
                    Log.d(TAG, "📥 تم استلام واجب من طالب")
                    sendMessage("SUBMIT_RESULT|received")
                }
            }
        }
        
        fun sendMessage(message: String) {
            try {
                writer?.println(message)
                writer?.flush()
            } catch (e: Exception) {
                Log.e(TAG, "فشل إرسال الرسالة: ${e.message}")
            }
        }
        
        fun stopClient() {
            isRunning = false
            try {
                reader?.close()
                writer?.close()
                socket.close()
                clients.remove(this)
                updateConnectedClients()
                Log.d(TAG, "👋 طالب انقطع")
            } catch (e: Exception) {
                Log.e(TAG, "خطأ في إغلاق الاتصال: ${e.message}")
            }
        }
    }
    
    private fun updateConnectedClients() {
        val count = clients.size
        Log.d(TAG, "📡 عدد الطلاب المتصلين: $count")
        // إرسال broadcast لتحديث الواجهة
        sendBroadcast(Intent("UPDATE_CONNECTED_COUNT").putExtra("count", count))
    }
    
    fun broadcastToAll(message: String) {
        clients.forEach { it.sendMessage(message) }
        Log.d(TAG, "📢 تم البث إلى ${clients.size} طالب")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serverThread?.stopServer()
        Log.d(TAG, "🛑 تم إيقاف السيرفر")
    }
}
