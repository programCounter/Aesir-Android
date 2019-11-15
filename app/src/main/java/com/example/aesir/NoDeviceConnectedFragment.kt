/*
File Name: NoDeviceConnectedFragment.kt
Author: Riley Larche
Date Updated: 2019-11-14
Android Studio Version:3.5.1
Tested on Android Version: 10 and 8

Logic that pertains to objects in the view for this fragment is contained here.
 */


//
// Packages and Imports
//
package com.example.aesir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class NoDeviceConnectedFragment: Fragment() {
    //
    // Fragment Functions
    //
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the view for this fragment
        return inflater.inflate(R.layout.setup_fragment_no_device, container, false)
    }
}