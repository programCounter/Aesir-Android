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
import android.util.Log
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
    private val bsiNameCharIndex = 0
    private val bsiTimeCharIndex = 1
    // Combine A then 140X then B to create UUID
    private val baseBuuid: String = "0e28"
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
    //val sesnorAddress: UUID = UUID.fromString(baseBuuid + "1411" + baseAuuid)
    val uploadInterval: UUID = UUID.fromString(baseBuuid + "1412" + baseAuuid)
    val bsiName: UUID = UUID.fromString(baseBuuid + "1413" + baseAuuid)
    val podS2: UUID = UUID.fromString(baseBuuid + "1414" + baseAuuid)
    val podS3: UUID = UUID.fromString(baseBuuid + "1415" + baseAuuid)
    val bsiTime: UUID = UUID.fromString(baseBuuid + "1416" + baseAuuid)


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

    fun tx(gatt: BluetoothGatt, character: List<BluetoothGattCharacteristic>, strCharacter: List<BluetoothGattCharacteristic>, data: BSIObject) {
        //Runs on submit button push

        // Create string "XXX" for which senors are active = 1 or
        // inactive = 0
        val cfg: Int =  data.pEnable + data.a1Enable +
                data.a2Enable

        // Convert the BSI Object into a iterable object
        val dataIterable: List<Int> =
            listOf(data.a1measureint,
                data.a2measureint,
                data.pAlarmtrigger,
                data.pAlarmshutoff,
                data.a1alarmON,
                data.a1alarmOFF,
                data.a2alarmON,
                data.a2alarmOFF,
                data.upldSize,
                cfg,
                data.txInterval,
                data.a1pod,
                data.a2pod)

        // Iterate and send data
        for (x in 0..character.lastIndex) {
            // Skip the name due to different type
            if (x != 10) {
                character[x].setValue(dataIterable[x], 17, 0)
                gatt.writeCharacteristic(character[x]) // Do the push to the remote device
                Thread.sleep(500)
            }
        }

        Thread.sleep(500)

        // Send Friendly Name
        strCharacter[0].setValue(data.name)
        gatt.writeCharacteristic(strCharacter[0])

        Thread.sleep(500)

        // Get time UTC -> number in min. to send
        // Send last
        data.dateTime = (System.currentTimeMillis() / 60000).toInt()
        strCharacter[1].setValue(data.dateTime, 20, 0)
        gatt.writeCharacteristic(strCharacter[1])
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