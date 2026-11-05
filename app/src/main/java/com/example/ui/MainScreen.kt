package com.example.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.components.InfoCreditsDialog
import com.example.ui.components.PlaceDetailSheet
import com.example.ui.favorites.FavoritesScreen
import com.example.ui.governorates.GovernorateDetailScreen
import com.example.ui.governorates.GovernoratesCatalogScreen
import com.example.ui.governorates.PlaceDetailDialog
import com.example.ui.map.MapTopBarAndControls
import com.example.ui.map.MapZoomAndLocationControls
import com.example.ui.map.OsmMapView
import com.example.ui.navigation.ActiveNavigationHUD
import com.example.ui.navigation.RoutePlanningCard
import com.example.ui.places.PlacesListScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.GoogleMapsBlue

enum class MainTab(val titleAr: String, val icon: ImageVector) {
    MAP("الخريطة", Icons.Default.Map),
    GOVERNORATES("المحافظات", Icons.Default.Apartment),
    PLACES("الأماكن", Icons.Default.Explore),
    FAVORITES("المحفوظات", Icons.Default.Bookmark),
    SETTINGS("الإعدادات", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val places by viewModel.places.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val activeRealRoute by viewModel.activeRealRoute.collectAsState()
    val isNavigating by viewModel.isNavigating.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    val distanceToNextManeuver by viewModel.distanceToNextManeuver.collectAsState()
    val isArrived by viewModel.isArrived.collectAsState()
    val isCalculatingRoute by viewModel.isCalculatingRoute.collectAsState()
    val routeErrorMessage by viewModel.routeErrorMessage.collectAsState()

    val mapTileType by viewModel.mapTileType.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedServiceType by viewModel.selectedServiceType.collectAsState()
    val showInfoDialog by viewModel.showInfoDialog.collectAsState()
    val onlineDetails by viewModel.onlinePlaceDetails.collectAsState()
    val isFetchingOnline by viewModel.isFetchingOnline.collectAsState()

    val targetCenterGeo by viewModel.targetCenterGeo.collectAsState()
    val targetZoom by viewModel.targetZoom.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val droppedPinLocation by viewModel.droppedPinLocation.collectAsState()

    val favoriteIds = favorites.map { it.placeId }.toSet()

    // Location Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.locationTracker.startTracking()
            viewModel.centerOnUserLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (!viewModel.locationTracker.hasLocationPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.locationTracker.startTracking()
        }
    }

    val selectedGovInfo by viewModel.selectedGovernorateInfo.collectAsState()
    val selectedPlaceForDialog by viewModel.selectedPlaceForDialog.collectAsState()
    val wikiArticle by viewModel.wikiArticleState.collectAsState()
    val isWikiLoading by viewModel.isWikiLoading.collectAsState()

    // Place detail dialog
    if (selectedPlaceForDialog != null) {
        PlaceDetailDialog(
            place = selectedPlaceForDialog!!,
            wikiArticle = wikiArticle,
            isWikiLoading = isWikiLoading,
            isOnline = isOnline,
            onFetchWiki = {
                val q = selectedPlaceForDialog!!.wikiSearchTerm ?: selectedPlaceForDialog!!.nameAr
                viewModel.fetchWikipediaArticle(q, forceRefresh = true)
            },
            onViewOnMap = { p ->
                viewModel.targetCenterGeo.value = p.location
                viewModel.targetZoom.value = 14.5
                viewModel.droppedPinLocation.value = p.location
                viewModel.droppedPinAddress.value = "${p.nameAr}، ${p.cityNameAr}، ${p.governorateNameAr}"
                selectedTab = 0
            },
            onGuideMe = { p ->
                val dynamicPlace = com.example.data.model.Place(
                    id = p.id,
                    nameAr = p.nameAr,
                    nameEn = p.type.titleAr,
                    governorateAr = p.governorateNameAr,
                    category = com.example.data.model.PlaceCategory.HISTORIC_CITIES,
                    location = p.location,
                    shortDescriptionAr = p.shortDescriptionAr,
                    namingOriginAr = p.fullDescriptionAr,
                    establishedDateAr = "معلم مصري موثق",
                    whyEstablishedAr = p.shortDescriptionAr,
                    egyptAffiliationAr = "جمهورية مصر العربية",
                    historicEventsAr = p.fullDescriptionAr,
                    rulersAndKingsAr = "",
                    famousBornAr = "",
                    visitingInfoAr = p.addressAr,
                    ticketPriceAr = "-",
                    openingHoursAr = "متاح",
                    interestingFactsAr = emptyList(),
                    bestTimeToVisitAr = "على مدار العام",
                    defaultRouteFromCairo = com.example.data.model.RouteInfo(
                        distanceKm = 0,
                        durationCarMinutes = 0,
                        durationBusMinutes = 0,
                        durationTrainMinutes = null,
                        mainHighwaysAr = emptyList(),
                        waypoints = listOf(com.example.data.model.PlacesRepository.CAIRO_LOCATION, p.location),
                        routeStops = emptyList(),
                        travelTipsAr = emptyList()
                    )
                )
                viewModel.selectPlace(dynamicPlace)
                viewModel.calculateRouteTo(dynamicPlace)
                selectedTab = 0
            },
            onDismissRequest = { viewModel.closePlaceDialog() }
        )
    }

    // Credits/Info Dialog
    val showCompassDialog by viewModel.showCompassDialog.collectAsState()
    val compassState by viewModel.compassState.collectAsState()

    if (showInfoDialog) {
        InfoCreditsDialog(onDismissRequest = { viewModel.closeInfoDialog() })
    }

    if (showCompassDialog) {
        com.example.ui.components.CompassAndQiblaDialog(
            compassState = compassState,
            gpsAccuracy = userLocation.accuracyMeters,
            currentSpeedKmh = userLocation.speedKmh,
            currentLocationName = if (userLocation.geoPoint != null) {
                "موقعك المباشر في مصر (${String.format(java.util.Locale.US, "%.2f, %.2f", userLocation.geoPoint!!.latitude, userLocation.geoPoint!!.longitude)})"
            } else {
                "القاهرة، مصر (مرجع)"
            },
            onDismissRequest = { viewModel.showCompassDialog.value = false }
        )
    }

    // Handle system back button
    BackHandler(enabled = isNavigating || activeRealRoute != null || selectedPlace != null || selectedGovInfo != null || selectedTab != 0) {
        when {
            selectedGovInfo != null -> viewModel.clearGovernorateInfo()
            isNavigating -> viewModel.exitNavigation()
            activeRealRoute != null -> viewModel.clearRoute()
            selectedPlace != null -> viewModel.clearSelectedPlace()
            selectedTab != 0 -> selectedTab = 0
        }
    }

    Scaffold(
        bottomBar = {
            if (!isNavigating && selectedGovInfo == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    MainTab.values().forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.titleAr
                                )
                            },
                            label = {
                                Text(
                                    text = tab.titleAr,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoogleMapsBlue,
                                selectedTextColor = GoogleMapsBlue,
                                indicatorColor = Color(0xFFE8F0FE),
                                unselectedIconColor = Color(0xFF5F6368),
                                unselectedTextColor = Color(0xFF5F6368)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: Real OpenStreetMap Engine for Egypt
                    Box(modifier = Modifier.fillMaxSize()) {
                        OsmMapView(
                            modifier = Modifier.fillMaxSize(),
                            mapTileType = mapTileType,
                            targetCenterGeo = targetCenterGeo,
                            targetZoom = targetZoom,
                            userLocation = userLocation,
                            places = places,
                            selectedPlace = selectedPlace,
                            selectedServiceType = selectedServiceType,
                            searchResults = searchResults,
                            activeRealRoute = activeRealRoute,
                            droppedPinLocation = droppedPinLocation,
                            onPlaceSelected = { place ->
                                viewModel.selectPlace(place)
                            },
                            onServiceSelected = { service ->
                                viewModel.selectServicePOI(service)
                            },
                            onSearchResultSelected = { result ->
                                viewModel.selectSearchResult(result)
                            },
                            onMapClicked = {
                                viewModel.clearSelectedPlace()
                                if (!isNavigating) {
                                    viewModel.clearRoute()
                                }
                            },
                            onMapLongClicked = { geo ->
                                viewModel.handleMapLongClick(geo)
                            }
                        )

                        // Top Search Bar & Governorates / Services Quick Bar (Hidden during active navigation)
                        if (!isNavigating) {
                            MapTopBarAndControls(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .statusBarsPadding(),
                                searchQuery = searchQuery,
                                searchResults = searchResults,
                                isSearching = isSearching,
                                selectedServiceType = selectedServiceType,
                                onSearchChange = { viewModel.onSearchQueryChanged(it) },
                                onSelectSearchResult = { viewModel.selectSearchResult(it) },
                                onSelectServiceType = { viewModel.selectServiceType(it) },
                                onSelectGovernorate = { gov ->
                                    viewModel.selectGovernorate(gov)
                                },
                                onResetView = {
                                    viewModel.resetView()
                                }
                            )
                        }

                        // Floating Map Controls: Tile Switcher, Zoom +/- and GPS Center
                        if (!isNavigating) {
                            MapZoomAndLocationControls(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(
                                        end = 16.dp,
                                        bottom = if (selectedPlace != null || activeRealRoute != null) 360.dp else 24.dp
                                    ),
                                mapTileType = mapTileType,
                                onCycleMapTileType = { viewModel.cycleMapTileType() },
                                onZoomIn = { viewModel.zoomIn() },
                                onZoomOut = { viewModel.zoomOut() },
                                onCenterOnUserLocation = {
                                    if (viewModel.locationTracker.hasLocationPermission()) {
                                        viewModel.centerOnUserLocation()
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                onOpenCompass = {
                                    viewModel.showCompassDialog.value = true
                                }
                            )
                        }

                        // Active Live Navigation HUD (Top Turn Banner + Bottom ETA status)
                        if (isNavigating && activeRealRoute != null) {
                            ActiveNavigationHUD(
                                route = activeRealRoute!!,
                                currentStepIndex = currentStepIndex,
                                distanceToNextManeuverMeters = distanceToNextManeuver,
                                currentSpeedKmh = userLocation.speedKmh,
                                isArrived = isArrived,
                                onRecalculate = { viewModel.recalculateCurrentRoute() },
                                onExitNavigation = { viewModel.exitNavigation() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Route Planning Card (Before starting active navigation)
                        AnimatedVisibility(
                            visible = activeRealRoute != null && !isNavigating,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it }),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            if (activeRealRoute != null) {
                                RoutePlanningCard(
                                    route = activeRealRoute!!,
                                    isCalculating = isCalculatingRoute,
                                    errorMessage = routeErrorMessage,
                                    onSelectTravelMode = { mode ->
                                        viewModel.setTravelMode(mode)
                                    },
                                    onStartNavigation = {
                                        viewModel.startLiveNavigation()
                                    },
                                    onClose = {
                                        viewModel.clearRoute()
                                    }
                                )
                            }
                        }

                        // Selected Place Details Bottom Sheet (When place selected and no route active)
                        AnimatedVisibility(
                            visible = selectedPlace != null && activeRealRoute == null && !isNavigating,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it }),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            if (selectedPlace != null) {
                                PlaceDetailSheet(
                                    place = selectedPlace!!,
                                    isFavorite = favoriteIds.contains(selectedPlace!!.id),
                                    isOnline = isOnline,
                                    onlineDetails = onlineDetails,
                                    isFetchingOnline = isFetchingOnline,
                                    onToggleFavorite = { viewModel.toggleFavorite(selectedPlace!!) },
                                    onShowRoute = { viewModel.calculateRouteTo(selectedPlace!!) },
                                    onFetchOnlineMore = { viewModel.fetchOnlineMoreDetails(selectedPlace!!) },
                                    onClose = { viewModel.clearSelectedPlace() }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Egypt Governorates Catalog & Detailed Wikipedia Screen
                    if (selectedGovInfo != null) {
                        GovernorateDetailScreen(
                            governorate = selectedGovInfo!!,
                            wikiArticle = wikiArticle,
                            isWikiLoading = isWikiLoading,
                            isOnline = isOnline,
                            onFetchWiki = {
                                viewModel.fetchWikipediaArticle(selectedGovInfo!!.nameAr, forceRefresh = true)
                            },
                            onBack = { viewModel.clearGovernorateInfo() },
                            onShowOnMap = { gov ->
                                viewModel.targetCenterGeo.value = gov.location
                                viewModel.targetZoom.value = 11.5
                                selectedTab = 0
                            },
                            onSelectPlace = { place ->
                                viewModel.openPlaceDialog(place)
                            }
                        )
                    } else {
                        GovernoratesCatalogScreen(
                            onSelectGovernorate = { gov ->
                                viewModel.selectGovernorateInfo(gov)
                            },
                            onShowOnMap = { gov ->
                                viewModel.targetCenterGeo.value = gov.location
                                viewModel.targetZoom.value = 11.5
                                selectedTab = 0
                            }
                        )
                    }
                }

                2 -> {
                    // TAB 2: Places Directory & Exploration
                    PlacesListScreen(
                        places = places,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        favoritePlaceIds = favoriteIds,
                        onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onSelectCategory = { viewModel.selectedCategory.value = it },
                        onPlaceClick = { place ->
                            viewModel.selectPlace(place)
                            selectedTab = 0
                        },
                        onNavigateToPlace = { place ->
                            viewModel.selectPlace(place)
                            viewModel.calculateRouteTo(place)
                            selectedTab = 0
                        },
                        onToggleFavorite = { place ->
                            viewModel.toggleFavorite(place)
                        }
                    )
                }

                3 -> {
                    // TAB 3: Saved Favorites & User Custom Notes
                    FavoritesScreen(
                        favorites = favorites,
                        onSelectPlace = { place ->
                            viewModel.selectPlace(place)
                            selectedTab = 0
                        },
                        onNavigateToPlace = { place ->
                            viewModel.selectPlace(place)
                            viewModel.calculateRouteTo(place)
                            selectedTab = 0
                        },
                        onRemoveFavorite = { placeId ->
                            viewModel.removeFavorite(placeId)
                        },
                        onUpdateNote = { placeId, note ->
                            viewModel.updateFavoriteNote(placeId, note)
                        },
                        onExplorePlaces = {
                            selectedTab = 2
                        }
                    )
                }

                4 -> {
                    // TAB 4: Settings & Offline Cache Info
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        showCityLabels = viewModel.showCityLabels.collectAsState().value,
                        isOnline = isOnline,
                        onToggleDarkTheme = { viewModel.setDarkTheme(it) },
                        onToggleCityLabels = { viewModel.showCityLabels.value = it },
                        onOpenInfoDialog = { viewModel.openInfoDialog() }
                    )
                }
            }
        }
    }
}
