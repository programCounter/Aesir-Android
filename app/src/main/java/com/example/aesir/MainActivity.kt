package com.example.aesir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Find and reference items in Activity
        val listView = findViewById<ListView>(R.id.device_list)

        //Initilize Variables
        val arraySize = 100
        val listItems = arrayOfNulls<String>(arraySize)

        //Fill Array
        for(i in 0 until arraySize){
            listItems[i] = "List Item"
        }

        //Create and set simple adapter for listView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        listView.adapter = adapter

    }
}
