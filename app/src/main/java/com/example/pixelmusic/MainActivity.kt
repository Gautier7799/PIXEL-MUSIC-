package com.example.pixelmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Data Model
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String
)

// 2. ViewModel
class MusicViewModel : ViewModel() {
    private val allSongs = listOf(
        Song("1", "Midnight Memories", "Pixel Artist", "3:45"),
        Song("2", "Electronic Dreams", "Cyber Beat", "4:12"),
        Song("3", "Acoustic Sunset", "Nature Sounds", "2:50"),
        Song("4", "Lo-Fi Study Beats", "Chill Hop", "3:10"),
        Song("5", "Arabic Classic Fusion", "Orient Echo", "5:00")
    )

    private val _songs = MutableStateFlow(allSongs)
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(allSongs.first())
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun searchMusic(query: String) {
        if (query.isBlank()) {
            _songs.value = allSongs
        } else {
            _songs.value = allSongs.filter {
                it.title.contains(query, ignoreCase = true) || 
                it.artist.contains(query, ignoreCase = true)
            }
        }
    }

    fun playSong(song: Song) {
        _currentSong.value = song
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }
}

// 3. UI Theme
@Composable
fun PixelMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFBB86FC),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6200EE),
            background = Color(0xFFF6F6F6),
            surface = Color.White
        )
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

// 4. Main Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PixelMusicTheme {
                MusicAppScreen()
            }
        }
    }
}

// 5. Main Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicAppScreen(viewModel: MusicViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val songs by viewModel.songs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pixel Music", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            currentSong?.let { song ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, fontWeight = FontWeight.Bold)
                            Text(song.artist, style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            IconButton(onClick = { viewModel.togglePlayPause() }) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "تشغيل / إيقاف"
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchMusic(it)
                },
                label = { Text("ابحث عن أغنية أو فنان...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(songs) { song ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.playSong(song) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentSong?.id == song.id) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        ListItem(
                            headlineContent = { Text(song.title, fontWeight = FontWeight.Medium) },
                            supportingContent = { Text("${song.artist} • ${song.duration}") },
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
