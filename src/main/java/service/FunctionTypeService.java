 package service;

import dto.function.FunctionTypeDto;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления типами функций.
 * Предоставляет бизнес-логику: валидацию, маппинг DTO ↔ Entity, безопасные операции.
 */
public interface FunctionTypeService {

    /**
     * Получить все типы функций.
     */
    List<FunctionTypeDto> findAll();

    /**
     * Получить все типы функций, отсортированные по приоритету (ASC), затем по локализованному названию (ASC).
     */
    List<FunctionTypeDto> findAllSortedByPriority();

    /**
     * Найти тип функции по ID.
     *
     * @param id идентификатор
     * @return Optional с DTO, если найден; empty — если нет
     */
    Optional<FunctionTypeDto> findById(Long id);

    /**
     * Найти тип функции по уникальному имени (техническому, напр. "LINEAR").
     *
     * @param name имя типа
     * @return Optional с DTO, если найден; empty — если нет
     */
    Optional<FunctionTypeDto> findByName(String name);

    /**
     * Создать новый тип функции.
     * Выполняет проверку уникальности имени.
     *
     * @param dto данные для создания
     * @return созданный DTO с присвоенным ID и временными метками
     * @throws IllegalArgumentException если имя уже занято
     */
    FunctionTypeDto create(FunctionTypeDto dto);

    /**
     * Обновить существующий тип функции.
     * При смене имени — проверяет его уникальность.
     *
     * @param id идентификатор обновляемой сущности
     * @param dto данные для обновления (null-поля игнорируются)
     * @return обновлённый DTO
     * @throws IllegalArgumentException если сущность не найдена или новое имя занято
     */
    FunctionTypeDto update(Long id, FunctionTypeDto dto);

    /**
     * Удалить тип функции по ID.
     *
     * @param id идентификатор
     * @throws IllegalArgumentException если сущность не найдена
     */
    void deleteById(Long id);

    /**
     * Поиск по фрагменту локализованного названия (регистронезависимо).
     * Например: "линей" → "Линейная", "Linear".
     *
     * @param fragment часть названия
     * @return список совпадений
     */
    List<FunctionTypeDto> searchByLocalizedNameFragment(String fragment);

    /**
     * Поиск по фрагменту технического ИЛИ локализованного названия (регистронезависимо).
     * Удобен для универсального поиска в UI.
     *
     * @param fragment часть названия
     * @return список совпадений
     */
    List<FunctionTypeDto> searchByNameOrLocalizedNameFragment(String fragment);

    /**
     * Проверить, существует ли тип функции с указанным именем.
     *
     * @param name имя для проверки
     * @return true, если существует; false — иначе
     */
    boolean existsByName(String name);
}