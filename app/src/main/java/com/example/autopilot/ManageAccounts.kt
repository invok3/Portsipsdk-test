package com.example.autopilot

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.google.android.material.button.MaterialButton

class ManageAccounts : AppCompatActivity() {

    init {
        instance = this
    }

    companion object {
        var instance: ManageAccounts? = null
    }

    fun updateState(account: User) {
        val lL = instance!!.findViewById<LinearLayout>(R.id.accounts_list)
        val statid: Int = when (account.statusCode) {
            200 -> {
                android.R.drawable.presence_online
            }
            null -> {
                android.R.drawable.presence_away
            }
            -1 -> {
                android.R.drawable.presence_offline
            }
            0 -> {
                android.R.drawable.presence_invisible
            }
            else -> {
                //progress
                android.R.drawable.presence_busy
            }
        }
        var found = false
        lL.children.forEach {
            it as CheckBox
            if (account.userInfo.Username == it.text) {
                found = true
                it.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    it.compoundDrawablesRelative[0],
                    it.compoundDrawablesRelative[1],
                    ResourcesCompat.getDrawable(
                        instance!!.resources, statid, null
                    ),
                    it.compoundDrawablesRelative[3]
                )
            }
        }
        if (!found) {
            addUserRow(account.userInfo)
        }
    }

    /**
     * {@inheritDoc}
     *
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are *not* resumed.
     */
    override fun onResume() {
        super.onResume()
        refreshList()
        println("Number of Users:${Kore.users.size}")
        findViewById<TextView>(R.id.account_list_count).text = "(${findViewById<LinearLayout>(R.id.accounts_list).childCount})"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_accounts)
        loadFromSharedPreferences()
    }

    private fun refreshList() {
        findViewById<LinearLayout>(R.id.accounts_list).removeAllViews()
        for (user in Kore.users.values){
            addUserRow(user.userInfo)
        }
    }

    private fun loadFromSharedPreferences() {
        try {
            for (sP in Kore.mSharedPreferencesRead.all.keys.takeWhile { it.contains("user-") }) {
                val rawString = Kore.mSharedPreferencesRead.getString(sP, "")
                rawString ?: continue
                val mUserInfo = UserInfo(rawString)
                addUserRow(mUserInfo)
            }
        } catch (ex: Exception) {
            Kore.logEx(ex, "ManageAccounts.loadFromSharedPreferences")
        }
    }

    private fun addUserRow(mUserInfo: UserInfo) {
        try {
            findViewById<LinearLayout>(R.id.accounts_list).children.forEach {
                val x=it as CheckBox
                if (it.text == mUserInfo.Username){return}
            }
            var mCheckBox: CheckBox =
                LayoutInflater.from(this).inflate(R.layout.my_user_row, null) as CheckBox
            mCheckBox.setOnLongClickListener { p0 ->
                val x = p0 as CheckBox
                val mIntent = Intent(instance, EditAccount::class.java)
                mIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                mIntent.action = "EDIT_ACCOUNT"
                mIntent.putExtra("user", x.text.toString())
                startActivity(mIntent)
                true
            }
            var drawableId = android.R.drawable.presence_offline
            if (Kore.users[mUserInfo.Username] != null) {
                drawableId = android.R.drawable.presence_invisible
                drawableId = when(Kore.users[mUserInfo.Username]!!.statusCode){
                    200 -> android.R.drawable.presence_online
                    null -> android.R.drawable.presence_away
                    else -> android.R.drawable.presence_busy
                }
            }
            mCheckBox.text = mUserInfo.Username
            mCheckBox.setCompoundDrawablesRelativeWithIntrinsicBounds(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_person_gray_24, null),
                null,
                ResourcesCompat.getDrawable(
                    resources, drawableId, null
                ),
                null
            )

            mCheckBox.setOnClickListener {
                val x = it as CheckBox
                var doEqual = true
                findViewById<LinearLayout>(R.id.accounts_list).children.forEach { itt ->
                    val y = itt as CheckBox
                    if (itt.isChecked != it.isChecked) {
                        doEqual = false
                    }
                }
                findViewById<CheckBox>(R.id.test_number_all_checkbox).isChecked = if (doEqual) {
                    it.isChecked
                } else {
                    false
                }
            }

            findViewById<LinearLayout>(R.id.accounts_list).addView(mCheckBox)
        } catch (ex: Exception) {
            Kore.logEx(ex, "ManageAccounts.addUserRow")
        }
    }

    fun addNewAccount(view: android.view.View) {
        cleanCheckedState(view)
        try {
            if (Kore.State == Kore.STATE_STARTED || Kore.State == Kore.STATE_PAUSED) {
                Toast.makeText(this, "Can't Add, Remove, Connect or Disconnect Accounts While Process is Running!", Toast.LENGTH_SHORT).show()
                return
            }
            val mIntent = Intent(this, EditAccount::class.java)
            mIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            mIntent.action = "ADD_ACCOUNT"
            startActivity(mIntent)
        } catch (ex: Exception) {
            Kore.logEx(ex, "ManageAccounts.addNewAccount")
        }
    }

    fun deleteAllAccounts(view: android.view.View) {
        try{
            cleanCheckedState(view)
            if (Kore.State == Kore.STATE_STARTED || Kore.State == Kore.STATE_PAUSED) {
                Toast.makeText(this, "Can't Add, Remove, Connect or Disconnect Accounts While Process is Running!", Toast.LENGTH_SHORT).show()
                return
            }
            //delete from view
            findViewById<LinearLayout>(R.id.accounts_list).removeAllViewsInLayout()
            //delete from sharedPreferences
            Kore.mSharedPreferencesWrite.clear().apply()
            //disconnect and delete from Core.accountList
            Kore.users.forEach {
                it.value.sessions.forEach { session ->
                    it.value.core.hangUp(session.id)
                }
                it.value.core.unRegisterServer()
                it.value.core.removeUser()
                Kore.users.remove(it.key)
            }
            findViewById<TextView>(R.id.account_list_count).text = "(${findViewById<LinearLayout>(R.id.accounts_list).childCount})"
        }catch (ex:Exception){
            Kore.logEx(ex, "deleteAllAccounts")
        }
    }

    fun deleteSelected(view: android.view.View) {
        cleanCheckedState(view)
        if (Kore.State == Kore.STATE_STARTED || Kore.State == Kore.STATE_PAUSED) {
            Toast.makeText(this, "Can't Add, Remove, Connect or Disconnect Accounts While Process is Running!", Toast.LENGTH_SHORT).show()
            return
        }
        val lL = findViewById<LinearLayout>(R.id.accounts_list)
        val asd = lL.children.toList() as List<CheckBox>
        asd.forEach {
            if (it.isChecked){

                try {
                    Kore.unregister(it.text.toString())
                } catch (ex: Exception) {
                    Kore.logEx(ex, "ManageAccounts.deleteSelected")
                }
                try {
                    Kore.delete(it.text.toString())
                } catch (ex: Exception) {
                    Kore.logEx(ex, "ManageAccounts.deleteSelected")
                }

                lL.removeView(it)
                Kore.mSharedPreferencesWrite.remove("user-${it.text}").apply()
            }
        }

        findViewById<TextView>(R.id.account_list_count).text = "(${findViewById<LinearLayout>(R.id.accounts_list).childCount})"
    }


    fun toggleAllAccounts(view: android.view.View) {
        val v = view as CheckBox
        findViewById<LinearLayout>(R.id.accounts_list).children.forEach {
            val itt = it as CheckBox
            it.isChecked = v.isChecked
        }
    }

    fun connectSelected(view: android.view.View) {
        try {
            cleanCheckedState(view)
            if (Kore.State == Kore.STATE_STARTED || Kore.State == Kore.STATE_PAUSED) {
                Toast.makeText(this, "Can't Add, Remove, Connect or Disconnect Accounts While Process is Running!", Toast.LENGTH_SHORT).show()
                return
            }
            val lL = findViewById<LinearLayout>(R.id.accounts_list)
            val asd = lL.children.toList() as List<CheckBox>
            asd.takeWhile { it.isChecked }.forEach {
                val mUser = Kore.users[it.text.toString()]
                mUser!!.statusCode=null
                mUser.statusText="Registration InProgress"
                mUser.sipMessage=""
                updateState(mUser)
                mUser.core.registerServer(90, 0)
            }
        } catch (ex: Exception) {
            Kore.logEx(ex, "ManageAccounts.connectSelected")
        }
    }

    fun disconnectSelected(view: android.view.View) {
        try {
            cleanCheckedState(view)
            if (Kore.State == Kore.STATE_STARTED || Kore.State == Kore.STATE_PAUSED) {
                Toast.makeText(this, "Can't Add, Remove, Connect or Disconnect Accounts While Process is Running!", Toast.LENGTH_SHORT).show()
                return
            }
            val lL = findViewById<LinearLayout>(R.id.accounts_list)
            val asd = lL.children.toList() as List<CheckBox>
            for (xyz in asd){
                if (xyz.isChecked){
                    val mUser = Kore.users[xyz.text.toString()]
                    mUser!!.core.unRegisterServer()
                    mUser.isConnected=false
                    mUser.statusCode=0
                    mUser.statusText="Disconnected"
                    mUser.sipMessage=""
                    updateState(mUser)
                }
            }
        } catch (ex: Exception) {
            Kore.logEx(ex, "ManageAccounts.disconnectSelected")
        }
    }

    private fun cleanCheckedState(view: View) {
        try {
            val v: MaterialButton = view as MaterialButton
            Thread {
                Thread.sleep(300)
                view.isChecked = false
            }.start()
        } catch (ex: Exception) {
            Kore.logEx(ex, "ContactsActivity.cleanCheckedState")
        }
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The [OnBackPressedDispatcher][.getOnBackPressedDispatcher] will be given a
     * chance to handle the back button before the default behavior of
     * [android.app.Activity.onBackPressed] is invoked.
     *
     * @see .getOnBackPressedDispatcher
     */
    private var pressedBackOnce = false

    override fun onBackPressed() {
        var acTiveEct: Boolean = false

        Kore.users.values.forEach {
            if (it.isConnected) {
                acTiveEct = true
            }
        }
        if (!acTiveEct) {
            if (pressedBackOnce) {
                finishAffinity()
            } else {
                Toast.makeText(this, "Double Tap Back Button To Exit!", Toast.LENGTH_SHORT).show()
                pressedBackOnce = true
                Thread {
                    Thread.sleep(1000)
                    try {
                        pressedBackOnce = false
                    } catch (ex: Exception) {
                    }
                }.start()
            }
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance=null
    }

}