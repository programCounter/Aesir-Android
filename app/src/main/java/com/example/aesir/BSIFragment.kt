/*
File Name: BSIFragment.kt
Author: Riley Larche
Date Updated: 2019-10-17
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic that pertains to objects in the view for the
bsi configuration fragment is contained in this file.
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
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment


//
// Setup Fragment Class
//
class BSIFragment : Fragment() {
    //
    // Private class VAL or VAR
    //
    private lateinit var mInterface: BSI


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
        return inflater.inflate(R.layout.bsi_fragment, container, false)
    }

    // Runs when the view is done being created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the Activity context
        val context = mInterface.bsiFragmentContextMover()

        // Get the selected BSI
        val bsi = mInterface.bsiObjectMover()

        // Set UI elements for the selected BSI
        val title = view.findViewById<TextView>(R.id.setup_bsi_title)
        if (bsi.friendlyName == null) {
            title.text = bsi.mac
        }
        else {
            title.text = bsi.friendlyName
        }

        // Set adapter for spinner
        val analog1spinner: Spinner = view.findViewById(R.id.bsi_analog_1_spinner)
        ArrayAdapter.createFromResource(context, R.array.bsi_spinner_options, android.R.layout.simple_spinner_item).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            analog1spinner.adapter = adapter
        }

    }

    // Runs when the view is attached (becomes one with) MainActivity
    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is BSI) {
            mInterface = context
        }
        else {
            throw ClassCastException(context.toString() + "must implement OnSearchButtonPressed!")
        }
    }


    //
    // Fragment Interface(s)
    //
    interface BSI {
        fun bsiFragmentContextMover(): Context
        fun bsiObjectMover(): BSIEntry
    }
}