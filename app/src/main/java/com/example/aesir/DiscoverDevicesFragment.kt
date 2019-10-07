package com.example.aesir

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.discover_devices_fragment.*

class DiscoverDevicesFragment : Fragment() {
    private lateinit var listener: OnSearchButtonPressed

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.discover_devices_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        search.setOnClickListener {
            listener.OnButtonPressed()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnSearchButtonPressed) {
            listener = context
        }
        else {
            throw ClassCastException(context.toString() + "must implement OnSearchButtonPressed!")
        }
    }

    // Container Activity must impliment this interface
    interface OnSearchButtonPressed {
        fun OnButtonPressed()
    }
}