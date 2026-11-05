package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.FavoritePlaceEntity
import com.example.data.model.GeoPoint
import com.example.data.model.Place
import com.example.data.model.PlaceCategory
import com.example.data.model.PlacesRepository
import com.example.data.model.RouteInfo
import com.example.data.model.ServicePOI
import com.example.data.model.ServiceType
import com.example.data.network.NetworkMonitor
import com.example.data.network.OnlineExplorerService
import com.example.data.model.CityLabel
import com.example.data.network.OnlinePlaceDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val favoriteDao = db.favoriteDao()
    private val networkMonitor = NetworkMonitor(application)

    // Online connectivity state
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = networkMonitor.isCurrentlyOnline()
    )

    // Favorites from Room
    val favorites: StateFlow<List<FavoritePlaceEntity>> = favoriteDao.getAllFavorites().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Search and filter state
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<PlaceCategory?>(null)
    val selectedServiceType = MutableStateFlow<ServiceType?>(null)

    // Map zoom and camera center
    val zoomScale = MutableStateFlow(1.0f)
    val targetCenterGeo = MutableStateFlow<GeoPoint?>(null)

    // Filtered places
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

    // Selected place on map
    private val _selectedPlace = MutableStateFlow<Place?>(null)
    val selectedPlace: StateFlow<Place?> = _selectedPlace.asStateFlow()

    // Selected Service POI
    private val _selectedServicePOI = MutableStateFlow<ServicePOI?>(null)
    val selectedServicePOI: StateFlow<ServicePOI?> = _selectedServicePOI.asStateFlow()

    // Active Navigation route
    private val _activeRoute = MutableStateFlow<RouteInfo?>(null)
    val activeRoute: StateFlow<RouteInfo?> = _activeRoute.asStateFlow()

    // Map Layers
    val showCityLabels = MutableStateFlow(true)
    val isDarkTheme = MutableStateFlow(false)

    // Info/Credits Dialog visibility
    val showInfoDialog = MutableStateFlow(false)

    // Live Online extra data for selected place
    val onlinePlaceDetails = MutableStateFlow<OnlinePlaceDetails?>(null)
    val isFetchingOnline = MutableStateFlow(false)

    fun zoomIn() {
        zoomScale.value = (zoomScale.value * 1.6f).coerceAtMost(25.0f)
    }

    fun zoomOut() {
        zoomScale.value = (zoomScale.value / 1.6f).coerceAtLeast(0.75f)
    }

    fun setZoom(scale: Float) {
        zoomScale.value = scale
    }

    fun selectGovernorate(gov: CityLabel) {
        targetCenterGeo.value = gov.location
        zoomScale.value = 4.2f
        val govPlace = PlacesRepository.governorateToPlace(gov)
        selectPlace(govPlace)
    }

    fun enterGovernorate(geo: GeoPoint) {
        targetCenterGeo.value = geo
        zoomScale.value = 4.2f
    }

    fun selectPlace(place: Place) {
        _selectedPlace.value = place
        _selectedServicePOI.value = null
        onlinePlaceDetails.value = null
        targetCenterGeo.value = place.location
        if (zoomScale.value < 2.0f) {
            zoomScale.value = 3.5f
        }
        if (isOnline.value) {
            fetchOnlineMoreDetails(place)
        }
    }

    fun selectServicePOI(poi: ServicePOI) {
        _selectedServicePOI.value = poi
        val nearest = PlacesRepository.getNearestPlace(poi.location)
        _selectedPlace.value = nearest
        targetCenterGeo.value = poi.location
    }

    fun selectServiceType(type: ServiceType?) {
        selectedServiceType.value = type
    }

    fun clearSelectedPlace() {
        _selectedPlace.value = null
        _selectedServicePOI.value = null
        onlinePlaceDetails.value = null
    }

    fun startRouteTo(place: Place) {
        _selectedPlace.value = place
        _activeRoute.value = place.defaultRouteFromCairo
        targetCenterGeo.value = place.location
    }

    fun clearRoute() {
        _activeRoute.value = null
    }

    fun resetView() {
        zoomScale.value = 1.0f
        targetCenterGeo.value = PlacesRepository.CAIRO_LOCATION
        clearSelectedPlace()
        clearRoute()
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
