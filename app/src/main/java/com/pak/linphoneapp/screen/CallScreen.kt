package com.pak.linphoneapp.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CallScreen(
    onCall: (String, Boolean) -> Unit, // Accepts callee and isVideoCall flag
    onMessage: (String, String) -> Unit // Message handling
) {
    // State for message input and callee input
    var message by remember { mutableStateOf("") }
    var callee by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TextField for Callee Input
        TextField(
            value = callee,
            onValueChange = { callee = it },
            label = { Text("Enter callee") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            // Buttons for Voice and Video Calls
            Button(onClick = { onCall(callee, false) }) {
                Text("Voice Call")
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(onClick = { onCall(callee, true) }) {
                Text("Video Call")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // TextField for Message Input
        TextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Enter your message") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Button to Send Message
        Button(onClick = {
            onMessage(callee, message) // Call the message handler
            message = "" // Clear the message after sending
        }) {
            Text("Send")
        }
    }
}
@Preview(showBackground = true)
@Composable
fun CallScreenPreview() {
    CallScreen(
        onCall = { _, _ -> },
        onMessage = { _, _ -> })
}