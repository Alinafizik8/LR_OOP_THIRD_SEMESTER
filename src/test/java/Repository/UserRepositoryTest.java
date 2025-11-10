package Repository;

import Entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class UserRepositoryTest {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryTest.class);
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private UserRepository userRepo;

    @BeforeEach
    void setUp() {
        logger.info("Initializing UserRepository test context");
        userRepo.deleteAll();
        logger.debug("Cleared all users");
    }

    @Test
    void save_and_findById_works() {
        logger.info("Test: save_and_findById_works");
        UserEntity user = new UserEntity("test@example.com", "tester", "hash", "USER");
        user = userRepo.save(user);
        logger.debug("Saved user: id={}, username='{}'", user.getId(), user.getUsername());

        Optional<UserEntity> found = userRepo.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("tester");
        logger.info("Test passed");
    }

    @Test
    void unique_constraints_enforced() {
        logger.info("Test: unique_constraints_enforced");
        userRepo.save(new UserEntity("u@u.com", "u1", "h", "USER"));
        logger.debug("First user 'u@u.com' saved");

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () ->
                userRepo.save(new UserEntity("u@u.com", "u2", "h", "USER"))
        );
        logger.warn("Duplicate email rejected by DB constraint");
        logger.info("Test passed");
    }

    @Test
    void findByUsername_and_findByEmail_work() {
        logger.info("Test: findByUsername_and_findByEmail_work");
        userRepo.save(new UserEntity("x@x.com", "xuser", "h", "USER"));
        logger.debug("User 'xuser' created");

        assertThat(userRepo.findByUsername("xuser")).isPresent();
        assertThat(userRepo.findByEmail("x@x.com")).isPresent();
        assertThat(userRepo.findByUsername("nonexist")).isEmpty();
        logger.debug("Username/email search works");
        logger.info("Test passed");
    }

    @Test
    void updatePassword_works() {
        logger.info("Test: updatePassword_works");
        Long id = userRepo.save(new UserEntity("p@p.com", "p", "old", "USER")).getId();
        userRepo.updatePassword(id, "new_hash");
        logger.debug("Updated password for user id={}", id);

        String actual = userRepo.findById(id).get().getPasswordHash();
        assertThat(actual).isEqualTo("new_hash");
        logger.info("Test passed");
    }

    @Test
    void delete_works() {
        logger.info("Test: delete_works");
        Long id = userRepo.save(new UserEntity("d@d.com", "d", "h", "USER")).getId();
        userRepo.deleteById(id);
        logger.debug("Deleted user id={}", id);

        assertThat(userRepo.findById(id)).isEmpty();
        logger.info("Test passed");
    }

    @Test
    void existsByUsername_works() {
        logger.info("Test: existsByUsername_works");
        userRepo.save(new UserEntity("a@a.com", "a", "h", "USER"));
        assertThat(userRepo.existsByUsername("a")).isTrue();
        assertThat(userRepo.existsByUsername("b")).isFalse();
        logger.debug("existsByUsername works");
        logger.info("Test passed");
    }
}