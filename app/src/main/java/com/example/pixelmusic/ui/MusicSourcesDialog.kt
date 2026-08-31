package com.example.pixelmusic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MusicSourcesDialog(onDismiss: () -> Unit) {
    var selectedMode by remember { mutableStateOf(0) } // 0: تصفح الملفات (Navig. fichier), 1: النظام (Système)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("مصادر الموسيقى", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("تحميل من", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                // أزرار الاختيار (Segmented Buttons)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { selectedMode = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 0.dp, bottomEnd = 0.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("تصفح الملفات") }
                    
                    Button(
                        onClick = { selectedMode = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 24.dp, bottomEnd = 24.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("النظام") }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "تحميل الموسيقى من المجلدات التي ستحددها. أبطأ، لكنه أكثر موثوقية. تتطلب هذه الميزة تطبيق مدير ملفات النظام.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // بطاقة طلب الصلاحيات
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("الوصول للتخزين موصى به", fontWeight = FontWeight.Bold)
                            Text("منح الوصول للتخزين يمكن أن يحل مشاكل التحميل.", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "تفعيل الصلاحيات")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مجلدات للتحميل", fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Add, contentDescription = "إضافة مجلد")
                }
                HorizontalDivider()
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { 
                Text("حفظ", fontWeight = FontWeight.Bold) 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("إلغاء", color = MaterialTheme.colorScheme.onSurfaceVariant) 
            }
        }
    )
}
