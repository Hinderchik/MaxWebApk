# MaxWebApk

Android-приложение-обёртка для веб-версии мессенджера **MAX** (https://web.max.ru).

Полноэкранный WebView с десктопным User-Agent, встроенным сертификатом Минцифры (Russian Trusted Root CA) и поддержкой всех веб-функций MAX (звонки, камера, микрофон, уведомления).

---

## 📱 Особенности

- **Полноэкранный WebView** — никаких нативных панелей, только веб-интерфейс
- **Десктопный User-Agent** (Chrome 120 / Windows 10) — MAX открывается в полной версии
- **Встроенный сертификат Минцифры** — Russian Trusted Root CA (PEM, Base64) прямо в коде, без внешних файлов
- **SSL доверие** — `onReceivedSslError` → `proceed()` для доменов `*.max.ru` / `*.oneme.ru`
- **WebRTC поддержка** — камера, микрофон, звонки через `WebChromeClient.onPermissionRequest`
- **Runtime permissions** — CAMERA, RECORD_AUDIO запрашиваются при старте
- **Back-браузер** — кнопка «Назад» → `webView.goBack()`
- **Минимальный API 24** (Android 7.0), таргет 34
- **Пакет**: `com.maxweb.xunkal1`

---

## 🔐 Сертификат Минцифры

Используется публичный корневой сертификат **Russian Trusted Root CA** (Ministry of Digital Development), источник:  
https://github.com/koenrh/russian-trusted-root-ca (файл `root-ca_rsa-2022.pem`)

PEM-строка вшита в `MainActivity.RUSSIAN_TRUSTED_ROOT_CA_PEM` (companion object, `@JvmField`).

> ⚠️ Сертификат используется **только для доверия HTTPS-соединениям к доменам MAX**. Это осознанное решение для обхода отсутствия сертификата в системном хранилище.

---

## 🛠 Сборка

### Требования
- Android SDK (compileSdk 34, build-tools 34.0.0)
- JDK 17+
- Gradle 8.6+ (wrapper генерируется автоматически)

### Команды
```bash
cd MaxWebApk
./gradlew assembleDebug
```

APK появится в `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📦 Установка

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

При первом запуске запросит разрешения:
- 📷 Камера
- 🎤 Микрофон

---

## 📂 Структура проекта

```
MaxWebApk/
├── settings.gradle.kts
├── build.gradle.kts          # AGP 8.4.0, Kotlin 1.9.20
├── gradle.properties         # android.useAndroidX=true
├── local.properties          # sdk.dir (локально)
├── LICENSE                   # MIT
├── .gitignore
├── README.md
└── app/
    ├── build.gradle.kts      # compileSdk 34, targetSdk 34
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/maxweb/xunkal1/
        │   ├── MainActivity.kt
        │   └── TrustManagerFactoryHelper.kt
        └── res/
            ├── layout/activity_main.xml
            ├── values/
            │   ├── strings.xml
            │   ├── colors.xml
            │   └── themes.xml
            └── mipmap-*/ic_launcher.png
```

---

## 🧪 Логи работы

```
Displayed com.maxweb.xunkal1/.MainActivity for user 0: +993ms
[0] connection attempt #0
[0] awaiting network...
[0] network online, connecting...
[0] connection opened
[0] session inited
[0] user logged in
```

---

## ⚖️ Лицензия

MIT License — см. файл [LICENSE](LICENSE).

---

## ⚠️ Дисклеймер

Это **неофициальная** обёртка для веб-версии MAX.  
Все торговые знаки, логотипы и сертификаты принадлежат их правообладателям (VK / Минцифры РФ).  
Приложение создано в образовательных/личных целях. Используйте на свой страх и риск.