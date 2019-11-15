/*
File Name: DeciveListAdapter.kt
Author: Riley Larche
Date Updated: 2019-11-15
Android Studio Version:
Tested on Android Version: 10

Class for data display inside ListView.
 */


//Packages and Imports
package com.example.aesir

import android.bluetooth.le.ScanResult
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView


class DeviceListAdapter(context: Context, private val dataSource: MutableList<ScanResult>?): BaseAdapter(){

    //Setup? !!Look into Layout Inflater!!
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    //Functions
    override fun getCount(): Int {
        return dataSource!!.size
    }

    override fun getItem(position: Int): Any {
        return dataSource!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        // If no previous view existed create one
        if (convertView == null) {
            view = inflater.inflate(R.layout.list_item_bt, parent, false)

            holder = ViewHolder()
            holder.titleTextView = view.findViewById(R.id.DeviceName) as TextView
            holder.subtitleTextView = view.findViewById(R.id.DeviceAddress) as TextView
            holder.thumbnailImageView = view.findViewById(R.id.DeviceThumbnail) as ImageView

            view.tag = holder
        }
        //
        else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        // Load subviews
        val deviceNameTextView = holder.titleTextView
        val deviceAddressTextView = holder.subtitleTextView
        //val deviceThumbnailImageView = holder.thumbnailImageView

        //Populate fields with data
        val deviceBt = getItem(position) as ScanResult

        // Determine device type here

        if (deviceBt.scanRecord?.deviceName == null) {
            deviceNameTextView.text = "Unknown Device"
        }
        else {
            deviceNameTextView.text = deviceBt.scanRecord?.deviceName
        }

        deviceAddressTextView.text = deviceBt.device?.address

        return view
    }


    private class ViewHolder {
        lateinit var titleTextView: TextView
        lateinit var subtitleTextView: TextView
        lateinit var thumbnailImageView: ImageView
    }
}