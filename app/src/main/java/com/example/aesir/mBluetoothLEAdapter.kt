/*
File Name: mBluetoothLEAdapter.kt
Author: Riley Larche
Date Updated: 2019-09-27
Android Studio Version:
Tested on Android Version: 10

All code pertaining to bluetooth within the app.
 */


//Packages and Imports
package com.example.aesir

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.widget.ListView
import androidx.core.app.ActivityCompat.startActivityForResult


class BluetoothLEAdapter(passedActivity: Activity) {
    //Public (Default) variables and values
    var scanner: BluetoothLeScanner? = null
    var scanResults: MutableList<ScanResult>? = null


    //Private variables and values
    private val activity = passedActivity
    private val requestEnable: Int = 1
    private val reportDelay: Long = 100
    private val mCallback = MCallBack()


    //Used classes
    private val tools = Tools(activity)


    //Functions
    fun getBluetooth(): BluetoothAdapter {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        //If Bluetooth is available on the device but not enabled, a dialog will open for the user to enable Bluetooth
        mBluetoothAdapter?.takeIf { !it.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(MainActivity(), enableBtIntent, requestEnable, null)
        }

        return mBluetoothAdapter
    }

    fun findBluetoothDevices(mBluetoothAdapter: BluetoothAdapter){
        //Get and instance of the Bluetooth Low Energy Scanner
        scanner = mBluetoothAdapter.bluetoothLeScanner

        //Create search settings object
        val mSettings: ScanSettings = ScanSettings.Builder().setReportDelay(reportDelay).build()

        //Stop previous scan if there was one
        stopScanningBluetoothDevices(scanner)

        //Start new scanner
        tools.showToast("Scanning...")
        scanner?.startScan(null, mSettings, mCallback)
    }

    fun stopScanningBluetoothDevices(mScanner: BluetoothLeScanner?) {
        //Stop scan if any
        mScanner?.stopScan(mCallback)
        mScanner?.flushPendingScanResults(mCallback)
    }


    //Inner Classes
    inner class MCallBack: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            tools.showToast("Single Result Found!")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            tools.showToast("Error on Scan!")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)

            tools.showToast("Batch Results Found!")

            scanResults = results
            val mAdapter = DeviceListAdapter(activity, scanResults)
            val deviceList = activity.findViewById<ListView>(R.id.device_list)
            deviceList.adapter = mAdapter

            stopScanningBluetoothDevices(scanner)
        }
    }
}