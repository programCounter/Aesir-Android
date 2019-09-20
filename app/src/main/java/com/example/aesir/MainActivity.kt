package com.example.aesir

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class MainActivity : AppCompatActivity() {

    //Global Variables
    private val requestEnableBt: Int = 0
    var mScanning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Find and reference items in Activity
        val searchButton = findViewById<Button>(R.id.search)
        val deviceList: ListView = findViewById<ExpandableListView>(R.id.device_list)

        //Init Local Variables
        val arraySize = 100
        val listItems = arrayOfNulls<String>(arraySize)

        //Run Setup Functions and Logic for MainActivity
        val adapterBt: BluetoothAdapter? = setUpBluetooth() //Only checks when app is first enabled... problem?

        searchButton.setOnClickListener{
            //Re-Direct the search button to the following function:
            findBluetoothDevices()
        }

        //Old Array Code:
        for(i in 0 until arraySize){
            listItems[i] = "List Item: $i"
        }
        //Create and set simple adapter for listView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        deviceList.adapter = adapter

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //Check which activity we are returning from.
        if(requestCode == requestEnableBt){
            //Was Bluetooth turned on? Display result to user.
            if(resultCode == Activity.RESULT_OK){
                showToast("Bluetooth Enabled!")
            }
            else{
                showToast("Bluetooth NOT Enabled!")
            }
        }
    }


    private fun showToast(text: String){
        //Shows message through system.
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
        toast.show()
    }


    fun setUpBluetooth(): BluetoothAdapter? {
        //Get the Bluetooth Adapter
        val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        }

        //Enable Bluetooth
        //If Bluetooth is available on the device but not enabled,
        //a dialog will open for the user to enable Bluetooth
        bluetoothAdapter?.takeIf { !it.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, requestEnableBt)
        }

        return bluetoothAdapter

    }


    fun findBluetoothDevices(){
        //Scan for devices (limited to kitkat or above, need older API for lower)
        showToast("Searching for Bluetooth LE Devices...")
    }
}


