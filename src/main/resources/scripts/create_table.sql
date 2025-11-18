CREATE TABLE IF NOT EXISTS users (...);
CREATE TABLE IF NOT EXISTS function_types (...);
CREATE TABLE IF NOT EXISTS tabulated_functions (...);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE function_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    localized_name VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE tabulated_functions (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    function_type_id BIGINT NOT NULL REFERENCES function_types(id) ON DELETE CASCADE,
    serialized_data BYTEA NOT NULL, -- бинарные данные функции (например, сериализованный объект)
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Индексы
CREATE INDEX idx_tabulated_functions_owner_id ON tabulated_functions(owner_id);
CREATE INDEX idx_tabulated_functions_function_type_id ON tabulated_functions(function_type_id);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_tabulated_functions_owner_id') THEN
        CREATE INDEX idx_tabulated_functions_owner_id ON tabulated_functions(owner_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_tabulated_functions_function_type_id') THEN
        CREATE INDEX idx_tabulated_functions_function_type_id ON tabulated_functions(function_type_id);
    END IF;
END $$;