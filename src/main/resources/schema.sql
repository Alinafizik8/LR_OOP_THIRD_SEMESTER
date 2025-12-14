-- Создание таблицы пользователей (users)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы типов функций (function_types)
CREATE TABLE IF NOT EXISTS function_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    localized_name VARCHAR(255) NOT NULL,
    priority INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Вставка базовых типов
INSERT INTO function_types (name, localized_name, priority, created_at, updated_at)
VALUES
  ('TABULATED', 'Табулированная', 1, NOW(), NOW()),
  ('SIN', 'Синус', 2, NOW(), NOW()),
  ('COS', 'Косинус', 3, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Создание таблицы табулированных функций (tabulated_functions)
CREATE TABLE IF NOT EXISTS tabulated_functions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    signature TEXT, -- Поле может быть NULL, если не всегда заполняется
    owner_id BIGINT NOT NULL, -- Внешний ключ на пользователя
    type_id BIGINT NOT NULL,  -- Внешний ключ на тип функции
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE, -- Если пользователь удален, функции тоже удаляются
    FOREIGN KEY (type_id) REFERENCES function_types(id) -- Связь с типом функции
);

-- Создание таблицы точек функции (function_points)
CREATE TABLE IF NOT EXISTS function_points (
    id BIGSERIAL PRIMARY KEY,
    function_id BIGINT NOT NULL, -- Внешний ключ на табулированную функцию
    x_value DOUBLE PRECISION NOT NULL, -- Значение X
    y_value DOUBLE PRECISION NOT NULL, -- Значение Y
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (function_id) REFERENCES tabulated_functions(id) ON DELETE CASCADE -- Если функция удалена, точки тоже удаляются
);

CREATE INDEX IF NOT EXISTS idx_tabulated_functions_owner_id ON tabulated_functions(owner_id);
CREATE INDEX IF NOT EXISTS idx_tabulated_functions_type_id ON tabulated_functions(type_id);
CREATE INDEX IF NOT EXISTS idx_function_points_function_id ON function_points(function_id);
