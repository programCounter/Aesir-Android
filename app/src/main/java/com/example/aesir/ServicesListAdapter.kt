/*
File Name: DiscoverDevicesFragment.kt
Author: Riley Larche
Date Updated: 2019-12-01
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic that pertains to the list adapter for the
Debug fragment is contained in this file.
 */


//
// Imports and Packages
//
package com.example.aesir

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


//
// Class
//
class CharacteristicListAdapter(context: Context, private val dataSource: MutableList<String>, private val dataSource2: MutableList<String>/*, private val sensorCfg: Int*/): BaseAdapter() {
    //
    // Private variables and values
    //
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    //
    // Functions
    //
    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_services_bt, parent, false)

            holder = ViewHolder()
            holder.titleTextView = view.findViewById(R.id.characteristic_name) as TextView
            holder.subtitleTextView = view.findViewById(R.id.chracteristic_value) as TextView

            view.tag = holder
        }
        else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val characterName = holder.titleTextView
        val characterValue = holder.subtitleTextView

        characterName.text = dataSource[position]
        characterValue.text = dataSource2[position]
        /*
        if (dataSource[position] == "Sensor Configuration") {
            characterValue.text = sensorCfg.toString()
        }
        else {
            characterValue.text = dataSource2[position]
        }

         */

        return view
    }


    //
    // Local Classes
    //
    private class ViewHolder {
        lateinit var titleTextView: TextView
        lateinit var subtitleTextView: TextView
    }
}