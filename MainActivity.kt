package com.example.stylussound

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("触控笔书写音效", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(32.dp))

                        Button(onClick = { startStylusService() }) {
                            Text("开启音效服务")
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { stopStylusService() }) {
                            Text("停止音效服务")
                        }
                        Spacer(Modifier.height(32.dp))
                        Text(
                            "提示：首次使用请授予通知权限。若需全局生效，请在系统设置中开启本应用的无障碍服务。",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    private fun startStylusService() {
        val intent = Intent(this, StylusSoundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopStylusService() {
        stopService(Intent(this, StylusSoundService::class.java))
    }
}