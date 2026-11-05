package com.example.ui.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.GeoPoint
import com.example.data.model.Place
import com.example.data.model.PlacesRepository
import com.example.data.model.RouteInfo
import com.example.data.model.ServicePOI
import com.example.data.model.ServiceType
import com.example.ui.theme.GoogleMapsGreenDelta
import com.example.ui.theme.GoogleMapsHighwayBorder
import com.example.ui.theme.GoogleMapsHighwayYellow
import com.example.ui.theme.GoogleMapsLand
import com.example.ui.theme.GoogleMapsLandDark
import com.example.ui.theme.GoogleMapsPinRed
import com.example.ui.theme.GoogleMapsRoadBorder
import com.example.ui.theme.GoogleMapsWater
import com.example.ui.theme.GoogleMapsWhiteRoad
import kotlinx.coroutines.launch
import kotlin.math.hypot

@Composable
fun EgyptMapCanvas(
    modifier: Modifier = Modifier,
    places: List<Place>,
    selectedPlace: Place?,
    activeRoute: RouteInfo?,
    selectedServiceType: ServiceType?,
    isDarkTheme: Boolean = false,
    zoomScaleState: Float = 1.0f,
    targetCenterGeo: GeoPoint? = null,
    onZoomScaleChange: ((Float) -> Unit)? = null,
    onPlaceSelected: (Place) -> Unit,
    onServiceSelected: (ServicePOI) -> Unit
) {
    var zoomScale by remember { mutableFloatStateOf(zoomScaleState) }
    val panOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val coroutineScope = rememberCoroutineScope()

    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(zoomScaleState) {
        if (zoomScaleState != zoomScale) {
            zoomScale = zoomScaleState
        }
    }

    // Auto-center camera when a target GeoPoint is requested (e.g. user selected place or entered governorate)
    LaunchedEffect(targetCenterGeo, canvasWidth, canvasHeight) {
        if (targetCenterGeo != null && canvasWidth > 0 && canvasHeight > 0) {
            val targetPan = calculatePanForGeo(targetCenterGeo, canvasWidth, canvasHeight, zoomScale)
            panOffset.animateTo(targetPan, animationSpec = tween(500))
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "routeDash")
    val routePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "routePhase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF1E293B) else GoogleMapsWater)
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldZoom = zoomScale
                    val newZoom = (zoomScale * zoom).coerceIn(0.75f, 25.0f)
                    zoomScale = newZoom
                    onZoomScaleChange?.invoke(newZoom)

                    // Focal point zoom calculation
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val currentPan = panOffset.value

                    val zoomFactor = (newZoom / oldZoom) - 1f
                    val newPanX = currentPan.x - (centroid.x - centerX - currentPan.x) * zoomFactor + pan.x
                    val newPanY = currentPan.y - (centroid.y - centerY - currentPan.y) * zoomFactor + pan.y

                    coroutineScope.launch {
                        panOffset.snapTo(Offset(newPanX, newPanY))
                    }
                }
            }
            .pointerInput(places, selectedServiceType, zoomScale, panOffset.value) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        val targetZoom = (zoomScale * 2.0f).coerceAtMost(25.0f)
                        zoomScale = targetZoom
                        onZoomScaleChange?.invoke(targetZoom)
                        val tappedGeo = screenToGeo(tapOffset, size.width.toFloat(), size.height.toFloat(), zoomScale, panOffset.value.x, panOffset.value.y)
                        val targetPan = calculatePanForGeo(tappedGeo, size.width.toFloat(), size.height.toFloat(), targetZoom)
                        coroutineScope.launch {
                            panOffset.animateTo(targetPan, animationSpec = tween(400))
                        }
                    },
                    onTap = { tapOffset ->
                        val curPan = panOffset.value
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        var hitPlace: Place? = null
                        var closestDist = Float.MAX_VALUE

                        // 1. Service POIs hit test
                        if (selectedServiceType != null) {
                            PlacesRepository.allServices.filter { it.type == selectedServiceType }.forEach { s ->
                                val pt = geoToScreen(s.location, width, height, zoomScale, curPan.x, curPan.y)
                                val d = hypot((tapOffset.x - pt.x).toDouble(), (tapOffset.y - pt.y).toDouble()).toFloat()
                                if (d < 45f && d < closestDist) {
                                    closestDist = d
                                    onServiceSelected(s)
                                    return@detectTapGestures
                                }
                            }
                        }

                        // 2. Centers and Districts hit test (when zoomed in)
                        if (zoomScale >= 1.6f) {
                            EgyptMapData.ALL_CENTERS.forEach { dist ->
                                val pt = geoToScreen(dist.location, width, height, zoomScale, curPan.x, curPan.y)
                                val d = hypot((tapOffset.x - pt.x).toDouble(), (tapOffset.y - pt.y).toDouble()).toFloat()
                                if (d < 50f && d < closestDist) {
                                    closestDist = d
                                    hitPlace = PlacesRepository.districtToPlace(dist)
                                }
                            }
                        }

                        // 3. Governorates hit test (always clickable, centers & zooms directly into the governorate!)
                        EgyptMapData.ALL_EGYPT_GOVERNORATES.forEach { gov ->
                            val pt = geoToScreen(gov.location, width, height, zoomScale, curPan.x, curPan.y)
                            val d = hypot((tapOffset.x - pt.x).toDouble(), (tapOffset.y - pt.y).toDouble()).toFloat()
                            if (d < 55f && d < closestDist) {
                                closestDist = d
                                hitPlace = PlacesRepository.governorateToPlace(gov)
                                // Smooth zoom and center directly into the clicked governorate!
                                val targetZoom = 4.2f
                                zoomScale = targetZoom
                                onZoomScaleChange?.invoke(targetZoom)
                                val newPan = calculatePanForGeo(gov.location, width, height, targetZoom)
                                coroutineScope.launch {
                                    panOffset.animateTo(newPan, animationSpec = tween(500))
                                }
                            }
                        }

                        // 4. Regular Landmark Places hit test
                        places.forEach { place ->
                            val pt = geoToScreen(place.location, width, height, zoomScale, curPan.x, curPan.y)
                            val d = hypot((tapOffset.x - pt.x).toDouble(), (tapOffset.y - pt.y).toDouble()).toFloat()
                            if (d < 50f && d < closestDist) {
                                closestDist = d
                                hitPlace = place
                            }
                        }

                        if (hitPlace != null) {
                            onPlaceSelected(hitPlace!!)
                        } else {
                            // Tapped anywhere else on the map: find nearest center/place with full history
                            val tappedGeo = screenToGeo(tapOffset, width, height, zoomScale, curPan.x, curPan.y)
                            val nearest = PlacesRepository.getNearestPlace(tappedGeo)
                            onPlaceSelected(nearest)
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            canvasWidth = size.width
            canvasHeight = size.height
            val width = size.width
            val height = size.height
            val curPan = panOffset.value

            drawRect(color = if (isDarkTheme) Color(0xFF17263C) else GoogleMapsWater)

            // Land polygon
            val landPath = Path()
            EgyptMapData.BORDER_POLYGON.forEachIndexed { index, geo ->
                val pt = geoToScreen(geo, width, height, zoomScale, curPan.x, curPan.y)
                if (index == 0) landPath.moveTo(pt.x, pt.y) else landPath.lineTo(pt.x, pt.y)
            }
            landPath.close()

            drawPath(landPath, color = if (isDarkTheme) GoogleMapsLandDark else GoogleMapsLand)
            drawPath(
                path = landPath,
                color = if (isDarkTheme) Color(0xFF38465C) else Color(0xFFD6D3CB),
                style = Stroke(width = 1.5f * zoomScale.coerceIn(1.0f, 2.5f))
            )

            clipPath(landPath) {
                // Nile Delta green agricultural plain
                val deltaPath = Path()
                val cairoPt = geoToScreen(GeoPoint(30.04, 31.23), width, height, zoomScale, curPan.x, curPan.y)
                val alexPt = geoToScreen(GeoPoint(31.21, 29.90), width, height, zoomScale, curPan.x, curPan.y)
                val portSaidPt = geoToScreen(GeoPoint(31.26, 32.30), width, height, zoomScale, curPan.x, curPan.y)
                val baltimPt = geoToScreen(GeoPoint(31.58, 30.95), width, height, zoomScale, curPan.x, curPan.y)

                deltaPath.moveTo(cairoPt.x, cairoPt.y)
                deltaPath.lineTo(alexPt.x, alexPt.y)
                deltaPath.lineTo(baltimPt.x, baltimPt.y)
                deltaPath.lineTo(portSaidPt.x, portSaidPt.y)
                deltaPath.close()
                drawPath(deltaPath, color = if (isDarkTheme) Color(0xFF1E382A) else GoogleMapsGreenDelta)

                // Secondary roads
                if (zoomScale >= 1.25f) {
                    val roadStroke = (zoomScale * 1.8f).coerceIn(2.5f, 10.0f)
                    EgyptMapData.SECONDARY_ROADS.forEach { roadPts ->
                        val rPath = Path()
                        roadPts.forEachIndexed { i, geo ->
                            val pt = geoToScreen(geo, width, height, zoomScale, curPan.x, curPan.y)
                            if (i == 0) rPath.moveTo(pt.x, pt.y) else rPath.lineTo(pt.x, pt.y)
                        }
                        drawPath(rPath, color = if (isDarkTheme) Color(0xFF3A4454) else GoogleMapsRoadBorder, style = Stroke(width = roadStroke * 1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        drawPath(rPath, color = if (isDarkTheme) Color(0xFF2D3748) else GoogleMapsWhiteRoad, style = Stroke(width = roadStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }

                // Primary Highways (Yellow Google Maps Style)
                val highwayStroke = (zoomScale * 2.2f).coerceIn(3.0f, 14.0f)
                EgyptMapData.PRIMARY_HIGHWAYS.forEach { highwayPts ->
                    val hPath = Path()
                    highwayPts.forEachIndexed { i, geo ->
                        val pt = geoToScreen(geo, width, height, zoomScale, curPan.x, curPan.y)
                        if (i == 0) hPath.moveTo(pt.x, pt.y) else hPath.lineTo(pt.x, pt.y)
                    }
                    drawPath(
                        path = hPath,
                        color = if (isDarkTheme) Color(0xFFD97706) else GoogleMapsHighwayBorder,
                        style = Stroke(width = highwayStroke * 1.35f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        path = hPath,
                        color = if (isDarkTheme) Color(0xFFFBBF24) else GoogleMapsHighwayYellow,
                        style = Stroke(width = highwayStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // Suez Canal
                val canalPath = Path()
                EgyptMapData.SUEZ_CANAL.forEachIndexed { i, geo ->
                    val pt = geoToScreen(geo, width, height, zoomScale, curPan.x, curPan.y)
                    if (i == 0) canalPath.moveTo(pt.x, pt.y) else canalPath.lineTo(pt.x, pt.y)
                }
                drawPath(canalPath, color = if (isDarkTheme) Color(0xFF1E3A5F) else GoogleMapsWater, style = Stroke(width = (zoomScale * 2.5f).coerceIn(2.5f, 12f), cap = StrokeCap.Round))

                // Lake Nasser
                val nasserPath = Path()
                EgyptMapData.LAKE_NASSER_BODY.forEachIndexed { i, geo ->
                    val pt = geoToScreen(geo, width, height, zoomScale, curPan.x, curPan.y)
                    if (i == 0) nasserPath.moveTo(pt.x, pt.y) else nasserPath.lineTo(pt.x, pt.y)
                }
                nasserPath.close()
                drawPath(nasserPath, color = if (isDarkTheme) Color(0xFF1E3A5F) else GoogleMapsWater)

                // Lake Qarun
                val qarunPath = Path()
                EgyptMapData.LAKE_QARUN.forEachIndexed { i, geo ->
                    val pt = geoToScreen(geo, width, height, zoomScale, curPan.x, curPan.y)
                    if (i == 0) qarunPath.moveTo(pt.x, pt.y) else qarunPath.lineTo(pt.x, pt.y)
                }
                qarunPath.close()
                drawPath(qarunPath, color = if (isDarkTheme) Color(0xFF1E3A5F) else GoogleMapsWater)

                // River Nile Main Stream
                val nileStroke = (zoomScale * 3.0f).coerceIn(3.5f, 18.0f)
                val nilePath = Path()
                EgyptMapData.NILE_RIVER_MAIN.forEachIndexed { i, geo ->
                    val pt = geoToScreen(geo, width, height, zoomScale, curPan.x, curPan.y)
                    if (i == 0) nilePath.moveTo(pt.x, pt.y) else nilePath.lineTo(pt.x, pt.y)
                }
                drawPath(nilePath, color = if (isDarkTheme) Color(0xFF2563EB) else Color(0xFF76A9FA), style = Stroke(width = nileStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

                // Nile Rosetta Branch
                val rosettaPath = Path()
                EgyptMapData.NILE_ROSETTA_BRANCH.forEachIndexed { i, geo ->
                    val pt = geoToScreen(geo, width, height, zoomScale, curPan.x, curPan.y)
                    if (i == 0) rosettaPath.moveTo(pt.x, pt.y) else rosettaPath.lineTo(pt.x, pt.y)
                }
                drawPath(rosettaPath, color = if (isDarkTheme) Color(0xFF2563EB) else Color(0xFF76A9FA), style = Stroke(width = nileStroke * 0.7f, cap = StrokeCap.Round))

                // Nile Damietta Branch
                val damiettaPath = Path()
                EgyptMapData.NILE_DAMIETTA_BRANCH.forEachIndexed { i, geo ->
                    val pt = geoToScreen(geo, width, height, zoomScale, curPan.x, curPan.y)
                    if (i == 0) damiettaPath.moveTo(pt.x, pt.y) else damiettaPath.lineTo(pt.x, pt.y)
                }
                drawPath(damiettaPath, color = if (isDarkTheme) Color(0xFF2563EB) else Color(0xFF76A9FA), style = Stroke(width = nileStroke * 0.7f, cap = StrokeCap.Round))

                // Famous Bridges (Visible when zooming into cities)
                if (zoomScale >= 2.0f) {
                    EgyptMapData.FAMOUS_BRIDGES.forEach { bridge ->
                        val start = geoToScreen(bridge.startPt, width, height, zoomScale, curPan.x, curPan.y)
                        val end = geoToScreen(bridge.endPt, width, height, zoomScale, curPan.x, curPan.y)

                        drawLine(color = Color(0xFF5F6368), start = start, end = end, strokeWidth = 6f)
                        drawLine(color = Color(0xFFF1F3F4), start = start, end = end, strokeWidth = 4f)

                        if (zoomScale >= 3.2f) {
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#1557B0")
                                    textSize = 15f
                                    isFakeBoldText = true
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    setShadowLayer(4f, 0f, 0f, android.graphics.Color.WHITE)
                                }
                                val midX = (start.x + end.x) / 2f
                                val midY = (start.y + end.y) / 2f
                                drawText(bridge.nameAr, midX, midY - 6f, paint)
                            }
                        }
                    }
                }
            }

            // Navigation Route
            if (activeRoute != null && activeRoute.waypoints.isNotEmpty()) {
                val routePath = Path()
                activeRoute.waypoints.forEachIndexed { i, geo ->
                    val pt = geoToScreen(geo, width, height, zoomScale, curPan.x, curPan.y)
                    if (i == 0) routePath.moveTo(pt.x, pt.y) else routePath.lineTo(pt.x, pt.y)
                }

                drawPath(
                    path = routePath,
                    color = Color(0xFF1557B0),
                    style = Stroke(width = (zoomScale * 3.5f).coerceIn(6f, 16f), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = routePath,
                    color = Color(0xFF1A73E8),
                    style = Stroke(width = (zoomScale * 2.5f).coerceIn(4f, 12f), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = routePath,
                    color = Color.White,
                    style = Stroke(
                        width = (zoomScale * 2.5f).coerceIn(4f, 12f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(16f * zoomScale.coerceIn(1f, 2f), 26f * zoomScale.coerceIn(1f, 2f)),
                            routePhase * zoomScale.coerceIn(1f, 2f)
                        )
                    )
                )

                val startPt = geoToScreen(activeRoute.waypoints.first(), width, height, zoomScale, curPan.x, curPan.y)
                val endPt = geoToScreen(activeRoute.waypoints.last(), width, height, zoomScale, curPan.x, curPan.y)

                drawCircle(color = Color(0xFF1A73E8), radius = 6.5f * zoomScale.coerceIn(1f, 2f), center = startPt)
                drawCircle(color = Color.White, radius = 3.5f * zoomScale.coerceIn(1f, 2f), center = startPt)

                drawCircle(color = GoogleMapsPinRed, radius = 7.5f * zoomScale.coerceIn(1f, 2f), center = endPt)
                drawCircle(color = Color.White, radius = 3.5f * zoomScale.coerceIn(1f, 2f), center = endPt)
            }

            // ALL 27 GOVERNORATES: Always drawn clearly so the user sees all governorates!
            EgyptMapData.ALL_EGYPT_GOVERNORATES.forEach { city ->
                val pt = geoToScreen(city.location, width, height, zoomScale, curPan.x, curPan.y)

                val dotRadius = if (city.isCapital) 5.5f * zoomScale.coerceIn(1f, 2.2f) else 3.5f * zoomScale.coerceIn(1f, 2f)

                drawCircle(
                    color = if (city.isCapital) GoogleMapsPinRed else (if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF5F6368)),
                    radius = dotRadius,
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = dotRadius * 0.45f,
                    center = pt
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = if (city.isCapital) {
                            if (isDarkTheme) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#B3261E")
                        } else {
                            if (isDarkTheme) android.graphics.Color.parseColor("#F1F5F9") else android.graphics.Color.parseColor("#202124")
                        }
                        textSize = (if (city.isCapital) 24f else 18f) * (zoomScale.coerceIn(1.0f, 2.5f) * 0.35f + 0.65f)
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        setShadowLayer(5f, 0f, 0f, if (isDarkTheme) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                    }
                    drawText(city.nameAr, pt.x, pt.y - (8f * zoomScale.coerceIn(1f, 2f)), paint)
                }
            }

            // ALL CENTERS & DISTRICTS: Appear when zooming in closer (>= 1.6f and expands up to 25.0f)!
            if (zoomScale >= 1.6f) {
                EgyptMapData.ALL_CENTERS.forEach { dist ->
                    val pt = geoToScreen(dist.location, width, height, zoomScale, curPan.x, curPan.y)

                    // Pin dot
                    drawCircle(color = Color(0xFF1565C0), radius = 5.0f * zoomScale.coerceIn(1.2f, 2.4f), center = pt)
                    drawCircle(color = Color.White, radius = 2.4f * zoomScale.coerceIn(1.2f, 2.4f), center = pt)

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#0D47A1")
                            textSize = 16f * (zoomScale.coerceIn(1.6f, 3.5f) * 0.35f + 0.65f)
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            setShadowLayer(4f, 0f, 0f, android.graphics.Color.WHITE)
                        }
                        drawText(dist.nameAr, pt.x, pt.y - (8f * zoomScale.coerceIn(1.2f, 2.2f)), paint)
                    }
                }
            }

            // SERVICE POIs (Gas, Parking, Food, Cafes, Hospitals)
            if (selectedServiceType != null) {
                PlacesRepository.allServices.filter { it.type == selectedServiceType }.forEach { service ->
                    val pt = geoToScreen(service.location, width, height, zoomScale, curPan.x, curPan.y)
                    drawServiceMarker(
                        drawScope = this,
                        service = service,
                        center = pt,
                        zoom = zoomScale.coerceIn(1.0f, 2.5f)
                    )
                }
            }

            // PRIMARY PLACES / LANDMARKS
            places.forEach { place ->
                val isSelected = selectedPlace?.id == place.id
                val pt = geoToScreen(place.location, width, height, zoomScale, curPan.x, curPan.y)

                drawGoogleMapsPin(
                    drawScope = this,
                    place = place,
                    center = pt,
                    zoom = zoomScale.coerceIn(1.0f, 2.5f),
                    isSelected = isSelected,
                    isDark = isDarkTheme
                )
            }
        }
    }
}
