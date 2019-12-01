/*
File Name: BSIFragment.kt
Author: Riley Larche
Date Updated: 2019-11-14
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
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.bsi_fragment.*


//
// Setup Fragment Class
//
class BSISetupFragment : Fragment(), AdapterView.OnItemSelectedListener {
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

        // Prepopulate edit text with name of connected device
        val nameInput = view.findViewById<EditText>(R.id.bsi_name_input)
        nameInput.setText(mInterface.bsiNameMover())

        // Set UI elements for the selected BSI
        val title = view.findViewById<TextView>(R.id.setup_bsi_title)
        title.text = mInterface.bsiNameMover()

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
            view.findViewById(R.id.bsi_alarm_shutoff_input_analog2))
        pui = listOf(view.findViewById(R.id.bsi_power_on_delay_pulse),
            view.findViewById(R.id.bsi_measurement_interval_pulse),
            view.findViewById(R.id.bsi_alarm_trigger_input_pulse),
            view.findViewById(R.id.bsi_alarm_shutoff_input_pulse))

        //Hide the UI elements by DEFAULT and select false in spinner
        analog1spinner.setSelection(0)
        analog2spinner.setSelection(0)
        pulsespinner.setSelection(0)
        showHide(a1ui, false)
        showHide(a2ui, false)
        showHide(pui, false)

        // Set button click listener
        setup_bsi_submit.setOnClickListener {
            setup_bsi_submit.text = R.string.setup_submit_changes_alt_text_1.toString()

            // Collect the data from the setup page and place
            // into a bsi object to be sent
            val bsi = BSIObject(bsi_name_input.text.toString())
            try {
                bsi.upldSize = Integer.parseInt(bsi_upld_interval_input.text.toString())
                bsi.txInterval = Integer.parseInt(bsi_tx_interval_input.text.toString())
            }
            catch (e: NumberFormatException) {

            }
            // Safe call and grab state of spinner
            if (a1spinner != null) {
                bsi.a1Enable = a1spinner!!.id
                try {
                    bsi.a1measureint = Integer.parseInt(bsi_measurement_interval_input_analog1.text.toString())
                    bsi.a1pod = Integer.parseInt(bsi_power_on_delay_input_analog1.text.toString())
                    //bsi.a1alarmON = Integer.parseInt()
                    //bsi.a1alarmOFF = Integer.parseInt()
                }
                catch (e: NumberFormatException) {

                }
            }
            if (a2spinner != null) {
                bsi.a2Enable = a2spinner!!.id
                try {
                    bsi.a2measureint = Integer.parseInt(bsi_alarm_shutoff_input_analog2.text.toString())
                    bsi.a2pod = Integer.parseInt(bsi_power_on_delay_input_analog2.text.toString())
                    //bsi.a2alarmON = Integer.parseInt()
                    //bsi.a2alarmOFF = Integer.parseInt()
                }
                catch (e: NumberFormatException) {

                }
            }
            if (pspinner != null) {
                bsi.pEnable = pspinner!!.id
                try {
                    bsi.pAlarmtrigger = Integer.parseInt(bsi_alarm_trigger_input_pulse.text.toString())
                    bsi.pAlarmshutoff = Integer.parseInt(bsi_alarm_shutoff_input_pulse.text.toString())
                }
                catch (e: NumberFormatException) {

                }
            }

            // commit the changes
            mInterface.commitConfig(bsi)
        }

        // Pre-fill UI elements with config data from device
        val existingConfig = mInterface.findConfig()
        setup_bsi_submit.text = this.getText(R.string.load_config)

        bsi_name_input.text = SpannableStringBuilder(existingConfig.name)
        bsi_upld_interval_input.text = SpannableStringBuilder(existingConfig.upldSize.toString())
        bsi_tx_interval_input.text = SpannableStringBuilder(existingConfig.txInterval.toString())

        // Logic for deciding what spinners are true/false
        when (existingConfig.sensorConfig) {
            // P-A1-A2 (XXX)
            1 -> {
                showHide(a2ui, true)
                analog2spinner.setSelection(1)
            }
            10 -> {
                showHide(a1ui, true)
                analog1spinner.setSelection(1)
            }
            11 -> {
                showHide(a1ui, true)
                showHide(a2ui, true)
            }
            100 -> {
                showHide(pui, true)
            }
            101 -> {
                showHide(pui, true)
                showHide(a2ui, true)
            }
            110 -> {
                showHide(pui, true)
                showHide(a1ui, true)
            }
            111 -> {
                showHide(a1ui, true)
                showHide(a2ui, true)
                showHide(pui, true)
            }

            // Populate the senor configuration fields
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
        fun bsiNameMover(): String
        fun findConfig(): BSIObject
        fun commitConfig(bsi: BSIObject)
    }
}


//
// Data Classes
//
data class BSIObject(var name: String) {
    //BSI
    var dateTime: Int = 0
    var txInterval: Int = 0
    var upldSize: Int = 0
    var sensorConfig: Int = 0

    //Sensors
    var a1Enable: Int = 0
    var a1pod: Int = 0
    var a1measureint: Int = 0
    var a1alarmON: Int = 0
    var a1alarmOFF: Int = 0

    var a2Enable: Int = 0
    var a2pod: Int = 0
    var a2measureint: Int = 0
    var a2alarmON: Int = 0
    var a2alarmOFF: Int = 0

    var pEnable: Int = 0
    var pAlarmtrigger: Int = 0
    var pAlarmshutoff: Int = 0
}