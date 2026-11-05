package com.example.ui.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.unit.dp
import com.example.data.model.CityLabel
import com.example.data.model.ServiceType
import com.example.ui.theme.GoogleMapsBlue

@Composable
fun MapTopBarAndControls(
    modifier: Modifier = Modifier,
    searchQuery: String,
    selectedServiceType: ServiceType?,
    onSearchChange: (String) -> Unit,
    onSelectServiceType: (ServiceType?) -> Unit,
    onSelectGovernorate: (CityLabel) -> Unit,
    onResetView: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 12.dp, end = 12.dp)
    ) {
        // Floating Google Maps Style Search Card
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث",
                    tint = Color(0xFF5F6368),
                    modifier = Modifier.size(24.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = "ابحث عن أي محافظة، مركز، حي، أو مكان في مصر...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF70757A)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFF202124),
                        unfocusedTextColor = Color(0xFF202124)
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح",
                            tint = Color(0xFF70757A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Governorates Quick Jump Bar (Directly zoom into any of Egypt's 27 governorates)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EgyptMapData.ALL_EGYPT_GOVERNORATES.forEach { gov ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, Color(0xFFDADCE0)),
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
                            color = Color(0xFF202124)
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
        color = if (isSelected) GoogleMapsBlue else Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, if (isSelected) GoogleMapsBlue else Color(0xFFE0E0E0)),
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
                color = if (isSelected) Color.White else Color(0xFF3C4043)
            )
        }
    }
}

@Composable
fun MapZoomAndLocationControls(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetView: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Zoom Controls Card (+ and -)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column {
                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "تكبير والدخول في المحافظة",
                        tint = Color(0xFF3C4043)
                    )
                }
                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "تصغير",
                        tint = Color(0xFF3C4043)
                    )
                }
            }
        }

        // Recenter / Location button
        FloatingActionButton(
            onClick = onResetView,
            containerColor = Color.White,
            contentColor = Color(0xFF5F6368),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            shape = CircleShape,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "إعادة ضبط الخريطة",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
