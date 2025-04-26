package com.canyoufix.ui.components.password

import android.annotation.SuppressLint
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction

@Composable
fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    label: String = "Пароль",
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
) {
    val passwordVisibility = rememberPasswordVisibilityState()

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        visualTransformation = passwordVisibility.visualTransformation,
        trailingIcon = {
            IconButton(onClick = passwordVisibility.toggle) {
                Icon(
                    imageVector = passwordVisibility.icon,
                    contentDescription = passwordVisibility.description
                )
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        modifier = modifier.then(
            focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier
        )
    )
}
