package repository;

import entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Множественный поиск по фрагменту имени пользователя
    List<UserEntity> findByUsernameContainingIgnoreCase(String usernameFragment);

    // Множественный поиск с пагинацией
    Page<UserEntity> findAll(Pageable pageable);

    // Множественный поиск с сортировкой по дате создания
    List<UserEntity> findAllByOrderByCreatedAtDesc();

    @Query("SELECT u FROM UserEntity u WHERE u.username = :param OR u.email = :param")
    Optional<UserEntity> findByUsernameOrEmail(@Param("param") String param);

    // поиск по связанным сущностям (аналог иерархии)
    @Query("SELECT DISTINCT u FROM UserEntity u JOIN u.functions f WHERE f.functionType.id = :typeId")
    List<UserEntity> findUsersByFunctionTypeId(@Param("typeId") Long typeId);

    @Modifying
    @Query("UPDATE UserEntity u SET u.passwordHash = :hash WHERE u.id = :id")
    int updatePassword(@Param("id") Long id, @Param("hash") String hash);

    @Modifying
    @Query("UPDATE UserEntity u SET u.role = :role WHERE u.id = :id")
    int updateRole(@Param("id") Long id, @Param("role") String role);
}