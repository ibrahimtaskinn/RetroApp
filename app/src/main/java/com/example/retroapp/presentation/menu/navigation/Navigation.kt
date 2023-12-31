package com.example.retroapp.presentation.menu.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.retroapp.R
import com.example.retroapp.navigation.ROUTE_ADD
import com.example.retroapp.presentation.auth.AuthViewModel
import com.example.retroapp.presentation.home.HomeScreen
import com.example.retroapp.presentation.home.HomeViewModel
import com.example.retroapp.presentation.retro.RetroScreen
import com.example.retroapp.presentation.retro.RetroViewModel
import com.example.retroapp.presentation.ui.theme.LightGray

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigation(
    name: String,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    retroViewModel: RetroViewModel
) {
    val items = listOf("HomeScreen", "RetroScreen")
    val selectedPage = remember { mutableStateOf(0) }


    Scaffold(
        content = {
            when (selectedPage.value) {
                0 -> HomeScreen(
                    homeViewModel = homeViewModel,
                    onCardClick = { navController.navigate("detail/${it.id}") },
                    onFabClick = { navController.navigate(ROUTE_ADD) },
                    navController = navController,
                    authViewModel = authViewModel,
                )

                1 -> RetroScreen(retroViewModel, navController)
            }
        },
        bottomBar = {
            NavigationBar(modifier = Modifier
                .height(70.dp)
                .background(Color.White)) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedPage.value == index,
                        onClick = { selectedPage.value = index },

                        icon = {
                            when (item) {
                                "HomeScreen" -> Icon(
                                    painter = painterResource(id = R.drawable.ic_home_icon),
                                    contentDescription = ""
                                )

                                "RetroScreen" -> Icon(
                                    painter = painterResource(id = R.drawable.ic_retro_icon),
                                    contentDescription = ""
                                )
                            }
                        }
                    )
                }
            }
        }
    )
}
