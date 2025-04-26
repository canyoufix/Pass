package com.canyoufix.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MetaInfo(
    val version: String,
    val exportedAt: String,
    val device: String,
    val androidVersion: String
)