package com.example.ui.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import com.example.data.model.GeoPoint
import com.example.data.model.Place
import com.example.data.model.PlacesRepository
import com.example.data.model.ServicePOI
import com.example.data.model.ServiceType
import com.example.ui.theme.GoogleMapsPinRed

// Convert Geo coordinates to 2D Screen coordinates
fun geoToScreen(
    geo: GeoPoint,
    viewWidth: Float,
    viewHeight: Float,
    scale: Float,
    panX: Float,
    panY: Float
): Offset {
    val minLat = PlacesRepository.EGYPT_BOUNDS_MIN_LAT
    val maxLat = PlacesRepository.EGYPT_BOUNDS_MAX_LAT
    val minLng = PlacesRepository.EGYPT_BOUNDS_MIN_LNG
    val maxLng = PlacesRepository.EGYPT_BOUNDS_MAX_LNG

    val normX = ((geo.longitude - minLng) / (maxLng - minLng)).toFloat()
    val normY = ((maxLat - geo.latitude) / (maxLat - minLat)).toFloat()

    val mapBaseWidth = viewWidth * 0.95f
    val mapBaseHeight = mapBaseWidth * 1.05f

    val baseX = (viewWidth - mapBaseWidth) / 2f
    val baseY = (viewHeight - mapBaseHeight) / 2f

    val absX = baseX + normX * mapBaseWidth
    val absY = baseY + normY * mapBaseHeight

    val centerX = viewWidth / 2f
    val centerY = viewHeight / 2f

    val zoomedX = centerX + (absX - centerX) * scale + panX
    val zoomedY = centerY + (absY - centerY) * scale + panY

    return Offset(zoomedX, zoomedY)
}

// Convert Screen coordinates to GeoPoint
fun screenToGeo(
    screenPt: Offset,
    viewWidth: Float,
    viewHeight: Float,
    scale: Float,
    panX: Float,
    panY: Float
): GeoPoint {
    val centerX = viewWidth / 2f
    val centerY = viewHeight / 2f

    val unzoomedX = ((screenPt.x - centerX - panX) / scale) + centerX
    val unzoomedY = ((screenPt.y - centerY - panY) / scale) + centerY

    val mapBaseWidth = viewWidth * 0.95f
    val mapBaseHeight = mapBaseWidth * 1.05f

    val baseX = (viewWidth - mapBaseWidth) / 2f
    val baseY = (viewHeight - mapBaseHeight) / 2f

    val normX = ((unzoomedX - baseX) / mapBaseWidth).coerceIn(0f, 1f)
    val normY = ((unzoomedY - baseY) / mapBaseHeight).coerceIn(0f, 1f)

    val minLat = PlacesRepository.EGYPT_BOUNDS_MIN_LAT
    val maxLat = PlacesRepository.EGYPT_BOUNDS_MAX_LAT
    val minLng = PlacesRepository.EGYPT_BOUNDS_MIN_LNG
    val maxLng = PlacesRepository.EGYPT_BOUNDS_MAX_LNG

    val lng = minLng + normX * (maxLng - minLng)
    val lat = maxLat - normY * (maxLat - minLat)

    return GeoPoint(lat, lng)
}

// Calculate the pan required to center the map on a given GeoPoint at scale
fun calculatePanForGeo(
    geo: GeoPoint,
    viewWidth: Float,
    viewHeight: Float,
    scale: Float
): Offset {
    val minLat = PlacesRepository.EGYPT_BOUNDS_MIN_LAT
    val maxLat = PlacesRepository.EGYPT_BOUNDS_MAX_LAT
    val minLng = PlacesRepository.EGYPT_BOUNDS_MIN_LNG
    val maxLng = PlacesRepository.EGYPT_BOUNDS_MAX_LNG

    val normX = ((geo.longitude - minLng) / (maxLng - minLng)).toFloat()
    val normY = ((maxLat - geo.latitude) / (maxLat - minLat)).toFloat()

    val mapBaseWidth = viewWidth * 0.95f
    val mapBaseHeight = mapBaseWidth * 1.05f

    val baseX = (viewWidth - mapBaseWidth) / 2f
    val baseY = (viewHeight - mapBaseHeight) / 2f

    val absX = baseX + normX * mapBaseWidth
    val absY = baseY + normY * mapBaseHeight

    val centerX = viewWidth / 2f
    val centerY = viewHeight / 2f

    val panX = -(absX - centerX) * scale
    val panY = -(absY - centerY) * scale

    return Offset(panX, panY)
}

// Draw Google Maps Red Pin
fun drawGoogleMapsPin(
    drawScope: DrawScope,
    place: Place,
    center: Offset,
    zoom: Float,
    isSelected: Boolean,
    isDark: Boolean
) {
    val r = if (isSelected) 10f * zoom.coerceIn(1.0f, 2.5f) else 6.5f * zoom.coerceIn(1.0f, 2.2f)

    drawScope.drawOval(
        color = Color(0x33000000),
        topLeft = Offset(center.x - r * 0.9f, center.y + r * 0.5f),
        size = Size(r * 1.8f, r * 0.8f)
    )

    drawScope.drawCircle(
        color = if (isSelected) GoogleMapsPinRed else Color(0xFFD93025),
        radius = r,
        center = center
    )

    drawScope.drawCircle(
        color = Color.White,
        radius = r * 0.45f,
        center = center
    )

    if (zoom >= 1.25f || isSelected) {
        drawScope.drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = if (isSelected) android.graphics.Color.parseColor("#D93025") else (if (isDark) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#202124"))
                textSize = (if (isSelected) 24f else 18f) * (zoom.coerceIn(1.0f, 2.2f) * 0.35f + 0.65f)
                isFakeBoldText = isSelected
                textAlign = android.graphics.Paint.Align.CENTER
                setShadowLayer(4f, 0f, 0f, if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
            drawText(place.nameAr, center.x, center.y - r - (4f * zoom.coerceIn(1.0f, 2.0f)), paint)
        }
    }
}

// Draw Service POI Marker
fun drawServiceMarker(
    drawScope: DrawScope,
    service: ServicePOI,
    center: Offset,
    zoom: Float
) {
    val r = 6.5f * zoom.coerceIn(1.0f, 2.5f)
    val pinColor = when (service.type) {
        ServiceType.GAS_STATION -> Color(0xFF0288D1)
        ServiceType.PARKING -> Color(0xFF1976D2)
        ServiceType.RESTAURANT -> Color(0xFFE65100)
        ServiceType.CAFE -> Color(0xFF795548)
        ServiceType.HOSPITAL -> Color(0xFFC2185B)
    }

    drawScope.drawCircle(color = pinColor, radius = r, center = center)
    drawScope.drawCircle(color = Color.White, radius = r * 0.4f, center = center)

    if (zoom >= 1.4f) {
        drawScope.drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#202124")
                textSize = 16f * (zoom.coerceIn(1.0f, 2.2f) * 0.35f + 0.65f)
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
                setShadowLayer(4f, 0f, 0f, android.graphics.Color.WHITE)
            }
            drawText(service.nameAr, center.x, center.y - r - (3f * zoom.coerceIn(1.0f, 2.0f)), paint)
        }
    }
}
