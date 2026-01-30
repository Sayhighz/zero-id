package com.zero.id.app.ui.screens.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zero.id.app.ui.theme.ZeroIDTheme

@Composable
fun VerificationSuccessScreen(onContinue: () -> Unit) {
    val darkGreen = Color(0xFF003D3D)
    val textColor = Color(0xFFC5C5C5)
    val buttonColor = Color(0xFF24D18E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(darkGreen.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = "Success", tint = buttonColor, modifier = Modifier.size(60.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ยืนยันตัวตนสำเร็จ",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ระบบได้รับการยืนยันแล้ว",
            color = textColor,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCard(title = "ข้อมูลส่วนตัวที่ส่ง", value = "0", unit = "Bytes", icon = { Icon(Icons.Default.Shield, "Shield", tint = buttonColor) })
            InfoCard(title = "ขนาดไฟล์ Proof", value = "256", unit = "Bytes", icon = { Icon(Icons.Default.Shield, "Shield", tint = buttonColor) })
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(darkGreen.copy(alpha = 0.5f))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = "Privacy", tint = buttonColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ความเป็นส่วนตัวของคุณได้รับการปกป้อง", color = textColor, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            Text("ดำเนินการต่อ", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "การยืนยันตัวตนใช้เทคโนโลยี Zero-Knowledge Proof",
            color = textColor,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VerificationSuccessScreenPreview() {
    ZeroIDTheme {
        VerificationSuccessScreen { }
    }
}
