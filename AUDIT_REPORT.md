# WLogger Audit Report

## 1) AGENTS.md coverage
- В рамках репозитория `/workspace/WLogger` файлов `AGENTS.md` на диске не найдено (проверено `find .. -name AGENTS.md -print`).
- Применены только инструкции, переданные в сообщении пользователя для области `/workspace/WLogger`.

## 2) Быстрый обзор структуры и ключевых модулей
- Сборка: Gradle (`build.gradle`, `settings.gradle`, wrapper).
- Точка входа плагина: `WLogger` (`src/main/java/me/penguinx13/wLogger/WLogger.java`).
- Команды: `CommandsExecutor`.
- Игровая логика: `BlockBreakListener`, `TreeRegenerationTask`, `LeafDecayTask`.
- Данные: `DataManager` (SQLite через `SQLiteManager`).
- Интеграции: Vault Economy, PlaceholderAPI.
- Конфигурация: `src/main/resources/config.yml`, `plugin.yml`.
- README в репозитории отсутствует.

## 3) Риск-скан по зонам

### Обработка ошибок
- В `onEnable()` при отсутствии Vault вызывается `onDisable()`, но сам плагин не отключается через PluginManager.
- В `claim` используется `Objects.requireNonNull(...getRegistration(Economy.class))`, что даёт NPE при отсутствии провайдера.
- `DataManager` выбрасывает `RuntimeException` на SQL-ошибках без graceful fallback.

### Валидация входа
- Базовая валидация аргументов команд присутствует.
- Логика admin-команд корректно ограничивает нижние границы для `backpack` и `costmultiplier`.

### Логирование
- Логирование ошибок БД недостаточно: исключения пробрасываются в рантайм без контекстного восстановления.

### Файлы/пути
- SQLite-файл строится из `plugin.getDataFolder()/players.db`.
- Путь не санитизируется (в текущем контексте низкий риск), но нет явной проверки доступности директории.

## 4) Несоответствия документации и реализации
- README отсутствует: нет описания команд, permission-узлов, зависимости от Vault/PlaceholderAPI.
- В `plugin.yml` нет секции `permissions`, хотя код использует `wlogger.admin`.

## 5) Список проблем с приоритетом

## [HIGH] 1. Плагин не отключается корректно при отсутствии Economy
**Где:** `WLogger.onEnable()`

**Симптом:** при отсутствии Vault registration вызывается `onDisable()`, но plugin lifecycle остаётся активным.

**Риск:** runtime-ошибки в командах/логике, т.к. дальше код предполагает наличие Economy.

**Issue:** `WL-001: Корректно отключать плагин при отсутствии Economy provider`

**Task-stub (required format):**
```yaml
task_stub:
  id: WL-001
  title: "Disable plugin via PluginManager when Economy is unavailable"
  priority: high
  type: bugfix
  files:
    - src/main/java/me/penguinx13/wLogger/WLogger.java
  actions:
    - Replace manual onDisable() call with Bukkit.getPluginManager().disablePlugin(this)
    - Return immediately after disable to stop further initialization
  acceptance_criteria:
    - Plugin fully disables when Economy registration is missing
    - No command/listener remains active in this scenario
```

## [HIGH] 2. Потенциальный NPE в `/wlogger claim`
**Где:** `CommandsExecutor.onCommand()`

**Симптом:** `Objects.requireNonNull(getRegistration(Economy.class))` может выбросить NPE.

**Риск:** падение выполнения команды для игрока.

**Issue:** `WL-002: Защитить claim от отсутствующего Economy provider`

**Task-stub (required format):**
```yaml
task_stub:
  id: WL-002
  title: "Handle missing Economy provider gracefully in claim command"
  priority: high
  type: bugfix
  files:
    - src/main/java/me/penguinx13/wLogger/CommandsExecutor.java
  actions:
    - Replace requireNonNull with explicit null-check for registration/provider
    - Send user-friendly message and abort claim when provider is absent
  acceptance_criteria:
    - /wlogger claim does not throw NPE when Economy is unavailable
    - Player receives clear error message
```

## [MEDIUM] 3. Проверка мира в `isLocation` допускает только равенство min/max world
**Где:** `BlockBreakListener.isLocation()`

**Симптом:** условие фактически требует, чтобы `location.min.world == location.max.world`; при расхождении регион всегда невалиден.

**Риск:** тихая неработоспособность зоны при конфигурационных ошибках.

**Issue:** `WL-003: Валидировать world-конфиг и явно сообщать о mismatch`

**Task-stub (required format):**
```yaml
task_stub:
  id: WL-003
  title: "Improve world-bound validation for location region"
  priority: medium
  type: bugfix
  files:
    - src/main/java/me/penguinx13/wLogger/BlockBreakListener.java
    - src/main/resources/config.yml
  actions:
    - Add explicit check/log warning when min.world != max.world
    - Keep region logic predictable and observable for admins
  acceptance_criteria:
    - Misconfigured worlds are reported in logs
    - Region behavior is deterministic and documented
```

## [MEDIUM] 4. Отсутствуют permissions в `plugin.yml`
**Где:** `src/main/resources/plugin.yml`

**Симптом:** в коде используется `wlogger.admin`, но permission не объявлен в descriptor.

**Риск:** админ-права хуже управляются/документируются.

**Issue:** `WL-004: Добавить декларацию permission wlogger.admin в plugin.yml`

**Task-stub (required format):**
```yaml
task_stub:
  id: WL-004
  title: "Declare wlogger.admin permission in plugin descriptor"
  priority: medium
  type: docs-config
  files:
    - src/main/resources/plugin.yml
  actions:
    - Add permissions section with wlogger.admin default/op description
  acceptance_criteria:
    - plugin.yml includes wlogger.admin permission node
    - Admin command access is documented and discoverable
```

## [LOW] 5. Отсутствует README/операционная документация
**Где:** репозиторий (root)

**Симптом:** нет описания конфигурации, зависимостей, миграций и команд.

**Риск:** ошибки эксплуатации и конфигурации.

**Issue:** `WL-005: Добавить README с командами, permissions и зависимостями`

**Task-stub (required format):**
```yaml
task_stub:
  id: WL-005
  title: "Create README with setup, dependencies, commands and config"
  priority: low
  type: documentation
  files:
    - README.md
  actions:
    - Document required plugins (Vault, economy provider, PlaceholderAPI)
    - Document /wlogger subcommands and permissions
    - Document config.yml fields and defaults
  acceptance_criteria:
    - New operators can install and configure plugin without reading source code
```
