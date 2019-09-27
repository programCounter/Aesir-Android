/*
File Name: Tools.kt
Author: Riley Larche
Date Updated: 2019-09-27
Android Studio Version:
Tested on Android Version: 10

Functions used throughout the application.
 */


//Packages and Imports
package com.example.aesir

import android.content.Context
import android.widget.Toast


class Tools(passedContext: Context) {
    //Public (Default) variables and values


    //Private variables and values
    private val context = passedContext


    //Used classes


    //Functions
    fun showToast(text: String){
        //Shows message through system.
        val toast = Toast.makeText(context.applicationContext, text, Toast.LENGTH_SHORT)
        toast.show()
    }
}