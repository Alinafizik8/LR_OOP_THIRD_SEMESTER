package service;

import dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import repository.UserRepository;
import entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    // одиночный поиск
    public Optional<UserEntity> findUserById(Long id) {
        logger.info("Поиск пользователя по ID: {}", id);
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isPresent()) {
            logger.debug("Найден пользователь: {}", user.get().getUsername());
        } else {
            logger.warn("Пользователь с ID {} не найден.", id);
        }
        return user;
    }

    public Optional<UserEntity> findUserByEmail(String email) {
        logger.info("Поиск пользователя по email: {}", email);
        Optional<UserEntity> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            logger.debug("Найден пользователь по email: {}", user.get().getEmail());
        } else {
            logger.warn("Пользователь с email {} не найден.", email);
        }
        return user;
    }

    // Множественный поиск
    public List<UserEntity> findUsersByUsernameFragment(String usernameFragment) {
        List<UserEntity> users = userRepository.findByUsernameContainingIgnoreCase(usernameFragment);
        logger.debug("Найдено {} пользователей по фрагменту имени: '{}'", users.size(), usernameFragment);
        return users;
    }

    // поиск с сортировкой
    public List<UserEntity> findAllUsersSortedByCreationDate() {
        logger.info("Получение всех пользователей, отсортированных по дате создания (DESC).");
        List<UserEntity> users = userRepository.findAllByOrderByCreatedAtDesc();
        logger.debug("Получено {} пользователей, отсортированных по дате создания DESC.", users.size());
        return users;
    }

    // поиск с пагинацией
    public Page<UserEntity> findAllUsersPaged(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserEntity> usersPage = userRepository.findAll(pageable);
        logger.debug("Получена страница пользователей: номер {}, размер {}, всего элементов {}, всего страниц {}",
                usersPage.getNumber(), usersPage.getSize(), usersPage.getTotalElements(), usersPage.getTotalPages());
        return usersPage;
    }

    // поиск по связанным сущностям
    public List<UserEntity> findUsersByFunctionTypeId(Long typeId) {
        logger.info("Поиск пользователей, у которых есть функции типа ID: {}", typeId);
        List<UserEntity> users = userRepository.findUsersByFunctionTypeId(typeId);
        logger.debug("Найдено {} пользователей, у которых есть функции типа ID: {}", users.size(), typeId);
        return users;
    }

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // <<<>>> маппинг
    @Mapper
    public interface UserMapper {
        UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

        UserResponse toUserResponse(UserEntity entity);
    }

    // 1. Регистрация
    public UserEntity register(String email, String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь с именем '" + username + "' уже существует");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Пользователь с email '" + email + "' уже существует");
        }

        // Хеширование пароля
        String encodedPassword = passwordEncoder.encode(password);

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(encodedPassword);
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.from(Instant.now()));

        // Сохранение
        UserEntity saved = userRepository.save(user);
        logger.info("Пользователь ID={} успешно зарегистрирован", saved.getId());
        return saved;
    }

    // 2. Аутентификация (логин)
    public UserEntity authenticate(String usernameOrEmail, String rawPassword) {
        Optional<UserEntity> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Неверные учётные данные");
        }

        UserEntity user = userOpt.get();

        // Сравнение пароля с хешем
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверные учётные данные");
        }

        logger.info("Пользователь ID={} успешно вошёл", user.getId());
        return user;
    }
    // <<<<>>>>
    // Для тестирования: эмулируем, что пользователь уже залогинен и идентифицирован
    // В реальной версии замените на SecurityContextHolder или JWT-парсинг
    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    // Метод для тестового входа (используется в @PostConstruct или тестах)
    public void setCurrentUserIdForTesting(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    // 3. Получение текущего пользователя (временно)
    public UserResponse getCurrentUser() {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            // Вариант 1: выбрасывать — строгий режим
            // throw new IllegalArgumentException("Пользователь не авторизован");

            // Вариант 2: возвращать демо-пользователя (для UI-тестов)
            // Создаём временного пользователя, если его ещё нет
            String demoEmail = "demo@example.com";
            Optional<UserEntity> demoOpt = userRepository.findByEmail(demoEmail);
            UserEntity demo = demoOpt.orElseGet(() -> {
                UserEntity u = new UserEntity();
                u.setEmail(demoEmail);
                u.setUsername("demo_user");
                u.setPasswordHash(passwordEncoder.encode("demo123"));
                u.setRole("USER");
                u.setCreatedAt(LocalDateTime.now());
                return userRepository.save(u);
            });
            return toUserResponse(demo);
        }

        return toUserResponse(
                userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"))
        );
    }
    private UserResponse toUserResponse(UserEntity e) {
        UserResponse dto = new UserResponse();
        dto.setId(e.getId());
        dto.setUsername(e.getUsername());
        dto.setEmail(e.getEmail());
        dto.setRole(e.getRole());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
