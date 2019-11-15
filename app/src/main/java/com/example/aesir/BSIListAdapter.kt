/*
/*
File Name: BSIListAdapter.kt
Author: Riley Larche
Date Updated: 2019-10-17
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic that pertains to the ListView for BSIs in
SetupFragment.kt is contained in this file
 */


//
// Packages and Imports
//
package com.example.aesir

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


//
// Adapter Class
//
class BSIListAdapter(context: Context, private val dataSource: MutableList<BSIEntry>?): BaseAdapter() {
    //
    // Private Class VAR and VAL
    //
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    //
    // Adapter Functions
    //
    override fun getCount(): Int {
        return dataSource!!.size
    }

    override fun getItem(position: Int): BSIEntry {
        return dataSource!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_services_bt, parent, false)

            holder = ViewHolder()
            holder.titleTextView = view.findViewById(R.id.service_name) as TextView
            holder.subtitleTextView = view.findViewById(R.id.service_uuid) as TextView

            view.tag = holder
        }
        else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val macAddress = holder.titleTextView
        val friendlyName = holder.subtitleTextView

        val bsi = getItem(position)

        //macAddress.text = bsi.mac
        //friendlyName.text = bsi.friendlyName

        return view
    }

    //
    // Inner Classes
    //
    private class ViewHolder {
        lateinit var titleTextView: TextView
        lateinit var subtitleTextView: TextView
    }
}

 */