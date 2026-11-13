package com.canyoufix.data.mapping

import com.canyoufix.data.entity.CardEntity
import com.canyoufix.domain.model.CardEntry

internal fun CardEntry.toEntity(transform: (String) -> String) =
    CardEntity(
        id = id,
        title = transform(title),
        number = transform(number),
        expiryDate = transform(expiryDate),
        cvc = transform(cvc),
        holderName = transform(holderName),
        lastModified = lastModified,
    )

internal fun CardEntity.toDomain(transform: (String) -> String) =
    CardEntry(
        id = id,
        title = transform(title),
        number = transform(number),
        expiryDate = transform(expiryDate),
        cvc = transform(cvc),
        holderName = transform(holderName),
        lastModified = lastModified,
    )
