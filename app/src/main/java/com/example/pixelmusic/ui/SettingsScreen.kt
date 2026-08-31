package com.example.pixelmusic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    var showSourcesDialog by remember { mutableStateOf(false) }

    if (showSourcesDialog) {
        MusicSourcesDialog(onDismiss = { showSourcesDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsItem(Icons.Default.Palette, "المظهر", "تغيير السمة وألوان التطبيق")
            SettingsItem(Icons.Default.Tune, "التخصيص", "تخصيص عناصر التحكم وسلوك واجهة المستخدم")
            SettingsItem(Icons.Default.MusicNote, "المحتوى", "التحكم في تحميل الموسيقى والصور")
            SettingsItem(Icons.Default.PlayArrow, "الصوت", "تكوين الصوت وسلوك التشغيل")

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "المكتبة", 
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), 
                color = MaterialTheme.colorScheme.primary, 
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            SettingsItem(null, "مصادر الموسيقى", "إدارة من أين يتم تحميل الموسيقى", onClick = { showSourcesDialog = true })
            SettingsItem(null, "تحديث الموسيقى", "إعادة تحميل مكتبة الموسيقى باستخدام العلامات المخبأة")
            SettingsItem(null, "فحص الموسيقى من جديد", "مسح ذاكرة التخزين المؤقت وإعادة تحميل كاملة")
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector?, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
