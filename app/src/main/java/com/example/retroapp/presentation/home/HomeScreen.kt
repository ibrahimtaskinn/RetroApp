package com.example.retroapp.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.retroapp.R
import com.example.retroapp.data.Resource
import com.example.retroapp.data.model.Notes


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCardClick: (Notes) -> Unit,
    onFabClick: () -> Unit,
    onLogoutClick: () -> Unit,
    navController: NavHostController,
) {


    val noteId = remember { mutableStateOf("") }
    val mDisplayMenu = remember { mutableStateOf(false) }
    val mContext = LocalContext.current.applicationContext
    val visible = remember { mutableStateOf(false) }
    val searchText = remember { mutableStateOf("") }
    val filterType = remember { mutableStateOf("") }
    val isDeleteDialogOpen = remember { mutableStateOf(false) }


    val notesState by viewModel.getFilteredNotes(searchText.value, filterType.value).collectAsState(null)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onFabClick() },
                modifier = Modifier.padding(bottom = 72.dp),
                containerColor = colorResource(id = R.color.button_color)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        },
        topBar = {
            TopAppBar(
                navigationIcon = {},
                actions = {
                    AnimatedVisibility(visible = visible.value) {
                        OutlinedTextField(
                            value = searchText.value,
                            onValueChange = { searchText.value = it },
                            label = { Text("Search", color = Color.Black, modifier = Modifier.align(CenterVertically)) },
                            modifier = Modifier
                                .padding(1.dp)
                                .size(220.dp, 60.dp)
                        )
                    }
                    IconButton(onClick = { visible.value = !visible.value }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp, 30.dp)
                        )
                    }
                    IconButton(onClick = { mDisplayMenu.value = !mDisplayMenu.value }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                        )
                    }
                    DropdownMenu(
                        expanded = mDisplayMenu.value,
                        onDismissRequest = { mDisplayMenu.value = false },
                        Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            onClick = {filterType.value = ""},
                            text = { Text(text = "Filtrelemeyi İptal Et ", fontSize = 16.sp, style = TextStyle.Default) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                            }
                        )
                        DropdownMenuItem(
                            onClick = {filterType.value = "Teknik Karar Toplantısı"},
                            text = { Text(text = "Teknik Karar Toplantısı", fontSize = 16.sp, style = TextStyle.Default) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.green_circle_icon),
                                    contentDescription = null,
                                    tint = Color.Green
                                )
                            }
                        )
                        DropdownMenuItem(
                            onClick = {filterType.value = "Retro Toplantısı"},
                            text = { Text(text = "Retro Toplantısı", fontSize = 16.sp, style = TextStyle.Default) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.yellow_circle_icon),
                                    contentDescription = null,
                                    tint = Color(R.color.yellow)
                                )
                            }
                        )
                        DropdownMenuItem(
                            onClick = { filterType.value = "Cluster Toplantısı" },
                            text = { Text(text = "Cluster Toplantısı", fontSize = 16.sp, style = TextStyle.Default) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.blue_circle_icon),
                                    contentDescription = null,
                                    tint = Color.Blue
                                )
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onLogoutClick()
                            },
                            text = { Text(text = "Logout", fontSize = 16.sp, style = TextStyle.Default) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                )
                            }
                        )
                    }
                },
                title = {
                    Text(text = "Home")
                }
            )
        }
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            when (notesState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is Resource.Success -> {
                    LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(2), verticalItemSpacing = 2.dp,
                        horizontalArrangement = Arrangement.spacedBy(2.dp) ){
                        items((notesState as Resource.Success<List<Notes>>).result){
                                card ->
                            CardItem(
                                card = card,
                                onClick = { onCardClick(card) },
                                onLongClick = {isDeleteDialogOpen.value = true; noteId.value = card.id;}
                            )
                        }

                    }
                }
                is Resource.Failure -> {
                    Text(
                        text = "Error loading data",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {}
            }
            if (isDeleteDialogOpen.value) {
                    AlertDialog(
                        onDismissRequest = {
                            isDeleteDialogOpen.value = false
                        },
                        title = {
                            Text(text = "Delete")
                        },
                        text = {
                            Text(text = "Do you want to delete this note?")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.deleteNote(noteId.value, onComplete = {})
                                    isDeleteDialogOpen.value = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text(text = "Delete")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    isDeleteDialogOpen.value = false
                                }
                            ) {
                                Text(text = "Cancel")
                            }
                        }
                    )
            }
        }
    }
}

