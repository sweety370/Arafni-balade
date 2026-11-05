package com.example.ui.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.FavoritePlaceEntity
import com.example.data.model.Place
import com.example.data.model.PlacesRepository
import com.example.ui.theme.GoogleMapsBlue
import com.example.ui.theme.GoogleMapsPinRed

@Composable
fun FavoritesScreen(
    favorites: List<FavoritePlaceEntity>,
    onSelectPlace: (Place) -> Unit,
    onNavigateToPlace: (Place) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onUpdateNote: (String, String) -> Unit,
    onExplorePlaces: () -> Unit
) {
    var editingEntity by remember { mutableStateOf<FavoritePlaceEntity?>(null) }
    var noteInput by remember { mutableStateOf("") }

    if (editingEntity != null) {
        AlertDialog(
            onDismissRequest = { editingEntity = null },
            title = {
                Text(
                    text = "ملاحظة السفر: ${editingEntity!!.nameAr}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF202124)
                )
            },
            text = {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("اكتب ملاحظتك الشخصية هنا...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF202124),
                        unfocusedTextColor = Color(0xFF202124)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateNote(editingEntity!!.placeId, noteInput)
                        editingEntity = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleMapsBlue)
                ) {
                    Text("حفظ", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntity = null }) {
                    Text("إلغاء", color = Color(0xFF5F6368))
                }
            },
            containerColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F0FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = GoogleMapsBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "الأماكن المحفوظة",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF202124),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "مواقعك المفضلة المحفوظة محلياً للوصول بدون إنترنت",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF5F6368)
                )
            }
        }

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkRemove,
                        contentDescription = null,
                        tint = Color(0xFFBDC1C6),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "لا توجد أماكن محفوظة بعد",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF3C4043),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يمكنك حفظ أي معلم أو موقع على الخريطة للرجوع إليه في وضع عدم الاتصال.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF70757A),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onExplorePlaces,
                        colors = ButtonDefaults.buttonColors(containerColor = GoogleMapsBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Explore, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استكشف الخريطة", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(favorites) { fav ->
                    val place = PlacesRepository.getPlaceById(fav.placeId)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE8EAED)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (place != null) onSelectPlace(place)
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fav.nameAr,
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
                                            text = "${fav.governorateAr} • ${fav.categoryName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF5F6368)
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            editingEntity = fav
                                            noteInput = fav.userNote
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "تعديل الملاحظة",
                                            tint = Color(0xFF5F6368),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onRemoveFavorite(fav.placeId) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف",
                                            tint = Color(0xFFD93025),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (fav.userNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F3F4),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ملاحظة: ${fav.userNote}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF3C4043),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (place != null) {
                                    Button(
                                        onClick = { onNavigateToPlace(place) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFE8F0FE),
                                            contentColor = GoogleMapsBlue
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "عرض المسار (${place.defaultRouteFromCairo.distanceKm} كم)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
