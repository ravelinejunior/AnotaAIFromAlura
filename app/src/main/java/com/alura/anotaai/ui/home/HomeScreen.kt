package com.alura.anotaai.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.alura.anotaai.R
import com.alura.anotaai.extensions.toDisplayDate
import com.alura.anotaai.model.Note
import com.alura.anotaai.model.NoteType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAddNewNote: () -> Unit = {},
    onOpenNote: (String) -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSnackbarVisible by remember { mutableStateOf(false) }
    var isSnackbarClickedOk by remember { mutableStateOf(false) }


    LaunchedEffect(state.notes) {
        while (!isSnackbarClickedOk) {
            delay(7000)

            if (state.notes.isEmpty()) {
                isSnackbarVisible = true

                val result = snackbarHostState.showSnackbar(
                    message = "A lista de notas está vazia!",
                    actionLabel = "OK",
                    duration = SnackbarDuration.Short
                )

                isSnackbarVisible = false

                if (result == SnackbarResult.ActionPerformed) {
                    isSnackbarClickedOk = true
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            AnimatedVisibility(
                visible = isSnackbarVisible,
                enter = slideInVertically { fullHeight -> -fullHeight } + fadeIn(),
                exit = slideOutVertically { fullHeight -> -fullHeight } + fadeOut()
            ) {
                SnackbarHost(snackbarHostState)
            }
        },
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    actions = {
                        IconButton(onClick = { onOpenProfile() }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configurações")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
                HorizontalDivider()
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAddNewNote()
                },
                content = { Icon(Icons.Default.Add, contentDescription = "Adicionar Nota") }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(state.notes) { item ->
                        ItemNote(
                            note = item,
                            onClick = { onOpenNote(item.id) },
                            onLongPress = {
                                viewModel.setItemToDelete(item)
                            }
                        )
                    }
                }

                if (state.notes.isNotEmpty()) {
                    AsyncImage(
                        R.mipmap.ic_launcher_foreground,
                        contentDescription = "Logo do app",
                        modifier = Modifier
                            .size(500.dp)
                            .alpha(0.1f),
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val lottieSpec = LottieCompositionSpec.RawRes(R.raw.empty)
                        val lottieComposition by rememberLottieComposition(
                            lottieSpec
                        )
                        LottieAnimation(
                            composition = lottieComposition,
                            modifier = Modifier.size(200.dp),
                            restartOnPlay = true,
                            isPlaying = true,
                            iterations = Int.MAX_VALUE,
                        )
                        Text(
                            text = "No notes added yet, click the button to add your note +",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            state.itemToDelete?.let { itemId ->
                AlertDialog(
                    onDismissRequest = { viewModel.setItemToDelete(null) },
                    title = { Text(text = stringResource(R.string.confirm_delete_title)) },
                    text = { Text(stringResource(R.string.confirm_delete_note)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.setItemToDelete(null)
                                viewModel.removeNote(itemId)
                            }
                        ) {
                            Text(stringResource(R.string.yes))
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { viewModel.setItemToDelete(null) }
                        ) {
                            Text(stringResource(R.string.not))
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun ItemNote(
    note: Note,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            val thumbnail = when (note.thumbnail) {
                NoteType.AUDIO.name -> R.drawable.ic_mic
                NoteType.TEXT.name -> R.drawable.ic_title
                else -> note.thumbnail
            }

            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = note.title,
                    fontSize = 20.sp
                )
                Text(
                    text = note.date.toDisplayDate(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Card(
                modifier = Modifier
                    .weight(0.2f)
                    .size(70.dp),
            ) {
                AsyncImage(
                    thumbnail,
                    contentDescription = "Minuatura da nota",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewItemListScreen() {
    HomeScreen()
}