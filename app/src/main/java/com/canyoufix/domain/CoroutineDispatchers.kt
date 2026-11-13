package com.canyoufix.domain

import kotlinx.coroutines.CoroutineDispatcher

class CoroutineDispatchers(val io: CoroutineDispatcher, val computation: CoroutineDispatcher)
