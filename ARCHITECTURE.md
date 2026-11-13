# Архитектура TwicePass

Проект собирается одним Android-модулем `app`. Внутри него сохранены границы Clean Architecture через пакеты и зависимости направлены к центру:

```text
app/src/main
├── com.canyoufix.domain       # модели, контракты репозиториев, use cases
├── com.canyoufix.data         # Room, DataStore adapters, файлы, sync orchestration
├── com.canyoufix.crypto       # криптография и session key store
├── com.canyoufix.settings     # настройки DataStore
├── com.canyoufix.sync         # Retrofit API и DTO
├── com.canyoufix.autofill     # Android Autofill adapter
├── com.canyoufix.ui           # Compose UI
│   ├── screens                # route-компоненты и composable-экраны
│   ├── viewmodel              # ViewModel и состояние экранов
│   ├── navigation             # NavController и маршруты
│   └── components             # переиспользуемые UI-компоненты
└── com.canyoufix.pass         # composition root: Activity, Koin, WorkManager
```

`domain` не знает об Android, Compose, Room, Retrofit и Koin. Контракты репозиториев находятся в `domain.repository`, а сценарии пользовательских действий — в `domain.usecase`. `data`, `crypto`, `settings` и `sync` реализуют эти контракты. `ui` получает только интерфейсы domain и отображает состояние из ViewModel. `pass.di` — единственная точка сборки графа зависимостей.

Навигация разделена на два уровня: `ui.navigation` владеет `NavController` и маршрутами, а `ui.screens` содержит route-компоненты и чистые composable-экраны. Route-компоненты получают ViewModel из Koin; дочерние composable получают только состояние и callbacks. Параметры редактора приходят в `EntryEditorViewModel` через `SavedStateHandle`, а черновик хранится только в памяти ViewModel. Пароли и содержимое записей не попадают в `SavedStateHandle`, `Bundle` или логи.

Состояние экранов представлено `StateFlow`. Все операции записи, импорта, экспорта, работы с файлами и синхронизации выполняются в ViewModel или data слоях с явными dispatcher-ами. `CancellationException` сохраняется как отмена. `MyApp` использует application scope только для наблюдения настроек синхронизации и планирования WorkManager; UI-операции используют `viewModelScope`, Autofill — scope жизненного цикла сервиса.

## Хранилище и совместимость

Сохранены имя базы `TwicePass`, версия схемы 1, таблицы, колонки, каталог `EncryptedFiles`, имена SharedPreferences/DataStore и ключи настроек. Схема Room находится в `app/schemas`. Деструктивная миграция отключена: будущие изменения требуют явной `Migration`.

Domain-модели содержат открытые значения только внутри авторизованной сессии. Room-сущности и очередь синхронизации хранят зашифрованные поля. Ошибка расшифровки не превращается в пустую запись. Локальные ревизии монотонны, устаревший редактор не перезаписывает изменённую или удалённую запись.

Импорт сначала проверяет мастер-пароль и полностью валидирует файл, затем выполняет замену или слияние в одной транзакции. Ошибка пароля или повреждённый файл не изменяют хранилище. Файловые операции выполняются во вне UI-потока, расшифровка проходит через приватный временный файл, временные файлы удаляются в `finally`.

Форматы AES-GCM, PBKDF2-HMAC-SHA256, длины соли и ключа оставлены совместимыми со старым приложением. Ключ сессии хранится только в памяти и очищается при блокировке. Autofill использует только `webDomain` и точное нормализованное имя хоста, не читает текст полей как источник домена и не пишет AssistStructure в лог.

## Проверки

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
.\gradlew.bat checkKotlinFormat
```

Unit-тесты находятся в `app/src/test`, инструментальные тесты — в `app/src/androidTest`. Граф Koin проверяется `DependencyGraphTest`, ViewModel — отдельными тестами состояния и конкурентного редактирования, data/crypto — тестами совместимости и транзакций.
