package com.example.ui.map

import android.content.Context
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.location.UserLocationState
import com.example.data.model.GeoPoint
import com.example.data.model.Place
import com.example.data.model.PlacesRepository
import com.example.data.model.ServicePOI
import com.example.data.model.ServiceType
import com.example.data.network.RealRoute
import com.example.data.network.SearchResultItem
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

enum class MapTileType(val titleAr: String) {
    STANDARD("الشوارع والمعالم (OSM)"),
    CARTODB_LIGHT("فاتح حديث (CartoDB)"),
    CARTODB_DARK("الوضع الليلي"),
    TOPO("تضاريس وطبوغرافيا")
}

// Bounding box strictly covering Egypt territory
private val EGYPT_BOUNDS = BoundingBox(
    32.0, // North (Mediterranean Sea Coast)
    37.0, // East (Red Sea / Sinai border)
    21.5, // South (Sudan border)
    24.5  // West (Libya border)
)

fun GeoPoint.toOsm(): OsmGeoPoint = OsmGeoPoint(latitude, longitude)
fun OsmGeoPoint.toDomain(): GeoPoint = GeoPoint(latitude, longitude)

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    mapTileType: MapTileType = MapTileType.STANDARD,
    targetCenterGeo: GeoPoint? = null,
    targetZoom: Double? = null,
    userLocation: UserLocationState? = null,
    places: List<Place> = emptyList(),
    selectedPlace: Place? = null,
    selectedServiceType: ServiceType? = null,
    searchResults: List<SearchResultItem> = emptyList(),
    activeRealRoute: RealRoute? = null,
    droppedPinLocation: GeoPoint? = null,
    onPlaceSelected: (Place) -> Unit,
    onServiceSelected: (ServicePOI) -> Unit,
    onSearchResultSelected: (SearchResultItem) -> Unit,
    onMapClicked: (GeoPoint) -> Unit,
    onMapLongClicked: (GeoPoint) -> Unit
) {
    val context = LocalContext.current

    val mapView = remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "ArafniBaladi-Egypt/2.0 (support@arafni-baladi.eg)"

        MapView(context).apply {
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            // Limit zoom out so user cannot see the whole world, only Egypt
            minZoomLevel = 5.8
            maxZoomLevel = 19.5
            isTilesScaledToDpi = true
            // Confine scrolling strictly within Egypt bounds
            setScrollableAreaLimitDouble(EGYPT_BOUNDS)
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            controller.setZoom(6.8)
            // Center of Egypt (Nile Valley / Cairo southward)
            controller.setCenter(OsmGeoPoint(26.9, 30.8))
        }
    }

    // Lifecycle cleanup
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    // Apply tile source
    LaunchedEffect(mapTileType) {
        val tileSource = when (mapTileType) {
            MapTileType.STANDARD -> TileSourceFactory.MAPNIK
            MapTileType.TOPO -> XYTileSource(
                "OpenTopoMap",
                0, 17, 256, ".png",
                arrayOf("https://a.tile.opentopomap.org/", "https://b.tile.opentopomap.org/", "https://c.tile.opentopomap.org/"),
                "OpenTopoMap"
            )
            MapTileType.CARTODB_LIGHT -> XYTileSource(
                "CartoDBPositron",
                0, 19, 256, ".png",
                arrayOf("https://a.basemaps.cartocdn.com/light_all/", "https://b.basemaps.cartocdn.com/light_all/"),
                "CartoDB Positron"
            )
            MapTileType.CARTODB_DARK -> XYTileSource(
                "CartoDBDark",
                0, 19, 256, ".png",
                arrayOf("https://a.basemaps.cartocdn.com/dark_all/", "https://b.basemaps.cartocdn.com/dark_all/"),
                "CartoDB Dark"
            )
        }
        mapView.setTileSource(tileSource)
        mapView.invalidate()
    }

    // Animate camera to target center
    LaunchedEffect(targetCenterGeo, targetZoom) {
        if (targetCenterGeo != null) {
            if (targetZoom != null) {
                mapView.controller.setZoom(targetZoom)
            }
            mapView.controller.animateTo(targetCenterGeo.toOsm())
        }
    }

    // Update Overlays
    LaunchedEffect(
        places,
        selectedPlace,
        selectedServiceType,
        searchResults,
        activeRealRoute,
        userLocation,
        droppedPinLocation
    ) {
        mapView.overlays.clear()

        // 1. Map events overlay for taps and long-presses
        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: OsmGeoPoint): Boolean {
                onMapClicked(p.toDomain())
                return false
            }

            override fun longPressHelper(p: OsmGeoPoint): Boolean {
                onMapLongClicked(p.toDomain())
                return true
            }
        })
        mapView.overlays.add(eventsOverlay)

        // 2. Active Route Polyline
        if (activeRealRoute != null && activeRealRoute.geometry.isNotEmpty()) {
            val polylineBorder = Polyline(mapView).apply {
                outlinePaint.color = 0xFF1557B0.toInt()
                outlinePaint.strokeWidth = 18f
                outlinePaint.strokeCap = Paint.Cap.ROUND
                outlinePaint.strokeJoin = Paint.Join.ROUND
                setPoints(activeRealRoute.geometry.map { it.toOsm() })
            }
            mapView.overlays.add(polylineBorder)

            val polylineMain = Polyline(mapView).apply {
                outlinePaint.color = 0xFF1A73E8.toInt()
                outlinePaint.strokeWidth = 12f
                outlinePaint.strokeCap = Paint.Cap.ROUND
                outlinePaint.strokeJoin = Paint.Join.ROUND
                setPoints(activeRealRoute.geometry.map { it.toOsm() })
            }
            mapView.overlays.add(polylineMain)

            // Destination Pin on Route
            val destMarker = Marker(mapView).apply {
                position = activeRealRoute.destination.toOsm()
                title = activeRealRoute.destinationNameAr
                snippet = "نقطة الوصول: ${activeRealRoute.distanceKmFormatted} • ${activeRealRoute.durationFormatted}"
                icon = MapMarkerHelper.createTeardropPin(context, 0xFFD93025.toInt(), iconSymbol = "🏁")
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(destMarker)
        }

        // 3. User GPS Location Marker
        if (userLocation?.geoPoint != null) {
            val userMarker = Marker(mapView).apply {
                position = userLocation.geoPoint.toOsm()
                title = "موقعك الحالي"
                snippet = if (userLocation.speedKmh > 1f) {
                    "السرعة: ${userLocation.speedKmh.toInt()} كم/ساعة"
                } else {
                    "دقة الموقع: ${userLocation.accuracyMeters.toInt()} متر"
                }
                icon = MapMarkerHelper.createUserLocationMarker(context, userLocation.bearingDegrees)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            mapView.overlays.add(userMarker)
        }

        // 4. Dropped Pin (user long-pressed on map)
        if (droppedPinLocation != null) {
            val droppedMarker = Marker(mapView).apply {
                position = droppedPinLocation.toOsm()
                title = "موقع محدد على الخريطة"
                snippet = "انقر لعرض تفاصيل الموقع وطلب مسار"
                icon = MapMarkerHelper.createTeardropPin(context, 0xFF8E24AA.toInt(), iconSymbol = "📍")
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    val nearest = PlacesRepository.getNearestPlace(droppedPinLocation)
                    onPlaceSelected(nearest)
                    true
                }
            }
            mapView.overlays.add(droppedMarker)
        }

        // 5. Search Results Markers
        if (searchResults.isNotEmpty()) {
            searchResults.forEach { item ->
                val marker = Marker(mapView).apply {
                    position = item.location.toOsm()
                    title = item.titleAr
                    snippet = item.subtitleAr
                    icon = MapMarkerHelper.createTeardropPin(context, 0xFF1976D2.toInt(), iconSymbol = "🔍")
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onSearchResultSelected(item)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
        }

        // 6. Service POIs (Hospitals, Gas Stations, Restaurants, Cafes, Parking)
        if (selectedServiceType != null) {
            PlacesRepository.allServices.filter { it.type == selectedServiceType }.forEach { service ->
                val marker = Marker(mapView).apply {
                    position = service.location.toOsm()
                    title = service.nameAr
                    snippet = "${service.addressAr} (${service.openStatusAr})"
                    icon = MapMarkerHelper.getServiceMarker(context, service.type)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onServiceSelected(service)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
        }

        // 7. Verified Places & Landmarks
        places.forEach { place ->
            val isSelected = selectedPlace?.id == place.id
            val pinColor = if (isSelected) 0xFFD93025.toInt() else 0xFF1565C0.toInt()
            val marker = Marker(mapView).apply {
                position = place.location.toOsm()
                title = place.nameAr
                snippet = "${place.governorateAr} • ${place.category.titleAr}"
                icon = MapMarkerHelper.createTeardropPin(
                    context,
                    pinColor,
                    iconSymbol = if (isSelected) "★" else "🏛"
                )
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    onPlaceSelected(place)
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}
