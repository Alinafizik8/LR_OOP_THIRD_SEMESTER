package com.example.alina.repository;

import com.example.alina.entity.TabulatedFunctionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TabulatedFunctionRepository extends JpaRepository<TabulatedFunctionEntity, Long> {

    // Изоляция по owner_id
    List<TabulatedFunctionEntity> findByOwnerId(Long ownerId);
    Optional<TabulatedFunctionEntity> findByIdAndOwnerId(Long id, Long ownerId);
    List<TabulatedFunctionEntity> findByOwnerIdAndFunctionTypeId(Long ownerId, Long typeId);

    // Обновление — только своего
    @Modifying
    @Query("""
        UPDATE TabulatedFunctionEntity f 
        SET f.name = :name 
        WHERE f.id = :id AND f.owner.id = :ownerId
        """)
    int updateName(@Param("id") Long id, @Param("ownerId") Long ownerId, @Param("name") String name);

    @Modifying
    @Query("""
        UPDATE TabulatedFunctionEntity f 
        SET f.serializedData = :data, f.name = :name 
        WHERE f.id = :id AND f.owner.id = :ownerId
        """)
    int updateDataAndName(
            @Param("id") Long id,
            @Param("ownerId") Long ownerId,
            @Param("data") byte[] data,
            @Param("name") String name
    );

    // Удаление — только своего
    @Modifying
    @Query("DELETE FROM TabulatedFunctionEntity f WHERE f.id = :id AND f.ownerId = :ownerId")
    boolean deleteByIdAndOwnerId(Long id, Long ownerId);

    // Множественный поиск с сортировкой по имени
    List<TabulatedFunctionEntity> findByOwnerIdOrderByNameAsc(Long ownerId);

    // Множественный поиск с сортировкой по дате создания
    List<TabulatedFunctionEntity> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    // Множественный поиск с пагинацией и сортировкой
    Page<TabulatedFunctionEntity> findByOwnerId(Long ownerId, Pageable pageable);

    // Поиск по фрагменту имени
    List<TabulatedFunctionEntity> findByNameContainingIgnoreCaseAndOwnerId(String nameFragment, Long ownerId);

    // Поиск по фрагменту имени с сортировкой
    List<TabulatedFunctionEntity> findByNameContainingIgnoreCaseAndOwnerIdOrderByCreatedAtDesc(String nameFragment, Long ownerId);

    // Поиск по связанным сущностям (аналог иерархии)
    // Найти функции по ID типа функции
    List<TabulatedFunctionEntity> findByFunctionTypeId(Long typeId);

    // Найти функции по ID типа функции и отсортировать
    List<TabulatedFunctionEntity> findByFunctionTypeIdOrderByCreatedAtDesc(Long typeId);
}