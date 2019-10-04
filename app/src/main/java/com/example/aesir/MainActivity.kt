/*
File Name: MainActivity.kt
Author: Riley Larche
Date Updated: 2019-09-27
Android Studio Version:
Tested on Android Version: 10

Logic for MainActivity in app Aesir is contained in this file.
 */


//BUG IN CODE: if bt is off app crashes

//Packages and Imports
package com.example.aesir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    //Public (Default) variables and values


    //Private variables and values
    //private val requestAccessFineLocation: Int = 0
    //private val requestEnableBt: Int = 1
    //private var bluetoothGatt: BluetoothGatt? = null
    private var previous_fragment: MenuItem? = null

    //Used classes
    private val tools = Tools(this)
    //private val mBTLEAdapter = BluetoothLEAdapter(this)
    //private val mGattCallback = GattCallback()


    //Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        val fragmentManager = supportFragmentManager

        //OnClick handlers for Navigation Bar
        val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        val itemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_discover -> {
                    //Make sure that we wont re-draw the same fragment
                    if (menuItem.itemId != previous_fragment?.itemId) {
                        val fragment = Discover_Devices_Fragment()
                        //fragmentManager.beginTransaction().replace(R.id.frame, fragment, fragment.javaClass.simpleName).commit()
                        val transaction = fragmentManager.beginTransaction().apply {
                            replace(R.id.frame, fragment)
                            addToBackStack(null)
                        }
                        transaction.commit()
                        previous_fragment = menuItem
                        tools.showToast("You clicked Discover!")
                        return@OnNavigationItemSelectedListener true
                    }
                }

                R.id.navigation_setup -> {
                    val fragment = Setup_Fragment()
                    //fragmentManager.beginTransaction().replace(R.id.fragment, fragment, fragment.javaClass.simpleName).commit()
                    val transaction = fragmentManager.beginTransaction().apply {
                        replace(R.id.frame, fragment)
                        addToBackStack(null)
                    }
                    transaction.commit()
                    tools.showToast("You clicked Setup!")
                    return@OnNavigationItemSelectedListener true
                }

                R.id.navigation_debug -> {
                    val fragment = Debug_Fragment()
                    //fragmentManager.beginTransaction().replace(R.id.frame, fragment, fragment.javaClass.simpleName).commit()
                    val transaction = fragmentManager.beginTransaction().apply {
                        replace(R.id.frame, fragment)
                        addToBackStack(null)
                    }
                    transaction.commit()
                    tools.showToast("You clicked Debug!")
                    return@OnNavigationItemSelectedListener true
                }

                R.id.navigation_info -> {
                    val fragment = Info_Fragment()
                    //fragmentManager.beginTransaction().replace(R.id.frame, fragment, fragment.javaClass.simpleName).commit()
                    val transaction = fragmentManager.beginTransaction().apply {
                        replace(R.id.frame, fragment)
                        addToBackStack(null)
                    }
                    transaction.commit()
                    tools.showToast("You clicked Info!")
                    return@OnNavigationItemSelectedListener true
                }
            }

            false
        }

        navBar.setOnNavigationItemSelectedListener(itemSelectedListener)
/*
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
        val closeConnection = findViewById<Button>(R.id.close_gatt)

        //Update parameters for items in the View
        searchButton.setOnClickListener{
            mBTLEAdapter.findBluetoothDevices(mBluetoothAdapter)
        }

        deviceList.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val clickedItem = parent.getItemAtPosition(position) as ScanResult
            val name = clickedItem.device.address
            tools.showToast("You clicked: $name")

            bluetoothGatt = clickedItem.device.connectGatt(this@MainActivity, false, mGattCallback)
            val attempt = bluetoothGatt?.discoverServices()
            if (attempt == true) {
                tools.showToast("Attempt made success")
            }
        }

        closeConnection.setOnClickListener {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            tools.showToast("GATT Connection closed.")
        }
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
        if (resultCode == Activity.RESULT_OK){
            tools.showToast("$permissionName granted.")
        }
        else {
            tools.showToast("$permissionName NOT granted.")
        }
    }


    inner class GattCallback: BluetoothGattCallback() {
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
                val mServices = gatt?.services

            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            //"Notification"

        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {

        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {

        }*/
    }

    fun ChangeFragment(fragmentManager: FragmentManager, fragmentToDisplay: Fragment) {
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.frame, fragmentToDisplay)
            addToBackStack(null)
        }

        transaction.commit()
    }

    //Fragment Inner Classes
    class Discover_Devices_Fragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            //Inflate the layout for this fragment
            return inflater.inflate(R.layout.discover_devices_fragment, container, false)
        }
    }

    class Setup_Fragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            //Inflate the layout for this fragment
            return inflater.inflate(R.layout.setup_fragment, container, false)
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
        }
    }

    class Debug_Fragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            //Inflate the layout for this fragment
            return inflater.inflate(R.layout.debug_fragment, container, false)
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
        }
    }

    class Info_Fragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            //Inflate the layout for this fragment
            return inflater.inflate(R.layout.info_fragment, container, false)
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
        }
    }
}
