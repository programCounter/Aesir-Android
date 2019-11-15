/*
File Name: Tools.kt
Author: Riley Larche
Date Updated: 2019-11-15
Android Studio Version:
Tested on Android Version: 10

Functions used throughout the application.
 */


//Packages and Imports
package com.example.aesir

import android.content.Context
import android.widget.Toast


class Tools(passedContext: Context?) {
    //Public (Default) variables and values
    val DISCONNECTED: Int = 0
    val CONNECTED: Int = 1


    //Private variables and values
    private val context = passedContext


    //Used classes


    //Functions
    fun showToast(text: String){
        //Shows message through system.
        if (context != null) {
            val toast = Toast.makeText(context.applicationContext, text, Toast.LENGTH_SHORT)
            toast.show()
        }
    }
}