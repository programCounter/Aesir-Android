/*
File Name: mBluetoothLEAdapter.kt
Author: Riley Larche
Date Updated: 2019-11-15
Android Studio Version:
Tested on Android Version: 10

All code pertaining to bluetooth within the app.
 */


//Packages and Imports
package com.example.aesir

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Parcel
import android.os.ParcelUuid
import android.widget.Button
import android.widget.ListView
import java.util.*


class BluetoothLEAdapter(passedActivity: Activity) {
    //
    //Public (Default) variables and values
    //
    val bsiSeriveUUID: ParcelUuid = ParcelUuid.fromString("0e28c1400-6801-4160-a7d6-a3b252dc43a1")
    val bsiServiceUUIDMask: ParcelUuid = ParcelUuid.fromString("000001111-0000-0000-0000-000000000000")
    //val LocalListenerServiceUUID: UUID = UUID.fromString("")
    var scanResults: MutableList<ScanResult>? = null


    //
    //Private variables and values
    //
    private val activity = passedActivity
    private val reportDelay: Long = 500
    private val mCallback = MCallBack()
    private var scanner: BluetoothLeScanner? = null



    //Used classes
    private val tools = Tools(activity)


    //Functions
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
        //val mFilter = ScanFilter.Builder().setDeviceName("AEsir ADC Test").build()
        val mFilter = ScanFilter.Builder().setServiceUuid(bsiSeriveUUID, bsiServiceUUIDMask).build()
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
        //val LocalLIstenerTime: String = DateFormat.getTimeInstance(3).format(Date())
        //Write to time character

        //TEST CODE (PUSH INT TO CHARACTER)
        //val toPush: Int = 101
        //BSIcharacter.setValue(toPush, 18, 0)

        val dataIterable: List<String> = listOf(data.txInterval.toString(), data.a1Enable.toString(),
            data.a1pod.toString(), data.a1measureint.toString(),
            data.a2Enable.toString(), data.a2pod.toString(),
            data.a2measureint.toString(), data.pEnable.toString(),
            data.pAlarmtrigger.toString(), data.pAlarmshutoff.toString())

        for (x in 0..character.count()) {
            character[x].setValue(dataIterable[x])
            gatt.writeCharacteristic(character[x])
            // need delay?
        }
    }


    //Inner Classes
    inner class MCallBack: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            stopScanningBluetoothDevices()
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

            val button = activity.findViewById<Button>(R.id.search)
            button.text = "Search"

            scanResults = results
            val mAdapter = DeviceListAdapter(activity, scanResults)
            val deviceList = activity.findViewById<ListView>(R.id.device_list)
            deviceList.adapter = mAdapter
        }
    }
}