package com.example.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CityLabel
import com.example.data.model.ServiceType
import com.example.data.network.SearchResultItem
import com.example.ui.theme.GoogleMapsBlue

@Composable
fun MapTopBarAndControls(
    modifier: Modifier = Modifier,
    searchQuery: String,
    searchResults: List<SearchResultItem> = emptyList(),
    isSearching: Boolean = false,
    selectedServiceType: ServiceType?,
    onSearchChange: (String) -> Unit,
    onSelectSearchResult: (SearchResultItem) -> Unit,
    onSelectServiceType: (ServiceType?) -> Unit,
    onSelectGovernorate: (CityLabel) -> Unit,
    onResetView: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 12.dp, end = 12.dp)
    ) {
        // Floating Search Bar Card
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                text = "ابحث عن أي شارع، محافظة، معلم، أو مستشفى...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(2.dp),
                            strokeWidth = 2.dp,
                            color = GoogleMapsBlue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Live Search Suggestions Dropdown List
                AnimatedVisibility(
                    visible = searchResults.isNotEmpty() && searchQuery.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                        ) {
                            items(searchResults) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectSearchResult(item)
                                            onSearchChange("")
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = GoogleMapsBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.titleAr,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = item.subtitleAr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = item.categoryAr,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Governorates Quick Jump Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EgyptMapData.ALL_EGYPT_GOVERNORATES.forEach { gov ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.clickable { onSelectGovernorate(gov) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = gov.nameAr,
                            tint = if (gov.isCapital) Color(0xFFD93025) else Color(0xFF1A73E8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = gov.nameAr,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (gov.isCapital) FontWeight.Bold else FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Category Service Filter Chips (Gas, Parking, Food, Cafes, Hospitals)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ServiceQuickChip(
                title = "محطات وقود",
                icon = Icons.Default.LocalGasStation,
                isSelected = selectedServiceType == ServiceType.GAS_STATION,
                iconTint = Color(0xFF0288D1),
                onClick = {
                    onSelectServiceType(if (selectedServiceType == ServiceType.GAS_STATION) null else ServiceType.GAS_STATION)
                }
            )

            ServiceQuickChip(
                title = "مواقف وجراجات",
                icon = Icons.Default.LocalParking,
                isSelected = selectedServiceType == ServiceType.PARKING,
                iconTint = Color(0xFF1976D2),
                onClick = {
                    onSelectServiceType(if (selectedServiceType == ServiceType.PARKING) null else ServiceType.PARKING)
                }
            )

            ServiceQuickChip(
                title = "مطاعم وأكل",
                icon = Icons.Default.Restaurant,
                isSelected = selectedServiceType == ServiceType.RESTAURANT,
                iconTint = Color(0xFFE65100),
                onClick = {
                    onSelectServiceType(if (selectedServiceType == ServiceType.RESTAURANT) null else ServiceType.RESTAURANT)
                }
            )

            ServiceQuickChip(
                title = "كافيهات",
                icon = Icons.Default.LocalCafe,
                isSelected = selectedServiceType == ServiceType.CAFE,
                iconTint = Color(0xFF795548),
                onClick = {
                    onSelectServiceType(if (selectedServiceType == ServiceType.CAFE) null else ServiceType.CAFE)
                }
            )

            ServiceQuickChip(
                title = "مستشفيات وطوارئ",
                icon = Icons.Default.LocalHospital,
                isSelected = selectedServiceType == ServiceType.HOSPITAL,
                iconTint = Color(0xFFC2185B),
                onClick = {
                    onSelectServiceType(if (selectedServiceType == ServiceType.HOSPITAL) null else ServiceType.HOSPITAL)
                }
            )
        }
    }
}

@Composable
private fun ServiceQuickChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) GoogleMapsBlue else MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, if (isSelected) GoogleMapsBlue else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else iconTint,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MapZoomAndLocationControls(
    modifier: Modifier = Modifier,
    mapTileType: MapTileType = MapTileType.STANDARD,
    onCycleMapTileType: () -> Unit = {},
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onCenterOnUserLocation: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Map Tile Type Switcher Button (OSM Standard, Carto Light, Dark, Topo)
        FloatingActionButton(
            onClick = onCycleMapTileType,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF1E88E5),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            shape = CircleShape,
            modifier = Modifier.size(46.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = "تبديل نمط الخريطة (${mapTileType.titleAr})",
                modifier = Modifier.size(22.dp)
            )
        }

        // Zoom Controls Card (+ and -)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "تكبير الخريطة",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "تصغير الخريطة",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Live GPS / Center on User Location button
        FloatingActionButton(
            onClick = onCenterOnUserLocation,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF1E88E5),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            shape = CircleShape,
            modifier = Modifier.size(46.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "تحديد موقعي المباشر عبر GPS",
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
