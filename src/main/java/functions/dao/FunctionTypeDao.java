package functions.dao;

import dto.FunctionTypeDTO;

import java.util.List;
import java.util.Optional;

public interface FunctionTypeDao {

    Long save(FunctionTypeDTO type);
    Optional<FunctionTypeDTO> findById(Long id);
    Optional<FunctionTypeDTO> findByName(String name);
    List<FunctionTypeDTO> findAll();
    List<FunctionTypeDTO> findAllSortedByPriority();
    void update(FunctionTypeDTO type);
    void deleteById(Long id);
}