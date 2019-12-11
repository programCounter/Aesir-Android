/*
File Name: mBluetoothLEAdapter.kt
Author: Riley Larche
Date Updated: 2019-12-11
Android Studio Version:
Tested on Android Version: 10

Code pertaining to bluetooth within the app.
 */


//
// Packages and Imports
//
package com.example.aesir

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.widget.Button
import android.widget.ListView
import java.lang.NumberFormatException
import java.util.*


//
// BluetoothLEAdapter Class
//
class BluetoothLEAdapter(passedActivity: Activity) {
    //
    // Private variables and values
    //
    private val activity = passedActivity
    private val reportDelay: Long = 500
    private val mCallback = MCallBack()
    private var scanner: BluetoothLeScanner? = null
    // Combine A then 14XX then B to create UUID
    private val baseBuuid: String = "0e28"
    private val baseAuuid: String = "-6801-4160-a7d6-a3b252dc43a1"

    // Shows which bits to test for (0 = ignore // 1 = must match)
    private val bsiServiceUUIDMask: ParcelUuid =
        ParcelUuid.fromString("000001111-0000-0000-0000-000000000000")


    //
    // Public (Default) variables and values
    //
    val bsiServiceUUID: ParcelUuid = ParcelUuid.fromString(baseBuuid + "1400" + baseAuuid)
    // Characteristic UUIDs
    val s2MeasureInterval: UUID = UUID.fromString(baseBuuid + "1401" + baseAuuid)
    val s3MeasureInterval: UUID = UUID.fromString(baseBuuid + "1402" + baseAuuid)
    val dtAlarmOn: UUID = UUID.fromString(baseBuuid + "1403" + baseAuuid) // Pulse Alarm on (time between x)
    val dtAlarmOff: UUID = UUID.fromString(baseBuuid + "1404" + baseAuuid) // Pulse Alarm off (time between x)
    val dmAlarmS2On: UUID = UUID.fromString(baseBuuid + "1405" + baseAuuid)
    val dmAlarmS2Off: UUID = UUID.fromString(baseBuuid + "1406" + baseAuuid)
    val dmAlarmS3On: UUID = UUID.fromString(baseBuuid + "1407" + baseAuuid)
    val dmAlarmS3Off: UUID = UUID.fromString(baseBuuid + "1408" + baseAuuid)
    val uploadSize: UUID = UUID.fromString(baseBuuid + "1409" + baseAuuid)
    val sensorConfig: UUID = UUID.fromString(baseBuuid + "1410" + baseAuuid)
    //val sesnorAddress: UUID = UUID.fromString(baseBuuid + "1411" + baseAuuid)
    //val uploadInterval: UUID = UUID.fromString(baseBuuid + "1412" + baseAuuid)
    val bsiName: UUID = UUID.fromString(baseBuuid + "1413" + baseAuuid)
    val podS2: UUID = UUID.fromString(baseBuuid + "1414" + baseAuuid)
    val podS3: UUID = UUID.fromString(baseBuuid + "1415" + baseAuuid)
    val bsiTime: UUID = UUID.fromString(baseBuuid + "1416" + baseAuuid)
    val sensA1: UUID = UUID.fromString(baseBuuid + "1417" + baseAuuid)
    val sensA2: UUID = UUID.fromString(baseBuuid + "1418" + baseAuuid)
    val sensP: UUID = UUID.fromString(baseBuuid + "1419" + baseAuuid)
    val bsiBattery: UUID = UUID.fromString(baseBuuid + "1444" + baseAuuid)
    val uuidList: MutableList<UUID> = mutableListOf(s2MeasureInterval, s3MeasureInterval,
        dtAlarmOn, dtAlarmOff, dmAlarmS2On, dmAlarmS2Off, dmAlarmS3On, dmAlarmS3Off,
        uploadSize, sensorConfig, bsiName, podS2, podS3)


    var scanResults: MutableList<ScanResult>? = null


    //
    // Used classes
    //
    private val tools = Tools(activity)


    //
    // Functions
    //
    fun getBluetooth(): BluetoothAdapter {
        val mBluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
            val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        }

        return mBluetoothAdapter
    }

    fun findBluetoothDevices(mBluetoothAdapter: BluetoothAdapter?){
        // Get and instance of the Bluetooth Low Energy Scanner
        scanner = mBluetoothAdapter?.bluetoothLeScanner

        // Create search settings object
        val mSettings = ScanSettings.Builder().
            setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).
            setReportDelay(reportDelay).
            build()

        // Create a scanning filter
        val mFilter = ScanFilter.Builder().setServiceUuid(bsiServiceUUID, bsiServiceUUIDMask).build()
        val scannerFilter = arrayListOf<ScanFilter>()
        scannerFilter.add(mFilter)

        // Stop previous scan if there was one
        stopScanningBluetoothDevices()

        //Start new scanner
        scanner?.startScan(scannerFilter, mSettings, mCallback)
    }

    fun stopScanningBluetoothDevices() {
        scanner?.stopScan(mCallback)
        scanner?.flushPendingScanResults(mCallback)
    }

    fun tx(gatt: BluetoothGatt, character: List<BluetoothGattCharacteristic>, strCharacter: List<BluetoothGattCharacteristic>, data: BSIObject) {
        //Runs on submit button push

        // Create binary value "XXX" for which senors are active = 1 or
        // inactive = 0
        var cfg = 0
        if (data.pEnable == 1) {
            cfg += 4
        }
        if (data.a1Enable == 1) {
            cfg += 2
        }
        if (data.a2Enable == 1) {
            cfg += 1
        }

        // Convert the BSI Object into a iterable object
        val dataIterable: List<Int> =
            listOf(data.a1measureint,
                data.a2measureint,
                data.pAlarmtrigger,
                data.pAlarmshutoff,
                data.a1alarmON,
                data.a1alarmOFF,
                data.a2alarmON,
                data.a2alarmOFF,
                data.upldSize,
                cfg,
                data.txInterval,
                data.a1pod,
                data.a2pod)

        // Iterate and send data
        for (x in 0..character.lastIndex) {
            // Skip the name due to different type
            /*
            if (x != 10) {
                */
            character[x].setValue(dataIterable[x], 17, 0)
            gatt.writeCharacteristic(character[x]) // Do the push to the remote device
            Thread.sleep(1000)

             /*
            }

              */
        }

        Thread.sleep(500)

        // Send Friendly Name
        strCharacter[0].setValue(data.name)
        gatt.writeCharacteristic(strCharacter[0])

        Thread.sleep(500)

        // Get time UTC -> number in min. to send
        // Send last
        data.dateTime = (System.currentTimeMillis() / 60000).toInt()
        strCharacter[1].setValue(data.dateTime, 20, 0)
        gatt.writeCharacteristic(strCharacter[1])
    }

    fun rx(uuidsStr: MutableList<String>, valuesStr: MutableList<String>): BSIObject {
        // MUST happen after discoveryServices
        // Runs when the user navigates to the BSI setup page
        // Finds current setup from the BSI for the population of UI
        val existingConfig = BSIObject("")

        uuidsStr.forEachIndexed {index, element ->
            when(element) {
                s2MeasureInterval.toString() ->
                    try {
                        existingConfig.a1measureint = valuesStr[index].toInt()
                    }
                    catch (e: NumberFormatException) {

                    }
                s3MeasureInterval.toString() ->
                    try {
                        existingConfig.a2measureint = valuesStr[index].toInt()
                    }
                    catch (e: NumberFormatException) {

                    }
                dtAlarmOn.toString() ->
                    try {
                        existingConfig.pAlarmtrigger = valuesStr[index].toInt()
                    }
                    catch (e: NumberFormatException) {

                    }
                dtAlarmOff.toString() ->
                    try {
                        existingConfig.pAlarmshutoff = valuesStr[index].toInt()
                    }
                    catch (e: NumberFormatException) {

                    }
                /*
                NOT IMPLEMENTED CONFIGURATION PARAMETERS
                 */
                //dmAlarmS2On.toString() ->
                //dmAlarmS2Off.toString() ->
                //dmAlarmS3On.toString() ->
                //dmAlarmS3Off.toString() ->
                uploadSize.toString() ->
                    try {
                        existingConfig.upldSize = valuesStr[index].toInt()
                    }
                    catch (e: NumberFormatException) {

                    }
                sensorConfig.toString() ->
                    try {
                        existingConfig.sensorConfig = valuesStr[index].toInt()
                    }
                    catch (e: NumberFormatException) {

                    }
                //uploadInterval.toString() ->
                //    existingConfig.txInterval = valuesStr[index].toInt()
                bsiName.toString() ->
                    try {
                        existingConfig.name = valuesStr[index]
                    }
                    catch (e: NumberFormatException) {

                    }
                podS2.toString() ->
                    try {
                        existingConfig.a1pod = valuesStr[index].toInt()
                    }
                    catch (e: NumberFormatException) {

                    }
                podS3.toString() ->
                    try {
                        existingConfig.a2pod = valuesStr[index].toInt()
                    }
                    catch (e: NumberFormatException) {

                    }
                bsiBattery.toString() ->
                    try {
                        existingConfig.battery = valuesStr[index].toInt()
                    }
                    catch (e: NumberFormatException) {

                    }
            }
        }
        return existingConfig
    }


    //Inner Classes
    inner class MCallBack: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            stopScanningBluetoothDevices()
            tools.showToast("Single Result Found")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            stopScanningBluetoothDevices()
            tools.showToast("Error on Scan!")
            tools.showToast(errorCode.toString())
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)

            stopScanningBluetoothDevices()

            Handler(Looper.getMainLooper()).post {
                val button = activity.findViewById<Button>(R.id.search)
                button.text = activity.getString(R.string.search_button_text)
            }

            scanResults = results
            val mAdapter = DeviceListAdapter(activity, scanResults)
            val deviceList = activity.findViewById<ListView>(R.id.device_list)
            deviceList.adapter = mAdapter
        }
    }
}