package com.example.alina.service;

import com.example.alina.dto.function.CreateFunctionFromMathRequest;
import com.example.alina.dto.function.CreateFunctionFromPointsRequest;
import com.example.alina.dto.function.TabulatedFunctionDto;
import com.example.alina.entity.FunctionTypeEntity;
import com.example.alina.entity.TabulatedFunctionEntity;
import com.example.alina.entity.UserEntity;
import com.example.alina.functions.*;
import com.example.alina.io.FunctionsIO;
import com.example.alina.repository.FunctionTypeRepository;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.alina.repository.TabulatedFunctionRepository;
import com.example.alina.repository.UserRepository;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TabulatedFunctionServiceImpl implements TabulatedFunctionService {

    private final TabulatedFunctionRepository repository;
    private final UserRepository userRepository;
    private final FunctionTypeRepository functionTypeRepository;

    public TabulatedFunctionServiceImpl(TabulatedFunctionRepository repository, UserRepository userRepository, FunctionTypeRepository functionTypeRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.functionTypeRepository = functionTypeRepository;
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

    @Transactional
    @Override
    public TabulatedFunctionDto createFromPoints(CreateFunctionFromPointsRequest request) {
        // 1. Валидация
        if (request.getXValues() == null || request.getYValues() == null) {
            throw new IllegalArgumentException("xValues and yValues must not be null");
        }
        if (request.getXValues().size() < 2 || request.getYValues().size() < 2) {
            throw new IllegalArgumentException("At least 2 points required");
        }
        if (request.getXValues().size() != request.getYValues().size()) {
            throw new IllegalArgumentException("xValues and yValues must have same length");
        }

        // 2. Создаём TabulatedFunction (например, ArrayTabulatedFunction)
        double[] x = request.getXValues().stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = request.getYValues().stream().mapToDouble(Double::doubleValue).toArray();
        TabulatedFunction func = new ArrayTabulatedFunction(x, y);

        // 3. Сериализуем в байты
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedOutputStream bos = new BufferedOutputStream(baos)) {
            FunctionsIO.serialize(bos, func);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize function", e);
        }
        byte[] serializedData = baos.toByteArray();

        // 4. Сохраняем в БД
        TabulatedFunctionEntity entity = new TabulatedFunctionEntity();
        entity.setName(request.getName());
        entity.setSerializedData(serializedData);

        // Найдём владельца
        UserEntity owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + request.getOwnerId()));
        entity.setOwner(owner);

        // Найдём тип функции (например, "TABULATED")
        FunctionTypeEntity type = functionTypeRepository.findByName("TABULATED")
                .orElseThrow(() -> new IllegalStateException("FunctionType 'TABULATED' not found"));
        entity.setFunctionType(type);

        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        TabulatedFunctionEntity saved = repository.save(entity);
        return toDto(saved);
    }
    @Transactional
    @Override
    public TabulatedFunctionDto createFromMath(CreateFunctionFromMathRequest request) {
        // 1. Определяем MathFunction по имени
        MathFunction mathFunc = switch (request.getMathFunctionType().toLowerCase()) {
            case "identity", "identityfunction" -> new IdentityFunction();
            case "sqr", "sqrfunction" -> new SqrFunction();
            case "constant", "constantfunction" -> new ConstantFunction(1.0);
            case "zero", "zerofunction" -> new ZeroFunction();
            case "unit", "unitfunction" -> new UnitFunction();
            default -> throw new IllegalArgumentException("Unknown math function: " + request.getMathFunctionType());
        };

        // 2. Создаём табулированную функцию
        TabulatedFunction tabFunc = new ArrayTabulatedFunction(
                mathFunc,
                request.getXFrom(),
                request.getXTo(),
                request.getCount()
        );

        // 3. Сериализуем
        byte[] serialized;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(baos)) {
            FunctionsIO.serialize(bos, tabFunc);
            serialized = baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize function", e);
        }

        // 4. Владелец и тип
        UserEntity owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + request.getOwnerId()));
        FunctionTypeEntity type = functionTypeRepository.findByName("TABULATED")
                .orElseThrow(() -> new IllegalStateException("Function type 'TABULATED' not found in DB"));

        // 5. Сохраняем
        TabulatedFunctionEntity entity = new TabulatedFunctionEntity();
        entity.setName(request.getName());
        entity.setOwner(owner);
        entity.setFunctionType(type);
        entity.setSerializedData(serialized);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        TabulatedFunctionEntity saved = repository.save(entity);
        return toDto(saved);
    }

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