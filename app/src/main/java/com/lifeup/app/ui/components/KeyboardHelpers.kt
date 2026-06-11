package com.lifeup.app.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.text.input.ImeAction

fun doneKeyboardOptions() = KeyboardOptions(imeAction = ImeAction.Done)

fun doneKeyboardActions(focusManager: FocusManager, onDone: () -> Unit) = KeyboardActions(
    onDone = {
        focusManager.clearFocus()
        onDone()
    }
)
