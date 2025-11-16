package service;

import dto.function.TabulatedFunctionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TabulatedFunctionService {

    // CRUD (все — с ownerId для изоляции!)
    List<TabulatedFunctionDto> findAllByOwner(Long ownerId);
    Page<TabulatedFunctionDto> findAllByOwner(Long ownerId, Pageable pageable);
    List<TabulatedFunctionDto> findAllByOwnerSortedByNameAsc(Long ownerId);
    List<TabulatedFunctionDto> findAllByOwnerSortedByCreatedAtDesc(Long ownerId);

    Optional<TabulatedFunctionDto> findByIdAndOwner(Long id, Long ownerId);
    TabulatedFunctionDto create(Long ownerId, TabulatedFunctionDto dto);
    TabulatedFunctionDto updateName(Long id, Long ownerId, String newName);
    TabulatedFunctionDto updateDataAndName(Long id, Long ownerId, byte[] serializedData, String newName);
    void deleteByIdAndOwner(Long id, Long ownerId);

    // Search
    List<TabulatedFunctionDto> searchByNameFragmentAndOwner(String fragment, Long ownerId);
    List<TabulatedFunctionDto> searchByNameFragmentAndOwnerSortedByCreatedAtDesc(String fragment, Long ownerId);

    // Relations
    List<TabulatedFunctionDto> findByFunctionTypeId(Long typeId);
    List<TabulatedFunctionDto> findByFunctionTypeIdSortedByCreatedAtDesc(Long typeId);
    List<TabulatedFunctionDto> findByFunctionTypeIdAndOwner(Long typeId, Long ownerId);
}