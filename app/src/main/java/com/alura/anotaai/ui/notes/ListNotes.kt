package com.alura.anotaai.ui.notes

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alura.anotaai.R
import com.alura.anotaai.extensions.audioDisplay
import com.alura.anotaai.model.BaseNote
import com.alura.anotaai.model.Note
import com.alura.anotaai.model.NoteItemAudio
import com.alura.anotaai.model.NoteItemImage
import com.alura.anotaai.model.NoteItemText
import com.alura.anotaai.model.NoteType
import kotlinx.coroutines.delay

@Composable
fun ListNotes(
    modifier: Modifier = Modifier,
    noteText: String = "",
    onNoteTextChanged: (String) -> Unit = {},
    noteState: Note = Note(),
    onPlayAudio: (String) -> Unit = {},
    onStopAudio: () -> Unit = {},
    onUpdatedItem: (String, String) -> Unit = { _, _ -> },
    onDeletedItem: (BaseNote) -> Unit = {}
) {
    val stateList = rememberLazyListState()
    var itemToDelete by remember { mutableStateOf<BaseNote?>(null) }

    LazyColumn(
        modifier = modifier,
        state = stateList,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(maxHeight = 100.dp),
            ) {
                BasicTextField(
                    value = noteText,
                    onValueChange = { onNoteTextChanged(it) },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (noteText.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.write_your_note),
                                    style = LocalTextStyle.current.copy(fontSize = 20.sp)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            HorizontalDivider(
                Modifier.padding(top = 8.dp)
            )

        }

        items(noteState.listItems.sortedByDescending { it.date }, key = { it.id }) { item ->
            when (item.type) {
                NoteType.TEXT -> {
                    ItemNoteText(
                        modifier = Modifier,
                        item = item as NoteItemText,
                        onUpdated = { updatedItemText ->
                            onUpdatedItem(
                                updatedItemText, item.id
                            )
                        },
                        onDeleted = {
                            itemToDelete = item
                        }
                    )
                }

                NoteType.IMAGE -> {
                    ItemNoteImage(
                        modifier = Modifier,
                        item = item as NoteItemImage,
                        onDeleted = {
                            itemToDelete = item
                        }
                    )
                }

                NoteType.AUDIO -> {
                    ItemNoteAudio(
                        modifier = Modifier,
                        item = item as NoteItemAudio,
                        onPlayAudio = onPlayAudio,
                        onStopAudio = onStopAudio,
                        onDeleted = {
                            itemToDelete = item
                        }
                    )
                }
            }
        }
    }

    itemToDelete?.let {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(text = stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_item)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletedItem(it)
                        itemToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                Button(
                    onClick = { itemToDelete = null }
                ) {
                    Text(stringResource(R.string.not))
                }
            }
        )
    }
}

@Composable
private fun ItemNoteText(
    modifier: Modifier = Modifier,
    item: NoteItemText,
    onUpdated: (String) -> Unit,
    onDeleted: () -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var stateText by remember { mutableStateOf(item.content) }
    Card(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { isEditing = true },
                    onLongPress = { onDeleted() }
                )
            }
    ) {
        if (isEditing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    modifier = Modifier
                        .weight(0.8f),
                    value = stateText,
                    onValueChange = {
                        stateText = it
                    },
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(16.dp)
                        ) {
                            innerTextField()
                        }
                    }
                )
                IconButton(
                    onClick = {
                        isEditing = false
                        onUpdated(stateText)
                    },
                    modifier = Modifier
                        .weight(0.2f)
                        .fillMaxHeight()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Fechar",
                    )
                }
            }
        } else {
            Text(
                item.content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun ItemNoteImage(
    modifier: Modifier = Modifier,
    item: NoteItemImage,
    onDeleted: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { expanded = !expanded },
                    onLongPress = { onDeleted() }
                )
            },
    ) {
        if (expanded) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth(),
                model = item.link,
                contentScale = ContentScale.Fit,
                contentDescription = "Imagem expandida"
            )
        } else {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth(),
                model = item.link,
                contentScale = ContentScale.Crop,
                contentDescription = "Imagem sem expansão"
            )
        }
    }
}

@Composable
private fun ItemNoteAudio(
    modifier: Modifier = Modifier,
    item: NoteItemAudio,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    onDeleted: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(false) }
    val icon = if (isPlaying) Icons.Filled.Close else Icons.Filled.PlayArrow

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            delay(item.duration * 1000L)
            isPlaying = false
        }
    }

    Card(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { onDeleted() }
            )
        },
        colors = CardDefaults.cardColors(
            containerColor = Color.Green.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Áudio ${item.duration.audioDisplay()}",
                modifier = Modifier
                    .padding(16.dp)
            )
            IconButton(
                onClick = {
                    if (isPlaying) {
                        onStopAudio()
                        isPlaying = false
                    } else {
                        onPlayAudio(item.link)
                        isPlaying = true
                    }
                },
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Tocar áudio",
                    tint = Color.Black,
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ListNotesPreview() {
    ListNotes(
        noteState = Note(
            listItems = listOf(
                NoteItemText(content = "Texto 1"),
                NoteItemImage(link = "https://alura.com.br"),
                NoteItemAudio(link = "https://audio.com", duration = 42),
                NoteItemText(content = "Texto 2"),
            )
        )
    )
}