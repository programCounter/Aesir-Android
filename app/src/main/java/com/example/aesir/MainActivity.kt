/*
File Name: MainActivity.kt
Author: Riley Larche
Date Updated: 2019-12-01
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic for MainActivity in app Aesir is contained in this file and any linking methods or objects.
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
import android.os.Looper
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.discover_devices_fragment.*
import java.lang.NullPointerException

//
// Start of MainActivity
//
class MainActivity : AppCompatActivity(), DiscoverDevicesFragment.Discover, BSISetupFragment.BSI, DebugFragment.DebugListener {
    //
    // Used classes
    //
    val tools = Tools(this)
    private var mBTLEAdapter = BluetoothLEAdapter(this)
    private var mGattCallback = GattCallback()
    private val discoverFrag: Fragment = DiscoverDevicesFragment()
    private val noConnectedDeviceFrag: Fragment = NoDeviceConnectedFragment()


    //
    // Private variables and values
    //
    private val requestAccessFineLocation: Int = 0
    private val requestEnableBt: Int = 1
    private val fragmentManager = supportFragmentManager
    private var uuidsStr: MutableList<String> = mutableListOf()
    private var valuesStr: MutableList<String> = mutableListOf()
    private var characterNamesStr: MutableList<String> = mutableListOf()
    private var mService: BluetoothGattService? = null
    private var config = BSIObject("KILL ME")
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var previousFragment: MenuItem? = null
    private var bluetoothServices: MutableList<BluetoothGattService?>? = null
    private var readCount: Int = 0
    private var navBar: BottomNavigationView? = null
    private var isBusy: Boolean = false


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
        // Replace Splash Screen with app theme with a delay
        // MUST be run BEFORE super.onCreate()
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        this.supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // Get the devices bluetooth adapter
        mBluetoothAdapter = mBTLEAdapter.getBluetooth()

        // OnClick handlers for Navigation Bar
        navBar = findViewById(R.id.bottomNavigationView)
        //What to do, when an item in the NavBar is clicked
        val itemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
                // Comparable to switch case
                when (menuItem.itemId) {
                    R.id.navigation_discover -> {
                        if (!isBusy) {
                            changeFragment(fragmentManager, discoverFrag, menuItem, 0)
                        }
                        else {
                            tools.showToast("Unable to Navigate. Please Wait.")
                        }
                        return@OnNavigationItemSelectedListener true
                    }

                    //Apply new logic for deciding what setup page to navigate to here
                    /*
                    CHECK VAR TO SEE IF BG TASK IS RUNNING, IF YES, DO NOT NAVIGATE
                    INFORM USER WHY THEY CAN NOT NAVIGATE WITH A TOAST
                     */
                    R.id.navigation_setup -> {
                        // See if a device is connected. If not, show the no connection fragment.
                        // If a device is connected, determine if it is a BSI or Local Listener
                        if (getConnectionState() == tools.DISCONNECTED && !isBusy) {
                            changeFragment(fragmentManager, noConnectedDeviceFrag, menuItem, 0)
                        }
                        else if (getConnectionState() == tools.CONNECTED && !isBusy) {
                            changeFragment(fragmentManager, BSISetupFragment(), menuItem, 0)
                        }
                        else {
                            tools.showToast("Unable to Navigate. Please Wait.")
                        }
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.navigation_debug -> {
                        if (getConnectionState() == tools.DISCONNECTED && !isBusy){
                            changeFragment(fragmentManager, noConnectedDeviceFrag, menuItem, 0)
                        }
                        else if (getConnectionState() == tools.CONNECTED && !isBusy) {
                            changeFragment(fragmentManager, DebugFragment(), menuItem, 0)
                        }
                        else {
                            tools.showToast("Unable to Navigate. Please Wait.")
                        }
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.navigation_info -> {
                        if (!isBusy) {
                            changeFragment(fragmentManager, InfoFragment(), menuItem, 0)
                        }
                        else {
                            tools.showToast("Unable to Navigate. Please Wait.")
                        }
                        return@OnNavigationItemSelectedListener true
                    }
                }

                false
            }

        //Set the onClick listener to the object defined above
        navBar?.setOnNavigationItemSelectedListener(itemSelectedListener)

        //Start with the discover fragment when the app opens
        changeFragment(fragmentManager, discoverFrag, null, 1)
    }

    // Runs when the view is destroyed (happens when the app is closed from recent apps).
    // Shut anything down that should NOT be running in the background (e.g. a bluetooth connection)
    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }

    override fun onStop() {
        super.onStop()
        disconnect()
    }

    // Runs when MainActivity returns to focus (e.g. a system dialogue box closes with a result)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //Check which activity we are returning from.
        if (requestCode == requestEnableBt) {
            displayPermissionStatus("Access to Bluetooth", resultCode)
        }
    }

    @SuppressWarnings("SameParameterValue")
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

    private fun getConnectionState(): Int {
        if (bluetoothGatt?.device != null) {
            return tools.CONNECTED
        }
        else {
            return tools.DISCONNECTED
        }
    }

    private fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }


    //
    // DiscoverDevices Functions
    //
    // Runs when the view is created. Only populate list if there
    // are items to populate it with.
    override fun discoverListViewDataMover(): MutableList<ScanResult>? {
        return mBTLEAdapter.scanResults
    }

    override fun discoverContextMover(): Context {
        return this@MainActivity
    }

    // Runs when the Search button is pressed.
    override fun updateConnectionStatus(): Int {
        return getConnectionState()
    }

    // Runs when the Search button is pressed.
    override fun onButtonSearch() {
        isBusy = true
        mBTLEAdapter.findBluetoothDevices(mBluetoothAdapter)
        search.text = getString(R.string.search_button_alt_text_3)
    }

    // Runs when the Search button is pressed.
    override fun onButtonDisconnect() {
        disconnect()
    }

    // Runs when an item in the Device List is pressed.
    // This initiates a GATT connection to the selected device.
    override fun onListPressed(): AdapterView.OnItemClickListener? {
        return AdapterView.OnItemClickListener { parent, _, position, _ ->

            if (bluetoothGatt != null) {
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
                bluetoothGatt = null
            }

            val clickedItem = parent.getItemAtPosition(position) as ScanResult

            // CONNECTION NOT WORKING. HAVE TO USE nRF Connect to make it work
            Handler(Looper.getMainLooper()).post {
                val button = findViewById<Button>(R.id.search)
                button.text = getString(R.string.search_button_alt_text_2)
            }
            isBusy = true
            bluetoothGatt = clickedItem.device.connectGatt(applicationContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE)
        }
    }


    //
    // BSI Setup Functions
    //
    override fun bsiFragmentContextMover(): Context {
        return this@MainActivity
    }

    override fun bsiNameMover(): String {
        val name = bluetoothGatt?.device?.name
        if (name != null) {
            return name
        }
        else {
            return "New BSI"
        }
    }

    // Runs when the page is being created
    override fun findConfig(): BSIObject {
        return config
    }

    // Runs when the user clicks the commit changes button on the setup page
    override fun commitConfig(bsi: BSIObject) {
        if (bluetoothGatt != null) {
            Handler(Looper.getMainLooper()).post {
                val submitButton = findViewById<Button>(R.id.setup_bsi_submit)
                submitButton.text = getString(R.string.setup_submit_changes_alt_text_1)
            }

            // get service to send data to
            val configService = bluetoothGatt!!.getService(mBTLEAdapter.bsiServiceUUID.uuid)

            // grab list of characteristics that the device has for sending configuration
            val configCharacteristic: List<BluetoothGattCharacteristic> =
                listOf(configService.getCharacteristic(mBTLEAdapter.s2MeasureInterval),
                    configService.getCharacteristic(mBTLEAdapter.s3MeasureInterval),
                    configService.getCharacteristic(mBTLEAdapter.dtAlarmOn),
                    configService.getCharacteristic(mBTLEAdapter.dtAlarmOff),
                    configService.getCharacteristic(mBTLEAdapter.dmAlarmS2On),
                    configService.getCharacteristic(mBTLEAdapter.dmAlarmS2Off),
                    configService.getCharacteristic(mBTLEAdapter.dmAlarmS3On),
                    configService.getCharacteristic(mBTLEAdapter.dmAlarmS3Off),
                    configService.getCharacteristic(mBTLEAdapter.uploadSize),
                    configService.getCharacteristic(mBTLEAdapter.sensorConfig),
                    configService.getCharacteristic(mBTLEAdapter.podS2),
                    configService.getCharacteristic(mBTLEAdapter.podS3))

            val strConfigCharacteristic: List<BluetoothGattCharacteristic> =
                listOf(configService.getCharacteristic(mBTLEAdapter.bsiName),
                configService.getCharacteristic(mBTLEAdapter.bsiTime))

            // Send the configuration to the remote device
            isBusy = true
            mBTLEAdapter.tx(bluetoothGatt!!, configCharacteristic, strConfigCharacteristic, bsi)
            // Update data once complete
        }
        else {
            tools.showToast("Error. No device connected!")
        }
    }


    //
    // Debug Functions
    //
    // Runs when the Populate button is pressed.
    override fun debugDataMover() {
        characterNamesStr = mutableListOf()
        // Creates a list of user readable names vs UUID strings
        uuidsStr.forEach { s ->
            when(s) {
                mBTLEAdapter.s3MeasureInterval.toString() -> characterNamesStr.add("A2 Measurement Interval:")
                mBTLEAdapter.bsiName.toString() -> characterNamesStr.add("BSI Name:")
                mBTLEAdapter.bsiTime.toString() -> characterNamesStr.add("UTC Time (Seconds):")
                mBTLEAdapter.dmAlarmS2Off.toString() -> characterNamesStr.add("A1 Alarm OFF Status:")
                mBTLEAdapter.dmAlarmS2On.toString() -> characterNamesStr.add("A1 Alarm ON Status:")
                mBTLEAdapter.dmAlarmS3Off.toString() -> characterNamesStr.add("A2 Alarm OFF Status:")
                mBTLEAdapter.dmAlarmS3On.toString() -> characterNamesStr.add("A2 Alarm ON Status:")
                mBTLEAdapter.dtAlarmOff.toString() -> characterNamesStr.add("Pulse Alarm OFF Status:")
                mBTLEAdapter.dtAlarmOn.toString() -> characterNamesStr.add("Pulse Alarm ON Status:")
                mBTLEAdapter.podS2.toString() -> characterNamesStr.add("Power ON Delay A1:")
                mBTLEAdapter.podS3.toString() -> characterNamesStr.add("Power OFF Delay A2:")
                mBTLEAdapter.s2MeasureInterval.toString() -> characterNamesStr.add("A1 Measurement Interval:")
                mBTLEAdapter.sensorConfig.toString() -> characterNamesStr.add("Sensor Configuration:")
                mBTLEAdapter.uploadSize.toString() -> characterNamesStr.add("Upload Size:")
                mBTLEAdapter.sensA1.toString() -> characterNamesStr.add("Analog 1 Sensor Reading:")
                mBTLEAdapter.sensA2.toString() -> characterNamesStr.add("Analog 2 Sensor Reading:")
                mBTLEAdapter.sensP.toString() -> characterNamesStr.add("Pulse Sensor Reading:")
                mBTLEAdapter.bsiBattery.toString() -> characterNamesStr.add("Current BSI Battery:")
            }
        }

        // pass the two string lists to be adapted into basic listView
        val mAdapter = CharacteristicListAdapter(this, characterNamesStr, valuesStr)
        val servicesList = findViewById<ListView>(R.id.debug_services_list)
        servicesList.adapter = mAdapter
    }

    override fun updateDataMover() {
        // update debug button wth new text here
        val button = findViewById<Button>(R.id.debug_refresh_characters)
        if (button != null) {
            button.text = getString(R.string.debug_refresh_button_alt_text_1)
        }
        // Reset counter and accumulated data
        readCount = 0
        uuidsStr = mutableListOf()
        valuesStr = mutableListOf()

        // Ask for new data
        try {
            isBusy = true
            bluetoothGatt?.readCharacteristic(mService?.getCharacteristic(mBTLEAdapter.uuidList[readCount]))
        }
        catch (e: NullPointerException) {
            // Implement
        }
    }


    //
    // MainActivity Inner Classes
    //
    // Functions in this class run on a ASYNC callback.
    // Bluetooth runs in a separate thread from the UI (main thread)
    inner class GattCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            // If we connected to the GATT server find services on device
            // Change button text back MUST BE DONE ON UI THREAD
            if (status == BluetoothGatt.GATT_SUCCESS) {
                valuesStr = mutableListOf()
                uuidsStr = mutableListOf()
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            bluetoothServices = gatt?.services
            mService = bluetoothGatt?.getService(mBTLEAdapter.bsiServiceUUID.uuid)
            try {
                Handler(Looper.getMainLooper()).post {
                    val button = findViewById<Button>(R.id.search)
                    button.text = getString(R.string.search_button_alt_text_4)
                }
                readCount = 0
                bluetoothGatt?.readCharacteristic(mService?.getCharacteristic(mBTLEAdapter.uuidList[readCount]))
            }
            catch (e: NullPointerException) {

            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            //"Notification"
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Handler(Looper.getMainLooper()).post {
                val submitButton = findViewById<Button>(R.id.setup_bsi_submit)
                submitButton.text = getString(R.string.setup_submit_changes)
            }
            isBusy = false
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            // Load this when we connect to the device
            // This is where i pull config data from
            uuidsStr.add(characteristic.uuid.toString())
            if (characteristic.uuid.toString() == mBTLEAdapter.bsiName.toString()) {
                valuesStr.add(characteristic.getStringValue(0))
            }
            else {
                //sensorConfig = characteristic.getIntValue(17, 0)
                valuesStr.add(characteristic.getIntValue(17, 0).toString())
            }

            // read the next value
            readCount += 1
            if (readCount <= 12) {
                bluetoothGatt?.readCharacteristic(mService?.getCharacteristic(mBTLEAdapter.uuidList[readCount]))
            }
            else {
                // break the cycle update the config with the lists
                // Reset button(s) text here
                config = mBTLEAdapter.rx(uuidsStr, valuesStr)

                if (navBar?.selectedItemId == R.id.navigation_discover) {
                    Handler(Looper.getMainLooper()).post {
                        val button = findViewById<Button>(R.id.search)
                        button.text = getString(R.string.search_button_alt_text_1)
                    }
                }
                else if (navBar?.selectedItemId == R.id.navigation_debug) {
                    Handler(Looper.getMainLooper()).post {
                        debugDataMover() // Update page with new content
                        val button = findViewById<Button>(R.id.debug_refresh_characters)
                        if (button != null) {
                            button.text = getString(R.string.debug_refresh_button)
                        }
                    }
                }
                isBusy = false
            }
        }
    }
}