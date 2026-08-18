package com.zikbluetooth.app.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import java.util.UUID

class BluetoothManager(private val context: Context) {
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // UUID unique pour l'application
    private val APP_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
    private val APP_NAME = "ZikBluetooth"
    
    // État des connexions
    private val _connectedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val connectedDevices: StateFlow<List<BluetoothDevice>> = _connectedDevices
    
    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices
    
    private val _isBluetoothEnabled = MutableStateFlow(isBluetoothEnabled())
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled
    
    private val _connectionStatus = MutableStateFlow("Déconnecté")
    val connectionStatus: StateFlow<String> = _connectionStatus
    
    private var bluetoothServerSocket: BluetoothServerSocket? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private val connectedSockets = mutableListOf<BluetoothSocket>()
    
    init {
        updateBluetoothStatus()
    }
    
    /**
     * Vérifier si Bluetooth est disponible et activé
     */
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null
    }
    
    /**
     * Vérifier si Bluetooth est activé
     */
    @SuppressLint("MissingPermission")
    private fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Activer Bluetooth
     */
    @SuppressLint("MissingPermission")
    fun enableBluetooth(): Boolean {
        return if (bluetoothAdapter != null && !isBluetoothEnabled()) {
            bluetoothAdapter.enable()
        } else {
            false
        }
    }
    
    /**
     * Désactiver Bluetooth
     */
    @SuppressLint("MissingPermission")
    fun disableBluetooth(): Boolean {
        return if (bluetoothAdapter != null && isBluetoothEnabled()) {
            bluetoothAdapter.disable()
        } else {
            false
        }
    }
    
    /**
     * Mettre à jour le statut Bluetooth
     */
    @SuppressLint("MissingPermission")
    private fun updateBluetoothStatus() {
        _isBluetoothEnabled.value = isBluetoothEnabled()
    }
    
    /**
     * Démarrer la découverte des appareils
     */
    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            _connectionStatus.value = "Permission de scan refusée"
            return
        }
        
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        
        _discoveredDevices.value = emptyList()
        _connectionStatus.value = "Recherche d'appareils..."
        bluetoothAdapter?.startDiscovery()
    }
    
    /**
     * Arrêter la découverte
     */
    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }
    
    /**
     * Ajouter un appareil découvert
     */
    @SuppressLint("MissingPermission")
    fun addDiscoveredDevice(device: BluetoothDevice) {
        val current = _discoveredDevices.value.toMutableList()
        if (!current.any { it.address == device.address }) {
            current.add(device)
            _discoveredDevices.value = current
        }
    }
    
    /**
     * Se connecter à un appareil
     */
    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            onError("Permission de connexion refusée")
            return
        }
        
        Thread {
            try {
                _connectionStatus.value = "Connexion à ${device.name}..."
                
                bluetoothSocket = device.createRfcommSocketToServiceRecord(APP_UUID)
                bluetoothSocket?.connect()
                
                connectedSockets.add(bluetoothSocket!!)
                
                val current = _connectedDevices.value.toMutableList()
                current.add(device)
                _connectedDevices.value = current
                
                _connectionStatus.value = "Connecté à ${device.name}"
                mainHandler.post(onSuccess)
            } catch (e: IOException) {
                _connectionStatus.value = "Erreur de connexion: ${e.message}"
                mainHandler.post { onError(e.message ?: "Erreur inconnue") }
            }
        }.start()
    }
    
    /**
     * Déconnecter d'un appareil
     */
    @SuppressLint("MissingPermission")
    fun disconnectDevice(device: BluetoothDevice) {
        try {
            bluetoothSocket?.close()
            val current = _connectedDevices.value.toMutableList()
            current.removeAll { it.address == device.address }
            _connectedDevices.value = current
            _connectionStatus.value = "Déconnecté"
        } catch (e: IOException) {
            _connectionStatus.value = "Erreur de déconnexion"
        }
    }
    
    /**
     * Commencer à écouter les connexions entrantes
     */
    @SuppressLint("MissingPermission")
    fun startListening(onDeviceConnected: (BluetoothSocket) -> Unit) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_LISTEN)) {
            return
        }
        
        Thread {
            try {
                bluetoothServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                    APP_NAME,
                    APP_UUID
                )
                
                while (true) {
                    bluetoothServerSocket?.accept()?.let { socket ->
                        connectedSockets.add(socket)
                        _connectionStatus.value = "Appareil connecté"
                        mainHandler.post { onDeviceConnected(socket) }
                    }
                }
            } catch (e: IOException) {
                _connectionStatus.value = "Erreur d'écoute: ${e.message}"
            }
        }.start()
    }
    
    /**
     * Envoyer des données via Bluetooth
     */
    fun sendData(data: ByteArray, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                bluetoothSocket?.outputStream?.write(data)
                bluetoothSocket?.outputStream?.flush()
                mainHandler.post(onSuccess)
            } catch (e: IOException) {
                mainHandler.post { onError(e.message ?: "Erreur d'envoi") }
            }
        }.start()
    }
    
    /**
     * Recevoir des données via Bluetooth
     */
    fun receiveData(onDataReceived: (ByteArray) -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val buffer = ByteArray(1024)
                while (bluetoothSocket?.isConnected == true) {
                    val bytes = bluetoothSocket?.inputStream?.read(buffer) ?: -1
                    if (bytes > 0) {
                        val data = buffer.copyOf(bytes)
                        mainHandler.post { onDataReceived(data) }
                    }
                }
            } catch (e: IOException) {
                mainHandler.post { onError(e.message ?: "Erreur de réception") }
            }
        }.start()
    }
    
    /**
     * Vérifier la permission
     */
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Nettoyer les ressources
     */
    fun cleanup() {
        stopDiscovery()
        try {
            bluetoothSocket?.close()
            bluetoothServerSocket?.close()
            connectedSockets.forEach { it.close() }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
