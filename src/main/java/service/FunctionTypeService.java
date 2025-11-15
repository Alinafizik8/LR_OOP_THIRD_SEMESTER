package service;

import repository.FunctionTypeRepository;
import entity.FunctionTypeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FunctionTypeService {

    private static final Logger logger = LoggerFactory.getLogger(FunctionTypeService.class);

    @Autowired
    private FunctionTypeRepository functionTypeRepository;

    // одиночный поиск
    public Optional<FunctionTypeEntity> findTypeById(Long id) {
        logger.info("Поиск типа функции по ID: {}", id);
        Optional<FunctionTypeEntity> type = functionTypeRepository.findById(id);
        if (type.isPresent()) {
            logger.debug("Найден тип функции: {}", type.get().getName());
        } else {
            logger.warn("Тип функции с ID {} не найден.", id);
        }
        return type;
    }

    public Optional<FunctionTypeEntity> findTypeByName(String name) {
        logger.info("Поиск типа функции по имени: {}", name);
        Optional<FunctionTypeEntity> type = functionTypeRepository.findByName(name);
        if (type.isPresent()) {
            logger.debug("Найден тип функции: {}", type.get().getName());
        } else {
            logger.warn("Тип функции с именем {} не найден.", name);
        }
        return type;
    }

    // множественный поиск
    public List<FunctionTypeEntity> findTypesByLocalizedNameFragment(String localizedNameFragment) {
        logger.info("Поиск типов функций по фрагменту локализованного названия: {}", localizedNameFragment);
        List<FunctionTypeEntity> types = functionTypeRepository.findByLocalizedNameContainingIgnoreCase(localizedNameFragment);
        logger.debug("Найдено {} типов функций по фрагменту локализованного названия: '{}'", types.size(), localizedNameFragment);
        return types;
    }

    public List<FunctionTypeEntity> findTypesByNameOrLocalizedNameFragment(String fragment) {
        logger.info("Поиск типов функций по фрагменту имени или локализованного названия: {}", fragment);
        List<FunctionTypeEntity> types = functionTypeRepository.findByNameOrLocalizedNameContainingIgnoreCase(fragment);
        logger.debug("Найдено {} типов функций по фрагменту: '{}'", types.size(), fragment);
        return types;
    }

    // поиск с сортировкой
    public List<FunctionTypeEntity> findAllTypesSortedByPriority() {
        logger.info("Получение всех типов функций, отсортированных по приоритету (ASC) и локализованному названию (ASC).");
        List<FunctionTypeEntity> types = functionTypeRepository.findAllSortedByPriority();
        logger.debug("Получено {} типов функций, отсортированных по приоритету и локализованному названию.", types.size());
        return types;
    }
}