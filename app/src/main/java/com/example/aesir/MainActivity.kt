/*
File Name: MainActivity.kt
Author: Riley Larche
Date Updated: 2019-11-21
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic for MainActivity in app Aesir is contained in this file and any linking methods or objects.
 */

/*
TODO List:
    2. Retain fragment state when user re-navigates to it. [IN PROGRESS]
    3. Add fragment for BSI specific configuration tasks. [IN PROGRESS]
    4. Fix Bug: If doing an async task and the user navigates off the page it was initiated on,
    the app crashes when results are fed to UI element that no longer exists [NULL POINTER].
    5. Come up with code naming standard and update/adhere to it.
    7. Tx data to device [In Progress]
    8. Read data back from device in debug fragment [In Progress]
    9. Fix Bug: App sometimes wont connect to device until NRFconnect is used before. Punchthrough
    wont work either. [In Progress] [POSTPONED]
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
import kotlinx.android.synthetic.main.bsi_fragment.*
import kotlinx.android.synthetic.main.discover_devices_fragment.*

//
// Start of MainActivity
//
class MainActivity : AppCompatActivity(), DiscoverDevicesFragment.Discover, BSISetupFragment.BSI, DebugFragment.DebugListener, LLSetupFragment.Setup {
    //
    // Used classes
    //
    val tools = Tools(this)
    private var mBTLEAdapter = BluetoothLEAdapter(this)
    private var mGattCallback = GattCallback()
    private val discoverFrag: Fragment = DiscoverDevicesFragment()
    private val noConnectedDeviceFrag: Fragment = NoDeviceConnectedFragment()
    //private val setupFrag: Fragment = LLSetupFragment()
    private val bsiFrag: Fragment = BSISetupFragment()
    private val debugFrag: Fragment = DebugFragment()
    private val infoFrag: Fragment = InfoFragment()


    //
    // Private variables and values
    //
    private val requestAccessFineLocation: Int = 0
    private val requestEnableBt: Int = 1
    private val fragmentManager = supportFragmentManager
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var previousFragment: MenuItem? = null
    private var bluetoothServices: MutableList<BluetoothGattService?>? = null
    //private var bsiList: MutableList<BSIEntry>? = ArrayList()


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

                    //Apply new logic for deciding what setup page to navigate to here
                    R.id.navigation_setup -> {
                        // See if a device is connected. If not, show the no connection fragment.
                        // If a device is connected, determine if it is a BSI or Local Listener
                        if (getConnectionState() == tools.DISCONNECTED) {
                            changeFragment(fragmentManager, noConnectedDeviceFrag, menuItem, 0)
                        }
                        else {
                            changeFragment(fragmentManager, bsiFrag, menuItem, 0)
                        }
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.navigation_debug -> {
                        if (getConnectionState() == tools.DISCONNECTED){
                            changeFragment(fragmentManager, noConnectedDeviceFrag, menuItem, 0)
                        }
                        else {
                            changeFragment(fragmentManager, debugFrag, menuItem, 0)
                        }
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
        mBTLEAdapter.findBluetoothDevices(mBluetoothAdapter)
        search.text = R.string.search_button_alt_text_3.toString()
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
            }

            val clickedItem = parent.getItemAtPosition(position) as ScanResult

            // CONNECTION NOT WORKING. HAVE TO USE nRF Connect to make it work
            Handler(Looper.getMainLooper()).post {
                val button = findViewById<Button>(R.id.search)
                button.text = R.string.search_button_alt_text_2.toString()
            }
            bluetoothGatt = clickedItem.device.connectGatt(applicationContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE)
        }
    }


    //
    // Local Listener Setup Functions [NOT IMPLEMENTED]
    //
    // Runs when the view is created.
    /*
    override fun setupListViewDataMover(): BSIListAdapter {
        return BSIListAdapter(this, bsiList)
    }

    override fun onAddDevicesPressed() {
    //    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
     */


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

    override fun commitConfig(bsi: BSIObject) {
        if (bluetoothGatt != null) {
            // get service to send data to
            val configService = bluetoothGatt!!.getService(mBTLEAdapter.bsiServiceUUID.uuid)

            // grab list of characteristics that the device has for sending configuration
            val configCharacteristic: List<BluetoothGattCharacteristic> =
                listOf(configService.getCharacteristic(mBTLEAdapter.uploadInterval),
                    configService.getCharacteristic(), configService.getCharacteristic(mBTLEAdapter.s2MeasureInterval),
                    configService.getCharacteristic(), configService.getCharacteristic(mBTLEAdapter.s3MeasureInterval),
                    configService.getCharacteristic(), configService.getCharacteristic(),
                    configService.getCharacteristic(mBTLEAdapter.sensorConfig))

            // Send the configuration to the remote device
            mBTLEAdapter.tx(bluetoothGatt!!, configCharacteristic, bsi)
            // Update name displayed on page (since the user can change it)
        }
        else {
            tools.showToast("Error. No device connected!")
        }
    }

    override fun onSubmitChanges() {
        tools.showToast("Not sure how you got here...")
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
            // If we connected to the GATT server find services on device
            // Change button text back MUST BE DONE ON UI THREAD
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Handler(Looper.getMainLooper()).post {
                    val button = findViewById<Button>(R.id.search)
                    if (button != null) {
                        button.text = R.string.search_button_alt_text_1.toString()
                    }
                }
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

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            //Confirm that the characteristic was actually changed
            tools.showToast("Characteristic was written!")

            Handler(Looper.getMainLooper()).post {
                val submitButton = findViewById<Button>(R.id.setup_bsi_submit)
                submitButton.text = R.string.setup_submit_changes_alt_text_2.toString()
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {

        }
    }
}
