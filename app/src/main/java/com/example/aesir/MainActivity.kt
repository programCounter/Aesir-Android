package com.example.aesir

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //Global Variables
    private val requestEnableBt: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Find and reference items in Activity
        val searchButton = findViewById<Button>(R.id.search)
        val deviceList: ListView = findViewById<ExpandableListView>(R.id.device_list)

        searchButton.setOnClickListener{
            //Re-Direct the search button to the following function:
            findBluetoothDevices()
        }

        //Old Array Code:
        //Create and set simple adapter for listView
        //val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        //deviceList.adapter = adapter

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


    fun findBluetoothDevices(){
        //Scan for devices (limited to kitkat or above, need older API for lower)
        showToast("Checking that Bluetooth is Enabled.")

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

        //Get and instance of the Bluetooth Low Energy Scanner
        val mScanner = mBluetoothAdapter?.bluetoothLeScanner

        //Stop previous scan if any
        mScanner?.stopScan(MCallBack())

        //showToast(mBluetoothAdapter.address.toString())
        textView.text = "Startting BT Scan...."
        mScanner?.startScan(MCallBack())
        //mScanner?.stopScan(MCallBack())
        //mScanner?.flushPendingScanResults(MCallBack())
    }


    inner class MCallBack: ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            textView.text = result?.device?.address
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            textView.text = "Scan FAIL"
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)

            textView.text = "Fuck me Scan"
        }
    }


}
