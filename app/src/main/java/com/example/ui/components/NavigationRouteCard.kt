package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Place
import com.example.data.model.RouteInfo
import com.example.ui.theme.GoogleMapsBlue

@Composable
fun NavigationRouteCard(
    modifier: Modifier = Modifier,
    destinationPlace: Place,
    routeInfo: RouteInfo,
    onCloseRoute: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F0FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = GoogleMapsBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "مسار التنقل المباشر",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoogleMapsBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "إلى ${destinationPlace.nameAr}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF202124),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onCloseRoute,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إنهاء",
                        tint = Color(0xFF5F6368),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8F9FA),
                    border = BorderStroke(1.dp, Color(0xFFE8EAED)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "المسافة", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5F6368))
                        Text(text = "${routeInfo.distanceKm} كم", style = MaterialTheme.typography.titleMedium, color = Color(0xFF202124), fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8F9FA),
                    border = BorderStroke(1.dp, Color(0xFFE8EAED)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = GoogleMapsBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "بالسيارة", style = MaterialTheme.typography.labelSmall, color = GoogleMapsBlue)
                        }
                        Text(text = formatMinutes(routeInfo.durationCarMinutes), style = MaterialTheme.typography.titleMedium, color = Color(0xFF202124), fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8F9FA),
                    border = BorderStroke(1.dp, Color(0xFFE8EAED)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (routeInfo.durationTrainMinutes != null) Icons.Default.Train else Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = Color(0xFF137333),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (routeInfo.durationTrainMinutes != null) "قطار" else "أتوبيس",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF137333)
                            )
                        }
                        Text(
                            text = formatMinutes(routeInfo.durationTrainMinutes ?: routeInfo.durationBusMinutes),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF202124),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "المحاور والطرق: " + routeInfo.mainHighwaysAr.joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5F6368)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "محطات واستراحات الطريق:",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF202124),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            routeInfo.routeStops.forEachIndexed { idx, stop ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(GoogleMapsBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${stop.titleAr} (${stop.distanceKmFromStart} كم) - ${stop.descriptionAr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF3C4043)
                    )
                }
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) {
        if (m > 0) "$h س $m د" else "$h ساعات"
    } else {
        "$m دقيقة"
    }
}
