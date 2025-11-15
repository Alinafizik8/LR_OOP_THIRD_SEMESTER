package service;

import entity.UserEntity;
import repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class UserServiceSortPerformanceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private static final int NUM_USERS = 10_000;

    private void generateUsers(int count) {
        userRepository.deleteAll();
        for (int i = 0; i < count; i++) {
            UserEntity user = new UserEntity();
            user.setUsername("perfuser_" + i);
            user.setEmail("perfuser_" + i + "@example.com");
            user.setPasswordHash("hash");
            user.setRole("USER");
            userRepository.save(user);
        }
        org.springframework.util.Assert.isTrue(userRepository.findAll().size() == count, "Данные не были созданы корректно");
    }

    @Test
    void performanceTest_FindAllUsersSortedByCreationDate() {
        generateUsers(NUM_USERS);

        long startTime = System.nanoTime();
        List<UserEntity> sortedUsers = userService.findAllUsersSortedByCreationDate();
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        System.out.println("Сортировка всех пользователей по дате создания (DESC) для " + NUM_USERS + " пользователей заняла: " + durationMs + " мс");

        assertTrue(sortedUsers.size() == NUM_USERS);
        if (sortedUsers.size() > 1) {
            assertTrue(sortedUsers.get(0).getCreatedAt().compareTo(sortedUsers.get(sortedUsers.size() - 1).getCreatedAt()) >= 0);
        }
    }

    @Test
    void performanceTest_FindAllUsersPagedAndSorted() {
        generateUsers(NUM_USERS);
        int pageSize = 50;
        int totalPages = (int) Math.ceil((double) NUM_USERS / pageSize);

        long startTime = System.nanoTime();
        for (int page = 0; page < totalPages; page++) {
            Page<UserEntity> pageResult = userService.findAllUsersPaged(page, pageSize, "username", "asc");
            assertTrue(pageResult.getContent().size() <= pageSize);
        }
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        System.out.println("Пагинация и сортировка (50 пользователей за раз) для " + NUM_USERS + " пользователей заняла: " + durationMs + " мс");
    }
}