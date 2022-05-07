package com.example.autopilot

import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import com.example.autopilot.Kore.Companion.logEx
import com.portsip.PortSipEnumDefine


class EditAccount : AppCompatActivity() {

    init {
        try {
            instance = this
        } catch (ex: Exception) {
            logEx(ex, "EditAccount.init")
        }
    }


    private var acc: User? = null
    private var mTransportType: Int = PortSipEnumDefine.ENUM_TRANSPORT_UDP
    private var mSRTPMode: Int = PortSipEnumDefine.ENUM_SRTPPOLICY_NONE
    private lateinit var mUser: EditText
    private lateinit var mCallDelay: EditText
    private lateinit var mMaxLines: EditText
    private lateinit var mPassword: EditText
    private lateinit var mSipHost: EditText
    private lateinit var mSipPort: EditText
    private lateinit var mProtocol: RadioGroup
    private lateinit var mOutBound: CheckBox
    private lateinit var mOutBoundHost: EditText
    private lateinit var mOutBoundPort: EditText
    private lateinit var srtp: RadioGroup
    private lateinit var mUserAgent: EditText
    private lateinit var PCMU: CheckBox
    lateinit var PCMA: CheckBox
    lateinit var G729: CheckBox
    lateinit var GSM: CheckBox
    lateinit var iLBC: CheckBox
    lateinit var G722: CheckBox
    lateinit var DTMF: CheckBox
    lateinit var SPEEX: CheckBox
    lateinit var SPEEX_WB: CheckBox
    lateinit var AMR: CheckBox
    lateinit var AMR_WB: CheckBox
    lateinit var ISACWB: CheckBox
    lateinit var ISACSWB: CheckBox
    lateinit var oPus: CheckBox
    lateinit var mLateOffer: CheckBox
    lateinit var PRACK: CheckBox

    //    private lateinit var Max_Line: EditText
//    private lateinit var Call_Delay: EditText
    private lateinit var Dial_Prefix: EditText
    private lateinit var mSaveBtn: Button
    private lateinit var registrationState: TextView


    private var useOutBound: Boolean = false

    companion object {
        var instance: EditAccount? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (isScreenSizeSmall()) {
            setContentView(R.layout.edit_account_small)
        } else {
            setContentView(R.layout.edit_account)
        }

        mUser = findViewById(R.id.mUser)
        mPassword = findViewById(R.id.mPassword)
        mSipHost = findViewById(R.id.mSipHost)
        mSipPort = findViewById(R.id.mSipPort)
        mProtocol = findViewById(R.id.mProtocol)
        mOutBound = findViewById(R.id.useOutBoundCheckBox)
        mOutBoundHost = findViewById(R.id.mOutBoundHost)
        mOutBoundPort = findViewById(R.id.mOutBoundPort)
        srtp = findViewById(R.id.srtp_radio_group)
        mUserAgent = findViewById(R.id.mUserAgent)
        PCMA = findViewById(R.id.PCMA)
        PCMU = findViewById(R.id.PCMU)
        G729 = findViewById(R.id.G729)
        GSM = findViewById(R.id.GSM)
        iLBC = findViewById(R.id.iLBC)
        G722 = findViewById(R.id.G722)
        DTMF = findViewById(R.id.DTMF)
        SPEEX = findViewById(R.id.SPEEX)
        SPEEX_WB = findViewById(R.id.SPEEX_WB)
        AMR = findViewById(R.id.AMR)
        AMR_WB = findViewById(R.id.AMR_WB)
        ISACSWB = findViewById(R.id.ISACSWB)
        ISACWB = findViewById(R.id.ISACWB)
        oPus = findViewById(R.id.Opus)
        mLateOffer = findViewById(R.id.mLateOffer)
        PRACK = findViewById(R.id.PRACK)
        mMaxLines = findViewById(R.id.Max_Line)
        mCallDelay = findViewById(R.id.Call_Delay)
        Dial_Prefix = findViewById(R.id.Dial_Prefix)
        mSaveBtn = findViewById(R.id.mSaveBtn)
        registrationState = findViewById(R.id.registrationState)



        mSaveBtn.setOnClickListener {
            try {
                if (Kore.State == Kore.STATE_STARTED || Kore.State == Kore.STATE_PAUSED) {
                    Toast.makeText(this, "Can't Add, Remove, Connect or Disconnect Accounts While Process is Running!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val mSipHostVal = mSipHost.text.toString()
                if (!isValidHost(mSipHostVal) && !useOutBound) {
                    Toast.makeText(baseContext, "Invalid Host", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val mSipOutBoundHost = mOutBoundHost.text.toString()
                if (useOutBound && !isValidHost(mSipOutBoundHost)){
                    Toast.makeText(baseContext, "Invalid OutBound Host", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!isValidCreds()) {
                    Toast.makeText(baseContext, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val newUser = UserInfo(
                    mUser.text.toString(),
                    mPassword.text.toString(),
                    mSipHostVal,
                    mSipPort.text.toString().toInt(),
                    useOutBound,
                    mOutBoundHost.text.toString(),
                    try {
                        mOutBoundPort.text.toString().toInt()
                    } catch (ex: Exception) {
                        0
                    },
                    mTransportType,
                    mSRTPMode,
                    mUserAgent.text.toString(),
                    DTMF.isChecked,
                    ISACSWB.isChecked,
                    ISACWB.isChecked,
                    G729.isChecked,
                    GSM.isChecked,
                    iLBC.isChecked,
                    oPus.isChecked,
                    G722.isChecked,
                    PCMA.isChecked,
                    PCMU.isChecked,
                    SPEEX.isChecked,
                    SPEEX_WB.isChecked,
                    AMR.isChecked,
                    AMR_WB.isChecked,
                    mLateOffer.isChecked,
                    PRACK.isChecked,
                    mMaxLines.text.toString().toInt(),
                    mCallDelay.text.toString().toLong() * 1000,
                    Dial_Prefix.text.toString()
                )
                Kore.saveToSharedPreferences(newUser)
                Kore.login(newUser)
            } catch (ex: Exception) {
                logEx(ex, "EditAccount.mConnectBtn")
            }
        }

        mPassword.setOnTouchListener { v, event ->
            try {
                if (event.action == MotionEvent.ACTION_UP) {
                    v.performClick()
                    if (event.rawX >= mPassword.right - mPassword.compoundDrawables[2].bounds.width()
                    ) {
                        if (mPassword.transformationMethod == HideReturnsTransformationMethod.getInstance()) {
                            mPassword.transformationMethod =
                                PasswordTransformationMethod.getInstance()
                        } else {
                            mPassword.transformationMethod =
                                HideReturnsTransformationMethod.getInstance()
                        }
                        true
                    }
                }
                false
            } catch (ex: Exception) {
                logEx(ex, "EditAccount.mPasswordTouchListener")
                false
            }
        }


        mProtocol.setOnCheckedChangeListener { radioGroup, i ->
            try {
                when (radioGroup.checkedRadioButtonId) {
                    R.id.udp -> {
                        mTransportType = PortSipEnumDefine.ENUM_TRANSPORT_UDP
                        tlsEnabled(false)
                    }
                    R.id.tcp -> {
                        mTransportType = PortSipEnumDefine.ENUM_TRANSPORT_TCP
                        tlsEnabled(false)
                    }
                    else -> {
                        mTransportType = PortSipEnumDefine.ENUM_TRANSPORT_TLS
                        tlsEnabled(true)
                    }
                }
            } catch (ex: Exception) {
                logEx(ex, "EditAccount.mProtocolChanged")
            }
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

    private fun isValidCreds(): Boolean {
        //valid username
        val tMyUser = mUser.text.toString()
        if (tMyUser.isEmpty() || tMyUser.isBlank() || tMyUser.length < 3) {
            return false
        }
        //valid password
        val tMyPassword = mPassword.text.toString()
        if (tMyPassword.isEmpty() || tMyPassword.isBlank() || tMyPassword.length < 3) {
            return false
        }
        return true
    }



    private fun tlsEnabled(b: Boolean) {
        try {

            srtp.visibility = if (b) {
                findViewById<LinearLayout>(R.id.media_encryption_option).visibility = View.VISIBLE
                View.VISIBLE
            } else {
                findViewById<LinearLayout>(R.id.media_encryption_option).visibility = View.GONE
                View.GONE
            }
            srtp.children.forEach {
                it.isEnabled = b
            }
            findViewById<TextView>(R.id.SRTP_Label).isEnabled = b
        } catch (ex: Exception) {
            logEx(ex, "EditAccount.tlsEnabled")
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
        try {
            acc = when (intent.action) {
                "ADD_ACCOUNT" -> {
                    null
                }
                "EDIT_ACCOUNT" -> {
                    Kore.users[intent.getStringExtra("user")]
                }
                else -> {
                    null
                }
            }
            if (acc != null) {
                val mUserInfo = acc!!.userInfo
                mUser.setText(mUserInfo.Username)
                mPassword.setText(mUserInfo.Password)
                mSipHost.setText(mUserInfo.SipHost)
                mSipPort.setText(mUserInfo.SipPort.toString())
                mOutBound.isChecked = mUserInfo.UseOutBound
                useOutBoundChanged(mOutBound)
                mOutBoundHost.setText(mUserInfo.OutBoundHost)
                mOutBoundPort.setText(mUserInfo.OutBoundPort.toString())
                mTransportType = mUserInfo.TransportType
                mProtocol.check(
                    when (mTransportType) {
                        PortSipEnumDefine.ENUM_TRANSPORT_TCP -> R.id.tcp
                        PortSipEnumDefine.ENUM_TRANSPORT_TLS -> R.id.tls
                        else -> R.id.udp
                    }
                )
                mSRTPMode = mUserInfo.SRTPMode
                srtp.check(
                    when (mSRTPMode) {
                        PortSipEnumDefine.ENUM_SRTPPOLICY_PREFER -> R.id.SRTP_Prefer
                        PortSipEnumDefine.ENUM_SRTPPOLICY_FORCE -> R.id.SRTP_Force
                        else -> R.id.SRTP_none
                    }
                )
                mUserAgent.setText(mUserInfo.UserAgent)
                PCMA.isChecked = mUserInfo.PCMA
                PCMU.isChecked = mUserInfo.PCMU
                G729.isChecked = mUserInfo.G729
                GSM.isChecked = mUserInfo.GSM
                iLBC.isChecked = mUserInfo.iLBC
                oPus.isChecked = mUserInfo.Opus
                G722.isChecked = mUserInfo.G722
                DTMF.isChecked = mUserInfo.DTMF
                SPEEX.isChecked = mUserInfo.Speex
                SPEEX_WB.isChecked = mUserInfo.SpeexWb
                AMR.isChecked = mUserInfo.AMR
                AMR_WB.isChecked = mUserInfo.AMRWb
                ISACSWB.isChecked = mUserInfo.ISACSWB
                ISACWB.isChecked = mUserInfo.ISACWB
                mLateOffer.isChecked = mUserInfo.LateOffer
                PRACK.isChecked = mUserInfo.PRack
                mMaxLines.setText(mUserInfo.MaxLines.toString())
                Dial_Prefix.setText(mUserInfo.DialPrefix)
                mCallDelay.setText((mUserInfo.CallDelay / 1000).toString())
                registrationState.text = acc!!.statusText
            }
        } catch (ex: Exception) {
            logEx(ex, "EditAccount.onResume")
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
    override fun onBackPressed() {
        try {
            if (Kore.users.isEmpty()) {
                finishAffinity()
                return
            }
            super.onBackPressed()
        } catch (ex: Exception) {
            logEx(ex, "EditAccount.onBackPressed")
        }
    }

    fun useOutBoundChanged(view: View) {
        try {
            val mCheckBox: CheckBox = findViewById(R.id.useOutBoundCheckBox)
            if (mCheckBox.isChecked) {
                useOutBound = true
                mOutBoundHost.isEnabled = true
                mOutBoundPort.isEnabled = true
                findViewById<LinearLayout>(R.id.useOutBoundOption).visibility = View.VISIBLE
            } else {
                useOutBound = false
                mOutBoundHost.isEnabled = false
                mOutBoundPort.isEnabled = false

                findViewById<LinearLayout>(R.id.useOutBoundOption).visibility = View.GONE
            }
        } catch (ex: Exception) {
            logEx(ex, "EditAccount.useOutBoundChanged")
        }
    }


    fun toggleAdvancedCodecs(view: View) {
        try {
            val mAdvancedCodecsGrid: GridLayout = findViewById(R.id.advanced_codecs)
            val mToggleAdvancedCodecsButton: CheckBox =
                findViewById(R.id.toggle_advanced_codecs_button)
            for (i in 3 until mAdvancedCodecsGrid.childCount) {
                mAdvancedCodecsGrid.getChildAt(i).also {
                    it.visibility = if (it.visibility == View.VISIBLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
            }
            mToggleAdvancedCodecsButton.also {
                if (it.text.toString() == this.resources.getString(R.string.more)) {
                    it.text = this.getString(R.string.less)
                    val mDrawable =
                        AppCompatResources.getDrawable(this, R.drawable.ic_baseline_expand_less_24)
                    it.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, mDrawable, null)
                } else {
                    it.text = this.getString(R.string.more)
                    val mDrawable =
                        AppCompatResources.getDrawable(this, R.drawable.ic_baseline_expand_more_24)
                    it.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, mDrawable, null)
                }

            }
        } catch (ex: Exception) {
            logEx(ex, "EditAccount.toggleAdvancedCodecs")
        }
    }

    private fun isValidHost(host: String): Boolean {
        try {
            if (!host.contains(".")){return false}
            val splitted = host.split(".")
            if (splitted.size<2){return false}
            splitted.forEach {
                try{
                    if (it.toInt() > 255) {
                        return false
                    }
                }catch (ex:Exception){
                    return@forEach
                }
            }
            return true
        } catch (ex: Exception) {
            logEx(ex, "EditAccount.isValidHost")
            return false
        }
    }

    fun toggleAdvancedSettings(view: View) {
        try {
            val mAdvancedSettingsSection: LinearLayout =
                findViewById(R.id.advanced_settings_section)
            val mView = findViewById<ImageButton>(R.id.toggle_advanced_settings_button)
            mAdvancedSettingsSection.also {
                if (it.visibility == View.GONE) {
                    mView.setImageResource(R.drawable.ic_baseline_expand_less_24)
                    mAdvancedSettingsSection.visibility = View.VISIBLE
                } else {
                    mView.setImageResource(R.drawable.ic_baseline_expand_more_24)
                    mAdvancedSettingsSection.visibility = View.GONE
                }
            }
        } catch (ex: Exception) {
            logEx(ex, "EditAccount.toggleAdvancedSettings")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance=null
    }

    fun updateStatus(username: String, statusText: String?, statusCode: Int) {
        try{
            if ((acc ?: return).userInfo.Username == username) {
                registrationState.text = "$statusText, $statusCode"
            }
        }catch (ex:Exception){
            logEx(ex, "updateStatus" )
        }
    }


}