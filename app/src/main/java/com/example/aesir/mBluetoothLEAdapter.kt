/*
File Name: mBluetoothLEAdapter.kt
Author: Riley Larche
Date Updated: 2019-11-21
Android Studio Version:
Tested on Android Version: 10

Code pertaining to bluetooth within the app.
 */


//
// Packages and Imports
//
package com.example.aesir

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.widget.Button
import android.widget.ListView
import java.util.*


//
// BluetoothLEAdapter Class
//
class BluetoothLEAdapter(passedActivity: Activity) {
    //
    // Private variables and values
    //
    private val activity = passedActivity
    private val reportDelay: Long = 500
    private val mCallback = MCallBack()
    private var scanner: BluetoothLeScanner? = null
    // Combine A then 140X then B to create UUID
    private val baseBuuid: String = "0e28c"
    private val baseAuuid: String = "-6801-4160-a7d6-a3b252dc43a1"

    // Shows which bits to test for (0 = ignore // 1 = must match)
    private val bsiServiceUUIDMask: ParcelUuid =
        ParcelUuid.fromString("000001111-0000-0000-0000-000000000000")


    //
    // Public (Default) variables and values
    //
    val bsiServiceUUID: ParcelUuid = ParcelUuid.fromString(baseBuuid + "1400" + baseAuuid)
    // Characteristic UUIDs
    val s2MeasureInterval: UUID = UUID.fromString(baseBuuid + "1401" + baseAuuid)
    val s3MeasureInterval: UUID = UUID.fromString(baseBuuid + "1402" + baseAuuid)
    val dtAlarmOn: UUID = UUID.fromString(baseBuuid + "1403" + baseAuuid) // Pulse Alarm on (time between x)
    val dtAlarmOff: UUID = UUID.fromString(baseBuuid + "1404" + baseAuuid) // Pulse Alarm off (time between x)
    val dmAlarmS2On: UUID = UUID.fromString(baseBuuid + "1405" + baseAuuid)
    val dmAlarmS2Off: UUID = UUID.fromString(baseBuuid + "1406" + baseAuuid)
    val dmAlarmS3On: UUID = UUID.fromString(baseBuuid + "1407" + baseAuuid)
    val dmAlarmS3Off: UUID = UUID.fromString(baseBuuid + "1408" + baseAuuid)
    val uploadSize: UUID = UUID.fromString(baseBuuid + "1409" + baseAuuid)
    val sensorConfig: UUID = UUID.fromString(baseBuuid + "1410" + baseAuuid)
    val sesnorAddress: UUID = UUID.fromString(baseBuuid + "1411" + baseAuuid)
    val uploadInterval: UUID = UUID.fromString(baseBuuid + "1412" + baseAuuid)
    val sensorData: UUID = UUID.fromString(baseBuuid + "1413" + baseAuuid)

    var scanResults: MutableList<ScanResult>? = null


    //
    // Used classes
    //
    private val tools = Tools(activity)


    //
    // Functions
    //
    fun getBluetooth(): BluetoothAdapter {
        val mBluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
            val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        }

        return mBluetoothAdapter
    }

    fun findBluetoothDevices(mBluetoothAdapter: BluetoothAdapter?){
        // Get and instance of the Bluetooth Low Energy Scanner
        scanner = mBluetoothAdapter?.bluetoothLeScanner

        // Create search settings object
        val mSettings = ScanSettings.Builder().
            setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).
            setReportDelay(reportDelay).
            build()

        // Create a scanning filter
        val mFilter = ScanFilter.Builder().setServiceUuid(bsiServiceUUID, bsiServiceUUIDMask).build()
        val scannerFilter = arrayListOf<ScanFilter>()
        scannerFilter.add(mFilter)

        // Stop previous scan if there was one
        stopScanningBluetoothDevices()

        //Start new scanner
        scanner?.startScan(scannerFilter, mSettings, mCallback)
    }

    fun stopScanningBluetoothDevices() {
        scanner?.stopScan(mCallback)
        scanner?.flushPendingScanResults(mCallback)
    }

    fun tx(gatt: BluetoothGatt, character: List<BluetoothGattCharacteristic>, data: BSIObject) {
        //Runs on submit button push

        //Need the GATT object
        //Need BSI object(s)
        //Write to character
        //Get time after write BSI object(s)
        //Style 3 is SHORT e.g. 3:30pm HH:MM:AM/PM
        //val LocalListenerTime: String = DateFormat.getTimeInstance(3).format(Date())
        //Write to time character

        // Create string "XXX" for which senors are active = 1 or
        // inactive = 0
        val cfg: Int =  data.pEnable + data.a1Enable +
                data.a2Enable

        // Convert the BSI Object into a iterable object
        val dataIterable: List<Int> = listOf(data.txInterval,
            data.a1pod, data.a1measureint,
             data.a2pod, data.a2measureint,
            data.pAlarmtrigger, data.pAlarmshutoff, cfg)

        // Iterate and send data
        for (x in 0..character.count()) {
            character[x].setValue(dataIterable[x], 0, 0)
            gatt.writeCharacteristic(character[x]) // Do the push to the remote device
            // need delay?
        }

        // Send Friendly Name
        // Replace 'x' with index of friendly name
        character[x].setValue(data.name)
        gatt.writeCharacteristic(character[x])
    }


    //Inner Classes
    inner class MCallBack: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            stopScanningBluetoothDevices()
            tools.showToast("Single Result Found")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            stopScanningBluetoothDevices()
            tools.showToast("Error on Scan!")
            tools.showToast(errorCode.toString())
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            stopScanningBluetoothDevices()

            Handler(Looper.getMainLooper()).post {
                val button = activity.findViewById<Button>(R.id.search)
                button.text = activity.getString(R.string.search_button_text)
            }

            scanResults = results
            val mAdapter = DeviceListAdapter(activity, scanResults)
            val deviceList = activity.findViewById<ListView>(R.id.device_list)
            deviceList.adapter = mAdapter
        }
    }
}