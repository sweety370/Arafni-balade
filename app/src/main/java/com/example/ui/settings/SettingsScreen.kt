package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoogleMapsBlue

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    showCityLabels: Boolean,
    isOnline: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleCityLabels: (Boolean) -> Unit,
    onOpenInfoDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F0FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = GoogleMapsBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "الإعدادات",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF202124),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تفضيلات العرض، نمط الخريطة، ومعلومات التطبيق",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF5F6368)
                )
            }
        }

        // --- SPECIFIC INFO BUTTON / CARD (AMR HUSSEIN & MOHAMED HUSSEIN) ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, GoogleMapsBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenInfoDialog() }
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F0FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "معلومات التطبيق",
                            tint = GoogleMapsBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "معلومات التطبيق وفريق العمل (Info)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF202124),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الفكرة: عمرو حسين | المبرمج: محمد حسين",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoogleMapsBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "اضغط لعرض تفاصيل المشروع",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF5F6368)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = GoogleMapsBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "تفضيلات الخريطة",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF202124),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE8EAED)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Show City Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Layers, contentDescription = null, tint = GoogleMapsBlue, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "عناوين المراكز والمحافظات", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF202124))
                            Text(text = "إظهار أسماء المدن والمراكز الإدارية على الخريطة", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5F6368))
                        }
                    }
                    Switch(
                        checked = showCityLabels,
                        onCheckedChange = onToggleCityLabels,
                        colors = SwitchDefaults.colors(checkedThumbColor = GoogleMapsBlue, checkedTrackColor = Color(0xFFD2E3FC))
                    )
                }

                Divider(color = Color(0xFFF1F3F4), modifier = Modifier.padding(vertical = 10.dp))

                // Dark Theme toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = Color(0xFF5F6368), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "المظهر الداكن للخريطة", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF202124))
                            Text(text = "تفعيل الألوان الداكنة أثناء القيادة الليلية", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5F6368))
                        }
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onToggleDarkTheme,
                        colors = SwitchDefaults.colors(checkedThumbColor = GoogleMapsBlue, checkedTrackColor = Color(0xFFD2E3FC))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Offline Status
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF1F3F4),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFF5F6368), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "حالة العمل بدون نت:", style = MaterialTheme.typography.titleSmall, color = Color(0xFF202124), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "جميع خرائط محافظات ومراكز ومعالم ومسارات مصر تعمل محلياً 100% دون الحاجة إلى الإنترنت. عند اتصال الجهاز بالشبكة، يتم البحث التلقائي عن تفاصيل موسوعية إضافية.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5F6368)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
