package com.voctext.app.ui.screens
import com.voctext.app.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.voctext.app.domain.model.Transcription
import com.voctext.app.ui.components.*
import com.voctext.app.ui.theme.VoctextRadius
import com.voctext.app.ui.theme.VoctextSpacing

import androidx.compose.material.icons.outlined.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    history: List<Transcription>,
    linkInputState: VoctextInputState,
    onLinkInputChange: (String) -> Unit,
    onLinkInputClear: () -> Unit,
    onLinkSubmit: () -> Unit,
    onImportFile: () -> Unit,
    onTranscriptionClick: (String) -> Unit,
    onMenuSettings: () -> Unit,
    onMenuAbout: () -> Unit,
    onMenuRedownloadDict: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                actions = {
                    IconButton(onClick = onMenuSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Paramètres",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Menu",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_about)) },
                            onClick = {
                                showMenu = false
                                onMenuAbout()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_redownload_dict)) },
                            onClick = {
                                showMenu = false
                                onMenuRedownloadDict()
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = VoctextSpacing.screenHorizontal),
        ) {
            Spacer(modifier = Modifier.height(VoctextSpacing.md))

            // Link input bar
            VoctextInput(
                state = linkInputState,
                onValueChange = onLinkInputChange,
                onClear = onLinkInputClear,
                onSubmit = onLinkSubmit,
                placeholder = stringResource(R.string.home_link_placeholder),
            )

            Spacer(modifier = Modifier.height(VoctextSpacing.lg))

            // Import button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                VoctextButton(
                    label = stringResource(R.string.home_import_label),
                    onClick = onImportFile,
                    variant = ButtonVariant.SECONDARY,
                    size = ButtonSize.LG,
                    leadingIcon = Icons.Outlined.Add,
                )
            }

            Spacer(modifier = Modifier.height(VoctextSpacing.lg))

            // History section
            Text(
                text = stringResource(R.string.home_history_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(VoctextSpacing.md))

            if (history.isEmpty()) {
                VoctextEmptyState(
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(VoctextSpacing.sm),
                    contentPadding = PaddingValues(bottom = VoctextSpacing.lg),
                ) {
                    items(
                        items = history,
                        key = { it.id },
                    ) { transcription ->
                        VoctextHistoryCard(
                            transcription = transcription,
                            onClick = { onTranscriptionClick(transcription.id) },
                        )
                    }
                }
            }
        }
    }
}