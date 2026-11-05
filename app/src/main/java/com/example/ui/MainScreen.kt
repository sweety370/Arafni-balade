package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.CityLabel
import com.example.ui.components.InfoCreditsDialog
import com.example.ui.components.NavigationRouteCard
import com.example.ui.components.PlaceDetailSheet
import com.example.ui.favorites.FavoritesScreen
import com.example.ui.map.EgyptMapCanvas
import com.example.ui.map.MapTopBarAndControls
import com.example.ui.map.MapZoomAndLocationControls
import com.example.ui.places.PlacesListScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.GoogleMapsBlue

enum class MainTab(val titleAr: String, val icon: ImageVector) {
    MAP("الخريطة", Icons.Default.Map),
    PLACES("دليل الأماكن", Icons.Default.Explore),
    FAVORITES("المحفوظات", Icons.Default.Bookmark),
    SETTINGS("الإعدادات", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val places by viewModel.places.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val activeRoute by viewModel.activeRoute.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedServiceType by viewModel.selectedServiceType.collectAsState()
    val showInfoDialog by viewModel.showInfoDialog.collectAsState()
    val onlineDetails by viewModel.onlinePlaceDetails.collectAsState()
    val isFetchingOnline by viewModel.isFetchingOnline.collectAsState()
    val zoomScale by viewModel.zoomScale.collectAsState()
    val targetCenterGeo by viewModel.targetCenterGeo.collectAsState()

    val favoriteIds = favorites.map { it.placeId }.toSet()

    // Handle back button
    BackHandler(enabled = selectedPlace != null || activeRoute != null || selectedTab != 0) {
        if (selectedPlace != null) {
            viewModel.clearSelectedPlace()
        } else if (activeRoute != null) {
            viewModel.clearRoute()
        } else if (selectedTab != 0) {
            selectedTab = 0
        }
    }

    // Credits/Info Dialog (Amr Hussein & Mohamed Hussein)
    if (showInfoDialog) {
        InfoCreditsDialog(onDismissRequest = { viewModel.closeInfoDialog() })
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                MainTab.values().forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        },
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: Interactive Egypt Vector Map with Google Maps Styling & Deep Zoom into Governorates
                    Box(modifier = Modifier.fillMaxSize()) {
                        EgyptMapCanvas(
                            modifier = Modifier.fillMaxSize(),
                            places = places,
                            selectedPlace = selectedPlace,
                            activeRoute = activeRoute,
                            selectedServiceType = selectedServiceType,
                            isDarkTheme = isDarkTheme,
                            zoomScaleState = zoomScale,
                            targetCenterGeo = targetCenterGeo,
                            onZoomScaleChange = { viewModel.setZoom(it) },
                            onPlaceSelected = { place ->
                                viewModel.selectPlace(place)
                            },
                            onServiceSelected = { service ->
                                viewModel.selectServicePOI(service)
                            }
                        )

                        // Top floating search bar & quick Governorates Jump & Service Filters
                        MapTopBarAndControls(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding(),
                            searchQuery = searchQuery,
                            selectedServiceType = selectedServiceType,
                            onSearchChange = { viewModel.searchQuery.value = it },
                            onSelectServiceType = { viewModel.selectServiceType(it) },
                            onSelectGovernorate = { gov ->
                                viewModel.selectGovernorate(gov)
                            },
                            onResetView = {
                                viewModel.resetView()
                            }
                        )

                        // Floating Zoom in (+), Zoom out (-), and Recenter Controls
                        MapZoomAndLocationControls(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = if (selectedPlace != null || activeRoute != null) 360.dp else 24.dp),
                            onZoomIn = { viewModel.zoomIn() },
                            onZoomOut = { viewModel.zoomOut() },
                            onResetView = {
                                viewModel.resetView()
                            }
                        )

                        // Active Navigation Route Card
                        AnimatedVisibility(
                            visible = activeRoute != null && selectedPlace != null,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it }),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            if (selectedPlace != null && activeRoute != null) {
                                NavigationRouteCard(
                                    destinationPlace = selectedPlace!!,
                                    routeInfo = activeRoute!!,
                                    onCloseRoute = { viewModel.clearRoute() }
                                )
                            }
                        }

                        // Selected Place / Center Bottom Sheet (with full detailed history)
                        AnimatedVisibility(
                            visible = selectedPlace != null && activeRoute == null,
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
                                    onShowRoute = { viewModel.startRouteTo(selectedPlace!!) },
                                    onFetchOnlineMore = { viewModel.fetchOnlineMoreDetails(selectedPlace!!) },
                                    onClose = { viewModel.clearSelectedPlace() }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Places & Centers Directory & Search
                    PlacesListScreen(
                        places = places,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        favoritePlaceIds = favoriteIds,
                        onSearchQueryChange = { viewModel.searchQuery.value = it },
                        onSelectCategory = { viewModel.selectedCategory.value = it },
                        onPlaceClick = { place ->
                            viewModel.selectPlace(place)
                            selectedTab = 0
                        },
                        onNavigateToPlace = { place ->
                            viewModel.startRouteTo(place)
                            selectedTab = 0
                        },
                        onToggleFavorite = { place ->
                            viewModel.toggleFavorite(place)
                        }
                    )
                }

                2 -> {
                    // TAB 2: Favorites & Bookmarks
                    FavoritesScreen(
                        favorites = favorites,
                        onSelectPlace = { place ->
                            viewModel.selectPlace(place)
                            selectedTab = 0
                        },
                        onNavigateToPlace = { place ->
                            viewModel.startRouteTo(place)
                            selectedTab = 0
                        },
                        onRemoveFavorite = { placeId ->
                            viewModel.removeFavorite(placeId)
                        },
                        onUpdateNote = { placeId, note ->
                            viewModel.updateFavoriteNote(placeId, note)
                        },
                        onExplorePlaces = {
                            selectedTab = 1
                        }
                    )
                }

                3 -> {
                    // TAB 3: Settings & Info
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        showCityLabels = viewModel.showCityLabels.collectAsState().value,
                        isOnline = isOnline,
                        onToggleDarkTheme = { viewModel.isDarkTheme.value = it },
                        onToggleCityLabels = { viewModel.showCityLabels.value = it },
                        onOpenInfoDialog = { viewModel.openInfoDialog() }
                    )
                }
            }
        }
    }
}
