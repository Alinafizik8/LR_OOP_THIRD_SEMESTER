package service;

import Entity.TabulatedFunctionEntity;
import java.util.List;
import java.util.Optional;

public interface TabulatedFunctionService {

    Optional<TabulatedFunctionEntity> findFunctionByIdAndOwnerId(Long id, Long ownerId);
    List<TabulatedFunctionEntity> findFunctionsByOwnerId(Long ownerId);
    List<TabulatedFunctionEntity> findFunctionsByOwnerIdAndTypeId(Long ownerId, Long typeId);
    TabulatedFunctionEntity saveFunction(TabulatedFunctionEntity function, Long ownerId); // Принимает ID владельца для валидации
    void updateFunctionName(Long id, Long ownerId, String newName);
    void updateFunctionDataAndName(Long id, Long ownerId, byte[] newData, String newName);
    void deleteFunctionByIdAndOwnerId(Long id, Long ownerId);
}