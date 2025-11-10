package service;

import Entity.TabulatedFunctionEntity;
import Repository.TabulatedFunctionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true) // Все методы по умолчанию в readOnly транзакциях
public class TabulatedFunctionServiceImpl implements TabulatedFunctionService {

    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionServiceImpl.class);

    @Autowired
    private TabulatedFunctionRepository functionRepository;

    @Override
    public Optional<TabulatedFunctionEntity> findFunctionByIdAndOwnerId(Long id, Long ownerId) {
        logger.debug("Searching for function by ID: {} and owner ID: {}", id, ownerId);
        Optional<TabulatedFunctionEntity> function = functionRepository.findByIdAndOwnerId(id, ownerId);
        if (function.isPresent()) {
            logger.debug("Function found by ID: {} and owner ID: {}", id, ownerId);
        } else {
            logger.info("Function not found by ID: {} for owner ID: {}", id, ownerId);
        }
        return function;
    }

    @Override
    public List<TabulatedFunctionEntity> findFunctionsByOwnerId(Long ownerId) {
        logger.debug("Fetching functions for owner ID: {}", ownerId);
        List<TabulatedFunctionEntity> functions = functionRepository.findByOwnerId(ownerId);
        logger.debug("Fetched {} functions for owner ID: {}", functions.size(), ownerId);
        return functions;
    }

    @Override
    public List<TabulatedFunctionEntity> findFunctionsByOwnerIdAndTypeId(Long ownerId, Long typeId) {
        logger.debug("Fetching functions for owner ID: {} and type ID: {}", ownerId, typeId);
        List<TabulatedFunctionEntity> functions = functionRepository.findByOwnerIdAndFunctionTypeId(ownerId, typeId);
        logger.debug("Fetched {} functions for owner ID: {} and type ID: {}", functions.size(), ownerId, typeId);
        return functions;
    }

    @Override
    @Transactional // Переопределяем readOnly на true для метода, который изменяет данные
    public TabulatedFunctionEntity saveFunction(TabulatedFunctionEntity function, Long ownerId) {
        // Валидация владельца может быть добавлена здесь, если функция создаётся не от имени владельца
        logger.info("Saving function '{}' for owner ID: {}", function.getName(), ownerId);
        function.setOwnerId(ownerId); // Устанавливаем ID владельца при сохранении
        function.setCreatedAt(LocalDateTime.now());
        function.setUpdatedAt(LocalDateTime.now());
        TabulatedFunctionEntity savedFunction = functionRepository.save(function);
        logger.info("Function saved successfully with ID: {} for owner ID: {}", savedFunction.getId(), ownerId);
        return savedFunction;
    }

    @Override
    @Transactional // Переопределяем readOnly на true для метода, который изменяет данные
    public void updateFunctionName(Long id, Long ownerId, String newName) {
        logger.info("Updating name for function ID: {} and owner ID: {} to: {}", id, ownerId, newName);
        int updatedRows = functionRepository.updateName(id, ownerId, newName);
        if (updatedRows == 0) {
            logger.warn("No function found to update name for ID: {} and owner ID: {}", id, ownerId);
            // В реальном приложении выбросите исключение, если функция не найдена или доступ запрещён
            // throw new AccessDeniedException("Function not found or access denied for user ID: " + ownerId);
        } else {
            logger.info("Function name updated successfully for ID: {} and owner ID: {}", id, ownerId);
        }
    }

    @Override
    @Transactional // Переопределяем readOnly на true для метода, который изменяет данные
    public void updateFunctionDataAndName(Long id, Long ownerId, byte[] newData, String newName) {
        logger.info("Updating data and name for function ID: {} and owner ID: {} to name: {}", id, ownerId, newName);
        int updatedRows = functionRepository.updateDataAndName(id, ownerId, newData, newName);
        if (updatedRows == 0) {
            logger.warn("No function found to update data and name for ID: {} and owner ID: {}", id, ownerId);
            // В реальном приложении выбросите исключение, если функция не найдена или доступ запрещён
            // throw new AccessDeniedException("Function not found or access denied for user ID: " + ownerId);
        } else {
            logger.info("Function data and name updated successfully for ID: {} and owner ID: {}", id, ownerId);
        }
    }

    @Override
    @Transactional // Переопределяем readOnly на true для метода, который изменяет данные
    public void deleteFunctionByIdAndOwnerId(Long id, Long ownerId) {
        logger.info("Deleting function by ID: {} for owner ID: {}", id, ownerId);
        // Метод deleteByIdAndOwnerId в репозитории уже обеспечивает изоляцию
        long countBefore = functionRepository.count(); // Логирование для отладки
        logger.debug("Functions count before delete: {}", countBefore);
        functionRepository.deleteByIdAndOwnerId(id, ownerId);
        long countAfter = functionRepository.count(); // Логирование для отладки
        logger.debug("Functions count after delete: {}", countAfter);
        if (countBefore == countAfter) {
            logger.warn("No function found to delete for ID: {} and owner ID: {}", id, ownerId);
            // В реальном приложении выбросите исключение, если функция не найдена или доступ запрещён
            // throw new AccessDeniedException("Function not found or access denied for user ID: " + ownerId);
        } else {
            logger.info("Function deleted successfully by ID: {} for owner ID: {}", id, ownerId);
        }
    }
}
