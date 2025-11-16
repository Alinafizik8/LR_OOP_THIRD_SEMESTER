package service;

import dto.function.FunctionTypeDto;
import java.util.List;
import java.util.Optional;

public interface FunctionTypeService {

    List<FunctionTypeDto> findAll();
    List<FunctionTypeDto> findAllSortedByPriority();
    Optional<FunctionTypeDto> findById(Long id);
    Optional<FunctionTypeDto> findByName(String name);
    FunctionTypeDto create(FunctionTypeDto dto);
    FunctionTypeDto update(Long id, FunctionTypeDto dto);
    void deleteById(Long id);
    List<FunctionTypeDto> searchByLocalizedNameFragment(String fragment);
    List<FunctionTypeDto> searchByNameOrLocalizedNameFragment(String fragment);
    boolean existsByName(String name);
}