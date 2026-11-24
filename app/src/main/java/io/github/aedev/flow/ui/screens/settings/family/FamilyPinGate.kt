package io.github.aedev.flow.ui.screens.settings.family

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.aedev.flow.R
import io.github.aedev.flow.data.contentfilter.family.FamilyPinRepository

/**
 * The PIN gate for the family area: setup on first use, entry afterwards. The PIN is 4–6
 * digits, typed on the number keyboard and never shown. There is no default PIN and no
 * "forgot PIN" escape here by design — clearing app data is the only reset, and that also
 * clears the whitelist, so a lost PIN cannot silently reopen the gate.
 */
@Composable
internal fun FamilyPinSetup(
    errorRes: Int?,
    onSetup: (String, String) -> Unit,
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    val confirmFocus = remember { FocusRequester() }
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.family_pin_setup_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FamilyPinField(
            value = pin,
            onValueChange = { pin = it },
            labelRes = R.string.family_pin_label,
            imeAction = ImeAction.Next,
            onNext = { confirmFocus.requestFocus() },
        )
        FamilyPinField(
            value = confirm,
            onValueChange = { confirm = it },
            labelRes = R.string.family_pin_confirm_label,
            imeAction = ImeAction.Done,
            focusRequester = confirmFocus,
            onDone = { onSetup(pin, confirm) },
        )
        errorRes?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Button(
            onClick = { onSetup(pin, confirm) },
            enabled = pin.isNotEmpty() && confirm.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.family_pin_set_button))
        }
    }
}

@Composable
internal fun FamilyPinEntry(
    errorRes: Int?,
    locked: Boolean,
    onUnlock: (String) -> Unit,
) {
    var pin by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.family_pin_entry_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FamilyPinField(
            value = pin,
            onValueChange = { pin = it },
            labelRes = R.string.family_pin_label,
            imeAction = ImeAction.Done,
            onDone = { onUnlock(pin) },
        )
        errorRes?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Button(
            onClick = { onUnlock(pin) },
            enabled = pin.isNotEmpty() && !locked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.family_pin_unlock_button))
        }
    }
}

@Composable
private fun FamilyPinField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    imeAction: ImeAction,
    focusRequester: FocusRequester? = null,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { text ->
            onValueChange(text.filter { it.isDigit() }.take(FamilyPinRepository.MAX_PIN_LENGTH))
        },
        label = { Text(stringResource(labelRes)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = imeAction,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = { onDone?.invoke() },
                onNext = { onNext?.invoke() },
            ),
        modifier =
            if (focusRequester != null) {
                Modifier.fillMaxWidth().focusRequester(focusRequester)
            } else {
                Modifier.fillMaxWidth()
            },
    )
}
