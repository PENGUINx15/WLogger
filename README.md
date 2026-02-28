# WLogger

Плагин для сервера Minecraft (Paper/Spigot 1.19+), который реализует механику лесорубки:
игрок "собирает" дерево по прогрессу, получает логи в счётчик и может обменять их на деньги через `/wlogger claim`.

## Зависимости

### Обязательные
- **Vault**
- **Экономический провайдер** (EssentialsX Economy, CMI Economy и т.п.)

### Опциональные
- **PlaceholderAPI** (для регистрации плейсхолдеров WLogger)

## Команды

- `/wlogger claim` — сдать накопленные блоки и получить награду.
- `/wlogger reload` — перезагрузить `config.yml` (требуется `wlogger.admin`).
- `/wlogger <set|add|rem> <backpack|costmultiplier> <значение>` — изменить server-default значения в БД (требуется `wlogger.admin`).

## Права

- `wlogger.admin` — доступ к административным подкомандам (`reload`, `set/add/rem`).
  - По умолчанию: `op`.

## Конфигурация (`config.yml`)

### Регион работы лесорубки
- `location.min.world`, `location.max.world` — мир региона (должны совпадать).
- `location.min.x/y/z`, `location.max.x/y/z` — границы кубоида.

### Экономика и таймеры
- `tree.reward` — выплата за 1 блок при `claim`.
- `tree.cooldown` — время восстановления дерева (секунды).

### Значения по умолчанию
- `defaultValues.backpack` — базовый размер "рюкзака".
- `defaultValues.costmultiplier` — множитель награды по умолчанию.

## Установка

1. Соберите jar: `./gradlew build`.
2. Поместите jar в папку `plugins`.
3. Установите Vault и экономический провайдер.
4. Запустите сервер и настройте `plugins/WLogger/config.yml`.
5. Выдайте админ-права при необходимости: `wlogger.admin`.
