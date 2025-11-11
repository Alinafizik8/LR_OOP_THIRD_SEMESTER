package Repository;

import Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE UserEntity u SET u.passwordHash = :hash WHERE u.id = :id")
    int updatePassword(@Param("id") Long id, @Param("hash") String hash);

    @Modifying
    @Query("UPDATE UserEntity u SET u.role = :role WHERE u.id = :id")
    int updateRole(@Param("id") Long id, @Param("role") String role);
}