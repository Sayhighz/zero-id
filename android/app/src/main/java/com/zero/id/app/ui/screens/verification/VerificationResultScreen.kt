package com.zero.id.app.ui.screens.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zero.id.app.ui.theme.ZeroIDTheme

@Composable
fun VerificationResultScreen(isSuccess: Boolean, message: String, onDone: () -> Unit) {
    val backgroundColor = Color(0xFF121212)
    val successColor = Color(0xFF24D18E)
    val errorColor = Color(0xFFE04A4A)

    // ปรับการแสดงผลตามข้อความ "Criteria not met" หรือ "Verification Failed"
    val isActuallySuccess = isSuccess && !message.contains("Criteria not met", ignoreCase = true) && !message.contains("Failed", ignoreCase = true)

    val primaryColor = if (isActuallySuccess) successColor else errorColor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display the message
        Text(
            text = message,
            color = primaryColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isActuallySuccess) "ยืนยันตัวตนสำเร็จ" else "ยืนยันตัวตนไม่สำเร็จ",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isActuallySuccess) {
            Text(
                text = "คุณสมบัติของคุณไม่ตรงตามที่กำหนดไว้\nกรุณาตรวจสอบข้อมูลหรือลองใหม่อีกครั้ง",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text(
                text = if (isActuallySuccess) "ดำเนินการต่อ" else "กลับหน้าหลัก",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
fun VerificationSuccessPreview() {
    ZeroIDTheme {
        VerificationResultScreen(isSuccess = true, message = "Verification Passed") { }
    }
}

@Preview(showBackground = true, name = "Criteria Not Met")
@Composable
fun VerificationFailedCriteriaPreview() {
    ZeroIDTheme {
        VerificationResultScreen(isSuccess = true, message = "Verification Failed (Criteria not met)") { }
    }
}

@Preview(showBackground = true, name = "Failure")
@Composable
fun VerificationFailurePreview() {
    ZeroIDTheme {
        VerificationResultScreen(isSuccess = false, message = "Network Error") { }
    }
}
