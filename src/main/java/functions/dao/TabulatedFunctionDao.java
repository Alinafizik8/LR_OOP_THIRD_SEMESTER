package functions.dao;

import functions.dto.TabulatedFunctionDTO;

import java.util.List;
import java.util.Optional;

public interface TabulatedFunctionDao {

    Long save(TabulatedFunctionDTO function);
    // === 1. Одиночный поиск (с изоляцией) ===
    Optional<TabulatedFunctionDTO> findByIdAndOwnerId(Long id, Long ownerId);
    // === 2. Множественный поиск (все функции пользователя) ===
    List<TabulatedFunctionDTO> findByOwnerId(Long ownerId);
    // === 3. Сортировка ===
    List<TabulatedFunctionDTO> findByOwnerIdSortedByNameAsc(Long ownerId);
    List<TabulatedFunctionDTO> findByOwnerIdSortedByCreatedAtDesc(Long ownerId);
    // === 4. Фильтрация + сортировка ===
    List<TabulatedFunctionDTO> findByOwnerIdAndTypeId(Long ownerId, Long typeId);
    List<TabulatedFunctionDTO> findByOwnerIdAndTypeIdSortedByNameAsc(Long ownerId, Long typeId);
    void updateName(Long id, Long ownerId, String newName);
    void updateDataAndName(Long id, Long ownerId, TabulatedFunctionDTO newData);
    void deleteByIdAndOwnerId(Long id, Long ownerId);

}