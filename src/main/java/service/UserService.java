package service;

import Repository.UserRepository;
import Entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
        logger.info("Поиск пользователей по фрагменту имени: {}", usernameFragment);
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
        logger.info("Получение всех пользователей с пагинацией (страница: {}, размер: {}) и сортировкой по '{} {}'", page, size, sortBy, direction);
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
}