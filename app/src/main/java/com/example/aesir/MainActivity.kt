/*
File Name: MainActivity.kt
Author: Riley Larche
Date Updated: 2019-10-07
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic for MainActivity in app Aesir is contained in this file.
 */


//BUG IN CODE: if bt is off app crashes

//Packages and Imports
package com.example.aesir

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.debug_fragment.*


class MainActivity : AppCompatActivity(), DiscoverDevicesFragment.OnPressed, DebugFragment.DebugListener {
    // Used classes
    val tools = Tools(this)
    private val mBTLEAdapter = BluetoothLEAdapter(this)
    private val mGattCallback = GattCallback()
    private val discoverFrag: Fragment = DiscoverDevicesFragment()
    private val setupFrag: Fragment = SetupFragment()
    private val debugFrag: Fragment = DebugFragment()
    private val infoFrag: Fragment = InfoFragment()


    // Public (Default) variables and values


    // Private variables and values
    private val requestAccessFineLocation: Int = 0
    private val requestEnableBt: Int = 1
    private val mBluetoothAdapter = mBTLEAdapter.getBluetooth()
    private var bluetoothGatt: BluetoothGatt? = null
    private var previousFragment: MenuItem? = null
    private var bluetoothServices: MutableList<BluetoothGattService?>? = null


    // Functions
    override fun onStart() {
        super.onStart()

        //Check for permissions right as the app starts
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            // Permission is not granted, request access to permission
            // Request the permission be granted
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), requestAccessFineLocation)
        }

        //If bluetooth is NOT enabled, request the user to do so here
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, requestEnableBt)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        val fragmentManager = supportFragmentManager

        //OnClick handlers for Navigation Bar
        val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        //What to do, when an item in the NavBar is clicked
        val itemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
                // Comparable to switch case
                when (menuItem.itemId) {
                    R.id.navigation_discover -> {
                        changeFragment(fragmentManager, discoverFrag, menuItem, 0)
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.navigation_setup -> {
                        changeFragment(fragmentManager, setupFrag, menuItem, 0)
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.navigation_debug -> {
                        changeFragment(fragmentManager, debugFrag, menuItem, 0)
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.navigation_info -> {
                        changeFragment(fragmentManager, infoFrag, menuItem, 0)
                        return@OnNavigationItemSelectedListener true
                    }
                }

                false
            }

        //Set the onClick listener to the object defined above
        navBar.setOnNavigationItemSelectedListener(itemSelectedListener)

        //Start with the discover fragment when the app opens
        changeFragment(fragmentManager, discoverFrag, null, 1)
    }

    override fun onButtonPressed() {
        tools.showToast("Searching for Devices...")
        mBTLEAdapter.findBluetoothDevices(mBluetoothAdapter)
    }

    override fun onListPressed(): AdapterView.OnItemClickListener? {
        return AdapterView.OnItemClickListener { parent, _, position, _ ->
            //Disconnect and stop scan before attempting a new connection just in case
            bluetoothGatt?.close()

            val clickedItem = parent.getItemAtPosition(position) as ScanResult
            val name = clickedItem.device.address
            tools.showToast("You clicked: $name")

            val handler = Handler()
            handler.postDelayed({
                clickedItem.device.createBond()
                bluetoothGatt = clickedItem.device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE)
            }, 500)
        }
    }

    override fun debugDataMover() {
        val mAdapter = ServicesListAdapter(this, bluetoothServices)
        val servicesList = findViewById<ListView>(R.id.debug_services_list)
        servicesList.adapter = mAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
        tools.showToast("GATT Connection closed.")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //Check which activity we are returning from.
        if (requestCode == requestEnableBt) {
            displayPermissionStatus("Access to Bluetooth", resultCode)
        }
    }

    private fun displayPermissionStatus(permissionName: String, resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            tools.showToast("$permissionName granted.")
        } else {
            tools.showToast("$permissionName NOT granted.")
        }
    }

    private fun changeFragment(fragmentManager: FragmentManager, fragmentToDisplay: Fragment, menuItem: MenuItem?, isLaunch: Int) {
        //Ensure that we are not navigating to the same fragment and then replace the view with the desired fragment
        if ((menuItem?.itemId != previousFragment?.itemId) || (isLaunch == 1)) {
            val transaction = fragmentManager.beginTransaction().apply {
                replace(R.id.frame, fragmentToDisplay)
            }

            transaction.commit()
            previousFragment = menuItem
        }
    }


    inner class GattCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            //If we connected to the GATT server find services on device
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            bluetoothServices = gatt?.services
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            //"Notification"
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {

        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {

        }
    }
}
