//package service;
//
//import dto.UserResponse;
//import org.mapstruct.Mapper;
//import org.mapstruct.factory.Mappers;
//import repository.UserRepository;
//import entity.UserEntity;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//
//import org.springframework.security.crypto.password.PasswordEncoder;
//import java.time.Instant;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class UserService {
//
//    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
//
//    @Autowired
//    private UserRepository userRepository;
//
//    // одиночный поиск
//    public Optional<UserEntity> findUserById(Long id) {
//        logger.info("Поиск пользователя по ID: {}", id);
//        Optional<UserEntity> user = userRepository.findById(id);
//        if (user.isPresent()) {
//            logger.debug("Найден пользователь: {}", user.get().getUsername());
//        } else {
//            logger.warn("Пользователь с ID {} не найден.", id);
//        }
//        return user;
//    }
//
//    public Optional<UserEntity> findUserByEmail(String email) {
//        logger.info("Поиск пользователя по email: {}", email);
//        Optional<UserEntity> user = userRepository.findByEmail(email);
//        if (user.isPresent()) {
//            logger.debug("Найден пользователь по email: {}", user.get().getEmail());
//        } else {
//            logger.warn("Пользователь с email {} не найден.", email);
//        }
//        return user;
//    }
//
//    // Множественный поиск
//    public List<UserEntity> findUsersByUsernameFragment(String usernameFragment) {
//        List<UserEntity> users = userRepository.findByUsernameContainingIgnoreCase(usernameFragment);
//        logger.debug("Найдено {} пользователей по фрагменту имени: '{}'", users.size(), usernameFragment);
//        return users;
//    }
//
//    // поиск с сортировкой
//    public List<UserEntity> findAllUsersSortedByCreationDate() {
//        logger.info("Получение всех пользователей, отсортированных по дате создания (DESC).");
//        List<UserEntity> users = userRepository.findAllByOrderByCreatedAtDesc();
//        logger.debug("Получено {} пользователей, отсортированных по дате создания DESC.", users.size());
//        return users;
//    }
//
//    // поиск с пагинацией
//    public Page<UserEntity> findAllUsersPaged(int page, int size, String sortBy, String direction) {
//        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//        Pageable pageable = PageRequest.of(page, size, sort);
//        Page<UserEntity> usersPage = userRepository.findAll(pageable);
//        logger.debug("Получена страница пользователей: номер {}, размер {}, всего элементов {}, всего страниц {}",
//                usersPage.getNumber(), usersPage.getSize(), usersPage.getTotalElements(), usersPage.getTotalPages());
//        return usersPage;
//    }
//
//    // поиск по связанным сущностям
//    public List<UserEntity> findUsersByFunctionTypeId(Long typeId) {
//        logger.info("Поиск пользователей, у которых есть функции типа ID: {}", typeId);
//        List<UserEntity> users = userRepository.findUsersByFunctionTypeId(typeId);
//        logger.debug("Найдено {} пользователей, у которых есть функции типа ID: {}", users.size(), typeId);
//        return users;
//    }
//
//    private final PasswordEncoder passwordEncoder;
//
//    @Autowired
//    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    // <<<>>> маппинг
//    @Mapper
//    public interface UserMapper {
//        UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
//
//        UserResponse toUserResponse(UserEntity entity);
//    }
//
//    // 1. Регистрация
//    public UserEntity register(String email, String username, String password) {
//        if (userRepository.findByUsername(username).isPresent()) {
//            throw new IllegalArgumentException("Пользователь с именем '" + username + "' уже существует");
//        }
//        if (userRepository.findByEmail(email).isPresent()) {
//            throw new IllegalArgumentException("Пользователь с email '" + email + "' уже существует");
//        }
//
//        // Хеширование пароля
//        String encodedPassword = passwordEncoder.encode(password);
//
//        UserEntity user = new UserEntity();
//        user.setEmail(email);
//        user.setUsername(username);
//        user.setPasswordHash(encodedPassword);
//        user.setRole("USER");
//        user.setCreatedAt(LocalDateTime.from(Instant.now()));
//
//        // Сохранение
//        UserEntity saved = userRepository.save(user);
//        logger.info("Пользователь ID={} успешно зарегистрирован", saved.getId());
//        return saved;
//    }
//
//    // 2. Аутентификация (логин)
//    public UserEntity authenticate(String usernameOrEmail, String rawPassword) {
//        Optional<UserEntity> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail);
//
//        if (userOpt.isEmpty()) {
//            throw new IllegalArgumentException("Неверные учётные данные");
//        }
//
//        UserEntity user = userOpt.get();
//
//        // Сравнение пароля с хешем
//        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
//            throw new IllegalArgumentException("Неверные учётные данные");
//        }
//
//        logger.info("Пользователь ID={} успешно вошёл", user.getId());
//        return user;
//    }
//    // <<<<>>>>
//    // Для тестирования: эмулируем, что пользователь уже залогинен и идентифицирован
//    // В реальной версии замените на SecurityContextHolder или JWT-парсинг
//    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
//
//    // Метод для тестового входа (используется в @PostConstruct или тестах)
//    public void setCurrentUserIdForTesting(Long userId) {
//        CURRENT_USER_ID.set(userId);
//    }
//
//    // 3. Получение текущего пользователя (временно)
//    public UserResponse getCurrentUser() {
//        Long userId = CURRENT_USER_ID.get();
//        if (userId == null) {
//            // Вариант 1: выбрасывать — строгий режим
//            // throw new IllegalArgumentException("Пользователь не авторизован");
//
//            // Вариант 2: возвращать демо-пользователя (для UI-тестов)
//            // Создаём временного пользователя, если его ещё нет
//            String demoEmail = "demo@example.com";
//            Optional<UserEntity> demoOpt = userRepository.findByEmail(demoEmail);
//            UserEntity demo = demoOpt.orElseGet(() -> {
//                UserEntity u = new UserEntity();
//                u.setEmail(demoEmail);
//                u.setUsername("demo_user");
//                u.setPasswordHash(passwordEncoder.encode("demo123"));
//                u.setRole("USER");
//                u.setCreatedAt(LocalDateTime.now());
//                return userRepository.save(u);
//            });
//            return toUserResponse(demo);
//        }
//
//        return toUserResponse(
//                userRepository.findById(userId)
//                        .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"))
//        );
//    }
//    private UserResponse toUserResponse(UserEntity e) {
//        UserResponse dto = new UserResponse();
//        dto.setId(e.getId());
//        dto.setUsername(e.getUsername());
//        dto.setEmail(e.getEmail());
//        dto.setRole(e.getRole());
//        dto.setCreatedAt(e.getCreatedAt());
//        return dto;
//    }
//}
// service/UserServiceImpl.java
//package service;
//
//import dto.UserResponse;
//import dto.auth.AuthResponse;
//import dto.auth.LoginRequest;
//import dto.user.CreateUserRequest;
//import entity.UserEntity;
//import util.JwtUtil; // ← вы создадите или используете заглушку
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//import java.util.Optional;
//
//@Service
//public class UserServiceImpl implements UserService {
//
//    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtUtil jwtUtil; // ← или любой способ генерации токена
//
//    @Autowired
//    public UserServiceImpl(UserRepository userRepository,
//                           PasswordEncoder passwordEncoder,
//                           JwtUtil jwtUtil) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.jwtUtil = jwtUtil;
//    }
//
//    // 1. Регистрация
//    @Override
//    public UserResponse register(CreateUserRequest request) {
//        // Валидация
//        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
//            throw new IllegalArgumentException("Пользователь с именем '" + request.getUsername() + "' уже существует");
//        }
//        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
//            throw new IllegalArgumentException("Пользователь с email '" + request.getEmail() + "' уже существует");
//        }
//
//        // Создание сущности
//        UserEntity user = new UserEntity();
//        user.setEmail(request.getEmail());
//        user.setUsername(request.getUsername());
//        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
//        user.setRole("USER");
//        user.setCreatedAt(java.time.LocalDateTime.now());
//
//        // Сохранение
//        UserEntity saved = userRepository.save(user);
//        logger.info("Пользователь ID={} успешно зарегистрирован", saved.getId());
//
//        // Маппинг в DTO
//        return toUserResponse(saved);
//    }
//
//    // 2. Логин
//    @Override
//    public AuthResponse login(LoginRequest request) {
//        Optional<UserEntity> userOpt = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail());
//
//        if (userOpt.isEmpty()) {
//            throw new IllegalArgumentException("Неверные учётные данные");
//        }
//
//        UserEntity user = userOpt.get();
//
//        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
//            throw new IllegalArgumentException("Неверные учётные данные");
//        }
//
//        // Генерация JWT-токена (пример)
//        String token = jwtUtil.generateToken(user); // или "fake-jwt-" + user.getId();
//        long expiresIn = 3600; // 1 час в секундах
//
//        // Формирование ответа
//        AuthResponse response = new AuthResponse();
//        response.setToken(token);
//        response.setExpiresIn(expiresIn);
//        response.setUser(toUserDto(user)); // ← нужно, чтобы UserDto совпадал с UserResponse по полям
//
//        logger.info("Пользователь ID={} успешно вошёл", user.getId());
//        return response;
//    }
//
//    // 3. Текущий пользователь
//    @Override
//    public UserResponse getCurrentUser() {
//        // Для тестов: эмуляция
//        Long demoId = 1L;
//        UserEntity demo = userRepository.findById(demoId)
//                .orElseGet(() -> {
//                    UserEntity u = new UserEntity();
//                    u.setUsername("demo");
//                    u.setEmail("demo@example.com");
//                    u.setPasswordHash(passwordEncoder.encode("demo123"));
//                    u.setRole("USER");
//                    u.setCreatedAt(java.time.LocalDateTime.now());
//                    return userRepository.save(u);
//                });
//        return toUserResponse(demo);
//    }
//
//    // Вспомогательные мапперы
//
//    private UserResponse toUserResponse(UserEntity e) {
//        UserResponse dto = new UserResponse();
//        dto.setId(e.getId());
//        dto.setUsername(e.getUsername());
//        dto.setEmail(e.getEmail());
//        dto.setRole(e.getRole());
//        dto.setCreatedAt(e.getCreatedAt());
//        return dto;
//    }
//
//    // Если UserDto и UserResponse идентичны — можно использовать один и тот же маппер
//    private dto.UserDto toUserDto(UserEntity e) {
//        dto.UserDto dto = new dto.UserDto();
//        dto.setId(e.getId());
//        dto.setUsername(e.getUsername());
//        dto.setEmail(e.getEmail());
//        dto.setRole(e.getRole());
//        dto.setCreatedAt(e.getCreatedAt());
//        return dto;
//    }
//}
// service/UserService.java
//package service;
//
//import dto.UserResponse;
//import dto.auth.AuthResponse;
//import dto.auth.LoginRequest;
//import dto.user.CreateUserRequest;
//import entity.UserEntity;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import repository.UserRepository;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor // Lombok автоматически создаёт конструктор для final-полей
//public class UserService {
//
//    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//        passwordEncoder = null;
//    }
//
//    // <<< ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ (как у вас) >>>
//
//    public Optional<UserEntity> findUserById(Long id) {
//        logger.info("Поиск пользователя по ID: {}", id);
//        Optional<UserEntity> user = userRepository.findById(id);
//        if (user.isPresent()) {
//            logger.debug("Найден пользователь: {}", user.get().getUsername());
//        } else {
//            logger.warn("Пользователь с ID {} не найден.", id);
//        }
//        return user;
//    }
//
//    public Optional<UserEntity> findUserByEmail(String email) {
//        logger.info("Поиск пользователя по email: {}", email);
//        Optional<UserEntity> user = userRepository.findByEmail(email);
//        if (user.isPresent()) {
//            logger.debug("Найден пользователь по email: {}", user.get().getEmail());
//        } else {
//            logger.warn("Пользователь с email {} не найден.", email);
//        }
//        return user;
//    }
//
//    // <<< МЕТОДЫ ДЛЯ AuthController >>>
//
//    /**
//     * Регистрация пользователя из DTO-запроса.
//     */
//    @Transactional
//    public UserResponse register(CreateUserRequest request) {
//        // Валидация существования
//        if (userRepository.findByUsername(String.valueOf(request.getClass())).isPresent()) {
//            throw new IllegalArgumentException("Пользователь с именем '" + request.getClass() + "' уже существует");
//        }
//        if (userRepository.findByEmail(String.valueOf(request.getClass())).isPresent()) {
//            throw new IllegalArgumentException("Пользователь с email '" + request.getClass() + "' уже существует");
//        }
//
//        // Создание сущности
//        UserEntity user = new UserEntity();
//        user.setUsername(String.valueOf(request.getClass()));
//        user.setEmail(String.valueOf(request.getClass()));
//        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
//        user.setRole(request.getRole() != null ? request.getRole() : "USER");
//        user.setCreatedAt(LocalDateTime.now());
//
//        // Сохранение
//        UserEntity saved = userRepository.save(user);
//        logger.info("Пользователь ID={} успешно зарегистрирован", saved.getId());
//
//        // Маппинг в DTO-ответ
//        return toUserResponse(saved);
//    }
//
//    /**
//     * Аутентификация и формирование AuthResponse.
//     */
//    @Transactional(readOnly = true)
//    public AuthResponse login(LoginRequest request) {
//        Optional<UserEntity> userOpt = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail());
//
//        if (userOpt.isEmpty()) {
//            throw new IllegalArgumentException("Неверные учётные данные");
//        }
//
//        UserEntity user = userOpt.get();
//
//        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
//            throw new IllegalArgumentException("Неверные учётные данные");
//        }
//
//        // Генерация "токена" (заглушка — можно заменить на JWT)
//        String token = "fake-jwt-token-" + user.getId();
//        long expiresIn = 3600; // 1 час
//
//        // Формируем AuthResponse
//        AuthResponse response = new AuthResponse();
//        response.setToken(token);
//        response.setExpiresIn(expiresIn);
//        response.setUser(toUserDto(user)); // ← создаём UserDto из UserEntity
//
//        logger.info("Пользователь ID={} успешно вошёл", user.getId());
//        return response;
//    }
//
//    /**
//     * Получение текущего пользователя (заглушка для демо-режима).
//     */
//    @Transactional(readOnly = true)
//    public UserResponse getCurrentUser() {
//        // Для тестов: создаём/находим демо-пользователя
//        String demoEmail = "demo@example.com";
//        UserEntity demo = userRepository.findByEmail(demoEmail)
//                .orElseGet(() -> {
//                    UserEntity u = new UserEntity();
//                    u.setUsername("demo_user");
//                    u.setEmail(demoEmail);
//                    u.setPasswordHash(passwordEncoder.encode("demo123"));
//                    u.setRole("USER");
//                    u.setCreatedAt(LocalDateTime.now());
//                    logger.info("Создан демо-пользователь для /me");
//                    return userRepository.save(u);
//                });
//
//        return toUserResponse(demo);
//    }
//
//    // <<< ВСПОМОГАТЕЛЬНЫЕ МАППЕРЫ >>>
//
//    private UserResponse toUserResponse(UserEntity e) {
//        UserResponse dto = new UserResponse();
//        dto.setId(e.getId());
//        dto.setUsername(e.getUsername());
//        dto.setEmail(e.getEmail());
//        dto.setRole(e.getRole());
//        dto.setCreatedAt(e.getCreatedAt());
//        return dto;
//    }
//
//    // Создаём UserDto (требуется в AuthResponse)
//    private dto.UserDto toUserDto(UserEntity e) {
//        dto.UserDto dto = new dto.UserDto();
//        dto.setId(e.getId());
//        dto.setUsername(e.getUsername());
//        dto.setEmail(e.getEmail());
//        dto.setRole(e.getRole());
//        dto.setCreatedAt(e.getCreatedAt());
//        return dto;
//    }
//}
