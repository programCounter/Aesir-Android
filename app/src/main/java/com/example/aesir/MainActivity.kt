/*
File Name: MainActivity.kt
Author: Riley Larche
Date Updated: 2019-09-27
Android Studio Version:
Tested on Android Version: 10

Logic for MainActivity in app Aesir is contained in this file.
 */


//Packages and Imports
package com.example.aesir

import android.app.Activity
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.*
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*


class MainActivity : AppCompatActivity() {
    //Public (Default) variables and values


    //Private variables and values
    private val requestAccessFineLocation: Int = 0
    private val requestEnableBt: Int = 1
    private val mGattCallback = GattCallback()
    private var bluetoothGatt: BluetoothGatt? = null

    //Used classes
    private val tools = Tools(this)
    private val mBTLEAdapter = BluetoothLEAdapter(this)


    //Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()
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

        //Find bluetooth device and populate list right as app starts
        val mBluetoothAdapter = mBTLEAdapter.getBluetooth()
        mBTLEAdapter.findBluetoothDevices(mBluetoothAdapter)

        //Find and reference items in Activity
        val searchButton = findViewById<Button>(R.id.search)
        val deviceList = findViewById<ListView>(R.id.device_list)
        val readGATT = findViewById<Button>(R.id.read_gatt)

        //Update parameters for items in the View
        searchButton.setOnClickListener{
            mBTLEAdapter.findBluetoothDevices(mBluetoothAdapter)
        }

        deviceList.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val clickedItem = parent.getItemAtPosition(position) as ScanResult
            val name = clickedItem.device.address
            tools.showToast("You clicked: $name")

            //bluetoothGatt = clickedItem.device.connectGatt(this, true, mGattCallback)
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
            tools.showToast("$permissionName granted.")
        }
        else {
            tools.showToast("$permissionName NOT granted.")
        }
    }


    inner class GattCallback: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, statis: Int, newState: Int) {

        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {

        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {

        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {

        }
    }
}
