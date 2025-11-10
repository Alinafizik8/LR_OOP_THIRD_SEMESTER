-- Создание:
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