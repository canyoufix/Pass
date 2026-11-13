package com.canyoufix.data.mapping

import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.domain.model.NoteEntry

internal fun NoteEntry.toEntity(transform: (String) -> String) =
    NoteEntity(
        id = id,
        title = transform(title),
        content = transform(content),
        lastModified = lastModified,
    )

internal fun NoteEntity.toDomain(transform: (String) -> String) =
    NoteEntry(
        id = id,
        title = transform(title),
        content = transform(content),
        lastModified = lastModified,
    )
