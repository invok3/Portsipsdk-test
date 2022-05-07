package com.example.autopilot

import com.portsip.PortSipEnumDefine
import com.portsip.PortSipSdk
import org.json.JSONObject

class User(
    var core: PortSipSdk,
    var sessions: MutableList<Session>,
    var userInfo: UserInfo,
    var isConnected: Boolean = false,
    var isFree: Boolean = true
) {
    var sipMessage: String? = null
    var statusCode: Int? = null
    var statusText: String? = null
}

class Session(var id: Long, var state: Boolean, var callee: String)

class UserInfo(
    var Username: String,
    var Password: String,
    var SipHost: String,
    var SipPort: Int,
    var UseOutBound: Boolean = false,
    var OutBoundHost: String = "",
    var OutBoundPort: Int = 12345,
    var TransportType: Int = PortSipEnumDefine.ENUM_TRANSPORT_UDP,
    var SRTPMode: Int = PortSipEnumDefine.ENUM_SRTPPOLICY_NONE,
    var UserAgent: String = "AutoPilot",
    var DTMF: Boolean = false,
    var ISACSWB: Boolean = false,
    var ISACWB: Boolean = false,
    var G729: Boolean = true,
    var GSM: Boolean = false,
    var iLBC: Boolean = false,
    var Opus: Boolean = false,
    var G722: Boolean = false,
    var PCMA: Boolean = true,
    var PCMU: Boolean = true,
    var Speex: Boolean = false,
    var SpeexWb: Boolean = false,
    var AMR: Boolean = false,
    var AMRWb: Boolean = false,
    var LateOffer: Boolean = false,
    var PRack: Boolean = false,
    var MaxLines: Int = 8,
    var CallDelay: Long = 3,
    var DialPrefix: String = "+"
) {
    constructor(Json: String) : this(JSONObject(Json))
    constructor(mJSONObject: JSONObject) : this("Username", "Password", "SipHost", 0) {
        this.Username = mJSONObject.getString("Username")
        this.Password = mJSONObject.getString("Password")
        this.SipHost = mJSONObject.getString("SipHost")
        this.SipPort = mJSONObject.getInt("SipPort")
        this.UseOutBound = mJSONObject.getBoolean("UseProxy")
        this.OutBoundHost = mJSONObject.getString("ProxyHost")
        this.OutBoundPort = mJSONObject.getInt("ProxyPort")
        this.TransportType = mJSONObject.getInt("TransportType")
        this.SRTPMode = mJSONObject.getInt("SRTPMode")
        this.UserAgent = mJSONObject.getString("UserAgent")
        this.DTMF = mJSONObject.getBoolean("DTMF")
        this.ISACSWB = mJSONObject.getBoolean("ISACSWB")
        this.ISACWB = mJSONObject.getBoolean("ISACWB")
        this.G729 = mJSONObject.getBoolean("G729")
        this.GSM = mJSONObject.getBoolean("GSM")
        this.iLBC = mJSONObject.getBoolean("iLBC")
        this.Opus = mJSONObject.getBoolean("Opus")
        this.G722 = mJSONObject.getBoolean("G722")
        this.PCMA = mJSONObject.getBoolean("PCMA")
        this.PCMU = mJSONObject.getBoolean("PCMU")
        this.Speex = mJSONObject.getBoolean("Speex")
        this.SpeexWb = mJSONObject.getBoolean("SpeexWb")
        this.AMR = mJSONObject.getBoolean("AMR")
        this.AMRWb = mJSONObject.getBoolean("AMRWb")
        this.LateOffer = mJSONObject.getBoolean("LateOffer")
        this.PRack = mJSONObject.getBoolean("PRack")
        this.MaxLines = mJSONObject.getInt("MaxLines")
        this.CallDelay = mJSONObject.getLong("CallDelay")
        this.DialPrefix = mJSONObject.getString("DialPrefix")
    }

    fun toJsonObject(): JSONObject {
        val mJSONObject = JSONObject()
        mJSONObject.put("Username", Username)
        mJSONObject.put("Password", Password)
        mJSONObject.put("SipHost", SipHost)
        mJSONObject.put("SipPort", SipPort)
        mJSONObject.put("UseProxy", UseOutBound)
        mJSONObject.put("ProxyHost", OutBoundHost)
        mJSONObject.put("ProxyPort", OutBoundPort)
        mJSONObject.put("TransportType", TransportType)
        mJSONObject.put("SRTPMode", SRTPMode)
        mJSONObject.put("UserAgent", UserAgent)
        mJSONObject.put("DTMF", DTMF)
        mJSONObject.put("ISACSWB", ISACSWB)
        mJSONObject.put("ISACWB", ISACWB)
        mJSONObject.put("G729", G729)
        mJSONObject.put("GSM", GSM)
        mJSONObject.put("iLBC", iLBC)
        mJSONObject.put("Opus", Opus)
        mJSONObject.put("G722", G722)
        mJSONObject.put("PCMA", PCMA)
        mJSONObject.put("PCMU", PCMU)
        mJSONObject.put("Speex", Speex)
        mJSONObject.put("SpeexWb", SpeexWb)
        mJSONObject.put("AMR", AMR)
        mJSONObject.put("AMRWb", AMRWb)
        mJSONObject.put("LateOffer", LateOffer)
        mJSONObject.put("PRack", PRack)
        mJSONObject.put("MaxLines", MaxLines)
        mJSONObject.put("CallDelay", CallDelay)
        mJSONObject.put("DialPrefix", DialPrefix)
        return mJSONObject
    }

    override fun toString(): String {
        return this.toJsonObject().toString()
    }

}
