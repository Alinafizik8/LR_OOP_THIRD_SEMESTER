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