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
import androidx.compose.material.icons.filled.Close
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
fun VerificationResultScreen(isSuccess: Boolean, onDone: () -> Unit) {
    val backgroundColor = Color(0xFF121212)
    val successColor = Color(0xFF24D18E)
    val errorColor = Color(0xFFE04A4A)
    val textColor = Color(0xFFC5C5C5)
    val darkGreen = Color(0xFF003D3D)
    val darkRed = Color(0xFF3D0000)

    val primaryColor = if (isSuccess) successColor else errorColor
    val darkColor = if (isSuccess) darkGreen else darkRed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(darkColor.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (isSuccess) "Success" else "Failed",
                tint = primaryColor,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isSuccess) "ยืนยันตัวตนสำเร็จ" else "ยืนยันตัวตนไม่สำเร็จ",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSuccess) "ระบบได้รับการยืนยันแล้ว" else "ระบบไม่สามารถยืนยันได้",
            color = textColor,
            fontSize = 16.sp
        )

        if (isSuccess) {
            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoCard(title = "ข้อมูลส่วนตัวที่ส่ง", value = "0", unit = "Bytes", icon = { Icon(Icons.Default.Shield, "Shield", tint = primaryColor) })
                InfoCard(title = "ขนาดไฟล์ Proof", value = "256", unit = "Bytes", icon = { Icon(Icons.Default.Shield, "Shield", tint = primaryColor) })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isSuccess) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(darkColor.copy(alpha = 0.5f))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Privacy", tint = primaryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ความเป็นส่วนตัวของคุณได้รับการปกป้อง", color = textColor, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text(if (isSuccess) "ดำเนินการต่อ" else "ลองอีกครั้ง", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSuccess) {
            Text(
                text = "การยืนยันตัวตนใช้เทคโนโลยี Zero-Knowledge Proof",
                color = textColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
fun VerificationSuccessPreview() {
    ZeroIDTheme {
        VerificationResultScreen(isSuccess = true) { }
    }
}

@Preview(showBackground = true, name = "Failure")
@Composable
fun VerificationFailurePreview() {
    ZeroIDTheme {
        VerificationResultScreen(isSuccess = false) { }
    }
}
