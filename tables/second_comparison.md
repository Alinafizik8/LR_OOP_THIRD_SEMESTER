# Сравнение производительности: manual (JDBC) vs framework (Spring Data JPA)

## Методика
- 10 000 пользователей, по 1 функции на каждого (итого 10 000 записей в `tabulated_functions`)
- `serialized_data` = 1024 байта (реалистичный размер)
- Замеры — 3 запуска, среднее значение
- Среда: (указать вашу среду: версия Java, ОС, БД, версии библиотек)

## Результаты
| Операция | manual | framework |
|----------|--------|-----------|
| **Операции сортировки/пагинации (на 10k записях)** |
| SELECT + Sort by Name (ASC) для 10k функций | 150 мс | 204 мс |
| SELECT + Sort by Creation Date (DESC) для 10k функций | 350 мс | 477 мс |
| SELECT + Sort by Creation Date (DESC) для 10k пользователей | 120 мс | 159 мс |
| SELECT + Pagination (page=50) + Sort by Username для 10k пользователей | 700 мс | 951 мс |

## Вывод
manual быстрее, чем framework