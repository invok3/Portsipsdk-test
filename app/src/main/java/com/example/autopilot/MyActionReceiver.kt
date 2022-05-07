package com.example.autopilot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.material.button.MaterialButtonToggleGroup
import java.io.File
import kotlin.Exception

class MyActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        when (intent.action) {
            "ACTION_PAUSE" -> {
                try{
                    if (ContactsActivity.instance != null) {
                        ContactsActivity.instance.findViewById<MaterialButtonToggleGroup>(R.id.main_control).check(R.id.pauseButton)
                    }
                }catch (ex:Exception){
                    Kore.logEx(ex, "BroadcastReceiver.check(pauseButton)")
                }
                if (Kore.State != Kore.STATE_STARTED) {
                    return
                }
                Kore.State = Kore.STATE_PAUSED
            }
            "ACTION_PLAY" -> {
                try{
                    if (ContactsActivity.instance != null) {
                        ContactsActivity.instance.findViewById<MaterialButtonToggleGroup>(R.id.main_control).check(R.id.playButton)
                    }
                }catch (ex:Exception){
                    Kore.logEx(ex, "BroadcastReceiver.check(playButton)")
                }
                if (Kore.State == Kore.STATE_STARTED) {
                    return
                }

                Kore.State = Kore.STATE_STARTED
                SipServiceFG.start(context)

            }
            "ACTION_STOP" -> {
                try{
                    if (ContactsActivity.instance != null) {
                        ContactsActivity.instance.findViewById<MaterialButtonToggleGroup>(R.id.main_control).check(R.id.stopButton)
                    }
                }catch (ex:Exception){
                    Kore.logEx(ex, "BroadcastReceiver.check(stopButton)")
                }
                Kore.State = Kore.STATE_STOPPED
            }
            "ACTION_ADD_SUCCESS" -> {
                if (intent.getStringExtra("number") != null && intent.getStringExtra("number") != "") {
                    ContactsActivity.instance.addSuccessRow(intent.getStringExtra("number")!!)
                    val mFile = File("${Kore.instance!!.getExternalFilesDir(null)!!.absolutePath}/SuccessNumbers.dat")
                    if (mFile.exists()) {
                        mFile.readText().apply {
                            if (!this.contains("-${intent.getStringExtra("number")!!}-\n")) {
                                mFile.appendText(("-${intent.getStringExtra("number")!!}-\n"))
                            }
                        }
                    } else {
                        mFile.parentFile!!.mkdirs()
                        mFile.appendText(("-${intent.getStringExtra("number")!!}-\n"))
                    }
                }
            }

            "ACTION_ADD_CONNECTED" -> {
                if (intent.getStringExtra("number") != null && intent.getStringExtra("number") != "") {
                    ContactsActivity.instance.addConnectedRow(intent.getStringExtra("number")!!)
                    val mFile = File("${Kore.instance!!.getExternalFilesDir(null)!!.absolutePath}/ConnectedNumbers.dat")
                    if (mFile.exists()) {
                        mFile.readText().apply {
                            if (!this.contains("-${intent.getStringExtra("number")!!}-\n")) {
                                mFile.appendText(("-${intent.getStringExtra("number")!!}-\n"))
                            }
                        }
                    } else {
                        mFile.parentFile!!.mkdirs()
                        mFile.appendText(("-${intent.getStringExtra("number")!!}-\n"))
                    }
                }
            }

        }
    }
}