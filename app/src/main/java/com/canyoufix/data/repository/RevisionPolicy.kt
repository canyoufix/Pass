package com.canyoufix.data.repository

internal fun requireUnchangedRevision(expected: Long, actual: Long, deleted: Boolean) {
    check(!deleted && expected == actual) { "Запись была изменена. Обновите её перед сохранением" }
}
