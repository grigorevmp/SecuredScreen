# SecuredScreen

Демо-проект для проверки best-effort защиты чувствительного UI от чтения через `AccessibilityService`, скриншотов и части вспомогательных системных каналов.

В репозитории есть не только victim-app, но и отдельный модуль `attacker`, который показывает, какие данные реально доступны внешнему accessibility service до и после включения защиты.

## Что внутри

- `app` — demo-приложение `com.grigorevmp.securedscreen` с двумя экранами: Compose и XML.
- `secure-ui` — библиотечный модуль с политиками защиты для Activity, View и Compose.
- `attacker` — demo-приложение `com.grigorevmp.attacker` с `AccessibilityService`, который логирует доступный UI victim app.

## Что делает `secure-ui`

При включённом secure mode библиотека может:

- скрывать accessibility-дерево через `importantForAccessibility`;
- очищать `AccessibilityEvent` и `AccessibilityNodeInfo`;
- ставить `FLAG_SECURE` для блокировки скриншотов и записи экрана;
- помечать View как sensitive на Android 14+ через `setAccessibilityDataSensitive(...)`;
- отключать assist/autofill best-effort;
- включать `filterTouchesWhenObscured`.

Текущее состояние режима хранится в `SharedPreferences` через `SecurityModeStore` и применяется ко всем экранам, которые на него подписаны.

## Сценарий демо

1. Собери и установи `app` и `attacker`.
2. Включи `UI Stealer Service` в системных настройках Accessibility.
3. Открой `Secure Screen Demo`.
4. Перейди на `Compose Screen` или `XML Screen`.
5. С выключенным switch в `attacker` будут видны тексты, значения полей и снимок accessibility-дерева.
6. Включи `Скрыть содержимое экрана`.
7. Открой тот же экран ещё раз и проверь, что логи в `attacker` стали пустыми, обрезанными или потеряли чувствительный контент.

## Быстрый старт

Требования:

- JDK 17
- Android SDK 36
- minSdk проекта: 24

Сборка debug APK:

```bash
./gradlew :app:assembleDebug :attacker:assembleDebug
```

Запуск из Android Studio:

- запускай `app` как victim-app;
- запускай `attacker` отдельно;
- после установки вручную включи accessibility service у `attacker`.

## Публичное API `secure-ui`

### Для Activity

```kotlin
@SecureScreen
class SensitiveActivity : SecureActivity()
```

`@SecureScreen` позволяет включать или отключать отдельные части политики:

```kotlin
@SecureScreen(
    blockAccessibilityTree = true,
    scrubAccessibilityPayload = true,
    markAccessibilityDataSensitive = true,
    blockAssistAndAutofill = true,
    blockScreenshots = true,
    filterTouchesWhenObscured = true,
)
class SensitiveActivity : SecureActivity()
```

### Для Compose

Применение host policy к корневому Compose View:

```kotlin
@Composable
fun SensitiveScreen() {
    ApplySecureComposeHostPolicy()
    val hidden by rememberSecureContentHidden()

    Column(
        modifier = Modifier.secureSemantics(hidden),
    ) {
        // sensitive content
    }
}
```

Полезные Compose API:

- `ApplySecureComposeHostPolicy(...)`
- `rememberSecurityModeStore()`
- `rememberSecureContentHidden()`
- `Modifier.secureSemantics()`
- `Modifier.secureSemantics(enabled)`

### Для View/XML

Разовое применение политики:

```kotlin
rootView.applySecurePolicy(
    enabled = true,
    config = SecureScreenConfig(),
)
```

Подписка View-дерева на глобальный режим:

```kotlin
rootView.bindSecurityMode(owner = viewLifecycleOwner)
```

Для XML в демо используется `SecureFrameLayout`, но это обычный `FrameLayout`-контейнер без собственной логики.

### Управление режимом

```kotlin
val store = SecurityModeStore.get(context)
store.setContentHidden(true)
```

Сокращение для `Context`:

```kotlin
val store = context.securityModeStore()
```

## Модули демо

### `app`

Главный экран позволяет:

- включать и выключать глобальный secure mode;
- открывать Compose-экран с чувствительным контентом;
- открывать XML-экран с чувствительным контентом;
- переходить в настройки Accessibility;
- запускать `attacker`, если он установлен.

### `attacker`

`StealerAccessibilityService` слушает события только от пакета `com.grigorevmp.securedscreen` и логирует:

- `event.text`;
- `event.contentDescription`;
- узлы из `event.source`;
- снимок `rootInActiveWindow`.

Это нужно именно для верификации того, что защита реально меняет наблюдаемое поведение accessibility layer.

## Ограничения

- Это best-effort защита, а не абсолютная гарантия.
- Блокировка accessibility-дерева может ломать легитимные accessibility-сценарии, включая screen readers и автоматизацию.
- `FLAG_SECURE` защищает от скриншотов, но сам по себе не решает проблему утечки через accessibility.
- Поведение может отличаться между версиями Android и OEM-прошивками.

## Текущее состояние проекта

- Kotlin 2.0.21
- Android Gradle Plugin 9.0.1
- Jetpack Compose BOM 2024.09.00
- compileSdk / targetSdk: 36
- minSdk: 24
