package Repository;

import Entity.TabulatedFunctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TabulatedFunctionRepository extends JpaRepository<TabulatedFunctionEntity, Long> {

    // 🔒 Изоляция по owner_id
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
    void deleteByIdAndOwnerId(Long id, Long ownerId);
}