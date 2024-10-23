package com.pak.linphoneapp.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CallInProgressScreen(caller: String?, onEndCall: () -> Unit) {
    // Layout for the ongoing call screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Call in Progress", style = MaterialTheme.typography.headlineSmall)

            Text(text = "Callee: $caller", style = MaterialTheme.typography.headlineMedium)
        }

        // End Call Button
        Button(
            onClick = {
                onEndCall() // Callback to end the call
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp) // 64 dp above the bottom
        ) {
            Text("End Call")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CallInProgressScreenPreview() {
    CallInProgressScreen(caller = "John Doe", onEndCall = {})
}


