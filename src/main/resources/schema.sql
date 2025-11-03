-- Индексы для оптимизации производительности

-- Индексы для таблицы contacts (обновлены для раздельных полей ФИО)
CREATE INDEX IF NOT EXISTS idx_contacts_last_name ON contacts (last_name);
CREATE INDEX IF NOT EXISTS idx_contacts_first_name ON contacts (first_name);
CREATE INDEX IF NOT EXISTS idx_contacts_middle_name ON contacts (middle_name);
CREATE INDEX IF NOT EXISTS idx_contacts_department_id ON contacts (department_id);
CREATE INDEX IF NOT EXISTS idx_contacts_full_name_position ON contacts (last_name, first_name, middle_name, position);

-- Индексы для таблицы departments
CREATE INDEX IF NOT EXISTS idx_departments_name ON departments (name);
CREATE INDEX IF NOT EXISTS idx_departments_parent_id ON departments (parent_department_id);

-- Индексы для таблицы users
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);
