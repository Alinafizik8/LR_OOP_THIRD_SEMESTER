package service;

import Entity.FunctionTypeEntity;
import Repository.FunctionTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // Все методы по умолчанию в readOnly транзакциях
public class FunctionTypeServiceImpl implements FunctionTypeService {

    private static final Logger logger = LoggerFactory.getLogger(FunctionTypeServiceImpl.class);

    @Autowired
    private FunctionTypeRepository functionTypeRepository;

    @Override
    public Optional<FunctionTypeEntity> findTypeById(Long id) {
        logger.debug("Searching for function type by ID: {}", id);
        Optional<FunctionTypeEntity> type = functionTypeRepository.findById(id);
        if (type.isPresent()) {
            logger.debug("Function type found by ID: {}", id);
        } else {
            logger.info("Function type not found by ID: {}", id);
        }
        return type;
    }

    @Override
    public Optional<FunctionTypeEntity> findTypeByName(String name) {
        logger.debug("Searching for function type by name: {}", name);
        Optional<FunctionTypeEntity> type = functionTypeRepository.findByName(name);
        if (type.isPresent()) {
            logger.debug("Function type found by name: {}", name);
        } else {
            logger.info("Function type not found by name: {}", name);
        }
        return type;
    }

    @Override
    public List<FunctionTypeEntity> findAllTypes() {
        logger.debug("Fetching all function types");
        List<FunctionTypeEntity> types = functionTypeRepository.findAll();
        logger.debug("Fetched {} function types", types.size());
        return types;
    }

    @Override
    public List<FunctionTypeEntity> findAllTypesSortedByPriority() {
        logger.debug("Fetching all function types sorted by priority");
        List<FunctionTypeEntity> types = functionTypeRepository.findAllSortedByPriority();
        logger.debug("Fetched {} function types sorted by priority", types.size());
        return types;
    }

    @Override
    @Transactional // Переопределяем readOnly на true для метода, который изменяет данные
    public FunctionTypeEntity saveType(FunctionTypeEntity type) {
        logger.info("Saving function type: {}", type.getName());
        FunctionTypeEntity savedType = functionTypeRepository.save(type);
        logger.info("Function type saved successfully with ID: {}", savedType.getId());
        return savedType;
    }

    @Override
    @Transactional // Переопределяем readOnly на true для метода, который изменяет данные
    public void deleteTypeById(Long id) {
        logger.info("Deleting function type by ID: {}", id);
        if (!functionTypeRepository.existsById(id)) {
            logger.warn("Attempt to delete non-existent function type with ID: {}", id);
            // В реальном приложении выбросите исключение, например, EntityNotFoundException
            // throw new EntityNotFoundException("FunctionType not found with ID: " + id);
            return; // Или просто выйдите, если удаление необязательно
        }
        functionTypeRepository.deleteById(id);
        logger.info("Function type deleted successfully with ID: {}", id);
    }
}