package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GoogleMapsBlue

@Composable
fun InfoCreditsDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFDADCE0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Label
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F0FE)
                ) {
                    Text(
                        text = "معلومات التطبيق وفريق العمل",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoogleMapsBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "عرفني بلدي",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF202124),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "خريطة ومعالم جمهورية مصر العربية التفاعلية",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5F6368),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Amr Hussein Card (Idea)
                CreditRoleCard(
                    icon = Icons.Default.Lightbulb,
                    roleTitle = "صاحب الفكرة",
                    name = "عمرو حسين",
                    description = "صاحب فكرة تطبيق عرفني بلدي لتوثيق معالم مصر جغرافياً وتاريخياً وإتاحتها للمواطنين والزوار بدون إنترنت بدقة عالية.",
                    badgeColor = GoogleMapsBlue
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mohamed Hussein Card (Developer)
                CreditRoleCard(
                    icon = Icons.Default.Code,
                    roleTitle = "المبرمج والمطور",
                    name = "محمد حسين",
                    description = "تطوير وبرمجة التطبيق ومحرك الخرائط ثلاثي الأبعاد 3D وحساب مسارات الملاحة والعمل في وضع عدم الاتصال بالكامل بأحدث تقنيات أندرويد.",
                    badgeColor = Color(0xFF137333)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Engine specs
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8F9FA),
                    border = BorderStroke(1.dp, Color(0xFFE8EAED)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = GoogleMapsBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مواصفات التشغيل:",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF202124),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "يعمل بالكامل بدون اتصال بالإنترنت (Offline 100%) لجميع المعالم والمسارات.\nيتيح ميزة البحث الموسوعي التلقائي المباشر عند توفر شبكة الإنترنت.\nمبني وفق معايير جوجل ماتيريال ديزاين المعتمدة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5F6368),
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Close Button
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleMapsBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "إغلاق",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditRoleCard(
    icon: ImageVector,
    roleTitle: String,
    name: String,
    description: String,
    badgeColor: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8F9FA),
        border = BorderStroke(1.dp, Color(0xFFE8EAED)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = roleTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF202124),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5F6368)
            )
        }
    }
}
