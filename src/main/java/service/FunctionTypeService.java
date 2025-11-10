package service;

import Entity.FunctionTypeEntity;
import java.util.List;
import java.util.Optional;

public interface FunctionTypeService {

    Optional<FunctionTypeEntity> findTypeById(Long id);
    Optional<FunctionTypeEntity> findTypeByName(String name);
    List<FunctionTypeEntity> findAllTypes();
    List<FunctionTypeEntity> findAllTypesSortedByPriority();
    FunctionTypeEntity saveType(FunctionTypeEntity type);
    void deleteTypeById(Long id); // Обычно администраторская функция
}