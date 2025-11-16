//// package service.impl;
//package service;
//
//import dto.user.UserDto;
//import entity.UserEntity;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import repository.UserRepository;
//import service.UserService;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional(readOnly = true)
//public class UserServiceImpl implements UserService {
//
//    private final UserRepository userRepository;
//
//    @Autowired
//    public UserServiceImpl(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    // ——— READ ———
//
//    @Override
//    public List<UserDto> findAll() {
//        return userRepository.findAll().stream()
//                .map(UserServiceImpl::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public Page<UserDto> findAll(Pageable pageable) {
//        return userRepository.findAll(pageable)
//                .map(UserServiceImpl::toDto);
//    }
//
//    @Override
//    public List<UserDto> findAllSortedByCreatedAtDesc() {
//        return userRepository.findAllByOrderByCreatedAtDesc().stream()
//                .map(UserServiceImpl::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public Optional<UserDto> findById(Long id) {
//        return userRepository.findById(id).map(UserServiceImpl::toDto);
//    }
//
//    @Override
//    public Optional<UserDto> findByUsername(String username) {
//        return userRepository.findByUsername(username).map(UserServiceImpl::toDto);
//    }
//
//    @Override
//    public Optional<UserDto> findByEmail(String email) {
//        return userRepository.findByEmail(email).map(UserServiceImpl::toDto);
//    }
//
//    @Override
//    public Optional<UserDto> findByUsernameOrEmail(String param) {
//        return userRepository.findByUsernameOrEmail(param).map(UserServiceImpl::toDto);
//    }
//
//    @Override
//    public List<UserDto> searchByUsernameFragment(String fragment) {
//        return userRepository.findByUsernameContainingIgnoreCase(fragment).stream()
//                .map(UserServiceImpl::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<UserDto> findUsersByFunctionTypeId(Long typeId) {
//        return userRepository.findUsersByFunctionTypeId(typeId).stream()
//                .map(UserServiceImpl::toDto)
//                .collect(Collectors.toList());
//    }
//
//    // ——— CREATE ———
//    @Transactional
//    @Override
//    public UserDto create(UserDto dto) {
//        if (existsByUsername(dto.getUsername())) {
//            throw new IllegalArgumentException("User with username '" + dto.getUsername() + "' already exists");
//        }
//        if (existsByEmail(dto.getEmail())) {
//            throw new IllegalArgumentException("User with email '" + dto.getEmail() + "' already exists");
//        }
//
//        UserEntity entity = toEntity(dto);
//        entity.setPasswordHash(""); // ← пароль должен устанавливаться отдельно (например, через register + hash)
//        entity.setCreatedAt(java.time.Instant.now());
//        UserEntity saved = userRepository.save(entity);
//        return toDto(saved);
//    }
//
//    // ——— UPDATE ———
//    @Transactional
//    @Override
//    public UserDto update(Long id, UserDto dto) {
//        UserEntity existing = userRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
//
//        if (dto.getUsername() != null && !dto.getUsername().equals(existing.getUsername())) {
//            if (existsByUsername(dto.getUsername())) {
//                throw new IllegalArgumentException("Username '" + dto.getUsername() + "' already taken");
//            }
//            existing.setUsername(dto.getUsername());
//        }
//
//        if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
//            if (existsByEmail(dto.getEmail())) {
//                throw new IllegalArgumentException("Email '" + dto.getEmail() + "' already taken");
//            }
//            existing.setEmail(dto.getEmail());
//        }
//
//        if (dto.getRole() != null) {
//            existing.setRole(dto.getRole());
//        }
//
//        UserEntity updated = userRepository.save(existing);
//        return toDto(updated);
//    }
//
//    @Transactional
//    @Override
//    public void updatePassword(Long id, String newPasswordHash) {
//        int updated = userRepository.updatePassword(id, newPasswordHash);
//        if (updated == 0) {
//            throw new IllegalArgumentException("User not found: " + id);
//        }
//    }
//
//    @Transactional
//    @Override
//    public void updateRole(Long id, String role) {
//        int updated = userRepository.updateRole(id, role);
//        if (updated == 0) {
//            throw new IllegalArgumentException("User not found: " + id);
//        }
//    }
//
//    // ——— DELETE ———
//    @Transactional
//    @Override
//    public void deleteById(Long id) {
//        if (!userRepository.existsById(id)) {
//            throw new IllegalArgumentException("User not found: " + id);
//        }
//        userRepository.deleteById(id);
//    }
//
//    // ——— UTIL ———
//    @Override
//    public boolean existsByUsername(String username) {
//        return userRepository.existsByUsername(username);
//    }
//
//    @Override
//    public boolean existsByEmail(String email) {
//        return userRepository.existsByEmail(email);
//    }
//
//    // ——— MAPPING ———
//    private static UserDto toDto(UserEntity e) {
//        UserDto dto = new UserDto();
//        dto.setId(e.getId());
//        dto.setUsername(e.getUsername());
//        dto.setEmail(e.getEmail());
//        dto.setRole(e.getRole());
//        dto.setCreatedAt(e.getCreatedAt());
//        return dto;
//    }
//
//    private static UserEntity toEntity(UserDto dto) {
//        UserEntity e = new UserEntity();
//        e.setUsername(dto.getUsername());
//        e.setEmail(dto.getEmail());
//        e.setRole(dto.getRole());
//        // passwordHash и createdAt устанавливаются отдельно
//        return e;
//    }
//}
package service;

import dto.user.UserDto;
import entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.UserRepository;
import service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ─── READ ───────────────────────────────────────────────────────

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserServiceImpl::toDto);
    }

    @Override
    public List<UserDto> findAllSortedByCreatedAtDesc() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(UserServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(UserServiceImpl::toDto);
    }

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(UserServiceImpl::toDto);
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserServiceImpl::toDto);
    }

    @Override
    public Optional<UserDto> findByUsernameOrEmail(String param) {
        return userRepository.findByUsernameOrEmail(param).map(UserServiceImpl::toDto);
    }

    @Override
    public List<UserDto> searchByUsernameFragment(String fragment) {
        return userRepository.findByUsernameContainingIgnoreCase(fragment).stream()
                .map(UserServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> findUsersByFunctionTypeId(Long typeId) {
        return userRepository.findUsersByFunctionTypeId(typeId).stream()
                .map(UserServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    // ─── CREATE ─────────────────────────────────────────────────────

    @Transactional
    @Override
    public UserDto create(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered");
        }

        UserEntity entity = toEntity(dto);
        entity.setCreatedAt(LocalDateTime.from(java.time.Instant.now()));
        // ⚠️ passwordHash должен устанавливаться отдельно (например, через registration flow)
        UserEntity saved = userRepository.save(entity);
        return toDto(saved);
    }

    // ─── UPDATE ─────────────────────────────────────────────────────

    @Transactional
    @Override
    public UserDto update(Long id, UserDto dto) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Username: validate uniqueness if changed
        if (dto.getUsername() != null && !dto.getUsername().equals(entity.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken");
            }
            entity.setUsername(dto.getUsername());
        }

        // Email: validate uniqueness if changed
        if (dto.getEmail() != null && !dto.getEmail().equals(entity.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered");
            }
            entity.setEmail(dto.getEmail());
        }

        // Role
        if (dto.getRole() != null) {
            entity.setRole(dto.getRole());
        }

        UserEntity updated = userRepository.save(entity);
        return toDto(updated);
    }

    @Transactional
    @Override
    public void updatePassword(Long id, String passwordHash) {
        int rows = userRepository.updatePassword(id, passwordHash);
        if (rows == 0) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
    }

    @Transactional
    @Override
    public void updateRole(Long id, String role) {
        int rows = userRepository.updateRole(id, role);
        if (rows == 0) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
    }

    // ─── DELETE ─────────────────────────────────────────────────────

    @Transactional
    @Override
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // ─── VALIDATION ─────────────────────────────────────────────────

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // ─── MAPPING ────────────────────────────────────────────────────

    private static UserDto toDto(UserEntity e) {
        UserDto dto = new UserDto();
        dto.setId(e.getId());
        dto.setUsername(e.getUsername());
        dto.setEmail(e.getEmail());
        dto.setRole(e.getRole());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private static UserEntity toEntity(UserDto dto) {
        UserEntity e = new UserEntity();
        e.setUsername(dto.getUsername());
        e.setEmail(dto.getEmail());
        e.setRole(dto.getRole());
        // createdAt будет проставлен в create()
        return e;
    }
}