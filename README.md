# MaxWebApk — Android приложение для MAX

Android-приложение-обёртка для веб-версии мессенджера **MAX** (https://web.max.ru).

## 🚀 Новые возможности

### ✅ Push-уведомления (Firebase Cloud Messaging)
- Получение уведомлений о новых сообщениях
- Уведомления о входящих звонках
- Каналы уведомлений: сообщения и звонки
- Работает даже когда приложение закрыто

### 📞 Звонки
- Поддержка WebRTC звонков через WebView
- Входящие звонки с полноэкранным уведомлением
- Мелодия вызова через foreground service
- Кнопки "Принять" и "Отклонить" в уведомлении

### 📁 Отправка файлов
- **Фото** — выбор из галереи или съёмка камерой
- **Видео** — выбор из галереи или запись камерой
- **Аудио** — выбор аудиофайлов
- **Документы** — любые файлы с устройства
- Поддержка множественного выбора

---

## 📱 Особенности

- **Полноэкранный WebView** — никаких нативных панелей
- **Десктопный User-Agent** (Chrome 120 / Windows 10)
- **Встроенный сертификат Минцифры** — Russian Trusted Root CA
- **SSL доверие** — для доменов `*.max.ru` / `*.oneme.ru`
- **WebRTC поддержка** — камера, микрофон, звонки
- **Push-уведомления** — Firebase Cloud Messaging
- **File Picker** — загрузка фото, видео, файлов
- **Минимальный API 24** (Android 7.0), таргет 34

---

## 🔧 Настройка Firebase

### 1. Создайте проект в Firebase Console
1. Перейдите на https://console.firebase.google.com/
2. Создайте новый проект
3. Добавьте Android-приложение с пакетом `com.maxweb.xunkal1`
4. Скачайте `google-services.json` и замените файл в `app/google-services.json`

### 2. Включите Cloud Messaging
1. В Firebase Console перейдите в **Cloud Messaging**
2. Создайте топик `max_users` для рассылки уведомлений

### 3. Отправка уведомлений

**Пример payload для сообщения:**
```json
{
  "to": "/topics/max_users",
  "data": {
    "type": "message",
    "sender": "Иван",
    "message": "Привет! Как дела?",
    "messageId": "12345",
    "timestamp": "1699123456"
  }
}
```

**Пример payload для звонка:**
```json
{
  "to": "/topics/max_users",
  "data": {
    "type": "call_request",
    "sender": "Иван",
    "messageId": "call_12345"
  }
}
```

---

## 🛠 Сборка

### Требования
- Android SDK (compileSdk 36, build-tools 35.0.0)
- JDK 17+
- Gradle 8.6+
- Firebase проект с настроенным FCM

### Команды
```bash
cd MaxWebApk
./gradlew assembleDebug
```

APK появится в `app/build/outputs/apk/debug/app-debug.apk`.

### Сборка релизной версии
```bash
./gradlew assembleRelease
```

---

## 📦 Установка

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

При первом запуске запросит разрешения:
- 📷 Камера
- 🎤 Микрофон
- 📁 Файлы (для Android 13+: фото, видео, аудио)
- 🔔 Уведомления (для Android 13+)

---

## 📂 Структура проекта

```
MaxWebApk/
├── app/
│   ├── src/main/
│   │   ├── java/com/maxweb/xunkal1/
│   │   │   ├── MainActivity.kt              # Основная активность
│   │   │   ├── MaxFirebaseMessagingService.kt  # FCM сервис
│   │   │   ├── MaxIncomingCallReceiver.kt   # Приём звонков
│   │   │   ├── MaxCallRingService.kt        # Мелодия вызова
│   │   │   ├── FileChooserHandler.kt        # Выбор файлов
│   │   │   ├── TrustManagerFactoryHelper.kt # SSL сертификат
│   │   │   └── prefs/AppPrefs.kt            # Настройки
│   │   ├── res/
│   │   │   ├── drawable/                    # Иконки уведомлений
│   │   │   └── xml/file_paths.xml           # FileProvider пути
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json                 # Firebase конфиг
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🔐 Разрешения

| Разрешение | Назначение |
|------------|------------|
| `INTERNET` | Доступ к интернету |
| `CAMERA` | Видеозвонки, фото |
| `RECORD_AUDIO` | Аудиозвонки |
| `READ_MEDIA_*` | Выбор файлов (Android 13+) |
| `POST_NOTIFICATIONS` | Push-уведомления (Android 13+) |
| `FOREGROUND_SERVICE` | Мелодия вызова |

---

## 🧪 Логи работы

```
D/FCM: Token: eX...xyz
D/MaxFcmService: Message received from: /topics/max_users
D/MaxFcmService: Notification sent: Иван - Привет!
D/MaxCallRingService: Starting ringtone for call from Иван
```

---

## ⚖️ Лицензия

MIT License — см. файл [LICENSE](LICENSE).

---

## ⚠️ Дисклеймер

Это **неофициальная** обёртка для веб-версии MAX.  
Все торговые знаки, логотипы и сертификаты принадлежат их правообладателям (VK / Минцифры РФ).  
Приложение создано в образовательных/личных целях.
