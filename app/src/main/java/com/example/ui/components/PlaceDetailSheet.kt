package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Place
import com.example.data.network.OnlinePlaceDetails
import com.example.ui.theme.GoogleMapsBlue
import com.example.ui.theme.GoogleMapsPinRed

@Composable
fun PlaceDetailSheet(
    modifier: Modifier = Modifier,
    place: Place,
    isFavorite: Boolean,
    isOnline: Boolean,
    onlineDetails: OnlinePlaceDetails?,
    isFetchingOnline: Boolean,
    onToggleFavorite: () -> Unit,
    onShowRoute: () -> Unit,
    onFetchOnlineMore: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(530.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Drag handle & close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F0FE)
                ) {
                    Text(
                        text = place.category.titleAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoogleMapsBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDADCE0))
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color(0xFF5F6368),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Place Title & Governorate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.nameAr,
                        style = MaterialTheme.typography.headlineMedium,
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
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "محافظة ${place.governorateAr} • (${place.nameEn})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5F6368)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F3F4))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "المفضلة",
                        tint = if (isFavorite) GoogleMapsBlue else Color(0xFF5F6368)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Route Action Button
            Button(
                onClick = onShowRoute,
                colors = ButtonDefaults.buttonColors(containerColor = GoogleMapsBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "عرض مسار التنقل والوصول إلى هناك",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. سبب التسمية ومَن سمّاها
            HistorySectionCard(
                title = "سبب التسمية ومَن سمّاها",
                icon = Icons.Default.Info,
                content = place.namingOriginAr,
                iconColor = GoogleMapsBlue
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. تاريخ التأسيس ولماذا أُنشئت
            HistorySectionCard(
                title = "تاريخ التأسيس والنشأة ولماذا أُنشئت",
                icon = Icons.Default.AccessTime,
                content = "${place.establishedDateAr}\n\nسبب وأهمية الإنشاء:\n${place.whyEstablishedAr}",
                iconColor = Color(0xFFF29900)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. متى أصبحت جزءاً من مصر وتاريخها الوطني
            HistorySectionCard(
                title = "تاريخ الانضمام للدولة المصرية ومكانتها الوطنية",
                icon = Icons.Default.Flag,
                content = place.egyptAffiliationAr,
                iconColor = Color(0xFF0D652D)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. أهم الأحداث التاريخية والمعارك
            HistorySectionCard(
                title = "أهم الأحداث التاريخية والمعارك التي وقعت هنا",
                icon = Icons.Default.History,
                content = place.historicEventsAr,
                iconColor = Color(0xFF137333)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. الملوك والفراعنة والقادة الذين عاشوا أو ماتوا هنا
            HistorySectionCard(
                title = "الملوك والفراعنة والقادة الذين عاشوا أو ماتوا أو دُفنوا هنا",
                icon = Icons.Default.AccountBalance,
                content = place.rulersAndKingsAr,
                iconColor = Color(0xFF8430CE)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6. أبرز الأعلام والعلماء والمشاهير الذين وُلدوا بها
            HistorySectionCard(
                title = "أبرز الأعلام والشخصيات والعلماء الذين وُلدوا هنا",
                icon = Icons.Default.Person,
                content = place.famousBornAr,
                iconColor = Color(0xFFC5221F)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Practical Visiting info: Hours & Tickets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8F9FA),
                    border = BorderStroke(1.dp, Color(0xFFE8EAED)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = GoogleMapsBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "مواعيد الزيارة", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5F6368))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = place.openingHoursAr, style = MaterialTheme.typography.bodySmall, color = Color(0xFF202124), fontWeight = FontWeight.Medium)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8F9FA),
                    border = BorderStroke(1.dp, Color(0xFFE8EAED)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, tint = Color(0xFF137333), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "رسوم الدخول", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5F6368))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = place.ticketPriceAr, style = MaterialTheme.typography.bodySmall, color = Color(0xFF202124), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Online Expansion (Wikipedia Open REST API)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF8F9FA),
                border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.TravelExplore, contentDescription = null, tint = GoogleMapsBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "معلومات موسوعية مباشرة عبر الإنترنت", style = MaterialTheme.typography.titleSmall, color = Color(0xFF202124), fontWeight = FontWeight.Bold)
                        }

                        if (isOnline && onlineDetails == null && !isFetchingOnline) {
                            Text(
                                text = "تحديث الآن",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoogleMapsBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8F0FE))
                                    .clickable { onFetchOnlineMore() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isFetchingOnline) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                            CircularProgressIndicator(strokeWidth = 2.dp, color = GoogleMapsBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "جارٍ جلب أحدث المعلومات الموسوعية...", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5F6368))
                        }
                    } else if (onlineDetails != null) {
                        Text(text = onlineDetails.extract, style = MaterialTheme.typography.bodySmall, color = Color(0xFF3C4043))
                    } else if (!isOnline) {
                        Text(text = "وضع عدم الاتصال: جميع البيانات التاريخية والجغرافية والمراكز محفوظة مسبقاً وتعمل بدون إنترنت.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF70757A))
                    } else {
                        Text(text = "اضغط على 'تحديث الآن' لجلب مقالات وتفاصيل إضافية من الموسوعة العالمية.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF70757A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HistorySectionCard(
    title: String,
    icon: ImageVector,
    content: String,
    iconColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F9FA),
        border = BorderStroke(1.dp, Color(0xFFE8EAED)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF202124),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF3C4043),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}
