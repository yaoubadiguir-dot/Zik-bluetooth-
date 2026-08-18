package com.zikbluetooth.app.sync

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.Serializable

data class MusicCommand(
    val command: String, // "play", "pause", "seek", "volume"
    val timestamp: Long,
    val data: Map<String, String> = emptyMap()
) : Serializable

data class SyncStatus(
    val isSynced: Boolean,
    val lastSyncTime: Long,
    val latency: Long = 0L
)

class MusicSync {
    
    private val mainHandler = Handler(Looper.getMainLooper())
    private val syncBuffer = mutableListOf<MusicCommand>()
    private val MAX_BUFFER_SIZE = 100
    
    private val _syncStatus = MutableStateFlow(SyncStatus(false, 0L))
    val syncStatus: StateFlow<SyncStatus> = _syncStatus
    
    private val _pendingCommands = MutableStateFlow<List<MusicCommand>>(emptyList())
    val pendingCommands: StateFlow<List<MusicCommand>> = _pendingCommands
    
    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError
    
    private var syncStartTime = 0L
    
    /**
     * Ajouter une commande à synchroniser
     */
    fun addCommand(
        command: String,
        data: Map<String, String> = emptyMap(),
        onCommandAdded: () -> Unit = {}
    ) {
        Thread {
            try {
                val musicCommand = MusicCommand(
                    command = command,
                    timestamp = System.currentTimeMillis(),
                    data = data
                )
                
                syncBuffer.add(musicCommand)
                
                // Limiter la taille du buffer
                if (syncBuffer.size > MAX_BUFFER_SIZE) {
                    syncBuffer.removeAt(0)
                }
                
                _pendingCommands.value = syncBuffer.toList()
                _syncError.value = null
                mainHandler.post(onCommandAdded)
            } catch (e: Exception) {
                _syncError.value = "Erreur d'ajout de commande: ${e.message}"
            }
        }.start()
    }
    
    /**
     * Synchroniser les commandes
     */
    fun synchronize(
        sendCommand: (MusicCommand) -> Boolean,
        onSyncComplete: () -> Unit = {},
        onSyncError: (String) -> Unit = {}
    ) {
        Thread {
            try {
                syncStartTime = System.currentTimeMillis()
                var successCount = 0
                var failureCount = 0
                
                // Copier le buffer pour éviter les modifications concurrentes
                val commandsToSync = syncBuffer.toList()
                
                for (command in commandsToSync) {
                    if (sendCommand(command)) {
                        successCount++
                        syncBuffer.remove(command)
                    } else {
                        failureCount++
                    }
                }
                
                val latency = System.currentTimeMillis() - syncStartTime
                val isSynced = failureCount == 0
                
                _syncStatus.value = SyncStatus(
                    isSynced = isSynced,
                    lastSyncTime = System.currentTimeMillis(),
                    latency = latency
                )
                
                _pendingCommands.value = syncBuffer.toList()
                _syncError.value = null
                
                mainHandler.post {
                    if (isSynced) {
                        onSyncComplete()
                    } else {
                        onSyncError("$failureCount commandes non synchronisées")
                    }
                }
            } catch (e: Exception) {
                _syncError.value = "Erreur de synchronisation: ${e.message}"
                mainHandler.post { onSyncError(e.message ?: "Erreur inconnue") }
            }
        }.start()
    }
    
    /**
     * Obtenir les commandes en attente
     */
    fun getPendingCommands(): List<MusicCommand> = syncBuffer.toList()
    
    /**
     * Vider le buffer
     */
    fun clearBuffer() {
        syncBuffer.clear()
        _pendingCommands.value = emptyList()
    }
    
    /**
     * Obtenir le nombre de commandes en attente
     */
    fun getPendingCommandCount(): Int = syncBuffer.size
    
    /**
     * Vérifier si synchronisé
     */
    fun isSynced(): Boolean = _syncStatus.value.isSynced
    
    /**
     * Obtenir la latence
     */
    fun getLatency(): Long = _syncStatus.value.latency
    
    /**
     * Créer une commande de lecture
     */
    fun createPlayCommand(): MusicCommand {
        return MusicCommand("play", System.currentTimeMillis())
    }
    
    /**
     * Créer une commande de pause
     */
    fun createPauseCommand(): MusicCommand {
        return MusicCommand("pause", System.currentTimeMillis())
    }
    
    /**
     * Créer une commande de recherche
     */
    fun createSeekCommand(position: Int): MusicCommand {
        return MusicCommand(
            "seek",
            System.currentTimeMillis(),
            mapOf("position" to position.toString())
        )
    }
    
    /**
     * Créer une commande de volume
     */
    fun createVolumeCommand(volume: Float): MusicCommand {
        return MusicCommand(
            "volume",
            System.currentTimeMillis(),
            mapOf("volume" to volume.toString())
        )
    }
    
    /**
     * Créer une commande de changement de piste
     */
    fun createTrackChangeCommand(trackPath: String): MusicCommand {
        return MusicCommand(
            "track_change",
            System.currentTimeMillis(),
            mapOf("track" to trackPath)
        )
    }
}
