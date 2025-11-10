package functions.dao;

import functions.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public abstract class AbstractUserDaoTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractUserDaoTest.class);
    protected UserDao dao;

    protected abstract void setupDatabase();
    protected abstract UserDao createDao();

    @BeforeEach
    void setUp() {
        logger.debug("Initializing test context for UserDao");
        setupDatabase();
        dao = createDao();
        logger.debug("DAO initialized: {}", dao.getClass().getSimpleName());
    }

    @Test
    void save_and_find_works() {
        logger.info("Test: save_and_find_works");

        UserDTO user = new UserDTO("test@example.com", "tester", "hash");
        Long id = dao.save(user);
        logger.debug("Saved user: id={}, username='{}', email='{}'", id, user.getUsername(), user.getEmail());

        Optional<UserDTO> found = dao.findById(id);
        assertThat(found).isPresent();
        UserDTO u = found.get();
        assertThat(u.getUsername()).isEqualTo("tester");
        logger.debug("Verified: user id={} → username='{}'", u.getId(), u.getUsername());

        logger.info("Test passed: save_and_find_works");
    }

    @Test
    void unique_constraints_enforced() {
        logger.info("Test: unique_constraints_enforced");

        dao.save(new UserDTO("u@u.com", "u1", "h"));
        logger.debug("First user 'u@u.com' saved");

        RuntimeException ex = catchThrowableOfType(() ->
                        dao.save(new UserDTO("u@u.com", "u2", "h")),
                RuntimeException.class
        );
        assertThat(ex).isNotNull();
        logger.warn("Duplicate email 'u@u.com' rejected as expected");

        logger.info("Test passed: unique_constraints_enforced");
    }

    @Test
    void findByUsername_and_findByEmail_work() {
        logger.info("Test: findByUsername_and_findByEmail_work");

        dao.save(new UserDTO("x@x.com", "xuser", "h"));
        logger.debug("User 'xuser' created");

        Optional<UserDTO> byUsername = dao.findByUsername("xuser");
        assertThat(byUsername).isPresent();
        logger.debug("Found by username: '{}'", byUsername.get().getUsername());

        Optional<UserDTO> byEmail = dao.findByEmail("x@x.com");
        assertThat(byEmail).isPresent();
        logger.debug("Found by email: '{}'", byEmail.get().getEmail());

        Optional<UserDTO> missing = dao.findByUsername("nonexist");
        assertThat(missing).isEmpty();
        logger.debug("User 'nonexist' not found (as expected)");

        logger.info("Test passed: findByUsername_and_findByEmail_work");
    }

    @Test
    void updatePassword_works() {
        logger.info("Test: updatePassword_works");

        Long id = dao.save(new UserDTO("p@p.com", "p", "old"));
        logger.debug("User id={} created with password='old'", id);

        dao.updatePassword(id, "new_hash");
        logger.debug("Updated password for user id={}", id);

        String actual = dao.findById(id).get().getPasswordHash();
        assertThat(actual).isEqualTo("new_hash");
        logger.debug("Verified new password hash length: {}", actual.length());

        logger.info("Test passed: updatePassword_works");
    }

    @Test
    void delete_works() {
        logger.info("Test: delete_works");

        Long id = dao.save(new UserDTO("d@d.com", "d", "h"));
        logger.debug("User id={} created for deletion", id);

        dao.deleteById(id);
        logger.debug("Deleted user id={}", id);

        Optional<UserDTO> found = dao.findById(id);
        assertThat(found).isEmpty();
        logger.debug("Confirmed: user id={} no longer exists", id);

        logger.info("Test passed: delete_works");
    }

    @Test
    void findAll_returns_all() {
        logger.info("Test: findAll_returns_all");

        dao.save(new UserDTO("a@a.com", "a", "h"));
        dao.save(new UserDTO("b@b.com", "b", "h"));
        logger.debug("Created 2 test users");

        List<UserDTO> list = dao.findAll();
        assertThat(list).hasSize(2);
        List<String> usernames = list.stream().map(UserDTO::getUsername).toList();
        logger.debug("Loaded {} users: {}", list.size(), usernames);

        assertThat(usernames).contains("a", "b");
        logger.info("Test passed: findAll_returns_all");
    }
}