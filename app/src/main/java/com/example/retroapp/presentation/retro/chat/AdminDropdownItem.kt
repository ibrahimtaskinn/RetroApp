package com.example.retroapp.presentation.retro.chat

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.retroapp.R
import com.example.retroapp.presentation.ui.theme.DarkBlue
import com.example.retroapp.presentation.ui.theme.LightBlue
import com.example.retroapp.presentation.ui.theme.Yellow

@Composable
fun AdminDropdownItem(
    mDisplayMenu: MutableState<Boolean>,
    navController: NavHostController,
    chatViewModel: ChatViewModel
) {
    val (dialogType, setDialogType) = remember { mutableStateOf("") }
    val (showDialog, setShowDialog) = remember { mutableStateOf(false) }
    val (inputTime, setInputTime) = remember { mutableStateOf("") }
    val dialogText = stringResource(id = R.string.toplantiyi_sonlandirmaya_emin_misin)


    DropdownMenu(
        expanded = mDisplayMenu.value,
        onDismissRequest = { mDisplayMenu.value = false },
        Modifier.background(Color.White),
    ) {

        DropdownMenuItem(
            onClick = {
                mDisplayMenu.value = false
                setDialogType("extend")
                setShowDialog(true)
            },
            text = { GetText(R.string.toplanti_suresi_güncelle) },
        )
        DropdownMenuItem(
            onClick = {
                mDisplayMenu.value = false
                setDialogType("end")
                setShowDialog(true)
            },
            text = { GetText(R.string.toplantiyi_sonlandir) },
        )
    }

    if (showDialog) {
        when (dialogType) {
            "extend", "reduce" -> CustomAlertDialog(
                chatViewModel = chatViewModel,
                inputTime = inputTime,
                setInputTime =setInputTime ,
                setShowDialog =setShowDialog )
            "end" -> ExitMeetingDialog(
                onDismiss = { setShowDialog(false) },
                chatViewModel,
                navController = navController,
                dialogText = dialogText,
                true
            )
        }
    }
}

@Composable
private fun GetText(typeString: Int) {
    Text(
        text = stringResource(id = typeString),
        fontSize = 16.sp,
        style = TextStyle.Default
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAlertDialog(chatViewModel: ChatViewModel,
    inputTime:String,
    setInputTime:(String)->Unit,setShowDialog:(Boolean)->Unit,
) {
    val contextForToast = LocalContext.current.applicationContext
    Dialog(
        onDismissRequest =  {setShowDialog(false)},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
       )
     {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.alertdialog_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                    Text(
                        text =stringResource(id = R.string.sureyi_guncelle),
                        color = Color.White,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Start
                    )
                Spacer(modifier = Modifier.height(14.dp))
                 TextField(
                        modifier=Modifier.background(LightBlue),colors = TextFieldDefaults.outlinedTextFieldColors( textColor = Color.Black, placeholderColor = Color.Gray, cursorColor = DarkBlue, focusedBorderColor = DarkBlue, unfocusedBorderColor = Color.Gray),
                        value = inputTime,
                        onValueChange = { setInputTime(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(stringResource(id = R.string.sure_dakika))},
                    )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier
                            .border(1.dp, Yellow, shape = RoundedCornerShape(size = 40.dp))
                            .size(115.dp, 40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        onClick = {setShowDialog(false)}
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Button(
                        modifier = Modifier.size(140.dp, 40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Yellow),
                        onClick = {
                            val newTime = inputTime.toIntOrNull()
                        if (newTime != null) {
                            chatViewModel.updateRetroTime(newTime)
                        } else{
                            Toast.makeText(contextForToast, "Yeni Süre Boş Olamaz", Toast.LENGTH_LONG).show()
                        }
                        setShowDialog(false)
                        },
                    ) {
                        Text(
                            text = stringResource(id =R.string.onayla),
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}


