package com.canyoufix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canyoufix.pass.R
import com.canyoufix.ui.components.*
import com.canyoufix.ui.platform.copySecret
import com.canyoufix.ui.viewmodel.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun GeneratorRoute(viewModel: GeneratorViewModel = koinViewModel()) {
    val options by viewModel.options.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ScreenFrame(stringResource(R.string.generator_title), status, viewModel::dismissMessage) {
        padding ->
        FormColumn(padding) {
            GlassPanel(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.generator_length, options.length),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Slider(
                        options.length.toFloat(),
                        { value -> viewModel.edit { it.copy(length = value.toInt()) } },
                        valueRange = 4f..128f,
                        steps = 0,
                    )
                    OptionSwitch(stringResource(R.string.generator_lowercase), options.lowercase) {
                        v ->
                        viewModel.edit { it.copy(lowercase = v) }
                    }
                    OptionSwitch(stringResource(R.string.generator_uppercase), options.uppercase) {
                        v ->
                        viewModel.edit { it.copy(uppercase = v) }
                    }
                    OptionSwitch(stringResource(R.string.generator_digits), options.digits) { v ->
                        viewModel.edit { it.copy(digits = v) }
                    }
                    OptionSwitch(stringResource(R.string.generator_symbols), options.symbols) { v ->
                        viewModel.edit { it.copy(symbols = v) }
                    }
                }
            }
            Button(
                viewModel::generate,
                enabled = !status.busy,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.action_generate))
            }
            GlassPanel(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecretField(
                        password,
                        {},
                        stringResource(R.string.generator_result),
                        readOnly = true,
                    )
                    OutlinedButton(
                        { copySecret(context, password) },
                        enabled = password.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.action_copy_password))
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, Modifier.weight(1f))
        Switch(
            checked,
            onChecked,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                    checkedTrackColor = androidx.compose.ui.graphics.Color(0xFF30D158),
                    checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
        )
    }
}
