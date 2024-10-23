package com.pak.linphoneapp.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun IncomingCallDialog(
    caller: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Incoming Call") },
        text = { Text(text = "You have an incoming call from $caller") },
        confirmButton = {
            Button(onClick = {
                onAccept()
            }) {
                Text("Accept")
            }
        },
        dismissButton = {
            Button(onClick = {
                onDecline()
            }) {
                Text("Decline")
            }
        }
    )
}
@Preview(showBackground = true)
@Composable
fun PreviewIncomingCallDialog() {
    IncomingCallDialog(caller = "John Doe", onAccept = {}, onDecline = {}, onDismiss = {})
}
