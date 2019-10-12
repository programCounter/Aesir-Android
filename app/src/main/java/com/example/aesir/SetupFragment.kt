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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SetupFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.setup_fragment, container, false)
    }

    interface setup {

    }
}