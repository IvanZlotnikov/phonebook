-- Подразделения
INSERT INTO departments (name, parent_department_id)
VALUES ('Руководство', NULL),
       ('IT Отдел', NULL),
       ('Бухгалтерия', NULL),
       ('Разработка', 2),
       ('Тестирование', 2),
       ('Системное администрирование', 2)
ON CONFLICT (name) DO NOTHING;


-- Пользователи для входа
INSERT INTO users (username, password, role, enabled)
VALUES ('user',
        '$2a$10$YmT2fwPl5MmNPWlr2anoeumsP1SD9RWwdNqLxRwt/5NEEAIEziP3i',
        'ROLE_USER' ,
        true),
       ('admin',
        '$2a$10$Q9RYENUTqzpOBwORlXjt0OzocNQUsJQsojWxGjX9KHyAIQ7KNDm62',
        'ROLE_ADMIN',
        true)
ON CONFLICT (username) DO NOTHING;

--
-- -- Роли пользователей
-- INSERT INTO authorities (username, authority)
-- VALUES ('user', 'ROLE_USER'),
--        ('admin', 'ROLE_ADMIN')
-- ON CONFLICT (username, authority) DO NOTHING;
-- --
-- --
-- -- Тестовые контакты

-- Контакты для Руководства
INSERT INTO contacts (full_name, position, department_id) VALUES
                                                              ('Иванов Иван Иванович', 'Генеральный директор', 1),
                                                              ('Петрова Мария Сергеевна', 'Заместитель директора', 1)
ON CONFLICT DO NOTHING;

-- Контакты для Разработки
INSERT INTO contacts (full_name, position, department_id) VALUES
                                                              ('Сидоров Алексей Петрович', 'Senior разработчик', 4),
                                                              ('Козлова Анна Викторовна', 'Middle разработчик', 4),
                                                              ('Никитин Дмитрий Олегович', 'Junior разработчик', 4)
ON CONFLICT DO NOTHING;

-- Контакты для Тестирования
INSERT INTO contacts (full_name, position, department_id) VALUES
                                                              ('Федоров Сергей Михайлович', 'Lead QA инженер', 5),
                                                              ('Орлова Екатерина Дмитриевна', 'QA инженер', 5)
ON CONFLICT DO NOTHING;

-- Контакты для Системного администрирования
INSERT INTO contacts (full_name, position, department_id) VALUES
                                                              ('Васильев Андрей Игоревич', 'Системный администратор', 6),
                                                              ('Морозова Ольга Александровна', 'Сетевой инженер', 6)
ON CONFLICT DO NOTHING;

-- Контакты для Бухгалтерии
INSERT INTO contacts (full_name, position, department_id) VALUES
                                                              ('Григорьева Татьяна Владимировна', 'Главный бухгалтер', 3),
                                                              ('Кузнецов Павел Николаевич', 'Бухгалтер', 3)
ON CONFLICT DO NOTHING;

-- ▲▲▲ ДОБАВЛЕНО: Телефоны для контактов ▲▲▲

-- Телефоны для руководства
INSERT INTO contact_work_phones (contact_id, phone_number) VALUES
                                                               (1, '+7 (495) 111-11-11'),
                                                               (1, '+7 (495) 111-11-12'),
                                                               (2, '+7 (495) 111-11-13')
ON CONFLICT DO NOTHING;

INSERT INTO contact_work_mobile_phones (contact_id, phone_number) VALUES
                                                                      (1, '+7 (916) 111-11-11'),
                                                                      (2, '+7 (916) 111-11-12')
ON CONFLICT DO NOTHING;

INSERT INTO contact_personal_phones (contact_id, phone_number) VALUES
    (1, '+7 (925) 111-11-11')
ON CONFLICT DO NOTHING;

-- Телефоны для разработчиков
INSERT INTO contact_work_phones (contact_id, phone_number) VALUES
                                                               (3, '+7 (495) 222-22-21'),
                                                               (4, '+7 (495) 222-22-22'),
                                                               (5, '+7 (495) 222-22-23')
ON CONFLICT DO NOTHING;

INSERT INTO contact_work_mobile_phones (contact_id, phone_number) VALUES
                                                                      (3, '+7 (916) 222-22-21'),
                                                                      (4, '+7 (916) 222-22-22')
ON CONFLICT DO NOTHING;

-- Телефоны для тестировщиков
INSERT INTO contact_work_phones (contact_id, phone_number) VALUES
                                                               (6, '+7 (495) 333-33-31'),
                                                               (7, '+7 (495) 333-33-32')
ON CONFLICT DO NOTHING;

-- Телефоны для сисадминов
INSERT INTO contact_work_phones (contact_id, phone_number) VALUES
                                                               (8, '+7 (495) 444-44-41'),
                                                               (9, '+7 (495) 444-44-42')
ON CONFLICT DO NOTHING;

INSERT INTO contact_work_mobile_phones (contact_id, phone_number) VALUES
                                                                      (8, '+7 (916) 444-44-41'),
                                                                      (9, '+7 (916) 444-44-42')
ON CONFLICT DO NOTHING;

-- Телефоны для бухгалтерии
INSERT INTO contact_work_phones (contact_id, phone_number) VALUES
                                                               (10, '+7 (495) 555-55-51'),
                                                               (11, '+7 (495) 555-55-52')
ON CONFLICT DO NOTHING;