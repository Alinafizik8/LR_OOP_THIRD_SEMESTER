package Repository;

import Entity.FunctionTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FunctionTypeRepository extends JpaRepository<FunctionTypeEntity, Long> {
    Optional<FunctionTypeEntity> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT f FROM FunctionTypeEntity f ORDER BY f.priority ASC, f.localizedName ASC")
    List<FunctionTypeEntity> findAllSortedByPriority();
}