package com.example.pixelmusic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pixelmusic.model.Song
import com.example.pixelmusic.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MusicViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("البحث عن الموسيقى") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.searchMusic(it)
                },
                label = { Text("ابحث عن أغنية أو فنان...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(songs) { song ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.playSong(song) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            ListItem(
                                headlineContent = { Text(song.title) },
                                supportingContent = { Text(song.artist) },
                                trailingContent = {
                                    IconButton(onClick = { viewModel.playSong(song) }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "تشغيل")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
