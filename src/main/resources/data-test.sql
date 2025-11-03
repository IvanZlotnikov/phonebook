-- Скрипт для генерации 10 000 тестовых контактов с телефонами.
-- ФИО разделено на три отдельных поля для точного поиска.
-- Внимание: выполнение этого скрипта может занять некоторое время.

-- Создаем временную функцию для генерации случайного номера телефона
CREATE OR REPLACE FUNCTION generate_phone() RETURNS TEXT AS $$
BEGIN
    RETURN '+7 (' || (100 + floor(random() * 900))::int || ') ' ||
           (100 + floor(random() * 900))::int || '-' ||
           (10 + floor(random() * 90))::int || '-' ||
           (10 + floor(random() * 90))::int;
END;
$$ LANGUAGE plpgsql;

DO $$
    DECLARE
        -- Массивы для генерации ФИО
        last_names TEXT[] := ARRAY['Смирнов', 'Иванов', 'Кузнецов', 'Соколов', 'Попов', 'Лебедев', 'Козлов', 'Новиков', 'Морозов', 'Петров', 'Волков', 'Соловьёв', 'Васильев', 'Зайцев', 'Павлов'];
        male_first_names TEXT[] := ARRAY['Александр', 'Дмитрий', 'Максим', 'Сергей', 'Андрей', 'Алексей', 'Артём', 'Илья', 'Кирилл', 'Михаил'];
        female_first_names TEXT[] := ARRAY['Анастасия', 'Мария', 'Анна', 'Виктория', 'Екатерина', 'Наталья', 'Марина', 'София', 'Елена', 'Ольга'];
        male_patronymics TEXT[] := ARRAY['Александрович', 'Дмитриевич', 'Сергеевич', 'Андреевич', 'Алексеевич', 'Иванович', 'Михайлович', 'Петрович', 'Николаевич', 'Владимирович'];
        female_patronymics TEXT[] := ARRAY['Александровна', 'Дмитриевна', 'Сергеевна', 'Андреевна', 'Алексеевна', 'Ивановна', 'Михайловна', 'Петровна', 'Николаевна', 'Владимировна'];

        -- Массив для генерации должностей
        positions TEXT[] := ARRAY['Менеджер', 'Специалист', 'Аналитик', 'Разработчик', 'Тестировщик', 'Дизайнер', 'Руководитель проекта', 'Системный администратор', 'Инженер', 'Консультант'];

        -- Переменные для цикла
        i INT;
        gender INT;
        last_name_val TEXT;
        first_name_val TEXT;
        patronymic_val TEXT;
        position_val TEXT;
        department_id_val INT;
        new_contact_id INT;

    BEGIN
        -- Цикл для создания 10 000 контактов
        FOR i IN 1..10000 LOOP
                -- Определяем пол для выбора имени и отчества
                gender := floor(random() * 2); -- 0 для мужчины, 1 для женщины

                last_name_val := last_names[1 + floor(random() * array_length(last_names, 1))];

                IF gender = 0 THEN
                    -- Мужское ФИО
                    first_name_val := male_first_names[1 + floor(random() * array_length(male_first_names, 1))];
                    patronymic_val := male_patronymics[1 + floor(random() * array_length(male_patronymics, 1))];
                ELSE
                    -- Женское ФИО
                    first_name_val := female_first_names[1 + floor(random() * array_length(female_first_names, 1))];
                    patronymic_val := female_patronymics[1 + floor(random() * array_length(female_patronymics, 1))];
                    -- Простое склонение фамилии для женщин
                    IF right(last_name_val, 2) = 'ов' OR right(last_name_val, 2) = 'ев' OR right(last_name_val, 2) = 'ин' THEN
                        last_name_val := last_name_val || 'а';
                    END IF;
                END IF;

                -- Выбираем случайную должность
                position_val := positions[1 + floor(random() * array_length(positions, 1))];

                -- Выбираем случайный ID подразделения (от 1 до 6, как в data.sql)
                department_id_val := floor(random() * 6 + 1);

                -- Вставляем сгенерированную запись и получаем её ID
                INSERT INTO contacts (last_name, first_name, middle_name, position, department_id)
                VALUES (last_name_val, first_name_val, patronymic_val, position_val, department_id_val)
                RETURNING id INTO new_contact_id;

                -- Добавляем рабочий телефон (с вероятностью 90%)
                IF random() < 0.9 THEN
                    INSERT INTO contact_work_phones (contact_id, phone_number) VALUES (new_contact_id, generate_phone());
                    -- Иногда добавляем второй рабочий телефон (с вероятностью 15%)
                    IF random() < 0.15 THEN
                        INSERT INTO contact_work_phones (contact_id, phone_number) VALUES (new_contact_id, generate_phone());
                    END IF;
                END IF;

                -- Добавляем мобильный рабочий телефон (с вероятностью 70%)
                IF random() < 0.7 THEN
                    INSERT INTO contact_work_mobile_phones (contact_id, phone_number) VALUES (new_contact_id, generate_phone());
                END IF;

                -- Добавляем личный телефон (с вероятностью 25%, делаем его более редким)
                IF random() < 0.25 THEN
                    INSERT INTO contact_personal_phones (contact_id, phone_number) VALUES (new_contact_id, generate_phone());
                END IF;

            END LOOP;
    END $$;

-- Удаляем временную функцию
DROP FUNCTION generate_phone;
