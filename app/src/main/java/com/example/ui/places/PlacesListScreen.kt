package com.example.ui.places

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Place
import com.example.data.model.PlaceCategory
import com.example.ui.theme.GoogleMapsBlue
import com.example.ui.theme.GoogleMapsPinRed

@Composable
fun PlacesListScreen(
    places: List<Place>,
    searchQuery: String,
    selectedCategory: PlaceCategory?,
    favoritePlaceIds: Set<String>,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (PlaceCategory?) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onNavigateToPlace: (Place) -> Unit,
    onToggleFavorite: (Place) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(top = 10.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "ابحث بالاسم أو المحافظة أو المعلم...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF70757A)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF5F6368)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح",
                            tint = Color(0xFF5F6368)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = GoogleMapsBlue,
                unfocusedBorderColor = Color(0xFFDADCE0),
                focusedTextColor = Color(0xFF202124),
                unfocusedTextColor = Color(0xFF202124)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onSelectCategory(null) },
                    label = { Text("جميع الأماكن") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8F0FE),
                        selectedLabelColor = GoogleMapsBlue,
                        containerColor = Color.White,
                        labelColor = Color(0xFF3C4043)
                    ),
                    border = BorderStroke(1.dp, if (selectedCategory == null) GoogleMapsBlue else Color(0xFFDADCE0))
                )
            }
            items(PlaceCategory.values()) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(if (isSelected) null else category) },
                    label = { Text(category.titleAr) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8F0FE),
                        selectedLabelColor = GoogleMapsBlue,
                        containerColor = Color.White,
                        labelColor = Color(0xFF3C4043)
                    ),
                    border = BorderStroke(1.dp, if (isSelected) GoogleMapsBlue else Color(0xFFDADCE0))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Places List
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(places) { place ->
                val isFav = favoritePlaceIds.contains(place.id)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE8EAED)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlaceClick(place) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place.nameAr,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF202124),
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = GoogleMapsPinRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${place.governorateAr} • ${place.category.titleAr}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF5F6368)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onToggleFavorite(place) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "حفظ",
                                    tint = if (isFav) GoogleMapsBlue else Color(0xFF70757A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = place.shortDescriptionAr,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3C4043),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F3F4)
                            ) {
                                Text(
                                    text = "المسافة من القاهرة: ${place.defaultRouteFromCairo.distanceKm} كم",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF5F6368),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = { onNavigateToPlace(place) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F0FE))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Directions,
                                    contentDescription = "عرض المسار",
                                    tint = GoogleMapsBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
