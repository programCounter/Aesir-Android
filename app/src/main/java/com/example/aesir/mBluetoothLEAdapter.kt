/*
File Name: mBluetoothLEAdapter.kt
Author: Riley Larche
Date Updated: 2019-11-12
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
import android.widget.ListView


class BluetoothLEAdapter(passedActivity: Activity) {
    //Public (Default) variables and values
    private var scanner: BluetoothLeScanner? = null
    var scanResults: MutableList<ScanResult>? = null


    //Private variables and values
    private val activity = passedActivity
    private val reportDelay: Long = 500
    private val mCallback = MCallBack()



    //Used classes
    private val tools = Tools(activity)


    //Functions
    fun getBluetooth(): BluetoothAdapter {
        /*
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        //If Bluetooth is available on the device but not enabled, a dialog will open for the user to enable Bluetooth
        mBluetoothAdapter?.takeIf { !it.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(MainActivity(), enableBtIntent, requestEnable, null)
        }
         */

        //New test bluetooth code
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
        val mFilter = ScanFilter.Builder().setDeviceName("AEsir ADC Test").build()
        val scannerFilter = arrayListOf<ScanFilter>()
        scannerFilter.add(mFilter)

        // Stop previous scan if there was one
        stopScanningBluetoothDevices()

        //Start new scanner
        tools.showToast("Scanning...")
        scanner?.startScan(null, mSettings, mCallback)
    }

    fun stopScanningBluetoothDevices() {
        tools.showToast("Stopped Scanning")
        scanner?.stopScan(mCallback)
        scanner?.flushPendingScanResults(mCallback)
    }

    fun txLocalListener(gatt: BluetoothGatt, BSIcharacter: BluetoothGattCharacteristic) {
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
        BSIcharacter.setValue("TEST DATA")
        gatt.writeCharacteristic(BSIcharacter)
    }


    //Inner Classes
    inner class MCallBack: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            stopScanningBluetoothDevices()
            tools.showToast("Single Result Found!")
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
            tools.showToast("Batch Results Found!")
            scanResults = results
            val mAdapter = DeviceListAdapter(activity, scanResults)
            val deviceList = activity.findViewById<ListView>(R.id.device_list)
            deviceList.adapter = mAdapter
        }
    }
}