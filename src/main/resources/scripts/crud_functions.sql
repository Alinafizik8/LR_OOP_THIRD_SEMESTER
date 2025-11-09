-- <<<<<>>>>> users

-- Создание:
INSERT INTO users (email, username, password_hash, role, created_at, updated_at)
VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
RETURNING id;

-- Чтение - Все пользователи:
SELECT id, email, username, password_hash, role, created_at, updated_at
FROM users;

-- Чтение по ID:
SELECT id, email, username, password_hash, role, created_at, updated_at
FROM users
WHERE id = ?;

-- Чтение по username:
SELECT id, email, username, password_hash, role, created_at, updated_at
FROM users
WHERE username = ?;

-- Чтение по email:
SELECT id, email, username, password_hash, role, created_at, updated_at
FROM users
WHERE email = ?;

-- Обновление - изменение роли:
UPDATE users
SET role = ?, updated_at = CURRENT_TIMESTAMP
WHERE id = ?;

-- Удаление:
DELETE FROM users
WHERE id = ?;


-- <<<<<>>>>> function_types

-- Создание (INSERT):
INSERT INTO function_types (name, localized_name, priority, created_at, updated_at)
VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
RETURNING id;

-- Чтение - Все типы (упорядоченные по приоритету и названию):
SELECT id, name, localized_name, priority, created_at, updated_at
FROM function_types
ORDER BY priority ASC, localized_name ASC;

-- Чтение по ID:
SELECT id, name, localized_name, priority, created_at, updated_at
FROM function_types
WHERE id = ?;

-- Чтение по name:
SELECT id, name, localized_name, priority, created_at, updated_at
FROM function_types
WHERE name = ?;

-- Обновление - изменение приоритета:
UPDATE function_types
SET priority = ?, updated_at = CURRENT_TIMESTAMP
WHERE id = ?;

-- Удаление:
DELETE FROM function_types
WHERE id = ?;


-- <<<<<>>>>> ЗАПРОСЫ ДЛЯ ТАБЛИЦЫ functions

-- Создание (INSERT):
INSERT INTO functions (owner_id, function_type_id, serialized_data, name, created_at, updated_at)
VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
RETURNING id;

-- Чтение - все функции пользователя (без serialized_data):
SELECT id, owner_id, function_type_id, name, created_at, updated_at
FROM functions
WHERE owner_id = ?
ORDER BY created_at DESC;

-- Чтение - только serialized_data и function_type_id по ID (и проверка владельца):
SELECT serialized_data, function_type_id
FROM functions
WHERE id = ? AND owner_id = ?;

-- Чтение - полные метаданные по ID (и проверка владельца):
SELECT id, owner_id, function_type_id, name, created_at, updated_at
FROM functions
WHERE id = ? AND owner_id = ?;

-- Обновление - изменение имени функции:
UPDATE functions
SET name = ?, updated_at = CURRENT_TIMESTAMP
WHERE id = ? AND owner_id = ?;

-- Удаление:
DELETE FROM functions
WHERE id = ? AND owner_id = ?;