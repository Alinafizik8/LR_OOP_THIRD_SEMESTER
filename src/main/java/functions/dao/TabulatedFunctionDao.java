package functions.dao;

import dto.TabulatedFunctionDTO;

import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы tabulated_functions.
 * Работает с сериализованными функциями (BYTEA).
 * Обеспечивает изоляцию по ownerId.
 */
public interface TabulatedFunctionDao {

    /**
     * Сохраняет новую функцию в БД. DTO уже содержит сериализованную функцию.
     */
    Long save(TabulatedFunctionDTO dto);

    /**
     * Находит функцию по ID и владельцу (гарантирует изоляцию).
     */
    // === Множественный поиск ===
    Optional<TabulatedFunctionDTO> findByIdAndOwnerId(Long id, Long ownerId);

    /**
     * Возвращает все функции пользователя.
     */
    List<TabulatedFunctionDTO> findByOwnerId(Long ownerId);

    /**
     * Возвращает функции пользователя указанного типа.
     */
    // === Одиночный поиск ===
    List<TabulatedFunctionDTO> findByOwnerIdAndTypeId(Long ownerId, Long typeId);

    /**
     * Обновляет только имя функции.
     */
    void updateName(Long id, Long ownerId, String newName);

    /**
     * Обновляет имя и функцию (сериализует заново).
     */
    void updateFunctionAndName(Long id, Long ownerId, TabulatedFunctionDTO newDto);

    /**
     * Удаляет функцию (только свою).
     */
    void deleteByIdAndOwnerId(Long id, Long ownerId);

    // === Сортировка ===
    List<TabulatedFunctionDTO> findByOwnerIdSortedByNameAsc(Long ownerId);
    List<TabulatedFunctionDTO> findByOwnerIdSortedByCreatedAtDesc(Long ownerId);

    // === Фильтрация + сортировка ===
    List<TabulatedFunctionDTO> findByOwnerIdAndTypeIdSortedByNameAsc(Long ownerId, Long typeId);
    List<TabulatedFunctionDTO> findByOwnerIdAndTypeIdSortedByCreatedAtDesc(Long ownerId, Long typeId);

    // === Поиск по имени (LIKE, case-insensitive) ===
    List<TabulatedFunctionDTO> findByOwnerIdAndNameContaining(Long ownerId, String nameFragment);
}