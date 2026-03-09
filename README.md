# 🤝 ShareIt

**ShareIt** — это многомодульное Java-приложение для шеринга вещей между пользователями.
Сервис позволяет публиковать вещи, искать доступные предметы, создавать запросы на вещи, бронировать их на определённый период и оставлять комментарии после завершённой аренды.

Проект построен как **два Spring Boot приложения**:
- **gateway** — внешний слой, который принимает HTTP-запросы, валидирует входные данные и проксирует их в основной сервис;
- **server** — основной backend с бизнес-логикой, JPA-репозиториями и работой с PostgreSQL.

---

## Возможности

### Пользователи
- создание пользователя;
- частичное обновление профиля;
- получение пользователя по `id`;
- удаление пользователя.

### Вещи
- добавление вещи владельцем;
- редактирование вещи только её владельцем;
- просмотр вещи по `id`;
- получение списка всех вещей владельца;
- поиск доступных вещей по тексту в названии или описании;
- привязка вещи к запросу на вещь.

### Бронирования
- создание бронирования вещи другим пользователем;
- подтверждение или отклонение бронирования владельцем вещи;
- просмотр бронирования владельцем или автором бронирования;
- получение списка бронирований по статусам:
  - `ALL`
  - `CURRENT`
  - `PAST`
  - `FUTURE`
  - `WAITING`
  - `REJECTED`

### Комментарии
- пользователь может оставить комментарий к вещи **только после завершённого подтверждённого бронирования**.

### Запросы вещей
- создание запроса на вещь;
- просмотр собственных запросов;
- просмотр запросов других пользователей;
- получение запроса по `id` вместе со связанными вещами.

---

## Архитектура проекта

Проект является **multi-module Maven** приложением:

```text
shareit/
├── gateway/   # валидация, входной REST-слой, проксирование запросов
├── server/    # бизнес-логика, JPA, PostgreSQL
├── postman/   # Postman collection для проверки API
└── .github/   # GitHub Actions workflow
```

### Модуль `gateway`

Отвечает за:
- приём внешних HTTP-запросов;
- валидацию DTO через Jakarta Validation;
- базовую обработку ошибок валидации;
- проксирование запросов в `server` через `RestTemplate`.

Порт по умолчанию:
- `8080`

Ключевая настройка:

```properties
server.port=8080
shareit-server.url=http://localhost:9090
```

### Модуль `server`

Отвечает за:
- бизнес-логику приложения;
- работу с пользователями, вещами, бронированиями, комментариями и запросами;
- доступ к БД через Spring Data JPA;
- хранение данных в PostgreSQL.

Порт по умолчанию:
- `9090`

Ключевые настройки:

```properties
server.port=9090
spring.datasource.url=jdbc:postgresql://localhost:5432/shareit
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always
```

---

## Стек технологий

- **Java 21**
- **Spring Boot 3.3.2**
- **Spring Web**
- **Spring Data JPA**
- **Jakarta Validation**
- **PostgreSQL**
- **H2** (для тестов)
- **Lombok**
- **MapStruct**
- **Maven**
- **JUnit 5 / Spring Boot Test**
- **Checkstyle**
- **SpotBugs**
- **JaCoCo**

---

## Бизнес-правила

В проекте реализованы важные ограничения:

- владелец не может бронировать собственную вещь;
- нельзя бронировать недоступную вещь (`available = false`);
- бронирование должно быть создано с датами в будущем;
- `end` должен быть строго позже `start`;
- подтвердить или отклонить бронирование может только владелец вещи;
- повторно обработать уже подтверждённое/отклонённое бронирование нельзя;
- комментарий разрешён только после завершённого подтверждённого бронирования;
- email пользователя должен быть уникальным.

---

## Заголовок авторизации/идентификации пользователя

Во многих запросах используется обязательный HTTP-заголовок:

```http
X-Sharer-User-Id
```

Именно по нему сервис понимает, от имени какого пользователя выполняется операция.

---

## Основные REST endpoint'ы

Ниже перечислены основные публичные маршруты, доступные через `gateway`.

### Пользователи

| Метод | Endpoint | Описание |
|---|---|---|
| `POST` | `/users` | Создать пользователя |
| `PATCH` | `/users/{userId}` | Обновить пользователя |
| `GET` | `/users/{userId}` | Получить пользователя по id |
| `DELETE` | `/users/{userId}` | Удалить пользователя |

### Вещи

| Метод | Endpoint | Описание |
|---|---|---|
| `POST` | `/items` | Создать вещь |
| `PATCH` | `/items/{itemId}` | Обновить вещь |
| `GET` | `/items/{itemId}` | Получить вещь по id |
| `GET` | `/items` | Получить вещи владельца |
| `GET` | `/items/search?text=...` | Поиск доступных вещей |
| `POST` | `/items/{itemId}/comment` | Добавить комментарий |

### Бронирования

| Метод | Endpoint | Описание |
|---|---|---|
| `POST` | `/bookings` | Создать бронирование |
| `PATCH` | `/bookings/{bookingId}?approved=true/false` | Подтвердить или отклонить бронирование |
| `GET` | `/bookings/{bookingId}` | Получить бронирование по id |
| `GET` | `/bookings?state=ALL` | Получить бронирования пользователя |
| `GET` | `/bookings/owner?state=ALL` | Получить бронирования вещей владельца |

### Запросы вещей

| Метод | Endpoint | Описание |
|---|---|---|
| `POST` | `/requests` | Создать запрос |
| `GET` | `/requests` | Получить собственные запросы |
| `GET` | `/requests/all` | Получить запросы других пользователей |
| `GET` | `/requests/{requestId}` | Получить запрос по id |

---

## Валидация входных данных

В `gateway` настроена проверка DTO.

### Пользователь
- `name` — обязателен при создании, не должен быть пустым;
- `email` — обязателен при создании, должен быть корректным email;
- при обновлении пустые строки для `name` и `email` запрещены.

### Вещь
- `name` — обязателен при создании;
- `description` — обязательна при создании;
- `available` — обязательное поле;
- `requestId`, если передан, должен быть положительным.

### Бронирование
- `start` — обязательно;
- `end` — обязательно;
- `itemId` — обязательно и должно быть положительным;
- дополнительно контроллер проверяет, что даты корректны и находятся в будущем.

### Комментарий
- `text` — не должен быть пустым.

### Запрос вещи
- `description` — не должна быть пустой.

---

## Структура БД

Схема инициализируется из файла:

```text
server/src/main/resources/schema.sql
```

### Таблицы

#### `users`
- `id`
- `name`
- `email` (unique)

#### `requests`
- `id`
- `description`
- `requestor_id`
- `created`

#### `items`
- `id`
- `name`
- `description`
- `available`
- `owner_id`
- `request_id`

#### `bookings`
- `id`
- `start_date`
- `end_date`
- `item_id`
- `booker_id`
- `status`

#### `comments`
- `id`
- `text`
- `item_id`
- `author_id`
- `created`

---

## Слои приложения

### В `server`

Проект разделён на привычные слои:
- `controller` — REST endpoints;
- `service` — бизнес-логика;
- `repository` — доступ к данным через Spring Data JPA;
- `model` — JPA-сущности;
- `dto` — объекты передачи данных;
- `mapper` — преобразование между сущностями и DTO.

Основные доменные пакеты:
- `user`
- `item`
- `booking`
- `request`
- `exception`

---

## Тестирование

В проекте уже есть набор unit/integration тестов для обоих модулей.

### Что покрыто тестами
- контроллеры `gateway`;
- контроллеры `server`;
- JSON-сериализация DTO;
- интеграционные тесты сервисов:
  - `UserServiceImplIT`
  - `BookingServiceImplIT`
  - `ItemRequestServiceImplIT`
  - `ItemServiceCreateWithRequestIT`
  - `ItemServiceFindByOwnerIT`

Для тестов используется **H2** в памяти в режиме совместимости с PostgreSQL:

```properties
spring.datasource.url=jdbc:h2:mem:shareit;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## CI

В репозитории подключён GitHub Actions workflow:

```text
.github/workflows/api-tests.yml
```

Он запускается на `pull_request` и использует внешний workflow:

```yaml
uses: yandex-praktikum/java-shareit/.github/workflows/api-tests.yml@ci
```

Это означает, что проект уже подготовлен к автоматической проверке API-тестами в CI.

---

## Postman

В репозитории есть коллекция:

```text
postman/sprint.json
```

Её можно импортировать в Postman и использовать для ручной проверки API.

---

## Как запустить проект локально

### 1. Требования

Убедитесь, что установлены:
- **JDK 21**
- **Maven 3.9+**
- **PostgreSQL**

### 2. Создать базу данных

Создайте БД `shareit` в PostgreSQL.

Пример:

```sql
CREATE DATABASE shareit;
```

При необходимости настройте пользователя/пароль под значения из `server/src/main/resources/application.properties`.

### 3. Запустить `server`

Из корня проекта:

```bash
mvn -pl server spring-boot:run
```

Сервис поднимется на:

```text
http://localhost:9090
```

### 4. Запустить `gateway`

В новом терминале:

```bash
mvn -pl gateway spring-boot:run
```

Gateway будет доступен на:

```text
http://localhost:8080
```

Все внешние запросы следует отправлять в `gateway`.

---

## Сборка проекта

Собрать все модули:

```bash
mvn clean package
```

Запустить тесты:

```bash
mvn test
```

Проверка стиля кода:

```bash
mvn -Pcheck checkstyle:check
```

---

## Примеры запросов

### Создание пользователя

```http
POST /users
Content-Type: application/json

{
  "name": "Igor",
  "email": "igor@example.com"
}
```

### Создание вещи

```http
POST /items
X-Sharer-User-Id: 1
Content-Type: application/json

{
  "name": "Дрель",
  "description": "Аккумуляторная дрель",
  "available": true
}
```

### Поиск вещей

```http
GET /items/search?text=дрель
X-Sharer-User-Id: 2
```

### Создание бронирования

```http
POST /bookings
X-Sharer-User-Id: 2
Content-Type: application/json

{
  "itemId": 1,
  "start": "2026-03-10T10:00:00",
  "end": "2026-03-12T10:00:00"
}
```

### Подтверждение бронирования

```http
PATCH /bookings/1?approved=true
X-Sharer-User-Id: 1
```

### Создание запроса на вещь

```http
POST /requests
X-Sharer-User-Id: 2
Content-Type: application/json

{
  "description": "Нужна стремянка на выходные"
}
```

---

## Что можно улучшить дальше

Потенциальные направления развития проекта:
- добавить Docker / Docker Compose для быстрого старта;
- вынести конфигурацию БД в переменные окружения;
- добавить Swagger / OpenAPI документацию;
- реализовать пагинацию на уровне `server` для списков бронирований, вещей и запросов;
- добавить централизованный обработчик ошибок в `server` с единым форматом ответа;
- расширить покрытие интеграционными тестами сложных сценариев.

---
