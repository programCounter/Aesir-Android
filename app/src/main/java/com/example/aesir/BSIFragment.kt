/*
File Name: BSIFragment.kt
Author: Riley Larche
Date Updated: 2019-11-04
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment


//
// Setup Fragment Class
//
class BSIFragment : Fragment(), AdapterView.OnItemSelectedListener {
    //
    // Private VAL or VAR
    //
    private lateinit var mInterface: BSI
    private var a1ui: List<TextView> = listOf()
    private var a2ui: List<TextView> = listOf()
    private var pui: List<TextView> = listOf()
    private var a1spinner: Spinner? = null
    private var a2spinner: Spinner? = null
    private var pspinner: Spinner? = null


    //
    // Fragment Functions
    //
    private fun showHide(elements: List<TextView>, show: Boolean) {
        var vis = View.GONE
        if (show) {
            vis = View.VISIBLE
        }

        for (item in elements) {
            //val uiElement = v.findViewById<TextView>(item)
            item.visibility = vis
        }
    }

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
        a1spinner = view.findViewById(R.id.bsi_analog_1_spinner)
        a2spinner = view.findViewById(R.id.bsi_analog_2_spinner)
        pspinner = view.findViewById(R.id.bsi_pulse_spinner)

        val analog1spinner: Spinner = view.findViewById(R.id.bsi_analog_1_spinner)
        ArrayAdapter.createFromResource(context, R.array.bsi_spinner_options, android.R.layout.simple_spinner_item).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            analog1spinner.adapter = adapter
        }

        val analog2spinner: Spinner = view.findViewById(R.id.bsi_analog_2_spinner)
        ArrayAdapter.createFromResource(context, R.array.bsi_spinner_options, android.R.layout.simple_spinner_item).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            analog2spinner.adapter = adapter
        }

        val pulsespinner: Spinner = view.findViewById(R.id.bsi_pulse_spinner)
        ArrayAdapter.createFromResource(context, R.array.bsi_spinner_options, android.R.layout.simple_spinner_item).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            pulsespinner.adapter = adapter
        }

        // Interface instance is this because Fragment extends it
        analog1spinner.onItemSelectedListener = this
        analog2spinner.onItemSelectedListener = this
        pulsespinner.onItemSelectedListener = this

        //Get UI elements for easy hiding or showing
        a1ui = listOf(view.findViewById(R.id.bsi_power_on_delay_analog1),
            view.findViewById(R.id.bsi_measurement_interval_analog1),
            view.findViewById(R.id.bsi_power_on_delay_input_analog1),
            view.findViewById(R.id.bsi_measurement_interval_input_analog1))
        a2ui = listOf(view.findViewById(R.id.bsi_power_on_delay_analog2),
            view.findViewById(R.id.bsi_measurement_interval_analog2),
            view.findViewById(R.id.bsi_power_on_delay_input_analog2),
            view.findViewById(R.id.bsi_measurement_interval_input_analog2))
        pui = listOf(view.findViewById(R.id.bsi_power_on_delay_pulse),
            view.findViewById(R.id.bsi_measurement_interval_pulse),
            view.findViewById(R.id.bsi_power_on_delay_input_pulse),
            view.findViewById(R.id.bsi_measurement_interval_input_pulse))

        //Hide the UI elements by DEFAULT and select false in spinner
        analog1spinner.setSelection(0)
        analog2spinner.setSelection(0)
        pulsespinner.setSelection(0)
        showHide(a1ui, false)
        showHide(a2ui, false)
        showHide(pui, false)
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
    // Spinner Listeners
    //
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (view != null) {
            when (parent?.id) {
                a1spinner?.id -> {
                    if (position == 1) {
                        //Sensor is active, show the UI elements
                        showHide(a1ui, true)
                    }
                    else {
                        showHide(a1ui, false)
                    }
                }

                a2spinner?.id -> {
                    if (position == 1) {
                        //Sensor is active, show the UI elements
                        showHide(a2ui, true)
                    }
                    else {
                        showHide(a2ui, false)
                    }
                }

                pspinner?.id -> {
                    if (position == 1) {
                        //Sensor is active, show the UI elements
                        showHide(pui, true)
                    }
                    else {
                        showHide(pui, false)
                    }
                }
            }

        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //Must be here for abstract error
    }


    //
    // Fragment Interface(s)
    //
    interface BSI {
        fun bsiFragmentContextMover(): Context
        fun bsiObjectMover(): BSIEntry
    }
}