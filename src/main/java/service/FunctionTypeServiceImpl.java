package service;

import dto.function.FunctionTypeDto;
import entity.FunctionTypeEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.FunctionTypeRepository;
import service.FunctionTypeService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FunctionTypeServiceImpl implements FunctionTypeService {

    private final FunctionTypeRepository functionTypeRepository;

    public FunctionTypeServiceImpl(FunctionTypeRepository functionTypeRepository) {
        this.functionTypeRepository = functionTypeRepository;
    }

    // ─── READ ───────────────────────────────────────────────────────

    @Override
    public List<FunctionTypeDto> findAll() {
        return functionTypeRepository.findAll().stream()
                .map(FunctionTypeServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FunctionTypeDto> findAllSortedByPriority() {
        return functionTypeRepository.findAllSortedByPriority().stream()
                .map(FunctionTypeServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FunctionTypeDto> findById(Long id) {
        return functionTypeRepository.findById(id).map(FunctionTypeServiceImpl::toDto);
    }

    @Override
    public Optional<FunctionTypeDto> findByName(String name) {
        return functionTypeRepository.findByName(name).map(FunctionTypeServiceImpl::toDto);
    }

    @Override
    public List<FunctionTypeDto> searchByLocalizedNameFragment(String fragment) {
        return functionTypeRepository.findByLocalizedNameContainingIgnoreCase(fragment).stream()
                .map(FunctionTypeServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<FunctionTypeDto> searchByNameOrLocalizedNameFragment(String fragment) {
        return functionTypeRepository.findByNameOrLocalizedNameContainingIgnoreCase(fragment).stream()
                .map(FunctionTypeServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    // ─── CREATE ─────────────────────────────────────────────────────

    @Transactional
    @Override
    public FunctionTypeDto create(FunctionTypeDto dto) {
        if (functionTypeRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Function type with name '" + dto.getName() + "' already exists");
        }

        FunctionTypeEntity entity = toEntity(dto);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        FunctionTypeEntity saved = functionTypeRepository.save(entity);
        return toDto(saved);
    }

    // ─── UPDATE ─────────────────────────────────────────────────────

    @Transactional
    @Override
    public FunctionTypeDto update(Long id, FunctionTypeDto dto) {
        FunctionTypeEntity entity = functionTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Function type not found with id: " + id));

        // Проверка уникальности имени при изменении
        if (dto.getName() != null && !dto.getName().equals(entity.getName())) {
            if (functionTypeRepository.existsByName(dto.getName())) {
                throw new IllegalArgumentException("Function type with name '" + dto.getName() + "' already exists");
            }
            entity.setName(dto.getName());
        }

        if (dto.getLocalizedName() != null) {
            entity.setLocalizedName(dto.getLocalizedName());
        }
        if (dto.getPriority() != null) {
            entity.setPriority(dto.getPriority());
        }

        entity.setUpdatedAt(Instant.now());
        FunctionTypeEntity updated = functionTypeRepository.save(entity);
        return toDto(updated);
    }

    // ─── DELETE ─────────────────────────────────────────────────────

    @Transactional
    @Override
    public void deleteById(Long id) {
        if (!functionTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Function type not found with id: " + id);
        }
        functionTypeRepository.deleteById(id);
    }

    // ─── VALIDATION ─────────────────────────────────────────────────

    @Override
    public boolean existsByName(String name) {
        return functionTypeRepository.existsByName(name);
    }

    // ─── MAPPING (ручной) ───────────────────────────────────────────

    private static FunctionTypeDto toDto(FunctionTypeEntity e) {
        FunctionTypeDto dto = new FunctionTypeDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setLocalizedName(e.getLocalizedName());
        dto.setPriority(e.getPriority());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    private static FunctionTypeEntity toEntity(FunctionTypeDto dto) {
        FunctionTypeEntity e = new FunctionTypeEntity();
        e.setName(dto.getName());
        e.setLocalizedName(dto.getLocalizedName());
        e.setPriority(dto.getPriority());
        // createdAt/updatedAt проставляются в create/update
        return e;
    }
}