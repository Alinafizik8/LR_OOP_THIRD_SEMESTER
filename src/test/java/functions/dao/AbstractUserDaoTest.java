package functions.dao;

import functions.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public abstract class AbstractUserDaoTest {

    protected UserDao dao;

    protected abstract void setupDatabase();
    protected abstract UserDao createDao();

    @BeforeEach
    void setUp() {
        setupDatabase();
        dao = createDao();
    }

    @Test
    void save_and_find_works() {
        UserDTO user = new UserDTO("test@example.com", "tester", "hash");
        Long id = dao.save(user);

        Optional<UserDTO> found = dao.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("tester");
    }

    @Test
    void unique_constraints_enforced() {
        dao.save(new UserDTO("u@u.com", "u1", "h"));
        assertThatThrownBy(() ->
                dao.save(new UserDTO("u@u.com", "u2", "h"))
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void findByUsername_and_findByEmail_work() {
        dao.save(new UserDTO("x@x.com", "xuser", "h"));
        assertThat(dao.findByUsername("xuser")).isPresent();
        assertThat(dao.findByEmail("x@x.com")).isPresent();
    }

    @Test
    void updatePassword_works() {
        Long id = dao.save(new UserDTO("p@p.com", "p", "old"));
        dao.updatePassword(id, "new_hash");
        assertThat(dao.findById(id).get().getPasswordHash()).isEqualTo("new_hash");
    }

    @Test
    void delete_works() {
        Long id = dao.save(new UserDTO("d@d.com", "d", "h"));
        dao.deleteById(id);
        assertThat(dao.findById(id)).isEmpty();
    }

    @Test
    void findAll_returns_all() {
        dao.save(new UserDTO("a@a.com", "a", "h"));
        dao.save(new UserDTO("b@b.com", "b", "h"));
        List<UserDTO> list = dao.findAll();
        assertThat(list).hasSize(2);
    }
}