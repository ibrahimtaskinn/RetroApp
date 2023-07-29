package com.example.retroapp.presentation.retro.chat

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.retroapp.navigation.ROUTE_HOME

@Composable
fun ExitMeetingDialog(
    onDismiss: () -> Unit,
    navController: NavHostController,
    dialogText:String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Toplantıdan Ayrıl")
        },
        text = {
            Text(text = dialogText)
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    navController.navigate(ROUTE_HOME)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "Evet")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(text = "Hayır")
            }
        }
    )
}