package com.teacher.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teacher.app.services.LocalServerService

class MainActivity : AppCompatActivity() {
    
    private lateinit var serverStatusText: TextView
    private lateinit var connectedCountText: TextView
    private lateinit var broadcastButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        serverStatusText = findViewById(R.id.serverStatusText)
        connectedCountText = findViewById(R.id.connectedCountText)
        broadcastButton = findViewById(R.id.broadcastButton)
        
        // تشغيل خدمة السيرفر المحلي
        val serviceIntent = Intent(this, LocalServerService::class.java)
        startService(serviceIntent)
        
        serverStatusText.text = "✅ الرادار يعمل"
        
        broadcastButton.setOnClickListener {
            Toast.makeText(applicationContext, "📡 جاري بث الدرس...", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(this, LocalServerService::class.java)
        stopService(serviceIntent)
    }
}
