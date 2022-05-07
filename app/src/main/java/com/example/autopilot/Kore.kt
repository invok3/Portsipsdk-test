package com.example.autopilot

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.text.TextUtils
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.portsip.OnPortSIPEvent
import com.portsip.PortSipEnumDefine
import com.portsip.PortSipEnumDefine.ENUM_LOG_LEVEL_DEBUG
import com.portsip.PortSipEnumDefine.ENUM_TRANSPORT_UDP
import com.portsip.PortSipErrorcode
import com.portsip.PortSipSdk
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random


abstract class Kore {

    companion object {
        private const val cdrWaitTime: Long = 2000
        var isFetchingResponse: Boolean = false
         var shuttingDown = mutableMapOf<String, Boolean>()
        var users = mutableMapOf<String, User>()
        private const val INSTANCE_ID: String = "instanceID"
        var isWarningOn: Boolean = false
        lateinit var mSharedPreferencesWrite: SharedPreferences.Editor
        lateinit var mSharedPreferencesRead: SharedPreferences
        var instance: Application? = null

        private var pMap = mutableMapOf<String, Int>()
        var startOver: Boolean = true
        var numbsToTest: JSONArray = JSONArray()
        const val STATE_STOPPED: Int = 0
        const val STATE_STARTED: Int = 1
        const val STATE_PAUSED: Int = 2
        var State: Int = STATE_STOPPED

        fun initSdk(mContext: Context, cUserInfo: UserInfo): PortSipSdk? {
            try {
                val core = PortSipSdk()
                core.CreateCallManager(mContext)
                //core.setOnPortSIPEvent(mOnPortSIPEvent)
                core.setOnPortSIPEvent(object : OnPortSIPEvent {

                    override fun onRegisterSuccess(statusText: String?, statusCode: Int, sipMessage: String?) {
                        val mStatus = if (statusText.isNullOrBlank() || statusText.isEmpty()) {
                            "Registration Success"
                        } else {
                            statusText
                        }
                        users[cUserInfo.Username]!!.statusText = mStatus
                        users[cUserInfo.Username]!!.statusCode = statusCode
                        users[cUserInfo.Username]!!.sipMessage = sipMessage
                        users[cUserInfo.Username]!!.isConnected = true
                        if (EditAccount.instance != null) {
                            EditAccount.instance!!.onBackPressed()
                        }
                        if (ManageAccounts.instance != null) {
                            ManageAccounts.instance!!.updateState(users[cUserInfo.Username]!!)
                        }
                        logEs("Registration Success:$statusCode")
                        logEs("$statusText, $statusCode, $sipMessage")
                        logEs("_______________________________________")
                        logEsGui("Registration Success:$statusCode")
                    }

                    override fun onRegisterFailure(statusText: String?, statusCode: Int, sipMessage: String?) {
                        logEs("Registration Failure:$statusCode")
                        logEs("$statusText, $statusCode, $sipMessage")
                        logEs("_______________________________________")
                        logEsGui("Registration Failure($statusText,$statusCode,$sipMessage)")
                        users[cUserInfo.Username]!!.statusText = statusText
                        users[cUserInfo.Username]!!.statusCode = statusCode
                        users[cUserInfo.Username]!!.sipMessage = sipMessage
                        users[cUserInfo.Username]!!.isConnected = false
                        if (EditAccount.instance != null) {
                            EditAccount.instance!!.updateStatus(cUserInfo.Username, statusText, statusCode)
                        }
                    }

                    override fun onInviteIncoming(
                        sessionId: Long,
                        callerDisplayName: String?,
                        caller: String?,
                        calleeDisplayName: String?,
                        callee: String?,
                        audioCodecNames: String?,
                        videoCodecNames: String?,
                        existsAudio: Boolean,
                        existsVideo: Boolean,
                        sipMessage: String?
                    ) {
                        //TODO("Not yet implemented")
                        logEs("onInviteIncoming")
                        logEs("sessionId: $sessionId, callerDisplayName: $callerDisplayName, caller: $caller, calleeDisplayName: $calleeDisplayName")
                        logEs("callee: $calleeDisplayName, audioCodecNames: $audioCodecNames")
                        logEs("videoCodecNames: $videoCodecNames, existsAudio: $existsAudio, existsVideo: $existsVideo")
                        logEs("sipMessage: $sipMessage")
                        logEs("_______________________________________")
                        logEsGui("Invite Incoming: from $caller")
                        core.rejectCall(sessionId, 0)
                        removeSession(cUserInfo, sessionId)
                    }


                    override fun onInviteTrying(sessionId: Long) {
                        val session: Session = getSessionById(sessionId)
                        val user: User = getsessionOwner(sessionId) ?: return
                        println("onInviteTrying : ${session.callee}")
                        logEs("Trying To call: ${session.callee}, by: ${user.userInfo.Username}, sessionId: $sessionId")
                        logEs("_______________________________________")
                        logEsGui("Trying To call: ${session.callee}, by: ${user.userInfo.Username}, sessionId: $sessionId")
                    }

                    override fun onInviteSessionProgress(
                        sessionId: Long,
                        audioCodecNames: String?,
                        videoCodecNames: String?,
                        existsEarlyMedia: Boolean,
                        existsAudio: Boolean,
                        existsVideo: Boolean,
                        sipMessage: String?
                    ) {
                        val session: Session = getSessionById(sessionId)
                        val user: User = getsessionOwner(sessionId) ?: return
                        println("onInviteSessionProgress : ${session.callee}")
                        //TODO("Not yet implemented")
                        logEs("Call in Progress: Callee: ${session.callee}, Caller: ${user.userInfo.Username}, sessionId: $sessionId")
                        logEs("audioCodecNames: $audioCodecNames, videoCodecNames: $videoCodecNames")
                        logEs("EarlyMedia: $existsEarlyMedia, Audio: $existsAudio, Video: $existsVideo")
                        logEs("sipMessage: $sipMessage")
                        logEs("_______________________________________")
                        logEsGui("Call in Progress: ${session.callee}")
                    }

                    override fun onInviteRinging(sessionId: Long, statusText: String?, statusCode: Int, sipMessage: String?) {
                        val session: Session = getSessionById(sessionId)
                        val user: User = getsessionOwner(sessionId) ?: return
                        println("onInviteRinging : ${session.callee}")
                        //TODO("Not yet implemented")

                        logEs("Ringing: Callee: ${session.callee}, Caller: ${user.userInfo.Username}, sessionId: $sessionId")
                        logEs("sessionId: $sessionId, statusText: $statusText, statusCode: $statusCode, sipMessage: $sipMessage")
                        logEs("_______________________________________")
                        logEsGui("Ringing: : ${session.callee}")
                    }

                    override fun onInviteAnswered(
                        sessionId: Long,
                        callerDisplayName: String?,
                        caller: String?,
                        calleeDisplayName: String?,
                        callee: String?,
                        audioCodecNames: String?,
                        videoCodecNames: String?,
                        existsAudio: Boolean,
                        existsVideo: Boolean,
                        sipMessage: String?
                    ) {
                        val session: Session = getSessionById(sessionId)
                        var user: User = getsessionOwner(sessionId) ?: return
                        println("onInviteAnswered : ${session.callee}")
                        //TODO("Not yet implemented")
                        logEs("InviteAnswered: Callee: $callee, Caller: $caller, sessionId: $sessionId")
                        logEs("audioCodecNames: $audioCodecNames, videoCodecNames: $videoCodecNames, Audio: $existsAudio, Video: $existsVideo")
                        logEs("sipMessage: $sipMessage")
                        logEs("_______________________________________")
                        logEsGui("Invite Answered: : ${session.callee}")
                    }

                    override fun onInviteFailure(sessionId: Long, reason: String?, code: Int, sipMessage: String?) {
                        val session: Session = getSessionById(sessionId)
                        val user: User = getsessionOwner(sessionId) ?: return
                        println("onInviteFailure : ${session.callee}")
                        //TODO("Not yet implemented")
                        logEs("InviteFailure: Callee: ${session.callee}, Caller: ${user.userInfo.Username}, sessionId: $sessionId")
                        logEs("reason: $reason, code: $code, sipMessage: $sipMessage")
                        logEs("_______________________________________")
                        logEsGui("Invite Failure: ${session.callee}")
                        user.core.hangUp(sessionId)
                        removeSession(user.userInfo, sessionId)
                        isFetchingResponse=false
                    }

                    override fun onInviteUpdated(
                        sessionId: Long,
                        audioCodecs: String?,
                        videoCodecs: String?,
                        screenCodecs: String?,
                        existsAudio: Boolean,
                        existsVideo: Boolean,
                        existsScreen: Boolean,
                        sipMessage: String?
                    ) {
                        val session: Session = getSessionById(sessionId)
                        val user: User = getsessionOwner(sessionId) ?: return
                        println("onInviteUpdated : ${session.callee}")
                        //TODO("Not yet implemented")
                        logEs("InviteUpdated: Callee: ${session.callee}, Caller: ${user.userInfo.Username}, sessionId: $sessionId")
                        logEs("audio Codecs: $audioCodecs, video Codecs: $videoCodecs, screen Codecs: $screenCodecs")
                        logEs("Audio: $existsAudio, Video: $existsVideo, Screen: $existsScreen")
                        logEs("sipMessage: $sipMessage")
                        logEs("_______________________________________")
                        logEsGui("Invite Failure: ${session.callee}")
                    }

                    override fun onInviteConnected(sessionId: Long) {
                        val session: Session = getSessionById(sessionId)
                        val user: User = getsessionOwner(sessionId) ?: return
                        println("onInviteConnected : ${session.callee}")
                        //TODO("Not yet implemented")
                        logEs("Connected: Callee: ${session.callee}, Caller: ${user.userInfo.Username}, sessionId: $sessionId")
                        logEs("_______________________________________")
                        logEsGui("Connected: ${session.callee}")
                        user.core.hangUp(sessionId)
                        Thread {
                            try {
                                addToTested(session.callee, user.userInfo.Username)
                                searchInBulks(session.callee, user.userInfo.Username, sessionId, user.userInfo)
                            } catch (ex: Exception) {
                                logEx(ex, "onInviteConnected")
                            }
                        }.start()
                    }

                    override fun onInviteBeginingForward(forwardTo: String?) {
                        println("onInviteBeginingForward")
                        //TODO("Not yet implemented")
                        logEs("Beginning Forward to: $forwardTo.")
                        logEs("_______________________________________")
                        logEsGui("Beginning Forward to: $forwardTo.")
                    }

                    override fun onInviteClosed(sessionId: Long, sipMessage: String?) {
                        val session: Session = getSessionById(sessionId)
                        println("onInviteClosed : ${session.callee}")
                        //TODO("Not yet implemented")
                        logEs("Invite Closed: sessionId: $sessionId, sipMessage: $sipMessage")
                        logEs("_______________________________________")
                        logEsGui("InviteClosed: ${session.callee}")
                    }

                    override fun onDialogStateUpdated(
                        BLFMonitoredUri: String?,
                        BLFDialogState: String?,
                        BLFDialogId: String?,
                        BLFDialogDirection: String?
                    ) {
                        println("onDialogStateUpdated")
                        //TODO("Not yet implemented")
                        logEs("Dialog State Updated: BLFMonitoredUri: $BLFMonitoredUri, BLFDialogState: $BLFDialogState, BLFDialogId: $BLFDialogId, BLFDialogDirection: $BLFDialogDirection")
                        logEs("_______________________________________")
                        logEsGui("Dialog State Updated.")
                    }

                    override fun onRemoteHold(sessionId: Long) {
                        val session: Session = getSessionById(sessionId)
                        println("onRemoteHold : ${session.callee}")
                        //TODO("Not yet implemented")
                        logEs("onRemoteHold: sessionId: $sessionId")
                        logEs("_______________________________________")
                        logEsGui("Remote Hold.")
                        core.hangUp(sessionId)
                        removeSession(cUserInfo, sessionId)
                    }

                    override fun onRemoteUnHold(
                        sessionId: Long,
                        audioCodecNames: String?,
                        videoCodecNames: String?,
                        existsAudio: Boolean,
                        existsVideo: Boolean
                    ) {
                        val session: Session = getSessionById(sessionId)
                        println("onRemoteUnHold : ${session.callee}")
                        //TODO("Not yet implemented")
                        logEs("RemoteUnHold: sessionId: $sessionId, audioCodecNames: $audioCodecNames, videoCodecNames: $videoCodecNames, existsAudio: $existsAudio, existsVideo: $existsVideo")
                        logEs("_______________________________________")
                        logEsGui("Remote UnHold.")
                    }

                    override fun onReceivedRefer(
                        sessionId: Long,
                        referId: Long,
                        to: String?,
                        referFrom: String?,
                        referSipMessage: String?
                    ) {
                        val session: Session = getSessionById(sessionId)
                        println("onReceivedRefer : ${session.callee}")
                        //TODO("Not yet implemented")
                        logEs("ReceivedRefer: sessionId: $sessionId, referId: $referId, to: $to, referFrom: $referFrom")
                        logEs("referSipMessage: $referSipMessage")
                        logEs("_______________________________________")
                        logEsGui("Received Refer.")
                    }

                    override fun onReferAccepted(sessionId: Long) {
                        val session: Session = getSessionById(sessionId)
                        println("onReferAccepted : ${session.callee}")
                        //TODO("Not yet implemented")
                        logEs("ReferAccepted: sessionId: $sessionId")
                        logEs("_______________________________________")
                        logEsGui("Refer Accepted.")
                    }

                    override fun onReferRejected(sessionId: Long, reason: String?, code: Int) {
                        val session: Session = getSessionById(sessionId)
                        println("onReferRejected : ${session.callee}")
                        logEs("onReferRejected: $sessionId, $reason, $code")
                        logEs("_______________________________________")
                        //TODO("Not yet implemented")
                    }

                    override fun onTransferTrying(sessionId: Long) {
                        val session: Session = getSessionById(sessionId)
                        println("onTransferTrying : ${session.callee}")
                        logEs("onTransferTrying: $sessionId")
                        logEs("_______________________________________")
                        //TODO("Not yet implemented")
                    }

                    override fun onTransferRinging(sessionId: Long) {
                        val session: Session = getSessionById(sessionId)
                        println("onTransferRinging : ${session.callee}")
                        logEs("onTransferRinging: $sessionId")
                        logEs("_______________________________________")
                        //TODO("Not yet implemented")
                    }

                    override fun onACTVTransferSuccess(sessionId: Long) {
                        val session: Session = getSessionById(sessionId)
                        println("onACTVTransferSuccess : ${session.callee}")
                        logEs("onACTVTransferSuccess: $sessionId")
                        logEs("_______________________________________")
                        //TODO("Not yet implemented")
                    }

                    override fun onACTVTransferFailure(sessionId: Long, reason: String?, code: Int) {
                        val session: Session = getSessionById(sessionId)
                        println("onACTVTransferFailure : ${session.callee}")
                        logEs("onACTVTransferFailure: $sessionId, $reason, $code")
                        logEs("_______________________________________")
                        //TODO("Not yet implemented")
                    }

                    override fun onReceivedSignaling(sessionId: Long, signaling: String?) {
                        val session: Session = getSessionById(sessionId)
                        println("onReceivedSignaling : ${session.callee}")
                        logEs("onReceivedSignaling: $sessionId, $signaling")
                        logEs("_______________________________________")
                        //TODO("Not yet implemented")
                    }

                    override fun onSendingSignaling(sessionId: Long, signaling: String?) {
                        val session: Session = getSessionById(sessionId)
                        println("onSendingSignaling : ${session.callee}")
                        logEs("onSendingSignaling: $sessionId, $signaling")
                        logEs("_______________________________________")
                        //TODO("Not yet implemented")
                    }

                    override fun onWaitingVoiceMessage(
                        messageAccount: String?,
                        urgentNewMessageCount: Int,
                        urgentOldMessageCount: Int,
                        newMessageCount: Int,
                        oldMessageCount: Int
                    ) {
                        println("onWaitingVoiceMessage")
                        //TODO("Not yet implemented")
                        logEs("onWaitingVoiceMessage")
                        logEs("messageAccount: $messageAccount, urgentNewMessageCount: $urgentNewMessageCount, urgentOldMessageCount: $urgentOldMessageCount")
                        logEs("newMessageCount: $newMessageCount, oldMessageCount: $oldMessageCount")
                        logEs("_______________________________________")
                        //logEsGui("Waiting Voice Message")
                    }

                    override fun onWaitingFaxMessage(
                        messageAccount: String?,
                        urgentNewMessageCount: Int,
                        urgentOldMessageCount: Int,
                        newMessageCount: Int,
                        oldMessageCount: Int
                    ) {
                        println("onWaitingFaxMessage")
                        //TODO("Not yet implemented")
                        logEs("onWaitingFaxMessage")
                        logEs("messageAccount: $messageAccount, urgentNewMessageCount: $urgentNewMessageCount, urgentOldMessageCount: $urgentOldMessageCount")
                        logEs("newMessageCount: $newMessageCount, oldMessageCount: $oldMessageCount")
                        logEs("_______________________________________")
                    }

                    override fun onRecvDtmfTone(sessionId: Long, tone: Int) {
                        val session: Session = getSessionById(sessionId)
                        println("onRecvDtmfTone : ${session.callee}")
                        //TODO("Not yet implemented")
                        logEs("onRecvDtmfTone")
                        logEs("sessionId: $sessionId, tone: $tone")
                        logEs("_______________________________________")
                    }

                    override fun onRecvOptions(optionsMessage: String?) {
                        println("onRecvOptions")
                        //TODO("Not yet implemented")
                        logEs("onRecvOptions: {$optionsMessage}")
                        logEs("_______________________________________")
                    }

                    override fun onRecvInfo(infoMessage: String?) {
                        println("onRecvInfo")
                        //TODO("Not yet implemented")
                        logEs("onRecvInfo: {$infoMessage}")
                        logEs("_______________________________________")
                    }

                    override fun onRecvNotifyOfSubscription(
                        sessionId: Long,
                        notifyMessage: String?,
                        messageData: ByteArray?,
                        messageDataLength: Int
                    ) {
                        val session: Session = getSessionById(sessionId)
                        println("onRecvNotifyOfSubscription : ${session.callee}")
                        //TODO("Not yet implemented")
                    }

                    override fun onPresenceRecvSubscribe(
                        subscribeId: Long,
                        fromDisplayName: String?,
                        from: String?,
                        subject: String?
                    ) {
                        println("onPresenceRecvSubscribe")
                        //TODO("Not yet implemented")
                    }

                    override fun onPresenceOnline(fromDisplayName: String?, from: String?, stateText: String?) {
                        println("onPresenceOnline")
                        //TODO("Not yet implemented")
                    }

                    override fun onPresenceOffline(fromDisplayName: String?, from: String?) {
                        println("onPresenceOffline")
                        //TODO("Not yet implemented")
                    }

                    override fun onRecvMessage(
                        sessionId: Long,
                        mimeType: String?,
                        subMimeType: String?,
                        messageData: ByteArray?,
                        messageDataLength: Int
                    ) {
                        val session: Session = getSessionById(sessionId)
                        println("onRecvMessage : ${session.callee}")
                        //TODO("Not yet implemented")
                    }

                    override fun onRecvOutOfDialogMessage(
                        fromDisplayName: String?,
                        from: String?,
                        toDisplayName: String?,
                        to: String?,
                        mimeType: String?,
                        subMimeType: String?,
                        messageData: ByteArray?,
                        messageDataLengthsipMessage: Int,
                        sipMessage: String?
                    ) {
                        println("onRecvOutOfDialogMessage")
                        //TODO("Not yet implemented")
                    }

                    override fun onSendMessageSuccess(sessionId: Long, messageId: Long, sipMessage: String?) {
                        val session: Session = getSessionById(sessionId)
                        println("onSendMessageSuccess : ${session.callee}")
                        //TODO("Not yet implemented")
                    }

                    override fun onSendMessageFailure(sessionId: Long, messageId: Long, reason: String?, code: Int, sipMessage: String?) {
                        val session: Session = getSessionById(sessionId)
                        println("onSendMessageFailure : ${session.callee}")
                        //TODO("Not yet implemented")
                    }

                    override fun onSendOutOfDialogMessageSuccess(p0: Long, p1: String?, p2: String?, p3: String?, p4: String?, p5: String?) {
                        println("onSendOutOfDialogMessageSuccess")
                        //TODO("Not yet implemented")
                    }

                    override fun onSendOutOfDialogMessageFailure(
                        messageId: Long,
                        fromDisplayName: String?,
                        from: String?,
                        toDisplayName: String?,
                        to: String?,
                        reason: String?,
                        code: Int,
                        sipMessage: String?
                    ) {
                        println("onSendOutOfDialogMessageFailure")
                        //TODO("Not yet implemented")
                    }

                    override fun onSubscriptionFailure(subscribeId: Long, statusCode: Int) {
                        println("onSubscriptionFailure")
                        //TODO("Not yet implemented")
                    }

                    override fun onSubscriptionTerminated(subscribeId: Long) {
                        println("onSubscriptionTerminated")
                        //TODO("Not yet implemented")
                    }

                    override fun onPlayAudioFileFinished(sessionId: Long, fileName: String?) {
                        val session: Session = getSessionById(sessionId)
                        println("onPlayAudioFileFinished : ${session.callee}")
                        //TODO("Not yet implemented")
                    }

                    override fun onPlayVideoFileFinished(sessionId: Long) {
                        val session: Session = getSessionById(sessionId)
                        println("onPlayVideoFileFinished : ${session.callee}")
                        //TODO("Not yet implemented")
                    }

                    override fun onAudioDeviceChanged(audioDevice: PortSipEnumDefine.AudioDevice?, set: Set<PortSipEnumDefine.AudioDevice?>?) {
                        println("onAudioDeviceChanged")
                        ////TODO("Not yet implemented")
                        logEs("AudioDeviceChanged: audioDevice: $audioDevice, set: $set")
                        logEs("_______________________________________")

                    }

                    override fun onRTPPacketCallback(sessionId: Long, mediaType: Int, enum_direction: Int, RTPPacket: ByteArray?, packetSize: Int) {
                        val session: Session = getSessionById(sessionId)
                        println("onRTPPacketCallback : ${session.callee}")
                        //TODO("Not yet implemented")
                    }

                    override fun onAudioRawCallback(
                        sessionId: Long,
                        callbackType: Int,
                        data: ByteArray?,
                        dataLength: Int,
                        samplingFreqHz: Int
                    ) {
                        val session: Session = getSessionById(sessionId)
                        println("onAudioRawCallback : ${session.callee}")
                        //TODO("Not yet implemented")
                    }

                    override fun onVideoRawCallback(l: Long, i: Int, i1: Int, i2: Int, bytes: ByteArray?, i3: Int) {
                        val session: Session = getSessionById(l)
                        println("onVideoRawCallback : ${session.callee}")
                        //TODO("Not yet implemented")
                    }


                })
                val lPort: Int = Random.nextInt(6000) + 5060
                var mResult: Int = core.initialize(
                    ENUM_TRANSPORT_UDP, "0.0.0.0", lPort, ENUM_LOG_LEVEL_DEBUG, "${Consts.sFiles.absolutePath}/Logs",
                    cUserInfo.MaxLines, "UserAgent", 0, 0, "${Consts.sFiles.absolutePath}/Crets", "", false, null
                )
                if (mResult != PortSipErrorcode.ECoreErrorNone) {
                    //TODO warn USer About The Error!
                    return null
                }
                mResult = core.setLicenseKey("LicenseKey")
                when (mResult) {
                    PortSipErrorcode.ECoreWrongLicenseKey -> {
                        //TODO warn USer About The Error!
                        return null
                    }
                    PortSipErrorcode.ECoreTrialVersionLicenseKey -> {}
                }
                core.setInstanceId(getInstanceID())
                return core
            } catch (ex: Exception) {
                logEx(ex, "initSdk")
                return null
            }
        }

        private fun searchInBulks(callee: String, username: String, sessionId: Long, userInfo: UserInfo) {
            //TODO ComebackHere
            try {
                val queue = Volley.newRequestQueue(instance!!)
                val mStringRequest: StringRequest = object : StringRequest(
                    Method.GET, instance!!.resources.getString(R.string.reached_numbers_link),
                    Response.Listener { response -> // response
                        val mBulkNumbs = JSONArray(response)
                        isFetchingResponse=false
                        removeSession(userInfo, sessionId)
                        for (nn in 0 until mBulkNumbs.length()) {
                            if (callee == mBulkNumbs[nn].toString()) {
                                //TODO add to Success
                                val mIntent =
                                    Intent(instance!!.applicationContext, MyActionReceiver::class.java).apply {
                                        action = "ACTION_ADD_SUCCESS"
                                        flags = FLAG_ACTIVITY_NEW_TASK
                                        putExtra("number", "$callee <- $username")
                                    }
                                instance!!.sendBroadcast(mIntent)
                                return@Listener
                            }
                        }
                        println("$callee wasn't in Cdr!")
                    },
                    Response.ErrorListener { error -> // TODO Auto-generated method stub
                        println("had an error fetching cdrlist on $callee")
                        isFetchingResponse=false
                        removeSession(userInfo, sessionId)
                        Toast.makeText(instance, error.message, Toast.LENGTH_LONG).show()
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
                /**
                TODO Sleep Before Fetching From CDR
                Could Be Static
                 best resault at 2000
                 **/
                Thread.sleep(cdrWaitTime)
                queue.add(mStringRequest)
            } catch (ex: Exception) {
                logEx(ex, "Kore.fetchBulks")
            }
        }

        private fun addToTested(callee: String, username: String) {
            val mIntent =
                Intent(instance!!.applicationContext, MyActionReceiver::class.java).apply {
                    action = "ACTION_ADD_CONNECTED"
                    flags = FLAG_ACTIVITY_NEW_TASK
                    putExtra("number", "$callee <- $username")
                }
            instance!!.sendBroadcast(mIntent)
        }

        private fun getsessionOwner(sessionId: Long): User? {
            users.values.forEach {
                it.sessions.forEach { session ->
                    if (session.id == sessionId) {
                        return it
                    }
                }
            }
            return null
        }

        private fun getSessionById(sessionId: Long): Session {
            val mSession = Session(0, false, "000")
            users.values.forEach {
                it.sessions.forEach { session ->
                    if (session.id == sessionId) {
                        return session
                    }
                }
            }
            return mSession
        }

        private fun removeSession(cUserInfo: UserInfo, sessionId: Long) {
            try {
                (users[cUserInfo.Username] ?: return).sessions.removeIf {
                    sessionId == it.id
                }

            } catch (ex: Exception) {
                logEx(ex, "removeSession")
            }
        }

        fun getInstanceID(): String? {
            try {
                var insanceid = mSharedPreferencesRead.getString(INSTANCE_ID, "")
                if (TextUtils.isEmpty(insanceid)) {
                    insanceid = UUID.randomUUID().toString()
                    mSharedPreferencesWrite.putString(INSTANCE_ID, insanceid).apply()
                }
                return insanceid
            } catch (ex: Exception) {
                logEx(ex, "getInstanceID")
                return null
            }
        }

        fun configPreferences(cUserInfo: UserInfo, sdk: PortSipSdk) {
            try {

                val mAduioCodec = listOf(
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_DTMF, cUserInfo.DTMF),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_ISACSWB, cUserInfo.ISACSWB),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_ISACWB, cUserInfo.ISACWB),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_G729, cUserInfo.G729),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_GSM, cUserInfo.GSM),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_ILBC, cUserInfo.iLBC),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_OPUS, cUserInfo.Opus),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_G722, cUserInfo.G722),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_PCMA, cUserInfo.PCMA),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_PCMU, cUserInfo.PCMU),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_SPEEX, cUserInfo.Speex),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_SPEEXWB, cUserInfo.SpeexWb),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_AMR, cUserInfo.AMR),
                    Pair(PortSipEnumDefine.ENUM_AUDIOCODEC_AMRWB, cUserInfo.AMRWb)
                )

                sdk.clearAudioCodec()
                for (codec in mAduioCodec) {
                    if (codec.second == true) {
                        sdk.addAudioCodec(codec.first)
                    }
                }

                println("isAudioCodecEmpty: ${sdk.isAudioCodecEmpty}")

                sdk.clearVideoCodec()
                sdk.addVideoCodec(PortSipEnumDefine.ENUM_VIDEOCODEC_H264)
                sdk.addVideoCodec(PortSipEnumDefine.ENUM_VIDEOCODEC_VP8)
                sdk.addVideoCodec(PortSipEnumDefine.ENUM_VIDEOCODEC_VP9)

                sdk.setVideoNackStatus(true)
                sdk.enableAEC(true)
                sdk.enableAGC(true)
                sdk.enableCNG(true)
                sdk.enableVAD(true)
                sdk.enableANS(false)
                val foward = false
                val fowardBusy = false
                val fowardto = null
                if (foward && !TextUtils.isEmpty(fowardto)) {
                    sdk.enableCallForward(fowardBusy, fowardto)
                }
                sdk.enableReliableProvisional(false)
                val resolution = "CIF"
                var width = 352
                var height = 288
                if (resolution == "QCIF") {
                    width = 176
                    height = 144
                } else if (resolution == "CIF") {
                    width = 352
                    height = 288
                } else if (resolution == "VGA") {
                    width = 640
                    height = 480
                } else if (resolution == "720P") {
                    width = 1280
                    height = 720
                } else if (resolution == "1080P") {
                    width = 1920
                    height = 1080
                }
                sdk.setVideoResolution(width, height)
            } catch (ex: Exception) {
                logEx(ex, "ConfigPreferences")
            }

        }

        fun login(cUserInfo: UserInfo) {
            try {
                val core = initSdk(instance!!, cUserInfo) ?: return

                core.setUser(
                    cUserInfo.Username, "", "", cUserInfo.Password, "", cUserInfo.SipHost, cUserInfo.SipPort,
                    "", 0, if (cUserInfo.UseOutBound) {
                        cUserInfo.OutBoundHost
                    } else {
                        ""
                    }, if (cUserInfo.UseOutBound) {
                        cUserInfo.OutBoundPort
                    } else {
                        0
                    }
                )
                //core.enableAudioManager(true)
                core.setAudioDevice(PortSipEnumDefine.AudioDevice.SPEAKER_PHONE)
                core.setVideoDeviceId(1)
                core.setSrtpPolicy(0)
                configPreferences(cUserInfo, core)
                core.enable3GppTags(false)
                val user = User(core, mutableListOf(), cUserInfo)
                users[cUserInfo.Username] = user
                core.registerServer(90, 0)
            } catch (ex: Exception) {
                logEx(ex, "login")
            }
        }

        fun unregister(username: String) {
            try {
                users[username]!!.sessions.forEach { session ->
                    if (session.state) {
                        users[username]!!.core.hangUp(session.id)
                    }
                }
                users[username]!!.core.unRegisterServer()
            } catch (ex: Exception) {
                logEx(ex, "unregister")
            }
        }

        fun delete(username: String) {
            try {
                unregister(username)
                users[username]!!.core.removeUser()
                users.remove(username)
            } catch (ex: Exception) {
                logEx(ex, "delete")
            }
        }


        fun clear() {
            try {
                mSharedPreferencesRead.all.keys.forEach {
                    if (it.contains("cNumbI-")) {
                        mSharedPreferencesWrite.putInt(it, 0).apply()
                    }
                }
                startOver = true
                mSharedPreferencesWrite.putBoolean("startOver", true).apply()
                numbsToTest = JSONArray()
                val mFile =
                    File("${instance!!.getExternalFilesDir(null)!!.absolutePath}/cTestNumbers.dat")
                if (mFile.exists()) {
                    mFile.delete()
                }
            } catch (ex: Exception) {
                logEx(ex, "clear")
            }
        }

        private fun save(username: String, i: Int) {
            try {
                mSharedPreferencesWrite.putInt("cNumbI-$username", i).apply()
                startOver = false
                mSharedPreferencesWrite.putBoolean("startOver", false).apply()
                val mFile =
                    File("${instance!!.getExternalFilesDir(null)!!.absolutePath}/cTestNumbers.dat")
                if (mFile.exists()) {
                    mFile.delete()
                } else {
                    mFile.parentFile!!.mkdirs()
                }
                mFile.writeText(numbsToTest.toString())
            } catch (ex: Exception) {
                logEx(ex, "save")
            }
        }

        private fun load() {
            try {

                val mFile =
                    File("${instance!!.getExternalFilesDir(null)!!.absolutePath}/cTestNumbers.dat")
                numbsToTest = JSONArray(mFile.readText())

                val y = mutableMapOf<String, Int>()
                mSharedPreferencesRead.all.keys.forEach {
                    if (it.contains("cNumbI-")) {
                        y[it] = mSharedPreferencesRead.getInt(it, 0)
                    }
                }
                pMap = y

            } catch (ex: Exception) {
                logEx(ex, "load")
            }
        }

        fun mMain(bContext: Context) {
            try {
                if (!mSharedPreferencesRead.getBoolean("startOver", true)) {
                    load()
                } else {
                    pMap = mutableMapOf()
                }
                val mFile =
                    File("${instance!!.getExternalFilesDir(null)!!.absolutePath}/cTestNumbers.dat")
                if (mFile.exists()) {
                    mFile.delete()
                }
                if (!mFile.parentFile!!.exists()) {
                    mFile.parentFile!!.mkdirs()
                }
                mFile.writeText(numbsToTest.toString())
                /**  **/
                SipServiceFG.updateNotification(bContext,0,0)
                userFor@ for (user in users) {
                    if (!user.value.isConnected){continue@userFor}
                    Thread {
                        try {
                            val startAt = pMap["cNumbI-${user.value.userInfo.Username}"] ?: 0
                            shuttingDown[user.value.userInfo.Username]=true
                            numberFor@ for (i in startAt until numbsToTest.length()) {
                                while (user.value.sessions.size >= user.value.userInfo.MaxLines) {
                                    /**
                                     * TODO Sleep Between FreeLine Checks
                                     * Static check for free line every 0.3s
                                     */
                                    Thread.sleep(333)
                                }
                                when (State) {
                                    STATE_STOPPED -> {
                                        clear()
                                        break@numberFor
                                    }
                                    STATE_PAUSED -> {
                                        save(user.value.userInfo.Username, i)
                                        break@numberFor
                                    }
                                }
                                //TODO on its Thread Sleeps Before Firing a Call Better not to Flood The Core! Especially at Start!
                                Thread.sleep(333)
                                val sessionId = user.value.core.call("${user.value.userInfo.DialPrefix}${numbsToTest[i]}", true, false)
                                if (sessionId <= 0) {
                                    //cant call
                                } else {
                                    isFetchingResponse=true
                                    //TODO start a thread to kill the call if time elapsed > 5/*chosen by user*/ seconds
                                    Thread {
                                        try {
                                            /**
                                             * a single call can take no more than 10 sec, then it will be terminated regardless!
                                             * but still should wrap the wait time for cdr ie: CallDelay+=cdrWaitTime
                                             */
                                            Thread.sleep(user.value.userInfo.CallDelay + cdrWaitTime)
                                                for (ses in user.value.sessions) {
                                                    if (ses.id == sessionId) {
                                                        user.value.core.hangUp(sessionId)
                                                        removeSession(user.value.userInfo, sessionId)
                                                    }
                                                }
                                        } catch (ex: Exception) {
                                            logEx(ex, "mMain.TimeElapsedOnCall")
                                        }
                                    }.start()
                                    user.value.core.sendVideo(sessionId,false)
                                    user.value.sessions.add(Session(sessionId, false, numbsToTest[i].toString()))
                                }
                            }

                            /**
                             * last account shuts down, Closes the Background Service!
                             */
                            shuttingDown[user.value.userInfo.Username]=true
                            var gShut=true
                            shuttingDown.forEach{
                                if (!it.value){
                                    gShut=false
                                    return@forEach
                                }
                            }
                            if (gShut && State== STATE_STOPPED){
                                while (isFetchingResponse){
                                    Thread.sleep(333)
                                }
                                bContext.stopService(Intent(bContext, SipServiceFG::class.java))
                                logEsGui("Stopped")
                            }
                        } catch (ex: Exception) {
                            logEx(ex, "userFor@mMain")
                        }
                    }.start()
                }


            } catch (ex: Exception) {
                //
            }
        }


        fun logEsGui(logTxt: String) {
            try {
                if (ContactsActivity.instance != null) {
                    ContactsActivity.instance.findViewById<TextView>(R.id.process).text = logTxt
                }
            } catch (ex: Exception) {
                logEx(ex, "ContactsActivity.logEs")
            }
        }

        fun logEs(logTxt: String) {
            try {
                if (Consts.debugLevelGui % 2 == 1) {
                    //odd level = println
                    //logEsGui(logTxt)
                }
                if (Consts.debugLevelGui > 1) {
                    // high level = write to Logs
                    File("${Consts.sFiles.absolutePath}/Logs").mkdirs()
                    val eFile =
                        File("${Consts.sFiles.absolutePath}/Logs/Log_MainFunction.txt")
                    if (!eFile.exists()) {
                        eFile.createNewFile()
                    }
                    eFile.appendText("$logTxt\n")
                }
            } catch (ex: Exception) {

            }
        }

        fun logEx(ex: Exception, method: String) {
            try {
                if (Consts.debugLevel % 2 == 1) {
                    //odd level = println
                    println("$method: ${ex.message}")
                    ex.printStackTrace()
                }
                if (Consts.debugLevel > 1) {
                    // high level = write to Logs
                    writeError(ex)
                }
            } catch (ex: Exception) {
                //
            }


        }

        fun writeError(ex: Exception, info: String = "", method: String = "Unknown") {
            try {
                File("${Consts.sFiles.absolutePath}/Logs").mkdirs()
                val eFile =
                    File("${Consts.sFiles.absolutePath}/Logs/Log_${System.currentTimeMillis()}.txt")
                eFile.writeText("Method: $method\n________________________________\n")
                eFile.writeText("Message: ${ex.message}\n________________________________\n")
                eFile.writeText("JsonBody:\n$info\n________________________________\n")
                eFile.writeText(ex.stackTraceToString())
            } catch (ex: Exception) {
                //
            }
        }

        fun loadFromSharedPreferences() {
            try {
                for (sP in mSharedPreferencesRead.all.keys) {
                    if (!sP.contains("user-")) {
                        continue
                    }
                    val rawString = mSharedPreferencesRead.getString(sP, "")
                    rawString ?: continue
                    val mUserInfo = UserInfo(rawString)
                    login(mUserInfo)
                }
            } catch (ex: Exception) {
                logEx(ex, "loadFromSharedPreferences")
            }
        }

        fun saveToSharedPreferences(cUserInfo: UserInfo) {
            try {
                mSharedPreferencesWrite.putString("user-${cUserInfo.Username}", cUserInfo.toString()).apply()
            } catch (ex: Exception) {
                logEx(ex, "saveToSharedPreferences")
            }
        }

    }
}

