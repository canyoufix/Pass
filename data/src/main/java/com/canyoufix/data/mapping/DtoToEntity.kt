package com.canyoufix.data.mapping

import com.canyoufix.data.entity.CardEntity
import com.canyoufix.data.entity.NoteEntity
import com.canyoufix.data.entity.PasswordEntity
import com.canyoufix.sync.dto.NoteDto
import com.canyoufix.sync.dto.PasswordDto
import com.canyoufix.sync.dto.CardDto

object DtoToEntity {

    fun NoteDto.toNoteEntity(): NoteEntity {
        return NoteEntity(
            id = this.id,
            title = this.title,
            content = this.content,
            lastModified = this.lastModified,
            isDeleted = this.isDeleted
        )
    }

    fun PasswordDto.toPasswordEntity(): PasswordEntity {
        return PasswordEntity(
            id = this.id,
            title = this.title,
            url = this.url,
            username = this.username,
            password = this.password,
            lastModified = this.lastModified,
            isDeleted = this.isDeleted
        )
    }

    fun CardDto.toCardEntity(): CardEntity {
        return CardEntity(
            id = this.id,
            title = this.title,
            number = this.number,
            expiryDate = this.expiryDate,
            cvc = this.cvc,
            holderName = this.holderName,
            lastModified = this.lastModified,
            isDeleted = this.isDeleted
        )
    }
}
