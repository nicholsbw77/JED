package com.jed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jed.app.ui.theme.CardBackground
import com.jed.app.ui.theme.GreenGood
import com.jed.app.ui.theme.MediumGray
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite
import kotlinx.coroutines.delay

@Composable
fun TimerDisplay(
    totalMs: Long,
    label: String,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingMs by remember { mutableLongStateOf(totalMs) }

    LaunchedEffect(totalMs) {
        remainingMs = totalMs
        while (remainingMs > 0) {
            delay(1000)
            remainingMs -= 1000
        }
        onComplete()
    }

    val progress = 1f - (remainingMs.toFloat() / totalMs)
    val minutes = (remainingMs / 60000).toInt()
    val seconds = ((remainingMs % 60000) / 1000).toInt()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = OffWhite.copy(alpha = 0.7f), fontSize = 14.sp)
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                color = if (remainingMs < 30000) OrangeAccent else OffWhite,
                fontSize = 48.sp, fontWeight = FontWeight.Black
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                color = if (progress > 0.9f) GreenGood else OrangeAccent,
                trackColor = MediumGray
            )
        }
    }
}
