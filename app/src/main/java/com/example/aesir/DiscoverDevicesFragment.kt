/*
File Name: DiscoverDevicesFragment.kt
Author: Riley Larche
Date Updated: 2019-10-07
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic that pertains to objects in the view for the
Discover Devices fragment is contained in this file.
 */


package com.example.aesir


import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.discover_devices_fragment.*

class DiscoverDevicesFragment : Fragment() {
    val tools = Tools(null)
    private lateinit var mInterface: Discover

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.discover_devices_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listData = mInterface.discoverListViewDataMover()
        val context = mInterface.discoverContextMover()

        if (!listData.isNullOrEmpty()) {
            device_list.adapter = DeviceListAdapter(context, listData)
        }

        search.setOnClickListener {
            setButtonText(true)
        }

        device_list.onItemClickListener = mInterface.onListPressed()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is Discover) {
            mInterface = context
        }
        else {
            throw ClassCastException(context.toString() + "must implement OnSearchButtonPressed!")
        }
    }

    override fun onResume() {
        super.onResume()

        // Restore what text should be shown on the button
        setButtonText(false)
    }

    private fun setButtonText(click: Boolean) {
        val connSatus = mInterface.updateConnectionStatus()
        // Already connected, button now disconnects
        if (connSatus == tools.CONNECTED && click) {
            mInterface.onButtonDisconnect()
            search.text = "Search"
        }
        // Already connected, no change
        else if (connSatus == tools.CONNECTED) {
            search.text = "Disconnect"
        }
        // No device connected, button searches for devices
        else if (connSatus == tools.DISCONNECTED && click) {
            mInterface.onButtonSearch()
            search.text = "Searching..."
        }
        // Disconnected, no change
        else if (connSatus == tools.DISCONNECTED) {
            search.text = "Search"
        }
    }

    // Container Activity must implement this interface
    interface Discover {
        fun discoverListViewDataMover(): MutableList<ScanResult>?
        fun discoverContextMover(): Context
        fun onButtonSearch()
        fun onButtonDisconnect()
        fun updateConnectionStatus(): Int
        fun onListPressed(): AdapterView.OnItemClickListener?
    }
}