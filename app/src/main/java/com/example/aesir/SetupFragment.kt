/*
File Name: DiscoverDevicesFragment.kt
Author: Riley Larche
Date Updated: 2019-10-07
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic that pertains to objects in the view for the
Setup fragment is contained in this file.
 */


package com.example.aesir

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.setup_fragment.*

class SetupFragment : Fragment() {
    private lateinit var listener: setup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.setup_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        add_item_to_network.setOnClickListener {
            listener.onAddDevicesPressed()
        }

        bsis_in_network_list.onItemClickListener = listener.onBSIListPressed()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is setup) {
            listener = context
        }
        else {
            throw ClassCastException(context.toString() + "must implement OnSearchButtonPressed!")
        }
    }

    interface setup {
        fun onAddDevicesPressed()
        fun onBSIListPressed() : AdapterView.OnItemClickListener?
    }
}