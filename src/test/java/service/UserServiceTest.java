package service;

import Entity.UserEntity;
import Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class UserServiceSortTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testFindAllUsersSortedByCreationDate() {
        UserEntity user3 = new UserEntity();
        user3.setUsername("user3");
        user3.setEmail("user3@example.com");
        user3.setPasswordHash("hash");
        user3.setRole("USER");
        user3 = userRepository.save(user3);

        try { Thread.sleep(10); } catch (InterruptedException e) {}

        UserEntity user1 = new UserEntity();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPasswordHash("hash");
        user1.setRole("USER");
        user1 = userRepository.save(user1);

        try { Thread.sleep(10); } catch (InterruptedException e) {}

        UserEntity user2 = new UserEntity();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("hash");
        user2.setRole("USER");
        user2 = userRepository.save(user2);

        List<UserEntity> sortedUsers = userService.findAllUsersSortedByCreationDate();

        assertEquals(3, sortedUsers.size());
        assertEquals("user2", sortedUsers.get(0).getUsername());
        assertEquals("user1", sortedUsers.get(1).getUsername());
        assertEquals("user3", sortedUsers.get(2).getUsername());
    }
}