# Client Application

This is the client-side application.

The server-side part can be found here: [Server Repository](https://github.com/canyoufix/TwicePass-server)  

## Сборка Android-приложения

Используйте Gradle Wrapper из репозитория. Для текущей конфигурации нужны
JDK 25, Android SDK Platform 37 и доступ к Google Maven / Maven Central.
В Android Studio выберите установленный JDK 25 в настройках Gradle JDK.
Путь к Android SDK задаётся локально в `local.properties` (`sdk.dir`);
этот файл не должен попадать в Git.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :domain:test testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.
Debug-сборка имеет отдельный application ID `com.canyoufix.pass.debug`:
она устанавливается рядом с обычным Pass и использует отдельное хранилище.
На подключённом устройстве или запущенном эмуляторе Android 9 (API 28) и новее:

```powershell
.\gradlew.bat :app:installDebug :app:connectedDebugAndroidTest
```

`StartupTest` запускает `MainActivity` и проверяет появление экрана создания
мастер-пароля или авторизации. Тест не сбрасывает существующее хранилище.
На MIUI для инструментальных тестов может потребоваться разрешение фонового
открытия Activity; блокировка системой видна в logcat как
`Abort background activity starts`. Установка через USB также должна быть разрешена.
Для ручной проверки используйте отдельное тестовое устройство: первый запуск,
создание мастер-пароля, добавление записи, перезапуск и повторный вход.

Версии зависимостей и плагинов находятся в `gradle/libs.versions.toml`.
Compose и Koin согласованы через BOM; Kotlin встроен в AGP 9, а плагины Compose
и сериализации используют одну версию Kotlin. Room остаётся в ветке 2.x;
формат базы и шифрования на этом этапе не меняется.
`minSdk` приложения остаётся 28, `targetSdk` — 35: изменение поведения Android
при повышении target SDK требует отдельной проверки.

## Архитектура и разработка

[ARCHITECTURE.md](ARCHITECTURE.md) описывает модули, совместимость, тесты и ограничения сервера.

Проверка стиля: `.\gradlew.bat checkKotlinFormat`. Форматирование: `.\gradlew.bat formatKotlin`.
