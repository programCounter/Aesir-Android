/*
File Name: DiscoverDevicesFragment.kt
Author: Riley Larche
Date Updated: 2019-10-17
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic that pertains to objects in the view for the
Setup fragment is contained in this file.
 */


//
// Packages and Imports
//
package com.example.aesir

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.setup_fragment.*


//
// Setup Fragment Class
//
class SetupFragment : Fragment() {
    //
    // Private class VAL or VAR
    //
    private lateinit var mInterface: Setup


    //
    // Fragment Functions
    //
    // Runs when the fragment is being loaded by the FragmentManager in MainActivity
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.setup_fragment, container, false)
    }

    // Runs when the view is done being created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup interface between UI elements and MainActivity
        bsis_in_network_list.adapter = mInterface.setupListViewDataMover()

        add_item_to_network.setOnClickListener {
            mInterface.onAddDevicesPressed()
        }
        submit_chnages.setOnClickListener {
            mInterface.onSubmitChanges()
        }

        bsis_in_network_list.onItemClickListener = mInterface.onBSIListPressed()
    }

    // Runs when the view is attached (becomes one with) MainActivity
    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is Setup) {
            mInterface = context
        }
        else {
            throw ClassCastException(context.toString() + "must implement OnSearchButtonPressed!")
        }
    }


    //
    // Fragment Interface(s)
    //
    interface Setup {
        fun setupListViewDataMover(): BSIListAdapter
        fun onAddDevicesPressed()
        fun onBSIListPressed(): AdapterView.OnItemClickListener?
        fun onSubmitChanges()
    }
}