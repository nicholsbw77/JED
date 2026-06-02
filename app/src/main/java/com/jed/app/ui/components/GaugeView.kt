package com.jed.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jed.app.ui.theme.GaugeBorder
import com.jed.app.ui.theme.GreenGood
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite
import com.jed.app.ui.theme.RedAlert
import com.jed.app.ui.theme.YellowWarn
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GaugeView(
    label: String,
    value: Float,
    unit: String,
    minValue: Float,
    maxValue: Float,
    modifier: Modifier = Modifier,
    warningThreshold: Float = maxValue * 0.75f,
    criticalThreshold: Float = maxValue * 0.9f,
    onClick: (() -> Unit)? = null
) {
    val sweepAngle = 240f
    val startAngle = 150f
    val normalizedValue = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    val animatedValue by animateFloatAsState(
        targetValue = normalizedValue,
        animationSpec = tween(300),
        label = "gauge"
    )

    val arcColor = when {
        value >= criticalThreshold -> RedAlert
        value >= warningThreshold -> YellowWarn
        else -> GreenGood
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            val padding = 12.dp.toPx()
            val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
            val arcOffset = Offset(padding, padding)

            drawArc(
                color = GaugeBorder,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = stroke,
                topLeft = arcOffset,
                size = arcSize
            )

            drawArc(
                color = arcColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle * animatedValue,
                useCenter = false,
                style = stroke,
                topLeft = arcOffset,
                size = arcSize
            )

            val needleAngle = Math.toRadians((startAngle + sweepAngle * animatedValue).toDouble())
            val center = Offset(size.width / 2, size.height / 2)
            val needleLength = (size.width / 2 - padding * 2)
            val needleEnd = Offset(
                center.x + (needleLength * cos(needleAngle)).toFloat(),
                center.y + (needleLength * sin(needleAngle)).toFloat()
            )
            drawLine(
                color = OrangeAccent,
                start = center,
                end = needleEnd,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawCircle(color = OrangeAccent, radius = 6.dp.toPx(), center = center)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (value == value.toLong().toFloat()) "${value.toLong()}" else "%.1f".format(value),
                color = OffWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(text = unit, color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = OffWhite.copy(alpha = 0.7f), fontSize = 10.sp)
        }
    }
}
