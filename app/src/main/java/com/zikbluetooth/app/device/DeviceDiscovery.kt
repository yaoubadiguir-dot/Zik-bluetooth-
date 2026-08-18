package com.zikbluetooth.app.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DeviceDiscovery(private val context: Context) {
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = _pairedDevices
    
    private val _availableDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val availableDevices: StateFlow<List<BluetoothDeviceInfo>> = _availableDevices
    
    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering
    
    private val _discoveryStatus = MutableStateFlow("Prêt")
    val discoveryStatus: StateFlow<String> = _discoveryStatus
    
    data class BluetoothDeviceInfo(
        val name: String,
        val address: String,
        val bondState: Int,
        val device: BluetoothDevice
    )
    
    init {
        loadPairedDevices()
    }
    
    /**
     * Charger les appareils appairés
     */
    @SuppressLint("MissingPermission")
    fun loadPairedDevices() {
        try {
            if (!hasPermission(Manifest.permission.BLUETOOTH)) {
                return
            }
            
            val devices = bluetoothAdapter?.bondedDevices?.map { device ->
                BluetoothDeviceInfo(
                    name = device.name ?: "Appareil inconnu",
                    address = device.address,
                    bondState = device.bondState,
                    device = device
                )
            }?.sortedBy { it.name } ?: emptyList()
            
            _pairedDevices.value = devices
            _discoveryStatus.value = "${devices.size} appareil(s) appairé(s)"
        } catch (e: Exception) {
            _discoveryStatus.value = "Erreur: ${e.message}"
        }
    }
    
    /**
     * Démarrer la recherche d'appareils
     */
    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            _discoveryStatus.value = "Permission de scan refusée"
            return
        }
        
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            _discoveryStatus.value = "Permission de localisation refusée"
            return
        }
        
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter?.cancelDiscovery()
            }
            
            _availableDevices.value = emptyList()
            _isDiscovering.value = true
            _discoveryStatus.value = "Recherche en cours..."
            bluetoothAdapter?.startDiscovery()
        } catch (e: Exception) {
            _discoveryStatus.value = "Erreur de découverte: ${e.message}"
            _isDiscovering.value = false
        }
    }
    
    /**
     * Arrêter la découverte
     */
    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        try {
            bluetoothAdapter?.cancelDiscovery()
            _isDiscovering.value = false
            _discoveryStatus.value = "Découverte arrêtée"
        } catch (e: Exception) {
            _discoveryStatus.value = "Erreur: ${e.message}"
        }
    }
    
    /**
     * Ajouter un appareil découvert
     */
    @SuppressLint("MissingPermission")
    fun addDiscoveredDevice(device: BluetoothDevice) {
        try {
            val deviceInfo = BluetoothDeviceInfo(
                name = device.name ?: "Appareil inconnu",
                address = device.address,
                bondState = device.bondState,
                device = device
            )
            
            val current = _availableDevices.value.toMutableList()
            if (!current.any { it.address == device.address }) {
                current.add(deviceInfo)
                _availableDevices.value = current.sortedBy { it.name }
                _discoveryStatus.value = "${current.size} appareil(s) trouvé(s)"
            }
        } catch (e: Exception) {
            _discoveryStatus.value = "Erreur: ${e.message}"
        }
    }
    
    /**
     * Obtenir les appareils appairés
     */
    fun getPairedDevices(): List<BluetoothDeviceInfo> = _pairedDevices.value
    
    /**
     * Obtenir les appareils disponibles
     */
    fun getAvailableDevices(): List<BluetoothDeviceInfo> = _availableDevices.value
    
    /**
     * Rechercher un appareil par adresse
     */
    fun findDeviceByAddress(address: String): BluetoothDeviceInfo? {
        return _pairedDevices.value.find { it.address == address }
            ?: _availableDevices.value.find { it.address == address }
    }
    
    /**
     * Obtenir l'état de l'appairage
     */
    fun getBondStateString(bondState: Int): String {
        return when (bondState) {
            BluetoothDevice.BOND_BONDED -> "Appairé"
            BluetoothDevice.BOND_BONDING -> "Appairage en cours"
            else -> "Non appairé"
        }
    }
    
    /**
     * Vérifier la permission
     */
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
