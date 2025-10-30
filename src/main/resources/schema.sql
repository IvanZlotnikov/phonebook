-- Индексы для оптимизации производительности

-- Индексы для таблицы contacts
CREATE INDEX IF NOT EXISTS idx_contacts_full_name ON contacts (full_name);
CREATE INDEX IF NOT EXISTS idx_contacts_department_id ON contacts (department_id);
CREATE INDEX IF NOT EXISTS idx_contacts_full_name_position ON contacts (full_name, position);

-- Индексы для таблицы departments
CREATE INDEX IF NOT EXISTS idx_departments_name ON departments (name);
CREATE INDEX IF NOT EXISTS idx_departments_parent_id ON departments (parent_department_id);

-- Индексы для таблицы users
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);