package com.jed.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jed.app.ui.theme.GaugeBackground
import com.jed.app.ui.theme.MediumGray
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite

@Composable
fun LineGraph(
    label: String,
    dataPoints: List<Float>,
    unit: String,
    minValue: Float,
    maxValue: Float,
    modifier: Modifier = Modifier,
    lineColor: Color = OrangeAccent,
    maxPoints: Int = 100
) {
    val points = if (dataPoints.size > maxPoints) dataPoints.takeLast(maxPoints) else dataPoints
    val currentValue = points.lastOrNull() ?: 0f

    Column(modifier = modifier.background(GaugeBackground).padding(12.dp)) {
        Text(
            text = "$label: ${"%.1f".format(currentValue)} $unit",
            color = OffWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold
        )

        Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val range = maxValue - minValue
                if (range <= 0 || points.size < 2) return@Canvas

                for (i in 0..4) {
                    val y = size.height * (1 - i / 4f)
                    drawLine(MediumGray, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f)
                }

                val path = Path()
                val stepX = size.width / (points.size - 1).coerceAtLeast(1)

                points.forEachIndexed { index, value ->
                    val x = index * stepX
                    val normalized = ((value - minValue) / range).coerceIn(0f, 1f)
                    val y = size.height * (1 - normalized)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}
