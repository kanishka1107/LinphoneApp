package com.pak.linphoneapp.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.pak.linphoneapp.LinphoneManager
import com.pak.linphoneapp.LinphoneManager.LinphoneListener
import org.linphone.core.Call

@Composable
fun StartScreen() {
    var isRegistered by remember { mutableStateOf(false) }
    var isCallInProgress by remember { mutableStateOf(false) }
    var isIncomingCall by remember { mutableStateOf(false) }

    val linphoneManager = LinphoneManager.getInstance()
    linphoneManager.setListener(object: LinphoneListener {
        override fun onRegistrationSuccess() {
            isRegistered = true
        }

        override fun onRegistrationFailed(message: String) {
            isRegistered = false
        }

        override fun onIncomingCallReceived(call: Call) {
            isIncomingCall = true
        }

        override fun onCallConnected(call: Call) {
            isCallInProgress = true // Accept the call
//            Toast.makeText(this, "Call connected", Toast.LENGTH_SHORT).show()
        }

        override fun onCallEnded(call: Call) {
            isCallInProgress = false // End the call
//            Toast.makeText(this, "Call ended", Toast.LENGTH_SHORT).show()
        }

    })

    isRegistered = linphoneManager.getRegistered()

    if (!isRegistered) {
        RegistrationScreen(onRegister = { user, pass ->
            // Call your registration logic here
            linphoneManager.registerUser(user, pass)
        })
    }
    else if(isCallInProgress) {
        linphoneManager.currentCall?.let {
            CallInProgressScreen(
                it.remoteAddress.username,
                onEndCall = {
                    linphoneManager.endCall(it)
                    isCallInProgress = false // End the call
                }
            )
        }
    }
    else if(isIncomingCall) {
        linphoneManager.currentCall?.let {
            IncomingCallDialog(
                it.remoteAddress.username,
                onAccept = {
                    linphoneManager.acceptCall(it)
                    isCallInProgress = true // Accept the call
                    isIncomingCall = false // Dismiss the dialog
                },
                onDecline = {
                    isIncomingCall = false // Dismiss the dialog
                },
                onDismiss = {
                    isIncomingCall = false // Dismiss the dialog
                }
            )
        }
    }
    else {
        CallScreen(
            onCall = { callee, isVideoCall ->
                linphoneManager.makeCall(callee, isVideoCall)
            },
            onMessage = { callee, message ->
                linphoneManager.sendMessage(callee, message)
            }
        )
    }
}
@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreen()
}