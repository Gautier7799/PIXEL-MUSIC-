package com.example.pixelmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelmusic.model.Song
import com.example.pixelmusic.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun searchMusic(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // استدعاء الـ API للبحث
                val results = RetrofitClient.apiService.searchSongs(query)
                _songs.value = results
            } catch (e: Exception) {
                // في حالة وجود خطأ (لا يوجد إنترنت مثلاً)، نمسح القائمة
                _songs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
