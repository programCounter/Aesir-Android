/*
File Name: DiscoverDevicesFragment.kt
Author: Riley Larche
Date Updated: 2019-10-07
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic that pertains to objects in the view for the
Debug fragment is contained in this file.
 */


package com.example.aesir

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.debug_fragment.*

class DebugFragment : Fragment() {
    private lateinit var listener: DebugListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.debug_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list_services.setOnClickListener {
            listener.debugDataMover()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is DebugListener) {
            listener = context
        }
        else {
            throw ClassCastException(context.toString() + "must implement OnSearchButtonPressed!")
        }
    }

    interface DebugListener {
        fun debugDataMover()
    }
}