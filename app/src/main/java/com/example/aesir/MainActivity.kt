/*
File Name: MainActivity.kt
Author: Riley Larche
Date Updated: 2019-10-07
Android Studio Version:
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
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.discover_devices_fragment.*


class MainActivity : AppCompatActivity(), DiscoverDevicesFragment.OnSearchButtonPressed {
    // Public (Default) variables and values


    // Private variables and values
    private val requestAccessFineLocation: Int = 0
    private val requestEnableBt: Int = 1
    private var bluetoothGatt: BluetoothGatt? = null
    private var previousFragment: MenuItem? = null


    // Used classes
    val tools = Tools(this)
    private val mBTLEAdapter = BluetoothLEAdapter(this)
    private val mGattCallback = GattCallback()
    private val discoverFrag: Fragment = DiscoverDevicesFragment()
    private val setupFrag: Fragment = SetupFragment()
    private val debugFrag: Fragment = DebugFragment()
    private val infoFrag: Fragment = InfoFragment()


    // Functions
    override fun onStart() {
        super.onStart()

        //Check for permissions right as the app starts
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            // Permission is not granted, request access to permission
            // Request the permission be granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                requestAccessFineLocation
            )
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

        //Find and reference items in Discover Fragment
        val searchButton = findViewById<Button>(R.id.search)
        val deviceList = findViewById<ListView>(R.id.device_list)

        //Find bluetooth device and populate list right as app starts
        val mBluetoothAdapter = mBTLEAdapter.getBluetooth()
        mBTLEAdapter.findBluetoothDevices(mBluetoothAdapter)

        //Update parameters for items in Discover's View
        //searchButton.setOnClickListener {
        //    tools.showToast("Searching for Devices...")
        //    mBTLEAdapter.findBluetoothDevices(mBluetoothAdapter)
        //}

        deviceList?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val clickedItem = parent.getItemAtPosition(position) as ScanResult
                val name = clickedItem.device.address
                tools.showToast("You clicked: $name")

                bluetoothGatt =
                    clickedItem.device.connectGatt(this@MainActivity, false, mGattCallback)
                val attempt = bluetoothGatt?.discoverServices()
                if (attempt == true) {
                    tools.showToast("Attempt made success")
                }
            }
    }

    override fun OnButtonPressed() {
        tools.showToast("Searching for Devices...")
        //mBTLEAdapter.findBluetoothDevices(mBluetoothAdapter)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.disconnect()
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

    private fun changeFragment(
        fragmentManager: FragmentManager,
        fragmentToDisplay: Fragment,
        menuItem: MenuItem?,
        isLaunch: Int
    ) {
        //Ensure that we are not navigating to the same fragment and then replace the view with the desired fragment
        if ((menuItem?.itemId != previousFragment?.itemId) || (isLaunch == 1)) {
            val transaction = fragmentManager.beginTransaction().apply {
                replace(R.id.frame, fragmentToDisplay)
                addToBackStack(null)
            }

            transaction.commit()
            previousFragment = menuItem
        }
    }


    inner class GattCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            //If GATT_SUCCESS and STATE_CONNECTED find services on device
            if (status == 0 && newState == 2) {
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == 0) {
                //val mServices = gatt?.services
                tools.showToast("I found some yummy services!")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
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

//Fragment Classes
class SetupFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.setup_fragment, container, false)
    }
}

class DebugFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.debug_fragment, container, false)
    }
}

class InfoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.info_fragment, container, false)
    }
}
