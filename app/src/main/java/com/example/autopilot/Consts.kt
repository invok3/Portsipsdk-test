package com.example.autopilot

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File

abstract class Consts(x:Context) {
    private var instance:Context = x

    companion object {

        val sFiles: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File("/storage/emulated/0/autopilot")
            //ContactsActivity.instance.getExternalFilesDir("Data")?:ContactsActivity.instance.filesDir
        } else {
            File("${Environment.getExternalStorageDirectory()}/autopilot")
        }

        const val debugLevel: Int = 3//3210 //0 stop, 1 print, 2 write logs, 3 both
        const val debugLevelGui: Int = 3//3210 //0 stop, 1 print, 2 write logs, 3 both
    }
}

