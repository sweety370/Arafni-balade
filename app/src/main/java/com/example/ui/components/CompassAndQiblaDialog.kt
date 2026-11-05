package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.sensor.CompassState

@Composable
fun CompassAndQiblaDialog(
    compassState: CompassState,
    gpsAccuracy: Float,
    currentSpeedKmh: Float,
    currentLocationName: String,
    onDismissRequest: () -> Unit
) {
    val animatedHeading by animateFloatAsState(
        targetValue = -compassState.azimuthDegrees,
        animationSpec = spring(),
        label = "compassRotation"
    )

    val animatedQiblaNeedle by animateFloatAsState(
        targetValue = compassState.qiblaRelativeAngle,
        animationSpec = spring(),
        label = "qiblaRotation"
    )

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "البوصلة وقبلة الصلاة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CompassCalibration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GPS Location Info Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الموقع: $currentLocationName (دقة: ${gpsAccuracy.toInt()}م)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Big Visual Compass Dial
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Compass Dial markings
                    Canvas(
                        modifier = Modifier
                            .size(210.dp)
                            .rotate(animatedHeading)
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2

                        // Outer ring
                        drawCircle(
                            color = Color(0xFF3F444E),
                            radius = radius - 4f,
                            style = Stroke(width = 3f)
                        )

                        // 360 degree tick marks
                        for (deg in 0 until 360 step 30) {
                            val rad = Math.toRadians(deg.toDouble())
                            val isMain = deg % 90 == 0
                            val tickLen = if (isMain) 16f else 8f
                            val startX = (center.x + (radius - 12f) * kotlin.math.sin(rad)).toFloat()
                            val startY = (center.y - (radius - 12f) * kotlin.math.cos(rad)).toFloat()
                            val endX = (center.x + (radius - 12f - tickLen) * kotlin.math.sin(rad)).toFloat()
                            val endY = (center.y - (radius - 12f - tickLen) * kotlin.math.cos(rad)).toFloat()

                            drawLine(
                                color = if (deg == 0) Color(0xFFEA4335) else Color(0xFF8E918F),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (isMain) 3.5f else 1.8f
                            )
                        }

                        // North Arrow (Red)
                        val northPath = Path().apply {
                            moveTo(center.x, center.y - radius + 22f)
                            lineTo(center.x - 14f, center.y)
                            lineTo(center.x, center.y - 12f)
                            close()
                        }
                        drawPath(northPath, color = Color(0xFFEA4335))

                        // South Arrow (White/Slate)
                        val southPath = Path().apply {
                            moveTo(center.x, center.y + radius - 22f)
                            lineTo(center.x + 14f, center.y)
                            lineTo(center.x, center.y + 12f)
                            close()
                        }
                        drawPath(southPath, color = Color(0xFF9AA0A6))
                    }

                    // Qibla Indicator Needle (Green Arrow pointing toward Kaaba)
                    Canvas(
                        modifier = Modifier
                            .size(190.dp)
                            .rotate(animatedQiblaNeedle)
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2

                        // Kaaba Pointer Path
                        val qiblaPath = Path().apply {
                            moveTo(center.x, center.y - radius + 14f)
                            lineTo(center.x - 10f, center.y - radius + 40f)
                            lineTo(center.x + 10f, center.y - radius + 40f)
                            close()
                        }
                        drawPath(qiblaPath, color = Color(0xFF10B981)) // Vivid Emerald Kaaba needle
                    }

                    // Center Pivot
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Direction and Qibla Readout
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mosque,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اتجاه القبلة: ${compassState.qiblaBearingDegrees.toInt()}° (جنوب شرقي في مصر)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "وجّه السهم الأخضر نحو الأعلى تماماً لتكون باتجاه الكعبة المشرفة للصلاة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cardinal Heading Readout
                Text(
                    text = "الاتجاه الحالي: ${compassState.azimuthDegrees.toInt()}° (${compassState.cardinalDirectionAr})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "إغلاق البوصلة",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
