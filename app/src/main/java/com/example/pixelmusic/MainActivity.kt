package com.example.pixelmusic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// --- Platform Enums & Models ---
enum class MusicSource(val displayName: String, val brandColor: Color) {
    ALL("Toutes les sources", Color(0xFF00677D)),
    YOUTUBE_MUSIC("YouTube Music", Color(0xFFFF0000)),
    SPOTIFY("Spotify", Color(0xFF1DB954)),
    DEEZER("Deezer", Color(0xFFA238FF)),
    LOCAL("Stockage local", Color(0xFF455A64))
}

data class SongItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val duration: String,
    val source: MusicSource = MusicSource.LOCAL,
    val externalUrl: String = ""
)

data class AlbumItem(
    val id: String,
    val name: String,
    val artist: String,
    val trackCount: Int,
    val source: MusicSource = MusicSource.LOCAL
)

data class ArtistItem(
    val id: String,
    val name: String,
    val songCount: Int,
    val source: MusicSource = MusicSource.LOCAL
)

data class GenreItem(
    val id: String,
    val name: String,
    val trackCount: Int
)

data class PlaylistItem(
    val id: String,
    val name: String,
    val trackCount: Int,
    val source: MusicSource = MusicSource.LOCAL
)

// --- ViewModel ---
class AuxioMusicViewModel : ViewModel() {
    private val localSongs = listOf(
        SongItem("1", "Midnight Serenade", "Elena Rostova", "Echoes of Night", "Classical", "3:42", MusicSource.LOCAL),
        SongItem("2", "Electric Horizon", "CyberPulse", "Neon City", "Electronic", "4:15", MusicSource.LOCAL),
        SongItem("3", "Acoustic Breeze", "David Vance", "Sunlight & Timber", "Acoustic", "2:58", MusicSource.LOCAL),
        SongItem("4", "Oriental Dream", "Layla Mansoor", "Oasis Sounds", "World", "5:20", MusicSource.LOCAL),
        SongItem("5", "Lo-Fi Cafe Vibes", "ChillMaster", "Coffee Beats Vol. 1", "Lo-Fi", "3:10", MusicSource.LOCAL),
        SongItem("6", "Starlight Symphony", "Elena Rostova", "Echoes of Night", "Classical", "4:45", MusicSource.LOCAL)
    )

    private val youtubeSongs = listOf(
        SongItem("yt_1", "Blinding Lights", "The Weeknd", "After Hours", "Pop / Synthwave", "3:20", MusicSource.YOUTUBE_MUSIC, "https://music.youtube.com/search?q=The+Weeknd+Blinding+Lights"),
        SongItem("yt_2", "Shape of You", "Ed Sheeran", "÷ (Divide)", "Pop", "3:53", MusicSource.YOUTUBE_MUSIC, "https://music.youtube.com/search?q=Ed+Sheeran+Shape+of+You"),
        SongItem("yt_3", "Starboy", "The Weeknd ft. Daft Punk", "Starboy", "Electro-Pop", "3:50", MusicSource.YOUTUBE_MUSIC, "https://music.youtube.com/search?q=The+Weeknd+Starboy"),
        SongItem("yt_4", "Levitating", "Dua Lipa", "Future Nostalgia", "Dance-Pop", "3:23", MusicSource.YOUTUBE_MUSIC, "https://music.youtube.com/search?q=Dua+Lipa+Levitating"),
        SongItem("yt_5", "Believer", "Imagine Dragons", "Evolve", "Alternative Rock", "3:24", MusicSource.YOUTUBE_MUSIC, "https://music.youtube.com/search?q=Imagine+Dragons+Believer")
    )

    private val spotifySongs = listOf(
        SongItem("sp_1", "As It Was", "Harry Styles", "Harry's House", "Indie Pop", "2:47", MusicSource.SPOTIFY, "https://open.spotify.com/search/Harry%20Styles%20As%20It%20Was"),
        SongItem("sp_2", "Flowers", "Miley Cyrus", "Endless Summer Vacation", "Pop Rock", "3:20", MusicSource.SPOTIFY, "https://open.spotify.com/search/Miley%20Cyrus%20Flowers"),
        SongItem("sp_3", "Cruel Summer", "Taylor Swift", "Lover", "Synth-Pop", "2:58", MusicSource.SPOTIFY, "https://open.spotify.com/search/Taylor%20Swift%20Cruel%20Summer"),
        SongItem("sp_4", "Stay", "The Kid LAROI, Justin Bieber", "F*CK LOVE 3", "Pop", "2:21", MusicSource.SPOTIFY, "https://open.spotify.com/search/The%20Kid%20LAROI%20Stay"),
        SongItem("sp_5", "Save Your Tears", "The Weeknd", "After Hours", "Synth-Pop", "3:35", MusicSource.SPOTIFY, "https://open.spotify.com/search/The%20Weeknd%20Save%20Your%20Tears")
    )

    private val deezerSongs = listOf(
        SongItem("dz_1", "Bad Guy", "Billie Eilish", "When We All Fall Asleep", "Electropop", "3:14", MusicSource.DEEZER, "https://www.deezer.com/search/Billie%20Eilish%20Bad%20Guy"),
        SongItem("dz_2", "Dance Monkey", "Tones and I", "The Kids Are Coming", "Pop", "3:29", MusicSource.DEEZER, "https://www.deezer.com/search/Dance%20Monkey")
    )

    private val allSampleAlbums = listOf(
        AlbumItem("1", "Echoes of Night", "Elena Rostova", 2, MusicSource.LOCAL),
        AlbumItem("2", "Neon City", "CyberPulse", 1, MusicSource.LOCAL),
        AlbumItem("3", "After Hours", "The Weeknd", 2, MusicSource.YOUTUBE_MUSIC),
        AlbumItem("4", "Harry's House", "Harry Styles", 1, MusicSource.SPOTIFY),
        AlbumItem("5", "Lover", "Taylor Swift", 1, MusicSource.SPOTIFY),
        AlbumItem("6", "Evolve", "Imagine Dragons", 1, MusicSource.YOUTUBE_MUSIC)
    )

    private val allSampleArtists = listOf(
        ArtistItem("1", "The Weeknd", 3, MusicSource.YOUTUBE_MUSIC),
        ArtistItem("2", "Elena Rostova", 2, MusicSource.LOCAL),
        ArtistItem("3", "Harry Styles", 1, MusicSource.SPOTIFY),
        ArtistItem("4", "Taylor Swift", 1, MusicSource.SPOTIFY),
        ArtistItem("5", "CyberPulse", 1, MusicSource.LOCAL),
        ArtistItem("6", "Imagine Dragons", 1, MusicSource.YOUTUBE_MUSIC)
    )

    private val allSampleGenres = listOf(
        GenreItem("1", "Pop & Synth-Pop", 6),
        GenreItem("2", "Classical", 2),
        GenreItem("3", "Electronic", 2),
        GenreItem("4", "Alternative Rock", 1),
        GenreItem("5", "Acoustic & Lo-Fi", 2)
    )

    private val allSamplePlaylists = listOf(
        PlaylistItem("1", "Mes Favoris (Local)", 4, MusicSource.LOCAL),
        PlaylistItem("2", "Top Hits 2026 (Spotify)", 5, MusicSource.SPOTIFY),
        PlaylistItem("3", "YouTube Music Mix", 5, MusicSource.YOUTUBE_MUSIC),
        PlaylistItem("4", "Relax & Focus", 3, MusicSource.LOCAL)
    )

    private val _isMusicLoaded = MutableStateFlow(false)
    val isMusicLoaded: StateFlow<Boolean> = _isMusicLoaded.asStateFlow()

    private val _activeSource = MutableStateFlow(MusicSource.ALL)
    val activeSource: StateFlow<MusicSource> = _activeSource.asStateFlow()

    private val _connectedPlatforms = MutableStateFlow(
        mapOf(
            MusicSource.LOCAL to true,
            MusicSource.YOUTUBE_MUSIC to true,
            MusicSource.SPOTIFY to true,
            MusicSource.DEEZER to false
        )
    )
    val connectedPlatforms: StateFlow<Map<MusicSource, Boolean>> = _connectedPlatforms.asStateFlow()

    private val _songs = MutableStateFlow<List<SongItem>>(emptyList())
    val songs: StateFlow<List<SongItem>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<AlbumItem>>(emptyList())
    val albums: StateFlow<List<AlbumItem>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<ArtistItem>>(emptyList())
    val artists: StateFlow<List<ArtistItem>> = _artists.asStateFlow()

    private val _genres = MutableStateFlow<List<GenreItem>>(emptyList())
    val genres: StateFlow<List<GenreItem>> = _genres.asStateFlow()

    private val _playlists = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val playlists: StateFlow<List<PlaylistItem>> = _playlists.asStateFlow()

    private val _currentSong = MutableStateFlow<SongItem?>(null)
    val currentSong: StateFlow<SongItem?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentScreen = MutableStateFlow("main")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun togglePlatformConnection(source: MusicSource) {
        val current = _connectedPlatforms.value.toMutableMap()
        val newState = !(current[source] ?: false)
        current[source] = newState
        _connectedPlatforms.value = current
        recalculateLibrary()
    }

    fun setFilterSource(source: MusicSource) {
        _activeSource.value = source
        recalculateLibrary()
    }

    fun loadMusicSources() {
        _isMusicLoaded.value = true
        recalculateLibrary()
        if (_currentSong.value == null) {
            _currentSong.value = _songs.value.firstOrNull()
        }
    }

    private fun recalculateLibrary() {
        if (!_isMusicLoaded.value) return

        val connected = _connectedPlatforms.value
        val sourceFilter = _activeSource.value

        val combinedSongs = mutableListOf<SongItem>()
        if (connected[MusicSource.LOCAL] == true) combinedSongs.addAll(localSongs)
        if (connected[MusicSource.YOUTUBE_MUSIC] == true) combinedSongs.addAll(youtubeSongs)
        if (connected[MusicSource.SPOTIFY] == true) combinedSongs.addAll(spotifySongs)
        if (connected[MusicSource.DEEZER] == true) combinedSongs.addAll(deezerSongs)

        val filteredSongs = if (sourceFilter == MusicSource.ALL) {
            combinedSongs
        } else {
            combinedSongs.filter { it.source == sourceFilter }
        }

        _songs.value = filteredSongs
        _albums.value = if (sourceFilter == MusicSource.ALL) allSampleAlbums else allSampleAlbums.filter { it.source == sourceFilter }
        _artists.value = if (sourceFilter == MusicSource.ALL) allSampleArtists else allSampleArtists.filter { it.source == sourceFilter }
        _genres.value = allSampleGenres
        _playlists.value = if (sourceFilter == MusicSource.ALL) allSamplePlaylists else allSamplePlaylists.filter { it.source == sourceFilter }
    }

    fun refreshMusic() {
        if (_isMusicLoaded.value) {
            loadMusicSources()
        }
    }

    fun clearMusic() {
        _isMusicLoaded.value = false
        _songs.value = emptyList()
        _albums.value = emptyList()
        _artists.value = emptyList()
        _genres.value = emptyList()
        _playlists.value = emptyList()
        _currentSong.value = null
        _isPlaying.value = false
    }

    fun playSong(song: SongItem) {
        _currentSong.value = song
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun nextSong() {
        val list = _songs.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _currentSong.value?.id }
        val nextIndex = if (currentIndex != -1 && currentIndex < list.size - 1) currentIndex + 1 else 0
        _currentSong.value = list[nextIndex]
        _isPlaying.value = true
    }

    fun previousSong() {
        val list = _songs.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _currentSong.value?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        _currentSong.value = list[prevIndex]
        _isPlaying.value = true
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}

// --- Theme Colors ---
private val AuxioTeal = Color(0xFF00677D)
private val AuxioTealDark = Color(0xFF4DD0E1)
private val BadgeBgLight = Color(0xFFE4ECEE)
private val BadgeBgDark = Color(0xFF263238)

@Composable
fun AuxioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = AuxioTealDark,
            secondary = Color(0xFF80CBC4),
            background = Color(0xFF101415),
            surface = Color(0xFF191C1D),
            surfaceVariant = Color(0xFF2B3133),
            onPrimary = Color.Black,
            onBackground = Color(0xFFE1E3E3),
            onSurface = Color(0xFFE1E3E3)
        )
    } else {
        lightColorScheme(
            primary = AuxioTeal,
            secondary = Color(0xFF006876),
            background = Color(0xFFFAFDFD),
            surface = Color.White,
            surfaceVariant = Color(0xFFE7ECEE),
            onPrimary = Color.White,
            onBackground = Color(0xFF191C1D),
            onSurface = Color(0xFF191C1D)
        )
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuxioTheme {
                val viewModel: AuxioMusicViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                AnimatedContent(
                    targetState = currentScreen,
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        "settings" -> {
                            AuxioSettingsScreen(
                                onNavigateBack = { viewModel.navigateTo("main") },
                                viewModel = viewModel
                            )
                        }
                        else -> {
                            AuxioMainScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

// Helper function to open song in YouTube Music / Spotify / Browser
fun openExternalMusic(context: Context, song: SongItem) {
    if (song.externalUrl.isNotEmpty()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.externalUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Impossible d'ouvrir le lien : ${song.title}", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Lecture locale : ${song.title}", Toast.LENGTH_SHORT).show()
    }
}

// --- Main Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuxioMainScreen(viewModel: AuxioMusicViewModel = viewModel()) {
    val context = LocalContext.current
    val tabs = listOf("Titres", "Albums", "Artistes", "Genres", "Playlists")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showSourcesDialog by remember { mutableStateOf(false) }
    var showPlayerSheet by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showPlatformSelectorDialog by remember { mutableStateOf(false) }

    val isMusicLoaded by viewModel.isMusicLoaded.collectAsState()
    val activeSource by viewModel.activeSource.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Rechercher sur YouTube Music, Spotify...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("search_input"),
                            singleLine = true
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.onSearchQueryChanged("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Auxio",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (activeSource != MusicSource.ALL) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = activeSource.brandColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = activeSource.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeSource.brandColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showPlatformSelectorDialog = true },
                            modifier = Modifier.testTag("platform_filter_button")
                        ) {
                            Icon(Icons.Default.CloudQueue, contentDescription = "Filtrer la plateforme")
                        }
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.testTag("search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Rechercher")
                        }
                        IconButton(
                            onClick = { showSortDialog = true },
                            modifier = Modifier.testTag("sort_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Trier")
                        }
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.testTag("more_button")
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sources de la musique (YouTube/Spotify)") },
                                    onClick = {
                                        showMenu = false
                                        showSourcesDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = AuxioTeal) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Paramètres") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.navigateTo("settings")
                                    },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isMusicLoaded) "Vider la liste" else "Charger toute la musique") },
                                    onClick = {
                                        showMenu = false
                                        if (isMusicLoaded) viewModel.clearMusic() else viewModel.loadMusicSources()
                                    },
                                    leadingIcon = { Icon(if (isMusicLoaded) Icons.Default.Delete else Icons.Default.Refresh, contentDescription = null) }
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentSong != null) {
                AuxioMiniPlayer(
                    song = currentSong!!,
                    isPlaying = isPlaying,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onClick = { showPlayerSheet = true },
                    onOpenExternal = { song ->
                        openExternalMusic(context, song)
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = AuxioTeal,
                            height = 3.dp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) AuxioTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTabIndex) {
                0 -> { // Titres
                    if (!isMusicLoaded || songs.isEmpty()) {
                        AuxioEmptyState(
                            icon = Icons.Default.MusicNote,
                            message = "Vos titres (YouTube Music, Spotify, Local) s'afficheront ici.",
                            onSourcesClick = { showSourcesDialog = true }
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(songs.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) || it.album.contains(searchQuery, ignoreCase = true) }) { song ->
                                SongListItem(
                                    song = song,
                                    isCurrent = currentSong?.id == song.id,
                                    isPlaying = isPlaying && currentSong?.id == song.id,
                                    onClick = { viewModel.playSong(song) },
                                    onOpenExternal = { openExternalMusic(context, song) }
                                )
                            }
                        }
                    }
                }
                1 -> { // Albums
                    if (!isMusicLoaded || albums.isEmpty()) {
                        AuxioEmptyState(
                            icon = Icons.Default.Album,
                            message = "Vos albums s'afficheront ici.",
                            onSourcesClick = { showSourcesDialog = true }
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(albums) { album ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(album.source.brandColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Album,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = album.source.brandColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(album.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${album.artist} • ${album.trackCount} titres", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            album.source.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = album.source.brandColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // Artistes
                    if (!isMusicLoaded || artists.isEmpty()) {
                        AuxioEmptyState(
                            icon = Icons.Default.People,
                            message = "Vos artistes s'afficheront ici.",
                            onSourcesClick = { showSourcesDialog = true }
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(artists) { artist ->
                                ListItem(
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(artist.source.brandColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = artist.source.brandColor)
                                        }
                                    },
                                    headlineContent = { Text(artist.name, fontWeight = FontWeight.Medium) },
                                    supportingContent = { Text("${artist.songCount} titres • ${artist.source.displayName}") }
                                )
                            }
                        }
                    }
                }
                3 -> { // Genres
                    if (!isMusicLoaded || genres.isEmpty()) {
                        AuxioEmptyState(
                            icon = Icons.Default.Brush,
                            message = "Les genres musicaux s'afficheront ici.",
                            onSourcesClick = { showSourcesDialog = true }
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(genres) { genre ->
                                ListItem(
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Brush, contentDescription = null, tint = AuxioTeal)
                                        }
                                    },
                                    headlineContent = { Text(genre.name, fontWeight = FontWeight.Medium) },
                                    supportingContent = { Text("${genre.trackCount} titres") }
                                )
                            }
                        }
                    }
                }
                4 -> { // Playlists
                    if (!isMusicLoaded || playlists.isEmpty()) {
                        AuxioEmptyState(
                            icon = Icons.Default.QueueMusic,
                            message = "Vos playlists Spotify, YouTube Music & Locales s'afficheront ici.",
                            onSourcesClick = { showSourcesDialog = true }
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(playlists) { playlist ->
                                ListItem(
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(playlist.source.brandColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.QueueMusic, contentDescription = null, tint = playlist.source.brandColor)
                                        }
                                    },
                                    headlineContent = { Text(playlist.name, fontWeight = FontWeight.Medium) },
                                    supportingContent = { Text("${playlist.trackCount} titres • ${playlist.source.displayName}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Platform Selector Dialog
    if (showPlatformSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showPlatformSelectorDialog = false },
            icon = { Icon(Icons.Default.CloudQueue, contentDescription = null, tint = AuxioTeal) },
            title = { Text("Filtrer par plateforme") },
            text = {
                Column {
                    MusicSource.values().forEach { source ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setFilterSource(source)
                                    showPlatformSelectorDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = activeSource == source,
                                onClick = {
                                    viewModel.setFilterSource(source)
                                    showPlatformSelectorDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = source.displayName,
                                fontWeight = if (activeSource == source) FontWeight.Bold else FontWeight.Normal,
                                color = if (source != MusicSource.ALL) source.brandColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlatformSelectorDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Sort Dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, tint = AuxioTeal) },
            title = { Text("Trier par") },
            text = {
                Column {
                    listOf("Titre", "Artiste", "Album", "Plateforme (Source)", "Durée").forEach { sortOption ->
                        TextButton(
                            onClick = { showSortDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(sortOption, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Sources Dialog
    if (showSourcesDialog) {
        MusicSourcesDialog(
            viewModel = viewModel,
            onDismiss = { showSourcesDialog = false }
        )
    }

    // Full Screen Player Modal
    if (showPlayerSheet && currentSong != null) {
        AuxioFullPlayerModal(
            song = currentSong!!,
            isPlaying = isPlaying,
            onDismiss = { showPlayerSheet = false },
            onPlayPause = { viewModel.togglePlayPause() },
            onNext = { viewModel.nextSong() },
            onPrev = { viewModel.previousSong() },
            onOpenExternal = { song -> openExternalMusic(context, song) }
        )
    }
}

// --- Music Sources Dialog ---
@Composable
fun MusicSourcesDialog(
    viewModel: AuxioMusicViewModel,
    onDismiss: () -> Unit
) {
    val connectedPlatforms by viewModel.connectedPlatforms.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = AuxioTeal) },
        title = { Text("Sources & Plateformes") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Activez les plateformes pour synchroniser vos titres et playlists :",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // YouTube Music Switch
                PlatformToggleRow(
                    name = "YouTube Music",
                    color = Color(0xFFFF0000),
                    icon = Icons.Default.PlayCircleFilled,
                    isConnected = connectedPlatforms[MusicSource.YOUTUBE_MUSIC] == true,
                    onToggle = { viewModel.togglePlatformConnection(MusicSource.YOUTUBE_MUSIC) }
                )

                // Spotify Switch
                PlatformToggleRow(
                    name = "Spotify",
                    color = Color(0xFF1DB954),
                    icon = Icons.Default.Audiotrack,
                    isConnected = connectedPlatforms[MusicSource.SPOTIFY] == true,
                    onToggle = { viewModel.togglePlatformConnection(MusicSource.SPOTIFY) }
                )

                // Deezer Switch
                PlatformToggleRow(
                    name = "Deezer",
                    color = Color(0xFFA238FF),
                    icon = Icons.Default.Equalizer,
                    isConnected = connectedPlatforms[MusicSource.DEEZER] == true,
                    onToggle = { viewModel.togglePlatformConnection(MusicSource.DEEZER) }
                )

                // Local Device Storage Switch
                PlatformToggleRow(
                    name = "Stockage local",
                    color = Color(0xFF455A64),
                    icon = Icons.Default.Folder,
                    isConnected = connectedPlatforms[MusicSource.LOCAL] == true,
                    onToggle = { viewModel.togglePlatformConnection(MusicSource.LOCAL) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.loadMusicSources()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AuxioTeal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Synchroniser la musique")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@Composable
fun PlatformToggleRow(
    name: String,
    color: Color,
    icon: ImageVector,
    isConnected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                if (isConnected) "Connecté & Synchronisé" else "Désactivé",
                fontSize = 12.sp,
                color = if (isConnected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isConnected,
            onCheckedChange = { onToggle() }
        )
    }
}

// --- Settings Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuxioSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuxioMusicViewModel
) {
    BackHandler { onNavigateBack() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var showSourcesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Paramètres",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK", color = AuxioTeal)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(msg)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Palette,
                    title = "Apparence",
                    subtitle = "Changer le thème et les couleurs de l'application",
                    onClick = { snackbarMessage = "Paramètre Apparence sélectionné" }
                )
            }
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Tune,
                    title = "Personnalisation",
                    subtitle = "Personnaliser les commandes et le comportement de l'interface utilisateur",
                    onClick = { snackbarMessage = "Paramètre Personnalisation sélectionné" }
                )
            }
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.MusicNote,
                    title = "Contenu",
                    subtitle = "Contrôler le chargement de la musique et des images",
                    onClick = { snackbarMessage = "Paramètre Contenu sélectionné" }
                )
            }
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.PlayArrow,
                    title = "Audio",
                    subtitle = "Configurer le son et le comportement de lecture",
                    onClick = { snackbarMessage = "Paramètre Audio sélectionné" }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                Text(
                    text = "Plateformes & Bibliothèque",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuxioTeal,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                SettingsActionItem(
                    title = "Sources de la musique (YouTube Music / Spotify / Local)",
                    subtitle = "Connecter vos comptes Spotify, YouTube Music, Deezer et stockage local",
                    onClick = { showSourcesDialog = true }
                )
            }
            item {
                SettingsActionItem(
                    title = "Actualiser la musique",
                    subtitle = "Recharge la bibliothèque musicale et synchronise le cloud",
                    onClick = {
                        viewModel.refreshMusic()
                        snackbarMessage = "Bibliothèque musicale actualisée avec succès"
                    }
                )
            }
            item {
                SettingsActionItem(
                    title = "Scanner à nouveau la musique",
                    subtitle = "Efface le cache de balises et recharge entièrement la bibliothèque (YouTube Music & Spotify inclus)",
                    onClick = {
                        viewModel.loadMusicSources()
                        snackbarMessage = "Scan complet des plateformes effectué"
                    }
                )
            }
        }
    }

    if (showSourcesDialog) {
        MusicSourcesDialog(
            viewModel = viewModel,
            onDismiss = { showSourcesDialog = false }
        )
    }
}

@Composable
fun SettingsCategoryItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        headlineContent = {
            Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Text(text = subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
}

@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// --- Empty State Composable ---
@Composable
fun AuxioEmptyState(
    icon: ImageVector,
    message: String,
    onSourcesClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (isSystemInDarkTheme()) BadgeBgDark else BadgeBgLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = if (isSystemInDarkTheme()) Color(0xFF90A4AE) else Color(0xFF546E7A)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = message,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSourcesClick,
                colors = ButtonDefaults.buttonColors(containerColor = AuxioTeal),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.testTag("sources_button")
            ) {
                Text(
                    text = "Sources de la musique",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// --- Song List Item ---
@Composable
fun SongListItem(
    song: SongItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onOpenExternal: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCurrent) AuxioTeal else song.source.brandColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isCurrent) Color.White else song.source.brandColor
                )
            }
        },
        headlineContent = {
            Text(
                text = song.title,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrent) AuxioTeal else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${song.artist} • ${song.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = song.source.brandColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = song.source.displayName,
                        fontSize = 10.sp,
                        color = song.source.brandColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.externalUrl.isNotEmpty()) {
                    IconButton(onClick = onOpenExternal) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Ouvrir sur la plateforme",
                            tint = song.source.brandColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Lire",
                        tint = AuxioTeal
                    )
                }
            }
        }
    )
}

// --- Mini Player ---
@Composable
fun AuxioMiniPlayer(
    song: SongItem,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onClick: () -> Unit,
    onOpenExternal: (SongItem) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(song.source.brandColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = song.source.brandColor)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${song.artist} • ${song.source.displayName}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }

            if (song.externalUrl.isNotEmpty()) {
                IconButton(onClick = { onOpenExternal(song) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Ouvrir",
                        tint = song.source.brandColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Lecture / Pause",
                    tint = AuxioTeal
                )
            }
        }
    }
}

// --- Full Screen Player Modal ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuxioFullPlayerModal(
    song: SongItem,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onOpenExternal: (SongItem) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(song.source.brandColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Album,
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    tint = song.source.brandColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = song.source.brandColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Source: ${song.source.displayName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = song.source.brandColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = song.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${song.artist} — ${song.album}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            var sliderPos by remember { mutableFloatStateOf(0.35f) }
            Slider(
                value = sliderPos,
                onValueChange = { sliderPos = it },
                colors = SliderDefaults.colors(
                    thumbColor = AuxioTeal,
                    activeTrackColor = AuxioTeal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1:15", style = MaterialTheme.typography.bodySmall)
                Text(song.duration, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrev) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Précédent", modifier = Modifier.size(36.dp))
                }

                FilledIconButton(
                    onClick = onPlayPause,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = AuxioTeal),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Lecture / Pause",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Suivant", modifier = Modifier.size(36.dp))
                }
            }

            if (song.externalUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { onOpenExternal(song) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = song.source.brandColor)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ouvrir dans ${song.source.displayName}")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
