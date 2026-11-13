package com.canyoufix.ui.viewmodel

import com.canyoufix.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GeneratorViewModel(private val generate: GeneratePassword) : ActionViewModel() {
    private val mutableOptions = MutableStateFlow(PasswordOptions())
    val options = mutableOptions.asStateFlow()
    private val mutablePassword = MutableStateFlow("")
    val password = mutablePassword.asStateFlow()

    fun edit(change: (PasswordOptions) -> PasswordOptions) {
        mutableOptions.value = change(options.value)
    }

    fun generate() = perform { mutablePassword.value = generate(options.value) }
}
