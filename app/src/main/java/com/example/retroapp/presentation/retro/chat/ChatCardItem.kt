package com.example.retroapp.presentation.retro.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.retroapp.R


@Composable
fun ChatCardItem(  ) {
    // dışardan gelicek değerler
    val selectedOption = rememberSaveable() { mutableStateOf("Select Type") }
    val comment = rememberSaveable() { mutableStateOf("") }
    val isBlur= rememberSaveable() { mutableStateOf(true)}
    var blurValue=0.dp

    if(isBlur.value) blurValue=10.dp

    Box(modifier = Modifier.fillMaxSize()) {

        Card( modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .border(
                1.5.dp,
                Color(R.color.white_f5),
                RoundedCornerShape(5.dp)
            )) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "$selectedOption : $comment",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = 16.sp,
                    color = Color.Black,
                   modifier = Modifier.blur(blurValue, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                )
            }

        }
    }
}
@Preview
@Composable
fun PreviewCard() {
    ChatCardItem()
}