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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class TabulatedFunctionRepositoryTest {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionRepositoryTest.class);
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

    @Autowired
    TabulatedFunctionRepository functionRepo;
    @Autowired
    UserRepository userRepo;
    @Autowired
    FunctionTypeRepository typeRepo;

    private UserEntity u1, u2;
    private FunctionTypeEntity tabType, sinType;

    @BeforeEach
    void setUp() {
        logger.info("Initializing test data");
        // Типы
        tabType = typeRepo.save(new FunctionTypeEntity("TABULATED", "Табулированная", 1));
        sinType = typeRepo.save(new FunctionTypeEntity("SIN", "Синус", 2));

        // Пользователи
        u1 = new UserEntity("u1@example.com", "user1", "hash", "USER");
        u2 = new UserEntity("u2@example.com", "user2", "hash", "USER");
        u1 = userRepo.save(u1);
        u2 = userRepo.save(u2);
        logger.info("Created users: u1={}, u2={}", u1.getId(), u2.getId());
    }

    private byte[] randomBytes(int n) {
        byte[] b = new byte[n];
        new Random().nextBytes(b);
        return b;
    }

    private TabulatedFunctionEntity func(UserEntity owner, FunctionTypeEntity type, String name, byte[] data) {
        TabulatedFunctionEntity f = new TabulatedFunctionEntity();
        f.setOwner(owner);
        f.setFunctionType(type);
        f.setName(name);
        f.setSerializedData(data);
        return f;
    }

}