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

// --- Platform Enums & Models ---
enum class MusicSource(
    val displayName: String,
    val shortName: String,
    val brandColor: Color,
    val badgeBg: Color
) {
    ALL("Toutes les sources", "Tous", Color(0xFF00677D), Color(0xFFE0F7FA)),
    YOUTUBE_MUSIC("YouTube Music", "YT Music", Color(0xFFFF0000), Color(0xFFFFEBEE)),
    SPOTIFY("Spotify", "Spotify", Color(0xFF1DB954), Color(0xFFE8F8EE)),
    DEEZER("Deezer", "Deezer", Color(0xFFA238FF), Color(0xFFF3E5F5)),
    LOCAL("Stockage local", "Local", Color(0xFF455A64), Color(0xFFECEFF1))
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

// --- ViewModel ---
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

// --- Theme Implementation with Material You Wallpaper Colors ---
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

// Helper to open platform links
fun openExternalMusic(context: Context, song: SongItem) {
    if (song.externalUrl.isNotEmpty()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.externalUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Ouverture de ${song.source.displayName}...", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Lecture locale en direct : ${song.title}", Toast.LENGTH_SHORT).show()
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}

// --- Live Audio Equalizer Waveform Indicator ---
@Composable
fun AudioEqualizerWaveform(isPlaying: Boolean, tint: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(h1, h2, h3).forEach { heightRatio ->
            val actualH = if (isPlaying) (16 * heightRatio).dp else 4.dp
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(actualH)
                    .clip(RoundedCornerShape(2.dp))
                    .background(tint)
            )
        }
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
    val accentColor = MaterialTheme.colorScheme.primary
    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Rechercher YouTube, Spotify, Local...") },
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
                                text = "Pixel Music",
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
                                        text = activeSource.shortName,
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
                            Icon(Icons.Default.CloudQueue, contentDescription = "Plateformes", tint = accentColor)
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
                                    text = { Text("Sources & Plateformes") },
                                    onClick = {
                                        showMenu = false
                                        showSourcesDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = accentColor) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Paramètres (Réglages)") },
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
                    isBuffering = isBuffering,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onClick = { showPlayerSheet = true },
                    onOpenExternal = { song -> openExternalMusic(context, song) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isScanning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = accentColor)
            }

            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = accentColor,
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
                                color = if (selectedTabIndex == index) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
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
                        val filtered = songs.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.artist.contains(searchQuery, ignoreCase = true) ||
                            it.album.contains(searchQuery, ignoreCase = true)
                        }
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filtered) { song ->
                                SongListItem(
                                    song = song,
                                    isCurrent = currentSong?.id == song.id,
                                    isPlaying = isPlaying && currentSong?.id == song.id,
                                    isBuffering = isBuffering && currentSong?.id == song.id,
                                    onClick = {
                                        if (currentSong?.id == song.id) {
                                            viewModel.togglePlayPause()
                                        } else {
                                            viewModel.playSong(song)
                                        }
                                    },
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val song = songs.find { it.album == album.name }
                                            if (song != null) viewModel.playSong(song)
                                            Toast.makeText(context, "Lecture : ${album.name}", Toast.LENGTH_SHORT).show()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(album.coverUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = album.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
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
                                    modifier = Modifier.clickable {
                                        val song = songs.find { it.artist == artist.name }
                                        if (song != null) viewModel.playSong(song)
                                        Toast.makeText(context, "Artiste : ${artist.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    leadingContent = {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(artist.avatarUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = artist.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                        )
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
                                    modifier = Modifier.clickable {
                                        Toast.makeText(context, "Genre: ${genre.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    leadingContent = {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(genre.coverUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = genre.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
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
                                    modifier = Modifier.clickable {
                                        Toast.makeText(context, "Playlist: ${playlist.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    leadingContent = {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(playlist.coverUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = playlist.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
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
            icon = { Icon(Icons.Default.CloudQueue, contentDescription = null, tint = accentColor) },
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
            icon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, tint = accentColor) },
            title = { Text("Trier par") },
            text = {
                Column {
                    listOf("Titre (A à Z)", "Artiste", "Album", "Plateforme (Source)", "Durée").forEach { sortOption ->
                        TextButton(
                            onClick = {
                                showSortDialog = false
                                Toast.makeText(context, "Trié par: $sortOption", Toast.LENGTH_SHORT).show()
                            },
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
            viewModel = viewModel,
            song = currentSong!!,
            isPlaying = isPlaying,
            isBuffering = isBuffering,
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
    val accentColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = accentColor) },
        title = { Text("Sources & Plateformes") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Activez ou désactivez les plateformes pour synchroniser vos musiques en ligne et en local :",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                PlatformToggleRow(
                    name = "YouTube Music",
                    color = Color(0xFFFF0000),
                    icon = Icons.Default.PlayCircleFilled,
                    isConnected = connectedPlatforms[MusicSource.YOUTUBE_MUSIC] == true,
                    onToggle = { viewModel.togglePlatformConnection(MusicSource.YOUTUBE_MUSIC) }
                )

                PlatformToggleRow(
                    name = "Spotify",
                    color = Color(0xFF1DB954),
                    icon = Icons.Default.Audiotrack,
                    isConnected = connectedPlatforms[MusicSource.SPOTIFY] == true,
                    onToggle = { viewModel.togglePlatformConnection(MusicSource.SPOTIFY) }
                )

                PlatformToggleRow(
                    name = "Deezer",
                    color = Color(0xFFA238FF),
                    icon = Icons.Default.Equalizer,
                    isConnected = connectedPlatforms[MusicSource.DEEZER] == true,
                    onToggle = { viewModel.togglePlatformConnection(MusicSource.DEEZER) }
                )

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
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Appliquer & Synchroniser")
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
    val context = LocalContext.current
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var showSourcesDialog by remember { mutableStateOf(false) }

    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showPersonalizationDialog by remember { mutableStateOf(false) }
    var showContentDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }

    val accentColor = MaterialTheme.colorScheme.primary

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
                            Text("OK", color = accentColor)
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
                    subtitle = "Changer le thème (Sombre/Clair) et la couleur d'accentuation",
                    onClick = { showAppearanceDialog = true }
                )
            }
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Tune,
                    title = "Personnalisation",
                    subtitle = "Commandes de gestes, vue de la file d'attente et affichage",
                    onClick = { showPersonalizationDialog = true }
                )
            }
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.MusicNote,
                    title = "Contenu",
                    subtitle = "Gestion du cache, pochettes d'albums et filtrage des pistes",
                    onClick = { showContentDialog = true }
                )
            }
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.PlayArrow,
                    title = "Audio",
                    subtitle = "Lecture continue (Gapless), ReplayGain et Fondu enchaîné (Crossfade)",
                    onClick = { showAudioDialog = true }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                Text(
                    text = "Plateformes & Bibliothèque",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                SettingsActionItem(
                    title = "Sources de la musique (YouTube Music / Spotify / Local)",
                    subtitle = "Gérer la synchronisation avec les plateformes et le stockage local",
                    onClick = { showSourcesDialog = true }
                )
            }
            item {
                SettingsActionItem(
                    title = "Actualiser la musique",
                    subtitle = "Recharge la bibliothèque musicale et synchronise le cloud",
                    onClick = {
                        viewModel.refreshMusic()
                        snackbarMessage = "Bibliothèque musicale actualisée avec succès !"
                    }
                )
            }
            item {
                SettingsActionItem(
                    title = "Scanner à nouveau la musique",
                    subtitle = "Efface le cache de balises et recharge entièrement la bibliothèque",
                    onClick = {
                        viewModel.scanMusicAsync {
                            snackbarMessage = "Scan complet effectué avec succès !"
                        }
                    }
                )
            }
        }
    }

    // Appearance Dialog
    if (showAppearanceDialog) {
        val themeMode by viewModel.themeMode.collectAsState()
        val isDynamicColor by viewModel.isDynamicColor.collectAsState()
        val currentAccent by viewModel.accentColor.collectAsState()
        val accentChoices = listOf(
            Color(0xFF00677D) to "Teal Pixel",
            Color(0xFF00897B) to "Émeraude",
            Color(0xFF1E88E5) to "Bleu Cobalt",
            Color(0xFF8E24AA) to "Violet",
            Color(0xFFD84315) to "Orange Flamboyant"
        )

        AlertDialog(
            onDismissRequest = { showAppearanceDialog = false },
            icon = { Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Apparence & Thème") },
            text = {
                Column {
                    Text("Mode d'affichage :", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        ThemeMode.SYSTEM to "Suivre le système",
                        ThemeMode.LIGHT to "Thème Clair",
                        ThemeMode.DARK to "Thème Sombre"
                    ).forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Material You Dynamic Wallpaper Colors
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleDynamicColor() }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Couleurs dynamiques (Material You)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 
                                    "Adopter les couleurs du fond d'écran du téléphone" 
                                else 
                                    "Nécessite Android 12 ou supérieur",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                            onCheckedChange = { viewModel.toggleDynamicColor() }
                        )
                    }

                    if (!isDynamicColor || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Couleur d'accentuation personnalisée :", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            accentChoices.forEach { (color, _) ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { viewModel.setAccentColor(color) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (currentAccent == color && !isDynamicColor) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppearanceDialog = false }) {
                    Text("Terminer")
                }
            }
        )
    }

    // Personalization Dialog
    if (showPersonalizationDialog) {
        var showCoversInList by remember { mutableStateOf(true) }
        var enableFastScroll by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showPersonalizationDialog = false },
            icon = { Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Personnalisation") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pochettes dans les listes", fontWeight = FontWeight.SemiBold)
                            Text("Afficher les images d'albums dans les listes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = showCoversInList, onCheckedChange = { showCoversInList = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Défilement rapide", fontWeight = FontWeight.SemiBold)
                            Text("Activer la barre de navigation alphabétique", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = enableFastScroll, onCheckedChange = { enableFastScroll = it })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPersonalizationDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Content Dialog
    if (showContentDialog) {
        val autoDownloadCovers by viewModel.autoDownloadCovers.collectAsState()
        val filterShortTracks by viewModel.filterShortTracks.collectAsState()

        AlertDialog(
            onDismissRequest = { showContentDialog = false },
            icon = { Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Contenu & Bibliothèque") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Téléchargement automatique des pochettes", fontWeight = FontWeight.SemiBold)
                            Text("Récupérer les images Spotify / YouTube Music", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = autoDownloadCovers, onCheckedChange = { viewModel.toggleAutoDownloadCovers() })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Filtrer les pistes courtes", fontWeight = FontWeight.SemiBold)
                            Text("Ignorer les audios de moins de 30 secondes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = filterShortTracks, onCheckedChange = { viewModel.toggleFilterShortTracks() })
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Cache de balises vidé avec succès", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vider le cache des pochettes")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContentDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Audio Dialog
    if (showAudioDialog) {
        val gapless by viewModel.gaplessPlayback.collectAsState()
        val replayGain by viewModel.replayGain.collectAsState()
        val crossfade by viewModel.crossfadeSeconds.collectAsState()

        AlertDialog(
            onDismissRequest = { showAudioDialog = false },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Paramètres Audio") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lecture sans blanc (Gapless)", fontWeight = FontWeight.SemiBold)
                            Text("Éliminer les silences بين les morceaux", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = gapless, onCheckedChange = { viewModel.toggleGapless() })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ReplayGain (Normalisation)", fontWeight = FontWeight.SemiBold)
                            Text("Ajuster automatiquement le volume sonore", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = replayGain, onCheckedChange = { viewModel.toggleReplayGain() })
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fondu enchaîné (Crossfade) : $crossfade secondes", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Slider(
                        value = crossfade.toFloat(),
                        onValueChange = { viewModel.setCrossfade(it.toInt()) },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioDialog = false }) {
                    Text("OK")
                }
            }
        )
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
    val accentColor = MaterialTheme.colorScheme.primary

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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = accentColor
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
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
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

// --- Song List Item with Real Artwork and Equalizer ---
@Composable
fun SongListItem(
    song: SongItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit,
    onOpenExternal: () -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            AudioEqualizerWaveform(
                                isPlaying = isPlaying,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        headlineContent = {
            Text(
                text = song.title,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isCurrent) accentColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
            ) {
                Text(
                    text = "${song.artist} • ${song.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = song.source.brandColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = song.source.shortName,
                        fontSize = 10.sp,
                        color = song.source.brandColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
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
                            contentDescription = "Ouvrir dans l'application",
                            tint = song.source.brandColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(onClick = onClick) {
                    if (isCurrent && isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = accentColor
                        )
                    } else {
                        Icon(
                            imageVector = if (isCurrent && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Lire",
                            tint = accentColor
                        )
                    }
                }
            }
        }
    )
}

// --- Mini Player with Real Artwork and Waveform ---
@Composable
fun AuxioMiniPlayer(
    song: SongItem,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onPlayPause: () -> Unit,
    onClick: () -> Unit,
    onOpenExternal: (SongItem) -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
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
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AudioEqualizerWaveform(isPlaying = isPlaying, tint = accentColor)
                }
                Text(
                    text = "${song.artist} • ${song.source.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            if (song.externalUrl.isNotEmpty()) {
                IconButton(onClick = { onOpenExternal(song) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Ouvrir dans l'application",
                        tint = song.source.brandColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            IconButton(onClick = onPlayPause) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = accentColor
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Lecture / Pause",
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// --- Full Screen Player Modal with High Res Artwork & Interactive Controls ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuxioFullPlayerModal(
    viewModel: AuxioMusicViewModel,
    song: SongItem,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onOpenExternal: (SongItem) -> Unit
) {
    val currentPosition by viewModel.currentPositionSeconds.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val isRepeat by viewModel.isRepeat.collectAsState()
    val accentColor = MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(song.source.brandColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isBuffering) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                color = song.source.brandColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (song.source) {
                            MusicSource.YOUTUBE_MUSIC -> Icons.Default.PlayCircleFilled
                            MusicSource.SPOTIFY -> Icons.Default.Audiotrack
                            MusicSource.DEEZER -> Icons.Default.Equalizer
                            else -> Icons.Default.Folder
                        },
                        contentDescription = null,
                        tint = song.source.brandColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Source : ${song.source.displayName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = song.source.brandColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${song.artist} — ${song.album}",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { viewModel.toggleFavorite(song.id) }) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (song.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Progress Slider
            val maxSeconds = song.durationSeconds.toFloat().coerceAtLeast(1f)
            val progress = (currentPosition.toFloat() / maxSeconds).coerceIn(0f, 1f)

            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    val newSeconds = (newProgress * maxSeconds).toInt()
                    viewModel.seekTo(newSeconds)
                },
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(currentPosition), style = MaterialTheme.typography.bodySmall)
                Text(song.duration, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Aléatoire",
                        tint = if (isShuffle) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onPrev) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Précédent", modifier = Modifier.size(36.dp))
                }

                FilledIconButton(
                    onClick = onPlayPause,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = accentColor),
                    modifier = Modifier.size(64.dp)
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Lecture / Pause",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Suivant", modifier = Modifier.size(36.dp))
                }

                IconButton(onClick = { viewModel.toggleRepeat() }) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = "Répéter",
                        tint = if (isRepeat) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
