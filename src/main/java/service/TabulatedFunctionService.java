package service;

import repository.TabulatedFunctionRepository;
import entity.TabulatedFunctionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TabulatedFunctionService {

    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionService.class);

    @Autowired
    private TabulatedFunctionRepository functionRepository;

    //Одиночный поиск
    public Optional<TabulatedFunctionEntity> findFunctionByIdAndOwner(Long id, Long ownerId) {
        logger.info("Поиск функции по ID: {} для владельца ID: {}", id, ownerId);
        Optional<TabulatedFunctionEntity> function = functionRepository.findByIdAndOwnerId(id, ownerId);
        if (function.isPresent()) {
            logger.debug("Функция с ID: {} найдена для владельца ID: {}", id, ownerId);
        } else {
            logger.warn("Функция с ID: {} не найдена для владельца ID: {}", id, ownerId);
        }
        return function;
    }

    // Множественный поиск
    public List<TabulatedFunctionEntity> findFunctionsByOwnerId(Long ownerId) {
        logger.info("Поиск всех функций для владельца ID: {}", ownerId);
        List<TabulatedFunctionEntity> functions = functionRepository.findByOwnerId(ownerId);
        logger.debug("Найдено {} функций для владельца ID: {}", functions.size(), ownerId);
        return functions;
    }

    public List<TabulatedFunctionEntity> findFunctionsByOwnerIdAndTypeId(Long ownerId, Long typeId) {
        logger.info("Поиск функций для владельца ID: {} и типа ID: {}", ownerId, typeId);
        List<TabulatedFunctionEntity> functions = functionRepository.findByOwnerIdAndFunctionTypeId(ownerId, typeId);
        logger.debug("Найдено {} функций для владельца ID: {} и типа ID: {}", functions.size(), ownerId, typeId);
        return functions;
    }

    // Поиск с сортировкой
    public List<TabulatedFunctionEntity> findFunctionsByOwnerIdSortedByName(Long ownerId) {
        logger.info("Поиск и сортировка функций по имени для владельца ID: {}", ownerId);
        List<TabulatedFunctionEntity> functions = functionRepository.findByOwnerIdOrderByNameAsc(ownerId);
        logger.debug("Найдено {} функций для владельца ID: {}, отсортированных по имени", functions.size(), ownerId);
        return functions;
    }

    public List<TabulatedFunctionEntity> findFunctionsByOwnerIdSortedByCreationDate(Long ownerId) {
        logger.info("Поиск и сортировка функций по дате создания (DESC) для владельца ID: {}", ownerId);
        List<TabulatedFunctionEntity> functions = functionRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
        logger.debug("Найдено {} функций для владельца ID: {}, отсортированных по дате создания DESC", functions.size(), ownerId);
        return functions;
    }

    //Поиск с пагинацией и сортировкой
    public Page<TabulatedFunctionEntity> findFunctionsByOwnerIdPaged(Long ownerId, int page, int size, String sortBy, String direction) {
        logger.info("Поиск функций для владельца ID: {} с пагинацией (страница: {}, размер: {}) и сортировкой по '{} {}'", ownerId, page, size, sortBy, direction);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TabulatedFunctionEntity> functionsPage = functionRepository.findByOwnerId(ownerId, pageable);
        logger.debug("Получена страница функций для владельца ID: {}: номер {}, размер {}, всего элементов {}, всего страниц {}",
                ownerId, functionsPage.getNumber(), functionsPage.getSize(), functionsPage.getTotalElements(), functionsPage.getTotalPages());
        return functionsPage;
    }

    // Множественный поиск по фрагменту имени
    public List<TabulatedFunctionEntity> findFunctionsByOwnerIdAndNameFragment(Long ownerId, String nameFragment) {
        logger.info("Поиск функций для владельца ID: {} по фрагменту имени: '{}'", ownerId, nameFragment);
        List<TabulatedFunctionEntity> functions = functionRepository.findByNameContainingIgnoreCaseAndOwnerId(nameFragment, ownerId);
        logger.debug("Найдено {} функций для владельца ID: {} по фрагменту имени: '{}'", functions.size(), ownerId, nameFragment);
        return functions;
    }

    public List<TabulatedFunctionEntity> findFunctionsByOwnerIdAndNameFragmentSorted(Long ownerId, String nameFragment) {
        logger.info("Поиск и сортировка функций для владельца ID: {} по фрагменту имени: '{}' (по дате создания DESC)", ownerId, nameFragment);
        List<TabulatedFunctionEntity> functions = functionRepository.findByNameContainingIgnoreCaseAndOwnerIdOrderByCreatedAtDesc(nameFragment, ownerId);
        logger.debug("Найдено {} функций для владельца ID: {} по фрагменту имени: '{}', отсортированных по дате создания DESC", functions.size(), ownerId, nameFragment);
        return functions;
    }

    // Поиск по связанным сущностям
    public List<TabulatedFunctionEntity> findFunctionsByFunctionTypeId(Long typeId) {
        logger.info("Поиск всех функций по типу ID: {}", typeId);
        List<TabulatedFunctionEntity> functions = functionRepository.findByFunctionTypeId(typeId);
        logger.debug("Найдено {} функций с типом ID: {}", functions.size(), typeId);
        return functions;
    }

    public List<TabulatedFunctionEntity> findFunctionsByFunctionTypeIdSorted(Long typeId) {
        logger.info("Поиск и сортировка функций по типу ID: {} (по дате создания DESC)", typeId);
        List<TabulatedFunctionEntity> functions = functionRepository.findByFunctionTypeIdOrderByCreatedAtDesc(typeId);
        logger.debug("Найдено {} функций с типом ID: {}, отсортированных по дате создания DESC", functions.size(), typeId);
        return functions;
    }

    // Сохранение
    public TabulatedFunctionEntity save(TabulatedFunctionEntity entity) {
        return functionRepository.save(entity);
    }

    // Удаление с проверкой владельца
    public void deleteById(Long id, Long ownerId) {
        boolean deleted = functionRepository.deleteByIdAndOwnerId(id, ownerId);
        if (!deleted) {
            throw new IllegalArgumentException("Функция ID=" + id + " не найдена или не принадлежит владельцу");
        }
    }
}