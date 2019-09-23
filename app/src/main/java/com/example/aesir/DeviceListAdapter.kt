/*
Author: Riley Larche
Date Updated: 2019-09-23
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


class DeviceListAdapter(private val context: Context, private val dataSource: MutableList<ScanResult>?): BaseAdapter(){

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
        //Get view for a row item
        val rowView = inflater.inflate(R.layout.list_item_bt, parent, false)

        //Get DeviceName element
        val deviceNameTextView = rowView.findViewById(R.id.DeviceName) as TextView

        //Get DeviceAddress element
        val deviceAddressTextView = rowView.findViewById(R.id.DeviceAddress) as TextView

        //Get Thumbnail element
        val deviceThumbnailImageView = rowView.findViewById(R.id.DeviceThumbnail) as ImageView

        //Populate fields with data
        val deviceBt = getItem(position) as ScanResult

        deviceNameTextView.text = deviceBt.device?.name
        deviceAddressTextView.text = deviceBt.device?.address
        //Change thumbnail according to if BSI OR LL

        return rowView
    }
}