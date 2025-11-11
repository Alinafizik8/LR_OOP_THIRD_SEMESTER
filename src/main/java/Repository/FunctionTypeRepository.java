package Repository;

import Entity.FunctionTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FunctionTypeRepository extends JpaRepository<FunctionTypeEntity, Long> {
    Optional<FunctionTypeEntity> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT f FROM FunctionTypeEntity f ORDER BY f.priority ASC, f.localizedName ASC")
    List<FunctionTypeEntity> findAllSortedByPriority();

    // Множественный поиск по фрагменту локализованного названия
    List<FunctionTypeEntity> findByLocalizedNameContainingIgnoreCase(String localizedNameFragment);

    // Поиск по фрагменту названия или локализованного названия
    @Query("SELECT f FROM FunctionTypeEntity f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :fragment, '%')) OR LOWER(f.localizedName) LIKE LOWER(CONCAT('%', :fragment, '%'))")
    List<FunctionTypeEntity> findByNameOrLocalizedNameContainingIgnoreCase(@Param("fragment") String fragment);
}