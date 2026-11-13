package com.canyoufix.data.mapping

import com.canyoufix.data.entity.PasswordEntity
import com.canyoufix.domain.model.PasswordEntry

internal fun PasswordEntry.toEntity(transform: (String) -> String) =
    PasswordEntity(
        id = id,
        title = transform(title),
        url = transform(url),
        username = transform(username),
        password = transform(password),
        lastModified = lastModified,
    )

internal fun PasswordEntity.toDomain(transform: (String) -> String) =
    PasswordEntry(
        id = id,
        title = transform(title),
        url = transform(url),
        username = transform(username),
        password = transform(password),
        lastModified = lastModified,
    )
