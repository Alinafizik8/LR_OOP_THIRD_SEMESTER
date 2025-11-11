package functions.dao;

import dto.TabulatedFunctionDTO;

import java.util.List;
import java.util.Optional;

public interface TabulatedFunctionDao {

    Long save(TabulatedFunctionDTO dto);
    // === Множественный поиск ===
    Optional<TabulatedFunctionDTO> findByIdAndOwnerId(Long id, Long ownerId);
    List<TabulatedFunctionDTO> findByOwnerId(Long ownerId);
    // === Одиночный поиск ===
    List<TabulatedFunctionDTO> findByOwnerIdAndTypeId(Long ownerId, Long typeId);
    void updateName(Long id, Long ownerId, String newName);
    void updateFunctionAndName(Long id, Long ownerId, TabulatedFunctionDTO newDto);
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