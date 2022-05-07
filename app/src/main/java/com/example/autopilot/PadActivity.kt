package com.example.autopilot

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.portsip.OnPortSIPEvent
import com.portsip.PortSipEnumDefine
import com.portsip.PortSipSdk

class PadActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {
    private var mCustomcore: PortSipSdk? = null
    private var mUser: User? = null
    private var sId: Long? = null
    private var isCalling: Boolean = false
    private lateinit var mNumbField: EditText
    private lateinit var mDialerStatusField: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isScreenSizeSmall()) {
            setContentView(R.layout.pad_activity_small)
        } else {
            setContentView(R.layout.pad_activity)
        }


        try {
            mUser = Kore.users.values.takeWhile { it.isConnected }.first()
        } catch (ex: Exception) {
            return
        }
        mUser = Kore.users.values.takeWhile { it.isConnected }.first()
        mCustomcore = Kore.initSdk(this, mUser!!.userInfo)
        mCustomcore!!.setOnPortSIPEvent(object : OnPortSIPEvent {
            override fun onRegisterSuccess(p0: String?, p1: Int, p2: String?) {
            }

            override fun onRegisterFailure(p0: String?, p1: Int, p2: String?) {
            }

            override fun onInviteIncoming(
                p0: Long,
                p1: String?,
                p2: String?,
                p3: String?,
                p4: String?,
                p5: String?,
                p6: String?,
                p7: Boolean,
                p8: Boolean,
                p9: String?
            ) {
            }

            override fun onInviteTrying(p0: Long) {
                dialerLogEs("onInvite Trying..")
            }

            override fun onInviteSessionProgress(p0: Long, p1: String?, p2: String?, p3: Boolean, p4: Boolean, p5: Boolean, p6: String?) {
                dialerLogEs("Session Progress..")
            }

            override fun onInviteRinging(p0: Long, p1: String?, p2: Int, p3: String?) {
                dialerLogEs("Ringing..")
            }

            override fun onInviteAnswered(
                p0: Long,
                p1: String?,
                p2: String?,
                p3: String?,
                p4: String?,
                p5: String?,
                p6: String?,
                p7: Boolean,
                p8: Boolean,
                p9: String?
            ) {
                dialerLogEs("Answered..")
            }

            override fun onInviteFailure(p0: Long, p1: String?, p2: Int, p3: String?) {
                dialerLogEs("Failed: $p1, $p2")
            }

            override fun onInviteUpdated(p0: Long, p1: String?, p2: String?, p3: String?, p4: Boolean, p5: Boolean, p6: Boolean, p7: String?) {
            }

            override fun onInviteConnected(p0: Long) {
                dialerLogEs("Connected.")
            }

            override fun onInviteBeginingForward(p0: String?) {
                dialerLogEs("Beginning Forward")
            }

            override fun onInviteClosed(p0: Long, p1: String?) {
                dialerLogEs("Closed!")
            }

            override fun onDialogStateUpdated(p0: String?, p1: String?, p2: String?, p3: String?) {

            }

            override fun onRemoteHold(p0: Long) {
                dialerLogEs("Remote Hold!")
            }

            override fun onRemoteUnHold(p0: Long, p1: String?, p2: String?, p3: Boolean, p4: Boolean) {
                dialerLogEs("Remote UnHold!")
            }

            override fun onReceivedRefer(p0: Long, p1: Long, p2: String?, p3: String?, p4: String?) {
                dialerLogEs("Received Refer")
            }

            override fun onReferAccepted(p0: Long) {
                dialerLogEs("Refer Accepted")
            }

            override fun onReferRejected(p0: Long, p1: String?, p2: Int) {
                dialerLogEs("Refer Rejected")
            }

            override fun onTransferTrying(p0: Long) {
                dialerLogEs("Transfer Trying")
            }

            override fun onTransferRinging(p0: Long) {
                dialerLogEs("Transfer Ringing")
            }

            override fun onACTVTransferSuccess(p0: Long) {
                dialerLogEs("ACTV Transfer Success")
            }

            override fun onACTVTransferFailure(p0: Long, p1: String?, p2: Int) {
                dialerLogEs("ACTV Transfer Failure")
            }

            override fun onReceivedSignaling(p0: Long, p1: String?) {
                dialerLogEs("Received Signaling")
            }

            override fun onSendingSignaling(p0: Long, p1: String?) {
                dialerLogEs("Sending Signaling")
            }

            override fun onWaitingVoiceMessage(p0: String?, p1: Int, p2: Int, p3: Int, p4: Int) {
            }

            override fun onWaitingFaxMessage(p0: String?, p1: Int, p2: Int, p3: Int, p4: Int) {
            }

            override fun onRecvDtmfTone(p0: Long, p1: Int) {
            }

            override fun onRecvOptions(p0: String?) {
            }

            override fun onRecvInfo(p0: String?) {
            }

            override fun onRecvNotifyOfSubscription(p0: Long, p1: String?, p2: ByteArray?, p3: Int) {
            }

            override fun onPresenceRecvSubscribe(p0: Long, p1: String?, p2: String?, p3: String?) {
            }

            override fun onPresenceOnline(p0: String?, p1: String?, p2: String?) {
            }

            override fun onPresenceOffline(p0: String?, p1: String?) {
            }

            override fun onRecvMessage(p0: Long, p1: String?, p2: String?, p3: ByteArray?, p4: Int) {
            }

            override fun onRecvOutOfDialogMessage(
                p0: String?,
                p1: String?,
                p2: String?,
                p3: String?,
                p4: String?,
                p5: String?,
                p6: ByteArray?,
                p7: Int,
                p8: String?
            ) {
            }

            override fun onSendMessageSuccess(p0: Long, p1: Long, p2: String?) {
            }

            override fun onSendMessageFailure(p0: Long, p1: Long, p2: String?, p3: Int, p4: String?) {
            }

            override fun onSendOutOfDialogMessageSuccess(p0: Long, p1: String?, p2: String?, p3: String?, p4: String?, p5: String?) {

            }

            override fun onSendOutOfDialogMessageFailure(
                p0: Long,
                p1: String?,
                p2: String?,
                p3: String?,
                p4: String?,
                p5: String?,
                p6: Int,
                p7: String?
            ) {
            }

            override fun onSubscriptionFailure(p0: Long, p1: Int) {
            }

            override fun onSubscriptionTerminated(p0: Long) {
            }

            override fun onPlayAudioFileFinished(p0: Long, p1: String?) {
            }

            override fun onPlayVideoFileFinished(p0: Long) {
            }

            override fun onAudioDeviceChanged(p0: PortSipEnumDefine.AudioDevice?, p1: MutableSet<PortSipEnumDefine.AudioDevice>?) {
            }

            override fun onRTPPacketCallback(p0: Long, p1: Int, p2: Int, p3: ByteArray?, p4: Int) {
            }

            override fun onAudioRawCallback(p0: Long, p1: Int, p2: ByteArray?, p3: Int, p4: Int) {
            }

            override fun onVideoRawCallback(p0: Long, p1: Int, p2: Int, p3: Int, p4: ByteArray?, p5: Int) {
            }
        })
        mCustomcore!!.setUser(
            mUser!!.userInfo.Username, "", "", mUser!!.userInfo.Password, "", mUser!!.userInfo.SipHost, mUser!!.userInfo.SipPort,
            "", 0, "", 0
        )
        //core.enableAudioManager(true)
        mCustomcore!!.setAudioDevice(PortSipEnumDefine.AudioDevice.SPEAKER_PHONE)
        mCustomcore!!.setVideoDeviceId(1)
        mCustomcore!!.setSrtpPolicy(0)
        Kore.configPreferences(mUser!!.userInfo, mCustomcore!!)
        mCustomcore!!.enable3GppTags(false)
        var user = User(mCustomcore!!, mutableListOf(), mUser!!.userInfo)
        Kore.users[mUser!!.userInfo.Username] = user
        mCustomcore!!.registerServer(90, 0)



        findViewById<Button>(R.id.btn1).setOnClickListener(this)
        findViewById<Button>(R.id.btn1).setOnLongClickListener(this)

        findViewById<Button>(R.id.btn2).setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawText("ABC"))
        findViewById<Button>(R.id.btn2).setOnClickListener(this)
        findViewById<Button>(R.id.btn3).setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawText("DEF"))
        findViewById<Button>(R.id.btn3).setOnClickListener(this)
        findViewById<Button>(R.id.btn4).setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawText("GHI"))
        findViewById<Button>(R.id.btn4).setOnClickListener(this)
        findViewById<Button>(R.id.btn5).setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawText("JKL"))
        findViewById<Button>(R.id.btn5).setOnClickListener(this)
        findViewById<Button>(R.id.btn6).setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawText("MNO"))
        findViewById<Button>(R.id.btn6).setOnClickListener(this)
        findViewById<Button>(R.id.btn7).setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawText("PQRS"))
        findViewById<Button>(R.id.btn7).setOnClickListener(this)
        findViewById<Button>(R.id.btn8).setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawText("TUV"))
        findViewById<Button>(R.id.btn8).setOnClickListener(this)
        findViewById<Button>(R.id.btn9).setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawText("WXYZ"))
        findViewById<Button>(R.id.btn9).setOnClickListener(this)
        findViewById<Button>(R.id.btn0).setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawText("+"))
        findViewById<Button>(R.id.btn0).setOnClickListener(this)
        findViewById<Button>(R.id.btn0).setOnLongClickListener(this)

        findViewById<Button>(R.id.btnT).setOnClickListener(this)
        findViewById<Button>(R.id.btnS).setOnClickListener(this)
        findViewById<ImageButton>(R.id.btn_erase).setOnClickListener(this)
        findViewById<ImageButton>(R.id.btn_erase).setOnLongClickListener(this)
        findViewById<ImageButton>(R.id.btn_voice_call).setOnClickListener(this)
        findViewById<ImageButton>(R.id.btn_video_call).setOnClickListener(this)

    }

    private fun dialerLogEs(s: String) {
        mDialerStatusField.text = s
    }

    override fun onResume() {
        super.onResume()
        mNumbField = findViewById(R.id.numb_field)
        mNumbField.showSoftInputOnFocus = false
        mDialerStatusField = findViewById(R.id.dialer_status)
    }

    override fun onDestroy() {
        super.onDestroy()
        Kore.login(mUser!!.userInfo)
        mCustomcore = null
        mUser = null
    }


    private fun isScreenSizeSmall(): Boolean {
        val screenWidth: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.width()
        } else {
            windowManager.defaultDisplay.width
        }
        return (screenWidth <= 480)
    }


    private fun drawText(text: String) = object : Drawable() {
        val paint = Paint().apply {
            this.color = Color.GRAY
            this.textSize = if (isScreenSizeSmall()) {
                8f
            } else {
                32f
            }
            this.isAntiAlias = true
            this.isFakeBoldText = true
            //this.setShadowLayer(6f, 0f, 0f, Color.BLACK)
            this.style = Paint.Style.FILL
            this.textAlign = Paint.Align.CENTER
        }


        override fun draw(canvas: Canvas) {
            canvas.drawText(
                text, 0F, if (isScreenSizeSmall()) {
                    2F
                } else {
                    8F
                }, paint
            )
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSLUCENT
        }

    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_erase -> {
                mNumbField.setText(mNumbField.text.dropLast(1))
            }
            R.id.btn_video_call -> {}
            R.id.btn_voice_call -> {
                //docall
                doCallHang()
            }
            else -> {
                mNumbField.append(
                    when (v.id) {
                        R.id.btn1 -> {
                            "1"
                        }
                        R.id.btn2 -> {
                            "2"
                        }
                        R.id.btn3 -> {
                            "3"
                        }
                        R.id.btn4 -> {
                            "4"
                        }
                        R.id.btn5 -> {
                            "5"
                        }
                        R.id.btn6 -> {
                            "6"
                        }
                        R.id.btn7 -> {
                            "7"
                        }
                        R.id.btn8 -> {
                            "8"
                        }
                        R.id.btn9 -> {
                            "9"
                        }
                        R.id.btn0 -> {
                            "0"
                        }
                        R.id.btnS -> {
                            "#"
                        }
                        else -> {
                            "*"
                        }
                    }
                )
            }
        }
    }

    private fun doCallHang() {
        //14035011553
        if (isCalling) {
            //hangup
            mCustomcore!!.hangUp(sId!!)
            findViewById<ImageButton>(R.id.btn_voice_call).setImageResource(R.drawable.ic_baseline_call_24)
            isCalling = false
        } else {
            //call
            if (mUser!!.core.isAudioCodecEmpty) {
                println("isAudioCodecEmpty")
                return
            }

            sId = mCustomcore!!.call("${findViewById<EditText>(R.id.numb_field).text}", false, false)
            if (sId!! <= 0) {
                println("Failed to call ${findViewById<EditText>(R.id.numb_field).text}:$sId")
                return
            } else {
                println("Calling ${findViewById<EditText>(R.id.numb_field).text}:$sId")
            }
            mUser!!.core.sendVideo(sId!!, false)
            findViewById<ImageButton>(R.id.btn_voice_call).setImageResource(R.drawable.ic_baseline_call_end_24)
            isCalling = true
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when (v!!.id) {
            R.id.btn1 -> {
                Toast.makeText(this, "No Voicemail Added!", Toast.LENGTH_SHORT).show()
            }
            R.id.btn0 -> {
                findViewById<EditText>(R.id.numb_field).append("+")
            }
            R.id.btn_erase -> {
                findViewById<EditText>(R.id.numb_field).text.clear()
            }

        }
        return true
    }
}