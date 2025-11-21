package com.example.alina.service;

import dto.function.TabulatedFunctionDto;
import entity.TabulatedFunctionEntity;
import entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.TabulatedFunctionRepository;
import repository.UserRepository;
import service.TabulatedFunctionService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TabulatedFunctionServiceImpl implements TabulatedFunctionService {

    private final TabulatedFunctionRepository repository;
    private final UserRepository userRepository;

    public TabulatedFunctionServiceImpl(TabulatedFunctionRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public boolean canModify(org.springframework.security.core.Authentication authentication, Long functionId) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            Long currentUserId = userDetails.getId();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (isAdmin) return true;

            // Проверяем через репозиторий напрямую
            return repository.findByIdAndOwnerId(functionId, currentUserId).isPresent();
        }
        return false;
    }

    // ─── READ (owner-scoped) ───────────────────────────────────────

    @Override
    public List<TabulatedFunctionDto> findAllByOwner(Long ownerId) {
        return repository.findByOwnerId(ownerId).stream()
                .map(TabulatedFunctionServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TabulatedFunctionDto> findAllByOwner(Long ownerId, Pageable pageable) {
        return repository.findByOwnerId(ownerId, pageable).map(TabulatedFunctionServiceImpl::toDto);
    }

    @Override
    public List<TabulatedFunctionDto> findAllByOwnerSortedByNameAsc(Long ownerId) {
        return repository.findByOwnerIdOrderByNameAsc(ownerId).stream()
                .map(TabulatedFunctionServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TabulatedFunctionDto> findAllByOwnerSortedByCreatedAtDesc(Long ownerId) {
        return repository.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(TabulatedFunctionServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TabulatedFunctionDto> findByIdAndOwner(Long id, Long ownerId) {
        return repository.findByIdAndOwnerId(id, ownerId).map(TabulatedFunctionServiceImpl::toDto);
    }

    // ─── SEARCH (owner-scoped) ─────────────────────────────────────

    @Override
    public List<TabulatedFunctionDto> searchByNameFragmentAndOwner(String fragment, Long ownerId) {
        return repository.findByNameContainingIgnoreCaseAndOwnerId(fragment, ownerId).stream()
                .map(TabulatedFunctionServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TabulatedFunctionDto> searchByNameFragmentAndOwnerSortedByCreatedAtDesc(String fragment, Long ownerId) {
        return repository.findByNameContainingIgnoreCaseAndOwnerIdOrderByCreatedAtDesc(fragment, ownerId).stream()
                .map(TabulatedFunctionServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    // ─── RELATIONS ─────────────────────────────────────────────────

    @Override
    public List<TabulatedFunctionDto> findByFunctionTypeId(Long typeId) {
        return repository.findByFunctionTypeId(typeId).stream()
                .map(TabulatedFunctionServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TabulatedFunctionDto> findByFunctionTypeIdSortedByCreatedAtDesc(Long typeId) {
        return repository.findByFunctionTypeIdOrderByCreatedAtDesc(typeId).stream()
                .map(TabulatedFunctionServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TabulatedFunctionDto> findByFunctionTypeIdAndOwner(Long typeId, Long ownerId) {
        return repository.findByOwnerIdAndFunctionTypeId(ownerId, typeId).stream()
                .map(TabulatedFunctionServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    // ─── CREATE ─────────────────────────────────────────────────────

//    @Transactional
//    @Override
//    public TabulatedFunctionDto create(Long ownerId, TabulatedFunctionDto dto) {
//        TabulatedFunctionEntity entity = toEntity(dto);
//        entity.setOwner(ownerId);
//        entity.setCreatedAt(Instant.now());
//        entity.setUpdatedAt(Instant.now());
//        TabulatedFunctionEntity saved = repository.save(entity);
//        return toDto(saved);
//    }

    @Transactional
    @Override
    public TabulatedFunctionDto create(Long ownerId, TabulatedFunctionDto dto) {
        // 1. Находим владельца
        UserEntity owner = repository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId)).getOwner();

        // 2. Создаём сущность
        TabulatedFunctionEntity entity = toEntity(dto);
        entity.setOwner(owner); // ← передаём UserEntity, а не Long!
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        // 3. Сохраняем
        TabulatedFunctionEntity saved = repository.save(entity);

        // 4. Возвращаем DTO
        return toDto(saved);
    }

    // ─── UPDATE (только своё!) ─────────────────────────────────────

    @Transactional
    @Override
    public TabulatedFunctionDto updateName(Long id, Long ownerId, String newName) {
        int updated = repository.updateName(id, ownerId, newName);
        if (updated == 0) {
            throw new IllegalArgumentException("Tabulated function not found or access denied (id=" + id + ", owner=" + ownerId + ")");
        }
        return findByIdAndOwner(id, ownerId)
                .orElseThrow(() -> new RuntimeException("Inconsistency after update"));
    }

    @Transactional
    @Override
    public TabulatedFunctionDto updateDataAndName(Long id, Long ownerId, byte[] serializedData, String newName) {
        int updated = repository.updateDataAndName(id, ownerId, serializedData, newName);
        if (updated == 0) {
            throw new IllegalArgumentException("Tabulated function not found or access denied");
        }
        return findByIdAndOwner(id, ownerId)
                .orElseThrow(() -> new RuntimeException("Inconsistency after update"));
    }

    // ─── DELETE (только своё!) ─────────────────────────────────────

    @Transactional
    @Override
    public void deleteByIdAndOwner(Long id, Long ownerId) {
        boolean deleted = repository.deleteByIdAndOwnerId(id, ownerId);
        if (!deleted) {
            throw new IllegalArgumentException("Tabulated function not found or access denied (id=" + id + ", owner=" + ownerId + ")");
        }
    }

    // ─── MAPPING ───────────────────────────────────────────────────

    private static TabulatedFunctionDto toDto(TabulatedFunctionEntity e) {
        TabulatedFunctionDto dto = new TabulatedFunctionDto();
        dto.setId(e.getId());
        dto.setName(e.getName());

        if (e.getFunctionType() != null) {
            dto.setType(e.getFunctionType().getName());
            dto.setLocalizedTypeName(e.getFunctionType().getLocalizedName());
        } else {
            dto.setType(null);
            dto.setLocalizedTypeName(null);
        }

        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());

        return dto;
    }

    private static TabulatedFunctionEntity toEntity(TabulatedFunctionDto dto) {
        TabulatedFunctionEntity e = new TabulatedFunctionEntity();
        e.setName(dto.getName());
        // type и localizedTypeName — не устанавливаются здесь, они берутся из связей
        // остальные поля — будут проставлены в create/update
        return e;
    }
}