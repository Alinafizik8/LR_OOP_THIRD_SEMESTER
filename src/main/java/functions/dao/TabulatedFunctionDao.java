package functions.dao;

import functions.dto.TabulatedFunctionDTO;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс доступа к данным таблицы tabulated_functions.
 * Определяет контракт для операций CRUD и поиска.
 */
public interface TabulatedFunctionDao {

    /**
     * Сохраняет новую функцию в БД.
     * @return идентификатор сохранённой записи
     */
    Long save(TabulatedFunctionDTO function);

    /**
     * Находит функцию по ID и ID владельца (гарантирует изоляцию).
     * @return Optional.empty(), если функция не найдена или не принадлежит пользователю
     */
    Optional<TabulatedFunctionDTO> findByIdAndOwnerId(Long id, Long ownerId);

    /**
     * Возвращает все функции, принадлежащие пользователю.
     */
    List<TabulatedFunctionDTO> findByOwnerId(Long ownerId);

    /**
     * Обновляет только имя функции.
     */
    void updateName(Long id, Long ownerId, String newName);

    /**
     * Обновляет имя и сериализованные данные функции.
     */
    void updateDataAndName(Long id, Long ownerId, TabulatedFunctionDTO newData);

    /**
     * Удаляет функцию, если она принадлежит владельцу.
     */
    void deleteByIdAndOwnerId(Long id, Long ownerId);
}