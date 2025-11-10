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