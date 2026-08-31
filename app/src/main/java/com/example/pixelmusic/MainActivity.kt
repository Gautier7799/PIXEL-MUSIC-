package com.example.pixelmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
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

// --- Models ---
data class SongItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val duration: String
)

data class AlbumItem(
    val id: String,
    val name: String,
    val artist: String,
    val trackCount: Int
)

data class ArtistItem(
    val id: String,
    val name: String,
    val songCount: Int
)

data class GenreItem(
    val id: String,
    val name: String,
    val trackCount: Int
)

data class PlaylistItem(
    val id: String,
    val name: String,
    val trackCount: Int
)

// --- ViewModel ---
class AuxioMusicViewModel : ViewModel() {
    private val sampleSongs = listOf(
        SongItem("1", "Midnight Serenade", "Elena Rostova", "Echoes of Night", "Classical", "3:42"),
        SongItem("2", "Electric Horizon", "CyberPulse", "Neon City", "Electronic", "4:15"),
        SongItem("3", "Acoustic Breeze", "David Vance", "Sunlight & Timber", "Acoustic", "2:58"),
        SongItem("4", "Oriental Dream", "Layla Mansoor", "Oasis Sounds", "World", "5:20"),
        SongItem("5", "Lo-Fi Cafe Vibes", "ChillMaster", "Coffee Beats Vol. 1", "Lo-Fi", "3:10"),
        SongItem("6", "Starlight Symphony", "Elena Rostova", "Echoes of Night", "Classical", "4:45")
    )

    private val sampleAlbums = listOf(
        AlbumItem("1", "Echoes of Night", "Elena Rostova", 2),
        AlbumItem("2", "Neon City", "CyberPulse", 1),
        AlbumItem("3", "Sunlight & Timber", "David Vance", 1),
        AlbumItem("4", "Oasis Sounds", "Layla Mansoor", 1),
        AlbumItem("5", "Coffee Beats Vol. 1", "ChillMaster", 1)
    )

    private val sampleArtists = listOf(
        ArtistItem("1", "Elena Rostova", 2),
        ArtistItem("2", "CyberPulse", 1),
        ArtistItem("3", "David Vance", 1),
        ArtistItem("4", "Layla Mansoor", 1),
        ArtistItem("5", "ChillMaster", 1)
    )

    private val sampleGenres = listOf(
        GenreItem("1", "Classical", 2),
        GenreItem("2", "Electronic", 1),
        GenreItem("3", "Acoustic", 1),
        GenreItem("4", "World", 1),
        GenreItem("5", "Lo-Fi", 1)
    )

    private val samplePlaylists = listOf(
        PlaylistItem("1", "Mes Favoris", 4),
        PlaylistItem("2", "Relax & Chill", 3),
        PlaylistItem("3", "Travail & Focus", 2)
    )

    private val _isMusicLoaded = MutableStateFlow(false)
    val isMusicLoaded: StateFlow<Boolean> = _isMusicLoaded.asStateFlow()

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

    fun loadMusicSources() {
        _isMusicLoaded.value = true
        _songs.value = sampleSongs
        _albums.value = sampleAlbums
        _artists.value = sampleArtists
        _genres.value = sampleGenres
        _playlists.value = samplePlaylists
        if (_currentSong.value == null) {
            _currentSong.value = sampleSongs.firstOrNull()
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

// --- Colors ---
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
                AuxioMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuxioMainScreen(viewModel: AuxioMusicViewModel = viewModel()) {
    val tabs = listOf("Titres", "Albums", "Artistes", "Genres", "Playlists")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showSourcesDialog by remember { mutableStateOf(false) }
    var showPlayerSheet by remember { mutableStateOf(false) }

    val isMusicLoaded by viewModel.isMusicLoaded.collectAsState()
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
                            placeholder = { Text("Rechercher...") },
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
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
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
                        Text(
                            text = "Auxio",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.testTag("search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Rechercher")
                        }
                        IconButton(
                            onClick = { /* Sort dialog */ },
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
                                    text = { Text("Sources de la musique") },
                                    onClick = {
                                        showMenu = false
                                        showSourcesDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Paramètres") },
                                    onClick = { showMenu = false },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isMusicLoaded) "Vider la liste" else "Charger démo") },
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
                    onClick = { showPlayerSheet = true }
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
                            message = "Vos titres s'afficheront ici.",
                            onSourcesClick = { showSourcesDialog = true }
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(songs.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) }) { song ->
                                SongListItem(
                                    song = song,
                                    isCurrent = currentSong?.id == song.id,
                                    isPlaying = isPlaying && currentSong?.id == song.id,
                                    onClick = { viewModel.playSong(song) }
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
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Album, contentDescription = null, modifier = Modifier.size(48.dp), tint = AuxioTeal)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(album.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${album.artist} • ${album.trackCount} titres", style = MaterialTheme.typography.bodySmall, maxLines = 1)
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
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = AuxioTeal)
                                        }
                                    },
                                    headlineContent = { Text(artist.name, fontWeight = FontWeight.Medium) },
                                    supportingContent = { Text("${artist.songCount} titres") }
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
                            message = "Vos playlists s'afficheront ici.",
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
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.QueueMusic, contentDescription = null, tint = AuxioTeal)
                                        }
                                    },
                                    headlineContent = { Text(playlist.name, fontWeight = FontWeight.Medium) },
                                    supportingContent = { Text("${playlist.trackCount} titres") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sources Dialog
    if (showSourcesDialog) {
        AlertDialog(
            onDismissRequest = { showSourcesDialog = false },
            icon = { Icon(Icons.Default.Folder, contentDescription = null, tint = AuxioTeal) },
            title = { Text("Sources de la musique") },
            text = {
                Column {
                    Text("Choisissez la source de votre bibliothèque musicale :")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.loadMusicSources()
                            showSourcesDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AuxioTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LibraryMusic, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Charger la bibliothèque musicale")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSourcesDialog = false }) {
                    Text("Fermer")
                }
            }
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
            onPrev = { viewModel.previousSong() }
        )
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
    onClick: () -> Unit
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
                    .background(if (isCurrent) AuxioTeal else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isCurrent) Color.White else AuxioTeal
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
            Text(
                text = "${song.artist} • ${song.duration}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        },
        trailingContent = {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Lire",
                    tint = AuxioTeal
                )
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
    onClick: () -> Unit
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
                    .background(AuxioTeal),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1)
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
    onPrev: () -> Unit
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
                    .background(AuxioTeal.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Album,
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    tint = AuxioTeal
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
