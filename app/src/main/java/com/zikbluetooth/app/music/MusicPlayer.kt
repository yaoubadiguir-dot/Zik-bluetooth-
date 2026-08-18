package com.zikbluetooth.app.music

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MusicPlayer(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition
    
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration
    
    private val _currentTrack = MutableStateFlow<String?>(null)
    val currentTrack: StateFlow<String?> = _currentTrack
    
    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError
    
    /**
     * Charger un fichier audio
     */
    fun loadTrack(uri: Uri, onLoaded: () -> Unit = {}, onError: (String) -> Unit = {}) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setOnPreparedListener {
                    _duration.value = duration
                    _currentTrack.value = uri.lastPathSegment
                    _playbackError.value = null
                    onLoaded()
                }
                setOnErrorListener { _, what, extra ->
                    val error = "MediaPlayer Error: $what, $extra"
                    _playbackError.value = error
                    onError(error)
                    true
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            val error = "Erreur lors du chargement: ${e.message}"
            _playbackError.value = error
            onError(error)
        }
    }
    
    /**
     * Charger depuis un chemin fichier
     */
    fun loadTrackFromPath(filePath: String, onLoaded: () -> Unit = {}, onError: (String) -> Unit = {}) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener {
                    _duration.value = duration
                    _currentTrack.value = filePath.substringAfterLast("/")
                    _playbackError.value = null
                    onLoaded()
                }
                setOnErrorListener { _, what, extra ->
                    val error = "Erreur MediaPlayer: $what, $extra"
                    _playbackError.value = error
                    onError(error)
                    true
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            val error = "Erreur de chargement: ${e.message}"
            _playbackError.value = error
            onError(error)
        }
    }
    
    /**
     * Démarrer la lecture
     */
    fun play(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer?.start()
                _isPlaying.value = true
                startPositionTracking()
                onSuccess()
            } else {
                onError("Aucune musique chargée")
            }
        } catch (e: Exception) {
            _playbackError.value = e.message
            onError(e.message ?: "Erreur de lecture")
        }
    }
    
    /**
     * Pause la lecture
     */
    fun pause(onSuccess: () -> Unit = {}) {
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
            onSuccess()
        } catch (e: Exception) {
            _playbackError.value = e.message
        }
    }
    
    /**
     * Arrêter la lecture
     */
    fun stop(onSuccess: () -> Unit = {}) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
            _currentPosition.value = 0
            _currentTrack.value = null
            onSuccess()
        } catch (e: Exception) {
            _playbackError.value = e.message
        }
    }
    
    /**
     * Reprendre la lecture
     */
    fun resume(onSuccess: () -> Unit = {}) {
        play(onSuccess)
    }
    
    /**
     * Aller à une position spécifique
     */
    fun seekTo(position: Int, onSuccess: () -> Unit = {}) {
        try {
            mediaPlayer?.seekTo(position)
            _currentPosition.value = position
            onSuccess()
        } catch (e: Exception) {
            _playbackError.value = e.message
        }
    }
    
    /**
     * Définir le volume
     */
    fun setVolume(volume: Float) {
        try {
            val clampedVolume = volume.coerceIn(0f, 1f)
            mediaPlayer?.setVolume(clampedVolume, clampedVolume)
        } catch (e: Exception) {
            _playbackError.value = e.message
        }
    }
    
    /**
     * Obtenir le volume actuel
     */
    fun getVolume(): Float {
        return try {
            // Note: MediaPlayer ne fournit pas de getter pour le volume
            // Utiliser une valeur stockée si nécessaire
            1f
        } catch (e: Exception) {
            1f
        }
    }
    
    /**
     * Vérifier si la musique est en cours de lecture
     */
    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
    
    /**
     * Obtenir la position actuelle
     */
    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    
    /**
     * Obtenir la durée totale
     */
    fun getDuration(): Int = mediaPlayer?.duration ?: 0
    
    /**
     * Suivre la position de lecture
     */
    private fun startPositionTracking() {
        Thread {
            while (isPlaying()) {
                _currentPosition.value = getCurrentPosition()
                Thread.sleep(1000) // Mettre à jour chaque seconde
            }
        }.start()
    }
    
    /**
     * Nettoyer les ressources
     */
    fun cleanup() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
