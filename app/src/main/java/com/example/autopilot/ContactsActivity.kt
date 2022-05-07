package com.example.autopilot


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.size
import androidx.core.widget.addTextChangedListener
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.autopilot.Kore.Companion.STATE_STARTED
import com.example.autopilot.Kore.Companion.clear
import com.example.autopilot.Kore.Companion.logEsGui
import com.example.autopilot.Kore.Companion.logEx
import com.example.autopilot.Kore.Companion.mSharedPreferencesRead
import com.example.autopilot.Kore.Companion.mSharedPreferencesWrite
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import org.json.JSONArray
import java.io.File
import java.io.PrintStream


class ContactsActivity : AppCompatActivity() {

    init {
        try {
            instance = this
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.init")
        }
    }

    private var overloading: Boolean = false
    var mTestNumbers: JSONArray = JSONArray()
    private var mCustomTestNumbers: JSONArray = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_AutoPilot)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contacts_activity)
        if (isScreenSizeSmall()) {
            setContentView(R.layout.contacts_activity_small)
        } else {
            setContentView(R.layout.contacts_activity)
        }

        getTestNumbs()

        val selector = findViewById<RadioGroup>(R.id.list_selector)
        selector.setOnCheckedChangeListener { _, i ->
            try {
                mCustomTestNumbers = JSONArray()
                when (i) {
                    R.id.list_all -> {
                        findViewById<LinearLayout>(R.id.list_custom_selector).visibility = View.GONE
                        mCustomTestNumbers = mTestNumbers
                        findViewById<TextView>(R.id.selected_count).text =
                            mCustomTestNumbers.length().toString()
                        findViewById<LinearLayout>(R.id.test_number_list).removeAllViewsInLayout()
                    }
                    R.id.list_custom -> {
                        findViewById<LinearLayout>(R.id.list_custom_selector).visibility =
                            View.VISIBLE
                        findViewById<TextView>(R.id.selected_count).text =
                            mCustomTestNumbers.length().toString()
                        findViewById<EditText>(R.id.country_code).text =
                            findViewById<EditText>(R.id.country_code).text
                    }
                }
            } catch (ex: Exception) {
                logEx(ex, "ContactsActivity.onCreate.selectorChangedListener")
            }
        }

        findViewById<EditText>(R.id.country_code).addTextChangedListener { text ->
            try {
                //show starts with
                mCustomTestNumbers = JSONArray()
                val y: String = text.toString()
                findViewById<LinearLayout>(R.id.test_number_list).removeAllViewsInLayout()
                for (x in 0 until mTestNumbers.length()) {
                    if (mTestNumbers[x].toString().startsWith(y, true)) {
                        //shows only 200 for performance!
                        if (mCustomTestNumbers.length() < 200) {
                            addTestRow(mTestNumbers[x].toString())
                        }
                        mCustomTestNumbers.put(mTestNumbers[x])
                    }
                }
                findViewById<TextView>(R.id.selected_count).text =
                    mCustomTestNumbers.length().toString()
            } catch (ex: Exception) {
                logEx(ex, "ContactsActivity.onCreate.countryCodeChangedListener")
            }
        }


        /*val selector = findViewById<Spinner>(R.id.selector)
        selector.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.Selector)
        )
        selector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    mAdapterView: AdapterView<*>?,
                    mView: View?,
                    i: Int,
                    l: Long
            ) {
                //println("Selector: i: $i, l: $l")
                findViewById<LinearLayout>(R.id.test_number_list).removeAllViewsInLayout()
                when (i) {
                    0 -> {
                        //show first y
                        val y: Int = try {
                            findViewById<EditText>(R.id.sValue).text.toString().toInt()
                        } catch (ex: Exception) {
                            0
                        }
                        if (mTestNumbers.length() < 1 || y < 1) {
                            return
                        }
                        for (x in 0 until y) {
                            addTestRow(mTestNumbers[x].toString())
                        }
                    }
                    1 -> {
                        //show last y
                        val y: Int = try {
                            findViewById<EditText>(R.id.sValue).text.toString().toInt()
                        } catch (ex: Exception) {
                            0
                        }
                        if (mTestNumbers.length() < 1 || y < 1) {
                            return
                        }
                        for (x in mTestNumbers.length() - y until mTestNumbers.length()) {
                            addTestRow(mTestNumbers[x].toString())
                        }
                    }
                    2 -> {
                        //show starts with
                        mCustomTestNumbers = JSONArray()
                        val y: String = findViewById<EditText>(R.id.sValue).text.toString()
                        for (x in 0 until mTestNumbers.length()) {
                            if (mTestNumbers[x].toString().startsWith(y, true)) {
                                //shows only 200 for performance!
                                if (mCustomTestNumbers.length() < 200) {
                                    addTestRow(mTestNumbers[x].toString())
                                }
                                mCustomTestNumbers.put(mTestNumbers[x])
                            }
                        }
                    }
                    3 -> {
                        //show ends with
                        mCustomTestNumbers = JSONArray()
                        val y: String = findViewById<EditText>(R.id.sValue).text.toString()
                        for (x in 0 until mTestNumbers.length()) {
                            if (mTestNumbers[x].toString().endsWith(y, true)) {
                                //shows only 200 for performance!
                                if (mCustomTestNumbers.length() < 200) {
                                    addTestRow(mTestNumbers[x].toString())
                                }
                                mCustomTestNumbers.put(mTestNumbers[x])
                            }
                        }
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //
            }
        }*/

        instance = this
        Kore.instance = instance.application
        mSharedPreferencesRead =
            Kore.instance!!.getSharedPreferences(application.packageName, MODE_PRIVATE)
        mSharedPreferencesWrite = mSharedPreferencesRead.edit()

        try {
            val mFile =
                File("${Kore.instance!!.getExternalFilesDir(null)!!.absolutePath}/ConnectedNumbers.dat")
            if (mFile.exists()) {
                mFile.readLines().forEach {
                    if (it.length > 1) {
                        addConnectedRow(it.replace("-", ""))
                    }
                }
            }
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.onCreate.loadReachedNumbers")
        }

        try {
            val mFile =
                File("${Kore.instance!!.getExternalFilesDir(null)!!.absolutePath}/SuccessNumbers.dat")
            if (mFile.exists()) {
                mFile.readLines().forEach {
                    if (it.length > 1) {
                        addSuccessRow(it.replace("-", ""))
                    }
                }
            }
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.onCreate.loadReachedNumbers")
        }

        findViewById<Button>(R.id.stopButton).setOnLongClickListener { view ->
            try{
                stop(view)
                Kore.users.values.forEach { user ->
                    user.sessions.forEach { session ->
                        user.core.hangUp(session.id)
                    }
                    user.sessions.clear()
                }
                clear()
                Kore.isFetchingResponse=false
                Kore.shuttingDown= mutableMapOf()
                baseContext.stopService(Intent(baseContext, SipServiceFG::class.java))
                return@setOnLongClickListener true
            }catch (ex:Exception){
                logEx(ex, "StopLongClicked!")
                return@setOnLongClickListener true
            }
        }

    }

    override fun onResume() {
        try {

            super.onResume()
            findViewById<MaterialButtonToggleGroup>(R.id.toggleShow).check(R.id.connected_numbers_count)
            //start up with saved account
            var foundUser=false
            for(sP in mSharedPreferencesRead.all.keys){
                if(sP.contains("user-")){
                    foundUser=true
                    break
                }
            }

            if (foundUser && Kore.users.isEmpty()) {
                Kore.loadFromSharedPreferences()
                //should instead start ManageAccounts
                return
            }
            //first run
            if (Kore.users.isEmpty()) {
                startActivity(Intent(this, EditAccount::class.java))
                return
            }
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.onResume")
        }
    }

    private fun isScreenSizeSmall(): Boolean {
        val screenWidth: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.width()
        } else {
            windowManager.defaultDisplay.width
        }
        return (screenWidth <= 480)
    }

    private fun getTestNumbs() {
        try {
            logEsGui("Fetching Test Numbers..")
//        val rit = findViewById<EditText>(R.id.CLI)
//        val cli: String = if (rit.text.toString() == "") {
//            "http://135.181.153.249/api/access/finder/test_number"
//        } else {
//            rit.text.toString()
//        }
            val cli = resources.getString(R.string.test_numbers_link)

//            var resp:JSONArray? = null
//            Thread{ resp = GeneralApi.getTestNumbers(cli) }.start()
            val queue = Volley.newRequestQueue(this)
            val mStringRequest: StringRequest = object : StringRequest(
                Method.GET, cli,
                Response.Listener { response -> // response
                    logEsGui("Parsing Test Numbers..")
                    mTestNumbers = JSONArray(response)
                    findViewById<TextView>(R.id.test_numbers_count).text =
                        mTestNumbers.length().toString()
                    val mFile = File("${getExternalFilesDir(null)!!.absolutePath}/TestNumbers.dat")
                    if (mFile.exists()) {
                        mFile.delete()
                    }
                    if (!mFile.parentFile!!.exists()) {
                        mFile.parentFile!!.mkdirs()
                    }
                    mFile.writeText(response)
                    logEsGui("Idle..")
                },
                Response.ErrorListener {
                    logEsGui("Error Fetching Test Numbers!")
                    val mMessage = if (it.message!!.contains("135.181.153.249")) {
                        it.message!!.replace("135.181.153.249", "cdr", true)
                    } else {
                        it.message
                    }
                    Toast.makeText(this, mMessage, Toast.LENGTH_LONG).show()
                    logEx(it, "getTestNumbs.VolleyError")
                    it.printStackTrace()
                }
            ) {
                //@Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["User-Agent"] = "OrangeCarrier_IPRN_*"
                    params["Accept"] = "*/*"
                    return params
                }
            }
            queue.add(mStringRequest)

        } catch (ex: Exception) {
            logEsGui("Error Fetching Test Numbers!")
            logEx(ex, "ContactsActivity.getTestNumbs")
        }
    }

    companion object {
        //        fun addBulkRow(cNumb: String) {
//            instance.addReachedRow(cNumb)
//            instance.mReachedNumbers.put(cNumb)
//        }
        lateinit var instance: ContactsActivity
    }

    private fun routeSelectAccount(action: String, view: View) {
        try {
            val mIntent = Intent(this, EditAccount::class.java)
            if (action == "ADD_ACCOUNT") {
                mIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            mIntent.action = action
            startActivity(mIntent)
            cleanCheckedState(view)
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.routeSelectAccount")
        }
    }

    fun pause(view: View) {
        try {

            findViewById<MaterialButtonToggleGroup>(R.id.main_control).check(R.id.pauseButton)
            if (overloading) {
                Toast.makeText(this, "Overloading, Please Wait 3 More Seconds!", Toast.LENGTH_SHORT).show()
                return
            }
            overloading = true
            Thread {
                Thread.sleep(3000)
                overloading = false
            }.start()

            if (Kore.State != Kore.STATE_STARTED) {
                return
            }
            Kore.State = Kore.STATE_PAUSED
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.pause")
        }
    }

    fun play(view: View) {

        try {
            findViewById<MaterialButtonToggleGroup>(R.id.main_control).check(R.id.playButton)
            if (overloading) {
                Toast.makeText(this, "Overloading, Please Wait 3 More Seconds!", Toast.LENGTH_SHORT).show()
                return
            }
            overloading = true
            Thread {
                Thread.sleep(3000)
                overloading = false
            }.start()
            var activeAccounts=false
            for (c in Kore.users.values){
                if (c.isConnected){
                    activeAccounts=true
                    break
                }
            }
            if (!activeAccounts) {
                Toast.makeText(this, "No Connected Accounts Yet!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ManageAccounts::class.java))
                return
            }


            if (Kore.State == Kore.STATE_STARTED) {
                return
            }

            if (Kore.State == Kore.STATE_PAUSED) {
                mCustomTestNumbers = Kore.numbsToTest
            }


            if (mCustomTestNumbers.length() == 0 && mTestNumbers.length() == 0) {
                Toast.makeText(baseContext, "No Test Numbers Selected!", Toast.LENGTH_SHORT).show()
                Kore.State = Kore.STATE_STOPPED
                return
            }

            Kore.State = Kore.STATE_STARTED

            Kore.numbsToTest = if (mCustomTestNumbers.length() == 0) {
                mTestNumbers
            } else {
                mCustomTestNumbers
            }

            if (Kore.startOver) {
                findViewById<LinearLayout>(R.id.success_number_list).removeAllViewsInLayout()
                findViewById<Button>(R.id.success_numbers_count).text = "Success: 0"
                findViewById<LinearLayout>(R.id.connected_number_list).removeAllViewsInLayout()
                findViewById<Button>(R.id.connected_numbers_count).text = "Tests: 0"
            }

            SipServiceFG.start(baseContext)
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.play")
        }

    }


    fun stop(view: View) {
        try {
            findViewById<MaterialButtonToggleGroup>(R.id.main_control).check(R.id.stopButton)
            if (overloading) {
                Toast.makeText(this, "Overloading, Please Wait 3 More Seconds!", Toast.LENGTH_SHORT).show()
                return
            }
            overloading = true
            Thread {
                Thread.sleep(3000)
                overloading = false
            }.start()
            if (Kore.State== STATE_STARTED){
            Kore.State = Kore.STATE_STOPPED}else{SipServiceFG.stop(baseContext)}
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.pause")
        }

    }

    fun toggleAllTestNumbs(view: View) {
        val v: CheckBox = findViewById(R.id.test_number_all_checkbox)
        val mTestNumberList: LinearLayout = findViewById(R.id.test_number_list)
        if (v.isChecked) {
            //enable all
            for (x in 0 until mTestNumberList.size) {
                val y: CheckBox = mTestNumberList[x] as CheckBox
                y.isChecked = true
            }
        } else {
            //disable all
            for (x in 0 until mTestNumberList.size) {
                val y: CheckBox = mTestNumberList[x] as CheckBox
                y.isChecked = false
            }
        }
    }

    private fun cleanCheckedState(view: View) {
        try {
            val v: MaterialButton = view as MaterialButton
            Thread {
                Thread.sleep(2000)
                view.isChecked = false
            }.start()
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.cleanCheckedState")
        }
    }

    private fun addTestRow(num: String) {
        try {
            val tNumber: String = num
            val mCheckBox: CheckBox = LayoutInflater
                .from(this)
                .inflate(R.layout.my_test_number, null) as CheckBox
            mCheckBox.text = tNumber
            findViewById<LinearLayout>(R.id.test_number_list).addView(mCheckBox)
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.addTestRow")
        }

    }

    fun addSuccessRow(tNumber: String) {
        try {
            findViewById<LinearLayout>(R.id.success_number_list).children.forEach {
                val x = it as CheckBox
                if (it.text == tNumber) {
                    return
                }
            }

            val mCheckBox: CheckBox = LayoutInflater
                .from(this)
                .inflate(R.layout.my_reached_number, null) as CheckBox
            mCheckBox.text = tNumber
            mCheckBox.isLongClickable = true
            mCheckBox.setOnLongClickListener(View.OnLongClickListener {
                val itt = it as CheckBox
                val mClipboardManager: ClipboardManager =
                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val mClipData: ClipData = ClipData.newPlainText("Number", itt.text)
                mClipboardManager.setPrimaryClip(mClipData)
                Toast.makeText(this, "${itt.text} was copied to clipboard!", Toast.LENGTH_SHORT)
                    .show()
                return@OnLongClickListener true
            })
            findViewById<LinearLayout>(R.id.success_number_list).addView(mCheckBox)
            val reachedNumbersCount = findViewById<Button>(R.id.success_numbers_count)
            val x: Int = reachedNumbersCount.text.toString().split(" ").last().toInt()
            "Success: ${x + 1}".also { reachedNumbersCount.text = it }
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.addSuccessRow")
        }
    }

    fun addConnectedRow(tNumber: String) {
        try {
            findViewById<LinearLayout>(R.id.connected_number_list).children.forEach {
                val x = it as CheckBox
                if (it.text == tNumber) {
                    return
                }
            }

            val mCheckBox: CheckBox = LayoutInflater
                .from(this)
                .inflate(R.layout.my_reached_number, null) as CheckBox
            mCheckBox.text = tNumber
            mCheckBox.isLongClickable = true
            mCheckBox.setOnLongClickListener(View.OnLongClickListener {
                val itt = it as CheckBox
                val mClipboardManager: ClipboardManager =
                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val mClipData: ClipData = ClipData.newPlainText("Number", itt.text)
                mClipboardManager.setPrimaryClip(mClipData)
                Toast.makeText(this, "${itt.text} was copied to clipboard!", Toast.LENGTH_SHORT)
                    .show()
                return@OnLongClickListener true
            })
            findViewById<LinearLayout>(R.id.connected_number_list).addView(mCheckBox)
            val reachedNumbersCount = findViewById<Button>(R.id.connected_numbers_count)
            val x: Int = reachedNumbersCount.text.toString().split(" ").last().toInt()
            "Tests: ${x + 1}".also { reachedNumbersCount.text = it }
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.addConnectedRow")
        }
    }

    private val saveFileDialogSuccess = registerForActivityResult(ActivityResultContracts.CreateDocument(), ActivityResultCallback {
        it ?: return@ActivityResultCallback
        val mOutputStream = contentResolver.openOutputStream(it)
        val x = PrintStream(mOutputStream)
        x.appendLine("Success Numbers")
        findViewById<LinearLayout>(R.id.success_number_list).children.forEach { itt ->
            val y = itt as CheckBox
            x.appendLine(itt.text)
        }
        x.appendLine(":EndOfFile")
    })

    private val saveFileDialogConnected = registerForActivityResult(ActivityResultContracts.CreateDocument(), ActivityResultCallback {
        it ?: return@ActivityResultCallback
        val mOutputStream = contentResolver.openOutputStream(it)
        val x = PrintStream(mOutputStream)
        x.appendLine("Connected Numbers:")
        findViewById<LinearLayout>(R.id.connected_number_list).children.forEach { itt ->
            val y = itt as CheckBox
            x.appendLine(itt.text)
        }
        x.appendLine(":EndOfFile")
    })


    fun saveLists(view: View) {
        try {
            when (findViewById<MaterialButtonToggleGroup>(R.id.toggleShow).checkedButtonId) {
                R.id.connected_numbers_count -> {
                    if (findViewById<LinearLayout>(R.id.connected_number_list).childCount < 1) {
                        return
                    } else {
                        saveFileDialogConnected.launch("ConnectedNumbers.txt")
                    }
                }
                R.id.success_numbers_count -> {
                    if (findViewById<LinearLayout>(R.id.success_number_list).childCount < 1) {
                        return
                    } else {
                        saveFileDialogSuccess.launch("SuccessNumbers.txt")
                    }
                }
                else -> {

                    return
                }
            }
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.saveReachedList")
        }
    }

    fun clearLists(view: View) {
        try {
            when (findViewById<MaterialButtonToggleGroup>(R.id.toggleShow).checkedButtonId) {
                R.id.connected_numbers_count -> {
                    findViewById<LinearLayout>(R.id.connected_number_list).removeAllViewsInLayout()
                    val mFile =
                        File("${Kore.instance!!.getExternalFilesDir(null)!!.absolutePath}/ConnectedNumbers.dat")
                    if (mFile.exists()) {
                        mFile.delete()
                    }
                    findViewById<Button>(R.id.connected_numbers_count).text = "Tests: 0"
                }
                R.id.success_numbers_count -> {
                    findViewById<LinearLayout>(R.id.success_number_list).removeAllViewsInLayout()
                    val mFile =
                        File("${Kore.instance!!.getExternalFilesDir(null)!!.absolutePath}/SuccessNumbers.dat")
                    if (mFile.exists()) {
                        mFile.delete()
                    }
                    findViewById<Button>(R.id.success_numbers_count).text = "Success: 0"
                }
                else -> {
                    return
                }
            }
        } catch (ex: Exception) {
            logEx(ex, "ContactsActivity.clearReachedList")
        }
    }

    fun addRouteSelectAccount(view: View) {
        routeSelectAccount("ADD_ACCOUNT", view)
    }

    fun manageAccounts(view: View) {
        startActivity(Intent(this, ManageAccounts::class.java))
        cleanCheckedState(view)

    }

    fun reTestNumbs(view: View) {
        getTestNumbs()
    }

    fun dialPad(view: android.view.View) {
        cleanCheckedState(view)
        try {
            val mUser = Kore.users.values.takeWhile { it.isConnected }.first()
        } catch (ex: Exception) {
            Toast.makeText(this, "Connect at least one Account First!", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, PadActivity::class.java))
    }

    fun showTested(view: android.view.View) {
        findViewById<MaterialButtonToggleGroup>(R.id.toggleShow).check(R.id.connected_numbers_count)
        findViewById<ScrollView>(R.id.success_scrollview).visibility = View.GONE
        findViewById<ScrollView>(R.id.connected_scrollview).visibility = View.VISIBLE
    }

    fun showSuccess(view: android.view.View) {
        findViewById<MaterialButtonToggleGroup>(R.id.toggleShow).check(R.id.success_numbers_count)
        findViewById<ScrollView>(R.id.success_scrollview).visibility = View.VISIBLE
        findViewById<ScrollView>(R.id.connected_scrollview).visibility = View.GONE
    }


}