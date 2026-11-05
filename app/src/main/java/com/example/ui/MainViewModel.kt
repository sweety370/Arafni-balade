package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.FavoritePlaceEntity
import com.example.data.location.LocationTracker
import com.example.data.location.UserLocationState
import com.example.data.model.CityLabel
import com.example.data.model.GeoPoint
import com.example.data.model.Place
import com.example.data.model.PlaceCategory
import com.example.data.model.PlacesRepository
import com.example.data.model.RouteInfo
import com.example.data.model.ServicePOI
import com.example.data.model.ServiceType
import com.example.data.network.GeocodingService
import com.example.data.network.NetworkMonitor
import com.example.data.network.OnlineExplorerService
import com.example.data.network.OnlinePlaceDetails
import com.example.data.network.RealRoute
import com.example.data.network.RoutingService
import com.example.data.network.SearchResultItem
import com.example.data.network.TravelMode
import com.example.ui.map.MapTileType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val favoriteDao = db.favoriteDao()
    private val networkMonitor = NetworkMonitor(application)
    val locationTracker = LocationTracker(application)

    // Online connectivity state
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = networkMonitor.isCurrentlyOnline()
    )

    // User GPS Location State
    val userLocation: StateFlow<UserLocationState> = locationTracker.locationState

    // Favorites from Room Database
    val favorites: StateFlow<List<FavoritePlaceEntity>> = favoriteDao.getAllFavorites().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Map style / Tile type
    val mapTileType = MutableStateFlow(MapTileType.STANDARD)

    // Search and filter state
    val searchQuery = MutableStateFlow("")
    val searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val isSearching = MutableStateFlow(false)
    private var searchJob: Job? = null

    val selectedCategory = MutableStateFlow<PlaceCategory?>(null)
    val selectedServiceType = MutableStateFlow<ServiceType?>(null)

    // Map camera center & zoom
    val targetCenterGeo = MutableStateFlow<GeoPoint?>(PlacesRepository.CAIRO_LOCATION)
    val targetZoom = MutableStateFlow<Double?>(6.8)

    // Selected place / POI on map
    private val _selectedPlace = MutableStateFlow<Place?>(null)
    val selectedPlace: StateFlow<Place?> = _selectedPlace.asStateFlow()

    private val _selectedServicePOI = MutableStateFlow<ServicePOI?>(null)
    val selectedServicePOI: StateFlow<ServicePOI?> = _selectedServicePOI.asStateFlow()

    // User Dropped Pin (on long-press)
    val droppedPinLocation = MutableStateFlow<GeoPoint?>(null)
    val droppedPinAddress = MutableStateFlow<String?>(null)

    // Real Navigation Route state
    private val _activeRealRoute = MutableStateFlow<RealRoute?>(null)
    val activeRealRoute: StateFlow<RealRoute?> = _activeRealRoute.asStateFlow()

    val isCalculatingRoute = MutableStateFlow(false)
    val routeTravelMode = MutableStateFlow(TravelMode.DRIVING)

    // Active Navigation HUD state
    val isNavigating = MutableStateFlow(false)
    val currentStepIndex = MutableStateFlow(0)
    val distanceToNextManeuver = MutableStateFlow(0.0)
    val isArrived = MutableStateFlow(false)

    // Legacy RouteInfo for backward compatibility
    val activeRoute: StateFlow<RouteInfo?> = MutableStateFlow(null)

    // Map layer & visual settings
    val showCityLabels = MutableStateFlow(true)
    val isDarkTheme = MutableStateFlow(false)
    val showInfoDialog = MutableStateFlow(false)

    // Online Wikipedia extra details
    val onlinePlaceDetails = MutableStateFlow<OnlinePlaceDetails?>(null)
    val isFetchingOnline = MutableStateFlow(false)

    // Filtered verified places
    val places: StateFlow<List<Place>> = combine(searchQuery, selectedCategory) { query, cat ->
        var list = if (query.isBlank()) PlacesRepository.places else PlacesRepository.searchPlaces(query)
        if (cat != null) {
            list = list.filter { it.category == cat }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlacesRepository.places
    )

    init {
        // Start GPS updates if permission is granted
        if (locationTracker.hasLocationPermission()) {
            locationTracker.startTracking()
        }

        // Monitor location during active navigation to update turn steps and arrival
        viewModelScope.launch {
            userLocation.collect { locState ->
                if (isNavigating.value && locState.geoPoint != null && _activeRealRoute.value != null) {
                    updateNavigationProgress(locState.geoPoint, _activeRealRoute.value!!)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationTracker.stopTracking()
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            searchResults.value = emptyList()
            isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // Debounce search keystrokes
            isSearching.value = true
            val results = GeocodingService.searchEgypt(newQuery)
            searchResults.value = results.getOrDefault(emptyList())
            isSearching.value = false
        }
    }

    fun selectSearchResult(item: SearchResultItem) {
        targetCenterGeo.value = item.location
        targetZoom.value = 14.5
        droppedPinLocation.value = item.location
        droppedPinAddress.value = item.subtitleAr

        // Find if this corresponds to a verified Place
        val matchingPlace = places.value.find { it.id == item.id }
        if (matchingPlace != null) {
            selectPlace(matchingPlace)
        } else {
            // Create a dynamic Place from the search result
            val dynamicPlace = Place(
                id = item.id,
                nameAr = item.titleAr,
                nameEn = item.categoryAr,
                governorateAr = item.subtitleAr,
                category = PlaceCategory.HISTORIC_CITIES,
                location = item.location,
                shortDescriptionAr = "${item.titleAr} - ${item.subtitleAr}",
                namingOriginAr = "موقع جغرافي موثق في جمهورية مصر العربية.",
                establishedDateAr = "معلم وموقع مسجل",
                whyEstablishedAr = item.categoryAr,
                egyptAffiliationAr = "جمهورية مصر العربية",
                historicEventsAr = "من المواقع الحيوية في ${item.subtitleAr}",
                rulersAndKingsAr = "معلم مصري",
                famousBornAr = "موقع مسجل",
                visitingInfoAr = "متاح للزيارة والتنقل",
                ticketPriceAr = "غير محدد",
                openingHoursAr = "متاح",
                interestingFactsAr = listOf(item.subtitleAr),
                bestTimeToVisitAr = "على مدار العام",
                defaultRouteFromCairo = RouteInfo(
                    distanceKm = 0,
                    durationCarMinutes = 0,
                    durationBusMinutes = 0,
                    durationTrainMinutes = null,
                    mainHighwaysAr = listOf(item.subtitleAr),
                    waypoints = listOf(PlacesRepository.CAIRO_LOCATION, item.location),
                    routeStops = emptyList(),
                    travelTipsAr = emptyList()
                )
            )
            selectPlace(dynamicPlace)
        }
    }

    fun selectGovernorate(gov: CityLabel) {
        targetCenterGeo.value = gov.location
        targetZoom.value = 11.5
        val govPlace = PlacesRepository.governorateToPlace(gov)
        selectPlace(govPlace)
    }

    fun selectPlace(place: Place) {
        _selectedPlace.value = place
        _selectedServicePOI.value = null
        onlinePlaceDetails.value = null
        droppedPinLocation.value = place.location
        droppedPinAddress.value = "${place.nameAr}، ${place.governorateAr}"
        targetCenterGeo.value = place.location
        targetZoom.value = 14.0

        if (isOnline.value) {
            fetchOnlineMoreDetails(place)
        }
    }

    fun selectServicePOI(poi: ServicePOI) {
        _selectedServicePOI.value = poi
        val nearest = PlacesRepository.getNearestPlace(poi.location)
        _selectedPlace.value = nearest
        droppedPinLocation.value = poi.location
        droppedPinAddress.value = poi.addressAr
        targetCenterGeo.value = poi.location
        targetZoom.value = 15.0
    }

    fun selectServiceType(type: ServiceType?) {
        selectedServiceType.value = type
        if (type != null) {
            val firstService = PlacesRepository.allServices.find { it.type == type }
            if (firstService != null && targetZoom.value ?: 0.0 < 10.0) {
                targetZoom.value = 11.0
            }
        }
    }

    fun clearSelectedPlace() {
        _selectedPlace.value = null
        _selectedServicePOI.value = null
        onlinePlaceDetails.value = null
        droppedPinLocation.value = null
        droppedPinAddress.value = null
    }

    fun handleMapLongClick(geo: GeoPoint) {
        droppedPinLocation.value = geo
        targetCenterGeo.value = geo
        viewModelScope.launch {
            val addrResult = GeocodingService.reverseGeocode(geo)
            val addr = addrResult.getOrDefault("موقع محدد على الخريطة")
            droppedPinAddress.value = addr

            // Create a dynamic place at this clicked location
            val pinPlace = Place(
                id = "pin_${geo.latitude}_${geo.longitude}",
                nameAr = addr.split("،").firstOrNull() ?: "موقع محدد",
                nameEn = "Pinned Location",
                governorateAr = addr,
                category = PlaceCategory.HISTORIC_CITIES,
                location = geo,
                shortDescriptionAr = "إحداثيات: ${String.format(java.util.Locale.US, "%.4f, %.4f", geo.latitude, geo.longitude)} • $addr",
                namingOriginAr = "تم تحديد هذا الموقع بواسطة النقر المطول على الخريطة.",
                establishedDateAr = "موقع مخصص",
                whyEstablishedAr = "نقطة ملاحة مخصصة",
                egyptAffiliationAr = "جمهورية مصر العربية",
                historicEventsAr = "",
                rulersAndKingsAr = "",
                famousBornAr = "",
                visitingInfoAr = addr,
                ticketPriceAr = "-",
                openingHoursAr = "-",
                interestingFactsAr = emptyList(),
                bestTimeToVisitAr = "أي وقت",
                defaultRouteFromCairo = RouteInfo(
                    distanceKm = 0,
                    durationCarMinutes = 0,
                    durationBusMinutes = 0,
                    durationTrainMinutes = null,
                    mainHighwaysAr = emptyList(),
                    waypoints = listOf(PlacesRepository.CAIRO_LOCATION, geo),
                    routeStops = emptyList(),
                    travelTipsAr = emptyList()
                )
            )
            _selectedPlace.value = pinPlace
        }
    }

    fun calculateRouteTo(place: Place, mode: TravelMode = routeTravelMode.value) {
        val startLocation = userLocation.value.geoPoint ?: PlacesRepository.CAIRO_LOCATION
        val endLocation = place.location
        val destName = place.nameAr

        routeTravelMode.value = mode
        isCalculatingRoute.value = true

        viewModelScope.launch {
            val result = RoutingService.calculateRoute(
                start = startLocation,
                end = endLocation,
                destinationName = destName,
                mode = mode
            )

            result.onSuccess { route ->
                _activeRealRoute.value = route
                currentStepIndex.value = 0
                distanceToNextManeuver.value = route.steps.firstOrNull()?.distanceMeters ?: 0.0
                isArrived.value = false
                targetCenterGeo.value = route.origin
                targetZoom.value = 13.0
            }.onFailure {
                // If service error occurs, use the realistic Egyptian corridor fallback
                val fallback = RoutingService.calculateRoute(
                    start = startLocation,
                    end = endLocation,
                    destinationName = destName,
                    mode = mode
                ).getOrNull()
                _activeRealRoute.value = fallback
            }

            isCalculatingRoute.value = false
        }
    }

    fun setTravelMode(mode: TravelMode) {
        val curRoute = _activeRealRoute.value ?: return
        val currentDestPlace = _selectedPlace.value
        if (currentDestPlace != null) {
            calculateRouteTo(currentDestPlace, mode)
        } else {
            // Re-route between existing route endpoints
            viewModelScope.launch {
                isCalculatingRoute.value = true
                val result = RoutingService.calculateRoute(
                    start = curRoute.origin,
                    end = curRoute.destination,
                    destinationName = curRoute.destinationNameAr,
                    mode = mode
                )
                if (result.isSuccess) {
                    _activeRealRoute.value = result.getOrNull()
                    routeTravelMode.value = mode
                }
                isCalculatingRoute.value = false
            }
        }
    }

    fun startLiveNavigation() {
        if (_activeRealRoute.value != null) {
            isNavigating.value = true
            isArrived.value = false
            currentStepIndex.value = 0
            val userPt = userLocation.value.geoPoint
            if (userPt != null) {
                targetCenterGeo.value = userPt
                targetZoom.value = 16.5
            }
        }
    }

    fun exitNavigation() {
        isNavigating.value = false
        isArrived.value = false
    }

    fun clearRoute() {
        _activeRealRoute.value = null
        isNavigating.value = false
        isArrived.value = false
    }

    fun recalculateCurrentRoute() {
        val dest = _activeRealRoute.value?.destination ?: _selectedPlace.value?.location ?: return
        val destName = _activeRealRoute.value?.destinationNameAr ?: _selectedPlace.value?.nameAr ?: "الوجهة"
        val userPt = userLocation.value.geoPoint ?: PlacesRepository.CAIRO_LOCATION

        viewModelScope.launch {
            isCalculatingRoute.value = true
            val result = RoutingService.calculateRoute(
                start = userPt,
                end = dest,
                destinationName = destName,
                mode = routeTravelMode.value
            )
            result.onSuccess {
                _activeRealRoute.value = it
                currentStepIndex.value = 0
                isArrived.value = false
            }
            isCalculatingRoute.value = false
        }
    }

    private fun updateNavigationProgress(userPt: GeoPoint, route: RealRoute) {
        val distanceToDestMeters = RoutingService.calculateHaversineKm(userPt, route.destination) * 1000.0

        // Arrival detection: within 35 meters of destination
        if (distanceToDestMeters <= 35.0) {
            isArrived.value = true
            distanceToNextManeuver.value = 0.0
            return
        }

        // Check distance to next maneuver step
        val steps = route.steps
        val curIndex = currentStepIndex.value
        if (curIndex in steps.indices) {
            val step = steps[curIndex]
            // Advance step if user made progress
            distanceToNextManeuver.value = (step.distanceMeters - 20.0).coerceAtLeast(0.0)
            if (step.distanceMeters <= 20.0 && curIndex + 1 < steps.size) {
                currentStepIndex.value = curIndex + 1
            }
        }
    }

    fun cycleMapTileType() {
        val types = MapTileType.values()
        val curIndex = types.indexOf(mapTileType.value)
        val nextIndex = (curIndex + 1) % types.size
        mapTileType.value = types[nextIndex]
    }

    fun centerOnUserLocation() {
        val userPt = userLocation.value.geoPoint
        if (userPt != null) {
            targetCenterGeo.value = userPt
            targetZoom.value = 15.5
        } else {
            // If GPS location not yet acquired, check permission & center on Cairo
            locationTracker.startTracking()
            targetCenterGeo.value = PlacesRepository.CAIRO_LOCATION
            targetZoom.value = 10.0
        }
    }

    fun resetView() {
        targetCenterGeo.value = PlacesRepository.CAIRO_LOCATION
        targetZoom.value = 6.8
        clearSelectedPlace()
        clearRoute()
    }

    fun zoomIn() {
        val current = targetZoom.value ?: 7.0
        targetZoom.value = (current + 1.5).coerceAtMost(19.0)
    }

    fun zoomOut() {
        val current = targetZoom.value ?: 7.0
        targetZoom.value = (current - 1.5).coerceAtLeast(4.5)
    }

    fun toggleFavorite(place: Place) {
        viewModelScope.launch {
            val favs = favorites.value
            val existing = favs.find { it.placeId == place.id }
            if (existing != null) {
                favoriteDao.deleteFavoriteById(place.id)
            } else {
                favoriteDao.insertFavorite(
                    FavoritePlaceEntity(
                        placeId = place.id,
                        nameAr = place.nameAr,
                        governorateAr = place.governorateAr,
                        categoryName = place.category.titleAr
                    )
                )
            }
        }
    }

    fun removeFavorite(placeId: String) {
        viewModelScope.launch {
            favoriteDao.deleteFavoriteById(placeId)
        }
    }

    fun updateFavoriteNote(placeId: String, note: String) {
        viewModelScope.launch {
            favoriteDao.updateNote(placeId, note)
        }
    }

    fun fetchOnlineMoreDetails(place: Place) {
        viewModelScope.launch {
            isFetchingOnline.value = true
            val result = OnlineExplorerService.fetchExpandedInfo(place.id, place.nameAr)
            if (result.isSuccess) {
                onlinePlaceDetails.value = result.getOrNull()
            }
            isFetchingOnline.value = false
        }
    }

    fun openInfoDialog() {
        showInfoDialog.value = true
    }

    fun closeInfoDialog() {
        showInfoDialog.value = false
    }
}
