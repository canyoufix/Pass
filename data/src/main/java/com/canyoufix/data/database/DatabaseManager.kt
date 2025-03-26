package com.canyoufix.data.database

import android.content.Context

class DatabaseManager(context: Context) {
    private val database: AppDatabase = AppDatabase.getInstance(context)

    // Получаем доступ к базе данных через SupportSQLiteDatabase
    private val databaseHelper = database.openHelper.writableDatabase


    // Метод для очистки всех данных из базы
    suspend fun clearAllData() {
        database.passwordDao().clearAll() // Очистка таблицы паролей
        database.cardDao().clearAll()     // Очистка таблицы карт
        database.noteDao().clearAll()     // Очистка таблицы заметок

        // Выполняем команду VACUUM для уменьшения размера базы данных
        databaseHelper.execSQL("VACUUM")
    }
}