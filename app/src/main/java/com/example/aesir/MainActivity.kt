/*
File Name: MainActivity.kt
Author: Riley Larche
Date Updated: 2019-10-15
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic for MainActivity in app Aesir is contained in this file.
 */

/*
TODO List:
    1. Fix bug: If BT is off, the app will crash upon launch.
    2. Retain fragment state when user re-navigates to it. [IN PROGRESS]
    3. Add fragment for BSI specific configuration tasks.
    4. Fix Bug: If doing an async task and the user navigates off the page it was initiated on,
    the app crashes when results are fed to UI element that no longer exists [NULL POINTER].
    5. Come up with code naming standard and update/adhere to it.
    6. Fix the layout for SetupFragment. The list interfere with the text input fields.
 */

//
//Packages and Imports
//
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
import android.widget.EditText
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView

//
// Start of MainActivity
//
class MainActivity : AppCompatActivity(), DiscoverDevicesFragment.Discover, BSIFragment.BSI, DebugFragment.DebugListener, SetupFragment.Setup {
    //
    // Used classes
    //
    val tools = Tools(this)
    private var mBTLEAdapter = BluetoothLEAdapter(this)
    private val mGattCallback = GattCallback()
    private val discoverFrag: Fragment = DiscoverDevicesFragment()
    private val setupFrag: Fragment = SetupFragment()
    private val bsiFrag: Fragment = BSIFragment()
    private val debugFrag: Fragment = DebugFragment()
    private val infoFrag: Fragment = InfoFragment()


    //
    // Public (Default) variables and values
    //


    //
    // Private variables and values
    //
    private val requestAccessFineLocation: Int = 0
    private val requestEnableBt: Int = 1
    private val mBluetoothAdapter = mBTLEAdapter.getBluetooth()
    private val fragmentManager = supportFragmentManager
    private var bluetoothGatt: BluetoothGatt? = null
    private var previousFragment: MenuItem? = null
    private var bluetoothServices: MutableList<BluetoothGattService?>? = null
    private var bsiList: MutableList<BSIEntry>? = ArrayList()
    private var bsiListSelection = BSIEntry("")


    //
    // MainActivity Functions
    //
    // Runs before the view is created. Keep code minimal to provide
    // a responsive UI
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

    // Runs when the view is created. Do setup for items in the view here.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        //val fragmentManager = supportFragmentManager

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

    // Runs when the view is destroyed (happens when the app is closed from recent apps).
    // Shut anything down that should NOT be running in the background (e.g. a bluetooth connection)
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
        tools.showToast("GATT Connection closed.")
    }

    // Runs when MainActivity returns to focus (e.g. a system dialogue box closes with a result)
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

    // Runs when an item is clicked in the NavigationBar.
    // Note: This function does NOT sync the highlighted item in the bar.
    // It is possible for the active fragment to become out of sync with the highlighted option!
    private fun changeFragment(fragmentManager: FragmentManager, fragmentToDisplay: Fragment, menuItem: MenuItem?, isLaunch: Int) {
        // Ensure that we are not navigating to the same fragment and then replace the view with the desired fragment
        if ((menuItem?.itemId != previousFragment?.itemId) || (isLaunch == 1)) {
            val transaction = fragmentManager.beginTransaction().apply {
                replace(R.id.frame, fragmentToDisplay)
            }

            // Make the view change and remember what fragment was just switched to.
            transaction.commit()
            previousFragment = menuItem
        }

        // For navigation that does NOT originate from the NavBar
        if (menuItem == null && isLaunch == 0) {
            val transaction = fragmentManager.beginTransaction().apply {
                replace(R.id.frame, fragmentToDisplay)
            }
            transaction.commit()
        }
    }


    //
    // DiscoverDevices Functions
    //
    // Runs when the view is created. Only populate list if there
    // are items to populate it with.
    override fun discoverListViewDataMover(): MutableList<ScanResult>? {
        //if (mBTLEAdapter.scanResults.isNullOrEmpty()) {
            //.adapter = DeviceListAdapter(this, mBTLEAdapter.scanResults)
        //}
        return mBTLEAdapter.scanResults
    }

    override fun discoverContextMover(): Context {
        return this@MainActivity
    }

    // Runs when the Search button is pressed.
    override fun onButtonPressed() {
        tools.showToast("Searching for Devices...")
        mBTLEAdapter.findBluetoothDevices(mBluetoothAdapter)
    }

    // Runs when an item in the Device List is pressed.
    // This initiates a GATT connection to the selected device.
    override fun onListPressed(): AdapterView.OnItemClickListener? {
        return AdapterView.OnItemClickListener { parent, _, position, _ ->
            //Disconnect and stop scan before attempting a new connection just in case
            if (bluetoothGatt != null) {
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
            }

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


    //
    // Setup Functions
    //
    // Runs when the view is created.
    override fun setupListViewDataMover(): BSIListAdapter {
        return BSIListAdapter(this, bsiList)
    }

    // Runs when Add Device is pressed.
    override fun onAddDevicesPressed() {
        // Find the editable fields in the fragments view
        // and retrieve the current typed string. Must convert to string
        // because the field contains an editable string.
        val macEntry = findViewById<EditText>(R.id.bsi_address_entry)
        val friendEntry = findViewById<EditText>(R.id.bsi_friendly_name)

        // Use data class to store data for passing to List Adapter. Check for null.
        if (macEntry.text.toString() != null && friendEntry.text.toString() != null) {
            var mBSI = BSIEntry(macEntry.text.toString())
            mBSI.friendlyName = friendEntry.text.toString()

            bsiList?.add(mBSI)
        }

        if (!bsiList.isNullOrEmpty()) {
            val mAdapter = BSIListAdapter(this, bsiList)
            val bsiListView = findViewById<ListView>(R.id.bsis_in_network_list)
            bsiListView.adapter = mAdapter
        }

        // Reset the typed text after the list has been updated.
        macEntry.text = null
        friendEntry.text = null
    }

    override fun onBSIListPressed(): AdapterView.OnItemClickListener? {
        return AdapterView.OnItemClickListener {parent, _, position, _ ->
            bsiListSelection = parent.getItemAtPosition(position) as BSIEntry
            changeFragment(fragmentManager, bsiFrag, null, 0)
        }
    }


    //
    // BSI Functions
    //
    override fun bsiObjectMover(): BSIEntry {
        return bsiListSelection
    }


    //
    // Setup Functions
    //
    override fun bsiFragmentContextMover(): Context {
        return this@MainActivity
    }


    //
    // Debug Functions
    //
    // Runs when the Populate button is pressed.
    override fun debugDataMover() {
        val mAdapter = ServicesListAdapter(this, bluetoothServices)
        val servicesList = findViewById<ListView>(R.id.debug_services_list)
        servicesList.adapter = mAdapter
    }


    //
    // MainActivity Inner Classes
    //
    // Functions in this class run on a ASYNC callback.
    // Bluetooth runs in a separate thread from the UI (main thread)
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


//
// MainActivity Data Classes
//
data class BSIEntry(val mac: String) {
    var friendlyName: String = ""
}
