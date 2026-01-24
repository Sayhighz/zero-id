package com.zero.id.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import com.zero.id.network.* // เรียกใช้โฟลเดอร์ network ที่ไนท์สร้างไว้

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Button(onClick = { /* เดี๋ยวใส่ฟังก์ชันยิง API */ }) {
                Text("Test Verify API")
            }
        }
    }
}