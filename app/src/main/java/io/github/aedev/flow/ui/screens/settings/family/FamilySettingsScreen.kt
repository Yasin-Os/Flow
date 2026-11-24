package io.github.aedev.flow.ui.screens.settings.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.aedev.flow.R
import io.github.aedev.flow.data.contentfilter.family.FamilyChannel
import io.github.aedev.flow.data.model.Channel
import io.github.aedev.flow.ui.components.settings.SettingsListScope
import io.github.aedev.flow.ui.components.settings.SettingsPage
import io.github.aedev.flow.ui.components.shared.ChannelAvatarImage
import io.github.aedev.flow.ui.components.shared.FlowSearchField

/**
 * The family area: the PIN gate, then the approved channels, a channels-only search to add
 * more, and the extra blocked words. Only the PIN holder gets here; the unlock lasts while
 * this page is open.
 */
@Composable
internal fun FamilySettingsScreen(
    onBack: (() -> Unit)?,
    highlight: String?,
    viewModel: FamilySettingsViewModel = hiltViewModel(),
) {
    val pinSet by viewModel.pinSet.collectAsStateWithLifecycle()
    val unlocked by viewModel.unlocked.collectAsStateWithLifecycle()
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()
    val pinLocked by viewModel.pinLocked.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val customWords by viewModel.customWords.collectAsStateWithLifecycle()
    val search by viewModel.search.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var newWord by rememberSaveable { mutableStateOf("") }

    SettingsPage(title = stringResource(R.string.family_settings_title), onBack = onBack, highlight = highlight) {
        when {
            !pinSet -> {
                item("family.pin.setup") {
                    FamilyPinSetup(errorRes = pinError, onSetup = viewModel::setupPin)
                }
            }

            !unlocked -> {
                item("family.pin.entry") {
                    FamilyPinEntry(errorRes = pinError, locked = pinLocked, onUnlock = viewModel::unlock)
                }
            }

            else -> {
                familyChannels(channels, viewModel::removeChannel)
                item("family.search.field") {
                    FlowSearchField(
                        query = query,
                        onQueryChange = {
                            query = it
                            viewModel.onQueryChange(it)
                        },
                        placeholder = stringResource(R.string.family_search_placeholder),
                        onClear = {
                            query = ""
                            viewModel.onQueryChange("")
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    )
                }
                familySearchResults(query, search, channels, viewModel::addChannel)
                familyWords(customWords, viewModel::removeWord)
                item("family.words.add") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = newWord,
                            onValueChange = { newWord = it },
                            label = { Text(stringResource(R.string.family_word_add_hint)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        Button(
                            onClick = {
                                viewModel.addWord(newWord)
                                newWord = ""
                            },
                            enabled = newWord.isNotBlank(),
                        ) {
                            Text(stringResource(R.string.family_word_add_button))
                        }
                    }
                }
            }
        }
    }
}

private fun SettingsListScope.familyChannels(
    channels: List<FamilyChannel>,
    onRemove: (FamilyChannel) -> Unit,
) {
    if (channels.isEmpty()) {
        item("family.channels.empty") {
            Text(
                text = stringResource(R.string.family_channels_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        return
    }
    group(key = "family.channels", header = R.string.family_channels_header) {
        channels.forEach { channel ->
            row("family.channels.${channel.channelId.ifBlank { channel.handle }}") { shape ->
                FamilyChannelRow(
                    name = channel.name,
                    supporting = channel.handle,
                    avatarUrl = null,
                    actionLabel = stringResource(R.string.family_channel_remove),
                    actionDescription = stringResource(R.string.family_channel_remove_item, channel.name),
                    shape = shape,
                    onAction = { onRemove(channel) },
                )
            }
        }
    }
}

private fun SettingsListScope.familySearchResults(
    query: String,
    search: FamilyChannelSearchState,
    channels: List<FamilyChannel>,
    onAdd: (Channel) -> Unit,
) {
    if (query.isBlank()) return
    when {
        search.blocked -> {
            item("family.search.blocked") {
                Text(
                    text = stringResource(R.string.family_search_blocked),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }

        search.results == null -> {
            item("family.search.loading") {
                Text(
                    text = stringResource(R.string.loading_ellipsis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }

        search.failed -> {
            item("family.search.failed") {
                Text(
                    text = stringResource(R.string.family_search_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }

        search.results.isEmpty() -> {
            item("family.search.empty") {
                Text(
                    text = stringResource(R.string.no_results_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }

        else -> {
            val addedIds = channels.mapNotNull { it.channelId.takeIf(String::isNotBlank) }.toSet()
            group(key = "family.search.results", header = null) {
                search.results.forEach { channel ->
                    val added = channel.id in addedIds
                    row("family.search.${channel.id}") { shape ->
                        FamilyChannelRow(
                            name = channel.name,
                            supporting = channel.handle.ifBlank { null },
                            avatarUrl = channel.thumbnailUrl,
                            actionLabel =
                                stringResource(
                                    if (added) R.string.family_search_added else R.string.family_search_add,
                                ),
                            actionDescription = stringResource(R.string.family_search_add_item, channel.name),
                            shape = shape,
                            actionEnabled = !added,
                            onAction = { onAdd(channel) },
                        )
                    }
                }
            }
        }
    }
}

private fun SettingsListScope.familyWords(
    words: Set<String>,
    onRemove: (String) -> Unit,
) {
    if (words.isEmpty()) return
    group(key = "family.words", header = R.string.family_words_header, footer = R.string.family_words_footer) {
        words.sorted().forEach { word ->
            row("family.words.$word") { shape ->
                FamilyChannelRow(
                    name = word,
                    supporting = null,
                    avatarUrl = null,
                    actionLabel = stringResource(R.string.family_word_remove),
                    actionDescription = stringResource(R.string.family_word_remove_item, word),
                    shape = shape,
                    onAction = { onRemove(word) },
                )
            }
        }
    }
}

/** One named item with a single text action, such as removing or adding it. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FamilyChannelRow(
    name: String,
    supporting: String?,
    avatarUrl: String?,
    actionLabel: String,
    actionDescription: String,
    shape: Shape,
    actionEnabled: Boolean = true,
    onAction: () -> Unit,
) {
    SegmentedListItem(
        verticalAlignment = Alignment.CenterVertically,
        shapes = ListItemDefaults.shapes(shape = shape),
        colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        leadingContent = {
            ChannelAvatarImage(
                url = avatarUrl,
                contentDescription = name,
                modifier = Modifier.size(40.dp),
            )
        },
        supportingContent = supporting?.let { { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
        trailingContent = {
            TextButton(
                onClick = onAction,
                enabled = actionEnabled,
                modifier = Modifier.semantics { contentDescription = actionDescription },
            ) {
                Text(actionLabel)
            }
        },
    ) {
        Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
