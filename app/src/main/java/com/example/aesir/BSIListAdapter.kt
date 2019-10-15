package com.example.aesir

import android.bluetooth.BluetoothGattService
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class BSIListAdapter(context: Context, private val dataSource: MutableList<BSIEntry>?): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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

        macAddress.text = bsi.mac
        friendlyName.text = bsi.friendlyName

        return view
    }

    private class ViewHolder {
        lateinit var titleTextView: TextView
        lateinit var subtitleTextView: TextView
    }
}