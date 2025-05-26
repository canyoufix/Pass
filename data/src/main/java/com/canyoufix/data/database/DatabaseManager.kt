package com.canyoufix.data.database

class DatabaseManager(
    private val database: AppDatabase // Внедряем через Koin
) {
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