package com.pak.linphoneapp

import android.annotation.SuppressLint
import android.content.Context
import org.linphone.core.Account
import org.linphone.core.Address
import org.linphone.core.Alert
import org.linphone.core.AudioDevice
import org.linphone.core.AuthInfo
import org.linphone.core.AuthMethod
import org.linphone.core.Call
import org.linphone.core.CallLog
import org.linphone.core.CallStats
import org.linphone.core.ChatMessage
import org.linphone.core.ChatMessageReaction
import org.linphone.core.ChatRoom
import org.linphone.core.Conference
import org.linphone.core.ConferenceInfo
import org.linphone.core.ConfiguringState
import org.linphone.core.Content
import org.linphone.core.Core
import org.linphone.core.CoreException
import org.linphone.core.CoreListener
import org.linphone.core.EcCalibratorStatus
import org.linphone.core.Event
import org.linphone.core.Factory
import org.linphone.core.Friend
import org.linphone.core.FriendList
import org.linphone.core.GlobalState
import org.linphone.core.InfoMessage
import org.linphone.core.PresenceModel
import org.linphone.core.ProxyConfig
import org.linphone.core.PublishState
import org.linphone.core.RegistrationState
import org.linphone.core.SubscriptionState
import org.linphone.core.VersionUpdateCheckResult

class LinphoneManager private constructor(private val context: Context) : CoreListener {
    private lateinit var core: Core
    private var factory = Factory.instance()
    private var ctx = context
    private var listener: LinphoneListener? = null
    var currentCall: Call? = null

    // Define the callback interface
    interface LinphoneListener {
        fun onRegistrationSuccess()
        fun onRegistrationFailed(message: String)
        fun onIncomingCallReceived(call: Call)
        fun onCallConnected(call: Call)
        fun onCallEnded(call: Call)

    }

    fun setListener(listener: LinphoneListener) {
        this.listener = listener
    }

    init {
        setupLinphoneCore()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: LinphoneManager? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = LinphoneManager(context.applicationContext)
            }
        }

        fun getInstance(): LinphoneManager {
            return INSTANCE ?: throw IllegalStateException("LinphoneManager is not initialized")
        }
    }

    private fun setupLinphoneCore() {
        val factory = Factory.instance()
        core = factory.createCore(null, null, ctx)
        core.start()
        core.addListener(this)

        val isRegistered = getRegistered()
        if(isRegistered) {
            val savedUsername = getStoredUsername()
            val savedPassword = getStoredPassword()
            registerUser(savedUsername, savedPassword)
        }
    }

    fun registerUser(username: String, password: String) {
        saveUserCredentials(username, password)

        // Create an identity address for the user
        val sipAddress = "sip:$username@${Constants.ASTERISK_SERVER_IP}"
        val serverAddress = "sip:${Constants.ASTERISK_SERVER_IP}"

        // Create a ProxyConfig with the identity
        val proxyConfig: ProxyConfig = core!!.createProxyConfig()
        proxyConfig.identityAddress = core!!.createAddress(sipAddress)
        proxyConfig.serverAddr = serverAddress
        proxyConfig.isRegisterEnabled = true

        // Set the credentials for authentication
        val authInfo: AuthInfo = factory.createAuthInfo(
            username,  // Username
            null,      // User ID (use null if not needed)
            password,  // Password
            null,      // Ha1 (use null if not needed)
            null,      // Realm (use null if not needed)
            Constants.ASTERISK_SERVER_IP  // Domain or SIP server
        )

        // Add the AuthInfo to the core
        core.addAuthInfo(authInfo)

        // Add the ProxyConfig to the core
        core.addProxyConfig(proxyConfig)

        // Set the created ProxyConfig as the default one
        core.defaultProxyConfig = proxyConfig
    }

    fun makeCall(callee: String, isVideoCall: Boolean) {
        try {
            // Create a call with video enabled
            val callParams = core.createCallParams(null)
            callParams?.isVideoEnabled = isVideoCall // Enable video

            // Make the call
            val call: Call? = callParams?.let { core.inviteWithParams(callee, it) }
            if (call != null) {
                println("Call initiated to $callee")
            } else {
                println("Failed to initiate call.")
            }
        } catch (e: CoreException) {
            e.printStackTrace()
            println("Error while making call: ${e.message}")
        }
    }

    fun sendMessage(address: String, message: String) {
        val linphoneAddress = factory.createAddress("sip:$address@${Constants.ASTERISK_SERVER_IP}")
        val chatRoom = linphoneAddress?.let { core.createChatRoom(it) }
        val msg = chatRoom?.createMessage(message)
        msg?.send()
    }

    fun acceptCall(call: Call?) {
        call?.accept()
    }
    fun rejectCall(call: Call?) {
        call?.terminate()
    }

    fun endCall(call: Call?) {
        call?.terminate()
    }


    fun setRegistered(isRegistered: Boolean) {
        val preferences = context.getSharedPreferences("linphone_prefs", Context.MODE_PRIVATE)
        preferences.edit().putBoolean("isRegistered", isRegistered).apply()
    }

    fun getRegistered(): Boolean {
        val preferences = context.getSharedPreferences("linphone_prefs", Context.MODE_PRIVATE)
        return preferences.getBoolean("isRegistered", false)
    }
    fun saveUserCredentials(username: String, password: String) {
        val preferences = context.getSharedPreferences("linphone_prefs", Context.MODE_PRIVATE)
        preferences.edit().putString("username", username).apply()
        preferences.edit().putString("password", password).apply()
    }

    fun getStoredUsername(): String {
        val preferences = context.getSharedPreferences("linphone_prefs", Context.MODE_PRIVATE)
        return preferences.getString("username", "").toString()
    }
    fun getStoredPassword(): String {
        val preferences = context.getSharedPreferences("linphone_prefs", Context.MODE_PRIVATE)
        return preferences.getString("password", "").toString()
    }

    override fun onGlobalStateChanged(core: Core, state: GlobalState?, message: String) {

    }

    @Deprecated("Deprecated in Java")
    override fun onRegistrationStateChanged(
        core: Core,
        proxyConfig: ProxyConfig,
        state: RegistrationState?,
        message: String
    ) {
        when (state) {
            RegistrationState.Ok -> {
                listener?.onRegistrationSuccess()
                setRegistered(true)
            }
            RegistrationState.Failed -> {
                listener?.onRegistrationFailed(message)
                setRegistered(false)
                saveUserCredentials("", "")
            }
            else -> {

            }
        }
    }

    override fun onConferenceInfoReceived(core: Core, conferenceInfo: ConferenceInfo) {

    }

    override fun onPushNotificationReceived(core: Core, payload: String?) {

    }

    override fun onPreviewDisplayErrorOccurred(core: Core, errorCode: Int) {

    }

    override fun onCallStateChanged(core: Core, call: Call, state: Call.State?, message: String) {
        when (state) {
            Call.State.IncomingReceived -> {
                // Show UI for incoming call
                currentCall = call
                listener?.onIncomingCallReceived(call)
            }
            Call.State.Connected -> {
                // Handle call connected state
                currentCall = call
                listener?.onCallConnected(call);
            }
            Call.State.End -> {
                // Handle call terminated state
                currentCall = null
                listener?.onCallEnded(call)
            }
            else -> {
                // Handle other call states if necessary
            }
        }

    }

    override fun onNotifyPresenceReceived(core: Core, linphoneFriend: Friend) {

    }

    override fun onNotifyPresenceReceivedForUriOrTel(
        core: Core,
        linphoneFriend: Friend,
        uriOrTel: String,
        presenceModel: PresenceModel
    ) {

    }

    override fun onNewSubscriptionRequested(core: Core, linphoneFriend: Friend, url: String) {

    }

    override fun onAuthenticationRequested(core: Core, authInfo: AuthInfo, method: AuthMethod) {

    }

    override fun onCallLogUpdated(core: Core, callLog: CallLog) {

    }

    override fun onCallIdUpdated(core: Core, previousCallId: String, currentCallId: String) {

    }

    override fun onMessageReceived(core: Core, chatRoom: ChatRoom, message: ChatMessage) {

    }

    override fun onNewMessageReaction(
        core: Core,
        chatRoom: ChatRoom,
        message: ChatMessage,
        reaction: ChatMessageReaction
    ) {

    }

    override fun onReactionRemoved(
        core: Core,
        chatRoom: ChatRoom,
        message: ChatMessage,
        address: Address
    ) {

    }

    override fun onMessagesReceived(
        core: Core,
        chatRoom: ChatRoom,
        messages: Array<out ChatMessage>
    ) {

    }

    override fun onMessageSent(core: Core, chatRoom: ChatRoom, message: ChatMessage) {

    }

    override fun onChatRoomSessionStateChanged(
        core: Core,
        chatRoom: ChatRoom,
        state: Call.State?,
        message: String
    ) {

    }

    override fun onChatRoomRead(core: Core, chatRoom: ChatRoom) {

    }

    override fun onMessageReceivedUnableDecrypt(
        core: Core,
        chatRoom: ChatRoom,
        message: ChatMessage
    ) {

    }

    override fun onIsComposingReceived(core: Core, chatRoom: ChatRoom) {

    }

    override fun onDtmfReceived(core: Core, call: Call, dtmf: Int) {

    }

    override fun onReferReceived(core: Core, referTo: String) {

    }

    override fun onCallGoclearAckSent(core: Core, call: Call) {

    }

    override fun onCallEncryptionChanged(
        core: Core,
        call: Call,
        mediaEncryptionEnabled: Boolean,
        authenticationToken: String?
    ) {

    }

    override fun onCallSendMasterKeyChanged(core: Core, call: Call, masterKey: String?) {

    }

    override fun onCallReceiveMasterKeyChanged(core: Core, call: Call, masterKey: String?) {

    }

    override fun onTransferStateChanged(core: Core, transfered: Call, callState: Call.State?) {

    }

    override fun onBuddyInfoUpdated(core: Core, linphoneFriend: Friend) {

    }

    override fun onCallStatsUpdated(core: Core, call: Call, callStats: CallStats) {

    }

    override fun onInfoReceived(core: Core, call: Call, message: InfoMessage) {

    }

    override fun onSubscriptionStateChanged(
        core: Core,
        linphoneEvent: Event,
        state: SubscriptionState?
    ) {

    }

    override fun onNotifySent(core: Core, linphoneEvent: Event, body: Content?) {

    }

    override fun onNotifyReceived(
        core: Core,
        linphoneEvent: Event,
        notifiedEvent: String,
        body: Content?
    ) {

    }

    override fun onSubscribeReceived(
        core: Core,
        linphoneEvent: Event,
        subscribeEvent: String,
        body: Content?
    ) {

    }

    override fun onPublishStateChanged(core: Core, linphoneEvent: Event, state: PublishState?) {

    }

    override fun onPublishReceived(
        core: Core,
        linphoneEvent: Event,
        publishEvent: String,
        body: Content?
    ) {

    }

    override fun onConfiguringStatus(core: Core, status: ConfiguringState?, message: String?) {

    }

    override fun onNetworkReachable(core: Core, reachable: Boolean) {

    }

    override fun onLogCollectionUploadStateChanged(
        core: Core,
        state: Core.LogCollectionUploadState?,
        info: String
    ) {

    }

    override fun onLogCollectionUploadProgressIndication(core: Core, offset: Int, total: Int) {

    }

    override fun onFriendListCreated(core: Core, friendList: FriendList) {

    }

    override fun onFriendListRemoved(core: Core, friendList: FriendList) {

    }

    override fun onCallCreated(core: Core, call: Call) {

    }

    override fun onVersionUpdateCheckResultReceived(
        core: Core,
        result: VersionUpdateCheckResult,
        version: String?,
        url: String?
    ) {

    }

    override fun onConferenceStateChanged(
        core: Core,
        conference: Conference,
        state: Conference.State?
    ) {

    }

    override fun onChatRoomStateChanged(core: Core, chatRoom: ChatRoom, state: ChatRoom.State?) {

    }

    override fun onChatRoomSubjectChanged(core: Core, chatRoom: ChatRoom) {

    }

    override fun onChatRoomEphemeralMessageDeleted(core: Core, chatRoom: ChatRoom) {

    }

    override fun onImeeUserRegistration(core: Core, status: Boolean, userId: String, info: String) {

    }

    override fun onQrcodeFound(core: Core, result: String?) {

    }

    override fun onFirstCallStarted(core: Core) {

    }

    override fun onLastCallEnded(core: Core) {

    }

    override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {

    }

    override fun onAudioDevicesListUpdated(core: Core) {

    }

    override fun onEcCalibrationResult(core: Core, status: EcCalibratorStatus?, delayMs: Int) {

    }

    override fun onEcCalibrationAudioInit(core: Core) {

    }

    override fun onEcCalibrationAudioUninit(core: Core) {

    }

    override fun onAccountRegistrationStateChanged(
        core: Core,
        account: Account,
        state: RegistrationState?,
        message: String
    ) {

    }

    override fun onDefaultAccountChanged(core: Core, account: Account?) {

    }

    override fun onAccountAdded(core: Core, account: Account) {

    }

    override fun onAccountRemoved(core: Core, account: Account) {

    }

    override fun onNewAlertTriggered(core: Core, alert: Alert) {

    }

    // Other methods...
}
