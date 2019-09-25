/*
Author: Riley Larche
Date Updated: 2019-09-23
Android Studio Version:
Tested on Android Version: 10

Logic for MainActivity in app Aesir is contained in this file.
 */


//Packages and Imports
package com.example.aesir

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {


    //Variables and instances of classes used within MainActivity
    private val requestAccessFineLocation: Int = 0
    private val requestEnableBt: Int = 1
    private var mScanner: BluetoothLeScanner? = null
    private val mCallback = MCallBack()
    private var scanResults: MutableList<ScanResult>? = null


    //Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        //This code runs when the activity is created.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        Move auto run functions like permission checking and first search to onStart()?
        User would see the UI before those functions start. Might make for more responsive UX.
         */

        //Check permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            // Permission is not granted, request access to permission

            //Request the permission be granted
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), requestAccessFineLocation)
        }

        //Get the Bluetooth Adapter
        val mBluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        }

        //Enable Bluetooth
        //If Bluetooth is available on the device but not enabled,
        //a dialog will open for the user to enable Bluetooth
        mBluetoothAdapter?.takeIf { !it.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, requestEnableBt)
        }

        //Populate list right as app starts
        findBluetoothDevices(mBluetoothAdapter)

        //Find and reference items in Activity
        val searchButton = findViewById<Button>(R.id.search)
        val deviceList = findViewById<ListView>(R.id.device_list)

        //Update parameters for items in the View
        searchButton.setOnClickListener{
            findBluetoothDevices(mBluetoothAdapter)
        }

        deviceList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val clickedItem = parent.getItemAtPosition(position) as ScanResult
            val name = clickedItem.device.address
            showToast("You clicked: $name")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //Check which activity we are returning from.
        if (requestCode == requestEnableBt) {
            displayPermissionStatus("Access to Bluetooth", resultCode)
        }
    }


    private fun displayPermissionStatus(permissionName: String, resultCode: Int) {
        if (resultCode == Activity.RESULT_OK){
            showToast("$permissionName granted.")
        }
        else {
            showToast("$permissionName NOT granted.")
        }
    }


    private fun showToast(text: String){
        //Shows message through system.
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
        toast.show()
    }


    private fun findBluetoothDevices(mBluetoothAdapter: BluetoothAdapter?){
        //Get and instance of the Bluetooth Low Energy Scanner
        mScanner = mBluetoothAdapter?.bluetoothLeScanner

        //Create search settings object
        val mSettings: ScanSettings = ScanSettings.Builder().setReportDelay(100).build()

        //Stop previous scan if there was one
        stopScanningBluetoothDevices(mScanner)

        //Start new scanner
        showToast("Scanning...")
        mScanner?.startScan(null, mSettings, mCallback)
    }


    private fun stopScanningBluetoothDevices(mScanner: BluetoothLeScanner?) {
        //Stop scan if any
        mScanner?.stopScan(mCallback)
        mScanner?.flushPendingScanResults(mCallback)
    }


    //Classes
    inner class MCallBack: ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            showToast("Single Result Found!")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            showToast("Error on Scan!")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)

            showToast("Batch Results Found!")

            scanResults = results
            val mAdapter = DeviceListAdapter(this@MainActivity, scanResults)
            val deviceList = findViewById<ListView>(R.id.device_list)
            deviceList.adapter = mAdapter

            stopScanningBluetoothDevices(mScanner)
        }
    }
}
