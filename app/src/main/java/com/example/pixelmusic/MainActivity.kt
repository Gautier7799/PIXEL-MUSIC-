package com.example.pixelmusic

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- فئات ومصادر المنصات ---
enum class MusicSource(val displayName: String, val brandColor: Color, val badgeBg: Color) {
    ALL("Toutes les sources", Color(0xFF00677D), Color(0xFFE0F7FA)),
    YOUTUBE_MUSIC("YouTube Music", Color(0xFFFF0000), Color(0xFFFFEBEE)),
    SPOTIFY("Spotify", Color(0xFF1DB954), Color(0xFFE8F8EE)),
    DEEZER("Deezer", Color(0xFFA238FF), Color(0xFFF3E5F5)),
    LOCAL("Stockage local", Color(0xFF455A64), Color(0xFFECEFF1))
}

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

data class SongItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val duration: String,
    val durationSeconds: Int = 210,
    val source: MusicSource = MusicSource.LOCAL,
    val coverUrl: String = "",
    val audioUrl: String = "",
    val externalUrl: String = "",
    var isFavorite: Boolean = false
)

data class AlbumItem(
    val id: String,
    val name: String,
    val artist: String,
    val trackCount: Int,
    val coverUrl: String,
    val source: MusicSource = MusicSource.LOCAL
)

data class ArtistItem(
    val id: String,
    val name: String,
    val songCount: Int,
    val avatarUrl: String,
    val source: MusicSource = MusicSource.LOCAL
)

data class GenreItem(
    val id: String,
    val name: String,
    val trackCount: Int,
    val coverUrl: String
)

data class PlaylistItem(
    val id: String,
    val name: String,
    val trackCount: Int,
    val coverUrl: String,
    val source: MusicSource = MusicSource.LOCAL
)

// --- ViewModel مع محرك تشغيل الصوت والتحكم بالألوان الديناميكية ---
class AuxioMusicViewModel : ViewModel() {
    private var mediaPlayer: MediaPlayer? = null

    private val localSongs = listOf(
        SongItem(
            id = "loc_1",
            title = "Midnight Serenade",
            artist = "Elena Rostova",
            album = "Echoes of Night",
            genre = "Classical",
            duration = "3:42",
            durationSeconds = 222,
            source = MusicSource.LOCAL,
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        ),
        SongItem(
            id = "loc_2",
            title = "Electric Horizon",
            artist = "CyberPulse",
            album = "Neon City",
            genre = "Electronic",
            duration = "4:15",
            durationSeconds = 255,
            source = MusicSource.LOCAL,
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
        ),
        SongItem(
            id = "loc_3",
            title = "Acoustic Breeze",
            artist = "David Vance",
            album = "Sunlight & Timber",
            genre = "Acoustic",
            duration = "2:58",
            durationSeconds = 178,
            source = MusicSource.LOCAL,
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
        ),
        SongItem(
            id = "loc_4",
            title = "Oriental Dream",
            artist = "Layla Mansoor",
            album = "Oasis Sounds",
            genre = "World",
            duration = "5:20",
            durationSeconds = 320,
            source = MusicSource.LOCAL,
            coverUrl = "https://images.unsplash.com/photo-1511735111819-9a3f7709049c?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
        ),
        SongItem(
            id = "loc_5",
            title = "Lo-Fi Cafe Vibes",
            artist = "ChillMaster",
            album = "Coffee Beats Vol. 1",
            genre = "Lo-Fi",
            duration = "3:10",
            durationSeconds = 190,
            source = MusicSource.LOCAL,
            coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"
        )
    )

    private val youtubeSongs = listOf(
        SongItem(
            id = "yt_1",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            genre = "Pop / Synthwave",
            duration = "3:20",
            durationSeconds = 200,
            source = MusicSource.YOUTUBE_MUSIC,
            coverUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
            externalUrl = "https://music.youtube.com/search?q=The+Weeknd+Blinding+Lights"
        ),
        SongItem(
            id = "yt_2",
            title = "Shape of You",
            artist = "Ed Sheeran",
            album = "÷ (Divide)",
            genre = "Pop",
            duration = "3:53",
            durationSeconds = 233,
            source = MusicSource.YOUTUBE_MUSIC,
            coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
            externalUrl = "https://music.youtube.com/search?q=Ed+Sheeran+Shape+of+You"
        ),
        SongItem(
            id = "yt_3",
            title = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy",
            genre = "Electro-Pop",
            duration = "3:50",
            durationSeconds = 230,
            source = MusicSource.YOUTUBE_MUSIC,
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3",
            externalUrl = "https://music.youtube.com/search?q=The+Weeknd+Starboy"
        ),
        SongItem(
            id = "yt_4",
            title = "Levitating",
            artist = "Dua Lipa",
            album = "Future Nostalgia",
            genre = "Dance-Pop",
            duration = "3:23",
            durationSeconds = 203,
            source = MusicSource.YOUTUBE_MUSIC,
            coverUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3",
            externalUrl = "https://music.youtube.com/search?q=Dua+Lipa+Levitating"
        ),
        SongItem(
            id = "yt_5",
            title = "Believer",
            artist = "Imagine Dragons",
            album = "Evolve",
            genre = "Alternative Rock",
            duration = "3:24",
            durationSeconds = 204,
            source = MusicSource.YOUTUBE_MUSIC,
            coverUrl = "https://images.unsplash.com/photo-1445985543470-41fba5c3144a?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            externalUrl = "https://music.youtube.com/search?q=Imagine+Dragons+Believer"
        )
    )

    private val spotifySongs = listOf(
        SongItem(
            id = "sp_1",
            title = "As It Was",
            artist = "Harry Styles",
            album = "Harry's House",
            genre = "Indie Pop",
            duration = "2:47",
            durationSeconds = 167,
            source = MusicSource.SPOTIFY,
            coverUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            externalUrl = "https://open.spotify.com/search/Harry%20Styles%20As%20It%20Was"
        ),
        SongItem(
            id = "sp_2",
            title = "Flowers",
            artist = "Miley Cyrus",
            album = "Endless Summer Vacation",
            genre = "Pop Rock",
            duration = "3:20",
            durationSeconds = 200,
            source = MusicSource.SPOTIFY,
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            externalUrl = "https://open.spotify.com/search/Miley%20Cyrus%20Flowers"
        ),
        SongItem(
            id = "sp_3",
            title = "Cruel Summer",
            artist = "Taylor Swift",
            album = "Lover",
            genre = "Synth-Pop",
            duration = "2:58",
            durationSeconds = 178,
            source = MusicSource.SPOTIFY,
            coverUrl = "https://images.unsplash.com/photo-1507838153414-b4b713384a76?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            externalUrl = "https://open.spotify.com/search/Taylor%20Swift%20Cruel%20Summer"
        ),
        SongItem(
            id = "sp_4",
            title = "Stay",
            artist = "The Kid LAROI, Justin Bieber",
            album = "F*CK LOVE 3",
            genre = "Pop",
            duration = "2:21",
            durationSeconds = 141,
            source = MusicSource.SPOTIFY,
            coverUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            externalUrl = "https://open.spotify.com/search/The%20Kid%20LAROI%20Stay"
        ),
        SongItem(
            id = "sp_5",
            title = "Save Your Tears",
            artist = "The Weeknd",
            album = "After Hours",
            genre = "Synth-Pop",
            duration = "3:35",
            durationSeconds = 215,
            source = MusicSource.SPOTIFY,
            coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
            externalUrl = "https://open.spotify.com/search/The%20Weeknd%20Save%20Your%20Tears"
        )
    )

    private val deezerSongs = listOf(
        SongItem(
            id = "dz_1",
            title = "Bad Guy",
            artist = "Billie Eilish",
            album = "When We All Fall Asleep",
            genre = "Electropop",
            duration = "3:14",
            durationSeconds = 194,
            source = MusicSource.DEEZER,
            coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
            externalUrl = "https://www.deezer.com/search/Billie%20Eilish%20Bad%20Guy"
        ),
        SongItem(
            id = "dz_2",
            title = "Dance Monkey",
            artist = "Tones and I",
            album = "The Kids Are Coming",
            genre = "Pop",
            duration = "3:29",
            durationSeconds = 209,
            source = MusicSource.DEEZER,
            coverUrl = "https://images.unsplash.com/photo-1520523839898-507125ef538a?w=500&auto=format&fit=crop&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3",
            externalUrl = "https://www.deezer.com/search/Dance%20Monkey"
        )
    )

    private val allSampleAlbums = listOf(
        AlbumItem("1", "Echoes of Night", "Elena Rostova", 2, "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80", MusicSource.LOCAL),
        AlbumItem("2", "Neon City", "CyberPulse", 1, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80", MusicSource.LOCAL),
        AlbumItem("3", "After Hours", "The Weeknd", 2, "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500&auto=format&fit=crop&q=80", MusicSource.YOUTUBE_MUSIC),
        AlbumItem("4", "Harry's House", "Harry Styles", 1, "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500&auto=format&fit=crop&q=80", MusicSource.SPOTIFY),
        AlbumItem("5", "Lover", "Taylor Swift", 1, "https://images.unsplash.com/photo-1507838153414-b4b713384a76?w=500&auto=format&fit=crop&q=80", MusicSource.SPOTIFY),
        AlbumItem("6", "Evolve", "Imagine Dragons", 1, "https://images.unsplash.com/photo-1445985543470-41fba5c3144a?w=500&auto=format&fit=crop&q=80", MusicSource.YOUTUBE_MUSIC)
    )

    private val allSampleArtists = listOf(
        ArtistItem("1", "The Weeknd", 3, "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500&auto=format&fit=crop&q=80", MusicSource.YOUTUBE_MUSIC),
        ArtistItem("2", "Elena Rostova", 2, "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80", MusicSource.LOCAL),
        ArtistItem("3", "Harry Styles", 1, "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500&auto=format&fit=crop&q=80", MusicSource.SPOTIFY),
        ArtistItem("4", "Taylor Swift", 1, "https://images.unsplash.com/photo-1507838153414-b4b713384a76?w=500&auto=format&fit=crop&q=80", MusicSource.SPOTIFY),
        ArtistItem("5", "CyberPulse", 1, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80", MusicSource.LOCAL),
        ArtistItem("6", "Imagine Dragons", 1, "https://images.unsplash.com/photo-1445985543470-41fba5c3144a?w=500&auto=format&fit=crop&q=80", MusicSource.YOUTUBE_MUSIC)
    )

    private val allSampleGenres = listOf(
        GenreItem("1", "Pop & Synth-Pop", 6, "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500&auto=format&fit=crop&q=80"),
        GenreItem("2", "Classical", 2, "https://images.unsplash.com/photo-1507838153414-b4b713384a76?w=500&auto=format&fit=crop&q=80"),
        GenreItem("3", "Electronic", 2, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80"),
        GenreItem("4", "Alternative Rock", 1, "https://images.unsplash.com/photo-1445985543470-41fba5c3144a?w=500&auto=format&fit=crop&q=80"),
        GenreItem("5", "Acoustic & Lo-Fi", 2, "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80")
    )

    private val allSamplePlaylists = listOf(
        PlaylistItem("1", "Mes Favoris (Local)", 4, "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80", MusicSource.LOCAL),
        PlaylistItem("2", "Top Hits 2026 (Spotify)", 5, "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500&auto=format&fit=crop&q=80", MusicSource.SPOTIFY),
        PlaylistItem("3", "YouTube Music Mix", 5, "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500&auto=format&fit=crop&q=80", MusicSource.YOUTUBE_MUSIC),
        PlaylistItem("4", "Relax & Focus", 3, "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&auto=format&fit=crop&q=80", MusicSource.LOCAL)
    )

    private val _isMusicLoaded = MutableStateFlow(true)
    val isMusicLoaded: StateFlow<Boolean> = _isMusicLoaded.asStateFlow()

    private val _activeSource = MutableStateFlow(MusicSource.ALL)
    val activeSource: StateFlow<MusicSource> = _activeSource.asStateFlow()

    private val _connectedPlatforms = MutableStateFlow(
        mapOf(
            MusicSource.LOCAL to true,
            MusicSource.YOUTUBE_MUSIC to true,
            MusicSource.SPOTIFY to true,
            MusicSource.DEEZER to true
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

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentPositionSeconds = MutableStateFlow(0)
    val currentPositionSeconds: StateFlow<Int> = _currentPositionSeconds.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentScreen = MutableStateFlow("main")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Settings
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isDynamicColor = MutableStateFlow(true)
    val isDynamicColor: StateFlow<Boolean> = _isDynamicColor.asStateFlow()

    private val _accentColor = MutableStateFlow(Color(0xFF00677D))
    val accentColor: StateFlow<Color> = _accentColor.asStateFlow()

    private val _gaplessPlayback = MutableStateFlow(true)
    val gaplessPlayback: StateFlow<Boolean> = _gaplessPlayback.asStateFlow()

    private val _replayGain = MutableStateFlow(true)
    val replayGain: StateFlow<Boolean> = _replayGain.asStateFlow()

    private val _crossfadeSeconds = MutableStateFlow(2)
    val crossfadeSeconds: StateFlow<Int> = _crossfadeSeconds.asStateFlow()

    private val _autoDownloadCovers = MutableStateFlow(true)
    val autoDownloadCovers: StateFlow<Boolean> = _autoDownloadCovers.asStateFlow()

    private val _filterShortTracks = MutableStateFlow(true)
    val filterShortTracks: StateFlow<Boolean> = _filterShortTracks.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        loadMusicSources()
        startAudioSyncTicker()
    }

    private fun startAudioSyncTicker() {
        viewModelScope.launch {
            while (true) {
                delay(500)
                try {
                    if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                        val pos = mediaPlayer?.currentPosition ?: 0
                        _currentPositionSeconds.value = pos / 1000
                    }
                } catch (e: Exception) {
                    // Safe guard
                }
            }
        }
    }

    fun playSong(song: SongItem) {
        _currentSong.value = song
        _currentPositionSeconds.value = 0
        _isBuffering.value = true
        _isPlaying.value = false

        viewModelScope.launch {
            try {
                mediaPlayer?.release()
                mediaPlayer = null

                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(song.audioUrl)
                    setOnPreparedListener { mp ->
                        _isBuffering.value = false
                        _isPlaying.value = true
                        mp.start()
                    }
                    setOnCompletionListener {
                        if (_isRepeat.value) {
                            seekTo(0)
                            it.start()
                        } else {
                            nextSong()
                        }
                    }
                    setOnErrorListener { _, _, _ ->
                        _isBuffering.value = false
                        _isPlaying.value = false
                        true
                    }
                    prepareAsync()
                }
                mediaPlayer = player
            } catch (e: Exception) {
                _isBuffering.value = false
                _isPlaying.value = false
            }
        }
    }

    fun togglePlayPause() {
        val current = _currentSong.value
        if (current == null) {
            val first = _songs.value.firstOrNull()
            if (first != null) playSong(first)
            return
        }

        try {
            if (mediaPlayer == null) {
                playSong(current)
            } else {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    _isPlaying.value = false
                } else {
                    mediaPlayer?.start()
                    _isPlaying.value = true
                }
            }
        } catch (e: Exception) {
            playSong(current)
        }
    }

    fun seekTo(seconds: Int) {
        _currentPositionSeconds.value = seconds
        try {
            mediaPlayer?.seekTo(seconds * 1000)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun nextSong() {
        val list = _songs.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _currentSong.value?.id }
        val nextIndex = if (_isShuffle.value) {
            (list.indices).random()
        } else {
            if (currentIndex != -1 && currentIndex < list.size - 1) currentIndex + 1 else 0
        }
        playSong(list[nextIndex])
    }

    fun previousSong() {
        val list = _songs.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _currentSong.value?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        playSong(list[prevIndex])
    }

    fun toggleShuffle() { _isShuffle.value = !_isShuffle.value }
    fun toggleRepeat() { _isRepeat.value = !_isRepeat.value }

    fun toggleFavorite(songId: String) {
        _songs.value = _songs.value.map {
            if (it.id == songId) it.copy(isFavorite = !it.isFavorite) else it
        }
        if (_currentSong.value?.id == songId) {
            _currentSong.value = _currentSong.value?.let { it.copy(isFavorite = !it.isFavorite) }
        }
    }

    fun setThemeMode(mode: ThemeMode) { _themeMode.value = mode }
    fun toggleDynamicColor() { _isDynamicColor.value = !_isDynamicColor.value }
    fun setAccentColor(color: Color) { 
        _accentColor.value = color
        _isDynamicColor.value = false
    }
    fun toggleGapless() { _gaplessPlayback.value = !_gaplessPlayback.value }
    fun toggleReplayGain() { _replayGain.value = !_replayGain.value }
    fun setCrossfade(seconds: Int) { _crossfadeSeconds.value = seconds }
    fun toggleAutoDownloadCovers() { _autoDownloadCovers.value = !_autoDownloadCovers.value }
    fun toggleFilterShortTracks() { _filterShortTracks.value = !_filterShortTracks.value }
    fun navigateTo(screen: String) { _currentScreen.value = screen }
    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }

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

    fun scanMusicAsync(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isScanning.value = true
            delay(1000)
            loadMusicSources()
            _isScanning.value = false
            onComplete()
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

    fun refreshMusic() { loadMusicSources() }

    fun clearMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _isMusicLoaded.value = false
        _songs.value = emptyList()
        _albums.value = emptyList()
        _artists.value = emptyList()
        _genres.value = emptyList()
        _playlists.value = emptyList()
        _currentSong.value = null
        _isPlaying.value = false
        _currentPositionSeconds.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

// --- تطبيق الثيم مع استخراج ألوان خلفية الهاتف الديناميكية ---
@Composable
fun PixelMusicTheme(
    viewModel: AuxioMusicViewModel,
    content: @Composable () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val isDynamicColor by viewModel.isDynamicColor.collectAsState()
    val context = LocalContext.current

    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val useDynamic = isDynamicColor && dynamicAvailable

    val colorScheme = when {
        useDynamic && isDark -> dynamicDarkColorScheme(context)
        useDynamic && !isDark -> dynamicLightColorScheme(context)
        isDark -> {
            darkColorScheme(
                primary = accentColor,
                secondary = accentColor.copy(alpha = 0.85f),
                background = Color(0xFF0E1315),
                surface = Color(0xFF161C1E),
                surfaceVariant = Color(0xFF222B2E),
                onPrimary = Color.White,
                onBackground = Color(0xFFE4E7E8),
                onSurface = Color(0xFFE4E7E8)
            )
        }
        else -> {
            lightColorScheme(
                primary = accentColor,
                secondary = accentColor.copy(alpha = 0.85f),
                background = Color(0xFFF9FBFC),
                surface = Color.White,
                surfaceVariant = Color(0xFFE8EEF0),
                onPrimary = Color.White,
                onBackground = Color(0xFF171D1F),
                onSurface = Color(0xFF171D1F)
            )
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AuxioMusicViewModel = viewModel()
            PixelMusicTheme(viewModel = viewModel) {
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
