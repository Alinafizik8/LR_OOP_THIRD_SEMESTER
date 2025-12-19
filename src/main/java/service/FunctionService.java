package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dto.CreateFunctionFromArraysRequest;
import dto.CreateFunctionFromMathRequest;
import dto.DifferentiationRequest;
import dto.FunctionDTO;
import dto.OperationRequest;
import dto.UpdateFunctionRequest;
import entity.FunctionEntity;
import entity.User;
import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import exceptions.InconsistentFunctionsException;
import functions.*;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import io.FunctionsIO;
import operations.TabulatedDifferentialOperator;
import operations.TabulatedFunctionOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.FunctionRepository;
import repository.UserRepository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FunctionService {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MathFunctionScanner mathFunctionScanner;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TabulatedFunctionFactory getFactory(String type) {
        if ("LINKED_LIST".equalsIgnoreCase(type)) {
            return new LinkedListTabulatedFunctionFactory();
        }
        return new ArrayTabulatedFunctionFactory();
    }

    private MathFunction getMathFunction(String type, Double constantValue) {
        return switch (type.toUpperCase()) {
            case "SQR" -> new SqrFunction();
            case "IDENTITY" -> new IdentityFunction();
            case "UNIT" -> new UnitFunction();
            case "ZERO" -> new ZeroFunction();
            case "CONSTANT" -> {
                if (constantValue == null) {
                    throw new IllegalArgumentException("Constant value is required for CONSTANT function");
                }
                yield new ConstantFunction(constantValue);
            }
            default -> throw new IllegalArgumentException("Unknown math function type: " + type);
        };
    }

    @Transactional
    public FunctionDTO createFunctionFromArrays(CreateFunctionFromArraysRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getXValues().length != request.getYValues().length) {
            throw new DifferentLengthOfArraysException("X and Y arrays must have the same length");
        }

        if (request.getXValues().length < 2) {
            throw new IllegalArgumentException("At least 2 points are required");
        }

        TabulatedFunctionFactory factory = getFactory(request.getFunctionType());
        TabulatedFunction function = factory.create(request.getXValues(), request.getYValues());

        FunctionEntity entity = new FunctionEntity();
        entity.setName(request.getName());
        entity.setFunctionType(request.getFunctionType());
        entity.setCount(function.getCount());
        entity.setUser(user);

        try {
            entity.setXValues(objectMapper.writeValueAsString(request.getXValues()));
            entity.setYValues(objectMapper.writeValueAsString(request.getYValues()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize function values", e);
        }

        entity = functionRepository.save(entity);
        return entityToDTO(entity);
    }

    @Transactional
    public FunctionDTO createFunctionFromMath(CreateFunctionFromMathRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getCount() < 2) {
            throw new IllegalArgumentException("At least 2 points are required");
        }

        MathFunction mathFunction = getMathFunction(request.getMathFunctionType(), request.getConstantValue());
        TabulatedFunctionFactory factory = getFactory(request.getFunctionType());

        TabulatedFunction function = factory.create(mathFunction, request.getXFrom(), request.getXTo(), request.getCount());

        double[] xValues = new double[function.getCount()];
        double[] yValues = new double[function.getCount()];

        int i = 0;
        for (Point point : function) {
            xValues[i] = point.x;
            yValues[i] = point.y;
            i++;
        }

        FunctionEntity entity = new FunctionEntity();
        entity.setName(request.getName());
        entity.setFunctionType(request.getFunctionType());
        entity.setCount(function.getCount());
        entity.setUser(user);

        try {
            entity.setXValues(objectMapper.writeValueAsString(xValues));
            entity.setYValues(objectMapper.writeValueAsString(yValues));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize function values", e);
        }

        entity = functionRepository.save(entity);
        return entityToDTO(entity);
    }

    public FunctionDTO previewFunctionFromMath(CreateFunctionFromMathRequest request) {
        if (request.getCount() < 2) {
            throw new IllegalArgumentException("At least 2 points are required");
        }

        MathFunction mathFunction = getMathFunction(request.getMathFunctionType(), request.getConstantValue());
        TabulatedFunctionFactory factory = getFactory(request.getFunctionType());

        TabulatedFunction function = factory.create(mathFunction, request.getXFrom(), request.getXTo(), request.getCount());

        double[] xValues = new double[function.getCount()];
        double[] yValues = new double[function.getCount()];

        int i = 0;
        for (Point point : function) {
            xValues[i] = point.x;
            yValues[i] = point.y;
            i++;
        }

        return new FunctionDTO(
                null,
                "Preview",
                request.getFunctionType(),
                xValues,
                yValues,
                function.getCount()
        );
    }

    public List<FunctionDTO> getUserFunctions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return functionRepository.findByUserId(user.getId()).stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    public FunctionDTO getFunctionById(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FunctionEntity entity = functionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found"));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return entityToDTO(entity);
    }

    @Transactional
    public FunctionDTO updateFunction(Long id, UpdateFunctionRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FunctionEntity entity = functionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found"));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }

        if (request.getXValues() != null && request.getYValues() != null) {
            if (request.getXValues().length != request.getYValues().length) {
                throw new DifferentLengthOfArraysException("X and Y arrays must have the same length");
            }

            if (request.getXValues().length < 2) {
                throw new IllegalArgumentException("At least 2 points are required");
            }

            try {
                entity.setXValues(objectMapper.writeValueAsString(request.getXValues()));
                entity.setYValues(objectMapper.writeValueAsString(request.getYValues()));
                entity.setCount(request.getXValues().length);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize function values", e);
            }
        }

        entity = functionRepository.save(entity);
        return entityToDTO(entity);
    }

    @Transactional
    public void deleteFunction(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FunctionEntity entity = functionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found"));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        functionRepository.delete(entity);
    }

    public FunctionDTO differentiateFunction(DifferentiationRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FunctionEntity entity = functionRepository.findById(request.getFunctionId())
                .orElseThrow(() -> new RuntimeException("Function not found"));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        try {
            double[] xValues = objectMapper.readValue(entity.getXValues(), double[].class);
            double[] yValues = objectMapper.readValue(entity.getYValues(), double[].class);

            TabulatedFunctionFactory factory = getFactory(request.getFunctionType() != null ?
                    request.getFunctionType() : entity.getFunctionType());

            TabulatedFunction sourceFunction = factory.create(xValues, yValues);

            TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);
            TabulatedFunction derivedFunction = operator.derive(sourceFunction);

            double[] derivedXValues = new double[derivedFunction.getCount()];
            double[] derivedYValues = new double[derivedFunction.getCount()];

            int i = 0;
            for (Point point : derivedFunction) {
                derivedXValues[i] = point.x;
                derivedYValues[i] = point.y;
                i++;
            }

            return new FunctionDTO(
                    null,
                    entity.getName() + " (производная)",
                    request.getFunctionType() != null ? request.getFunctionType() : entity.getFunctionType(),
                    derivedXValues,
                    derivedYValues,
                    derivedFunction.getCount()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize function values", e);
        }
    }

    public FunctionDTO performOperation(OperationRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FunctionEntity firstEntity = functionRepository.findById(request.getFirstFunctionId())
                .orElseThrow(() -> new RuntimeException("First function not found"));
        FunctionEntity secondEntity = functionRepository.findById(request.getSecondFunctionId())
                .orElseThrow(() -> new RuntimeException("Second function not found"));

        if (!firstEntity.getUser().getId().equals(user.getId()) ||
                !secondEntity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        try {
            double[] xValues1 = objectMapper.readValue(firstEntity.getXValues(), double[].class);
            double[] yValues1 = objectMapper.readValue(firstEntity.getYValues(), double[].class);
            double[] xValues2 = objectMapper.readValue(secondEntity.getXValues(), double[].class);
            double[] yValues2 = objectMapper.readValue(secondEntity.getYValues(), double[].class);

            TabulatedFunctionFactory factory = getFactory(request.getFunctionType() != null ?
                    request.getFunctionType() : firstEntity.getFunctionType());

            TabulatedFunction function1 = factory.create(xValues1, yValues1);
            TabulatedFunction function2 = factory.create(xValues2, yValues2);

            TabulatedFunctionOperationService operationService = new TabulatedFunctionOperationService(factory);
            TabulatedFunction resultFunction;

            String operationName;
            try {
                switch (request.getOperation().toUpperCase()) {
                    case "PLUS":
                        resultFunction = operationService.plus(function1, function2);
                        operationName = "+";
                        break;
                    case "MINUS":
                        resultFunction = operationService.minus(function1, function2);
                        operationName = "-";
                        break;
                    case "MULTIPLY":
                        resultFunction = operationService.multiply(function1, function2);
                        operationName = "*";
                        break;
                    case "DIVIDE":
                        resultFunction = operationService.divide(function1, function2);
                        operationName = "/";
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown operation: " + request.getOperation());
                }
            } catch (InconsistentFunctionsException e) {
                throw new InconsistentFunctionsException("Функции несовместимы для операции: " + e.getMessage());
            } catch (ArithmeticException e) {
                throw new ArithmeticException("Ошибка при выполнении операции: " + e.getMessage());
            }

            double[] resultXValues = new double[resultFunction.getCount()];
            double[] resultYValues = new double[resultFunction.getCount()];

            int i = 0;
            for (Point point : resultFunction) {
                resultXValues[i] = point.x;
                resultYValues[i] = point.y;
                i++;
            }

            return new FunctionDTO(
                    null,
                    firstEntity.getName() + " " + operationName + " " + secondEntity.getName(),
                    request.getFunctionType() != null ? request.getFunctionType() : firstEntity.getFunctionType(),
                    resultXValues,
                    resultYValues,
                    resultFunction.getCount()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize function values", e);
        }
    }

    public String serializeFunctionToBase64(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FunctionEntity entity = functionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found"));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        try {
            double[] xValues = objectMapper.readValue(entity.getXValues(), double[].class);
            double[] yValues = objectMapper.readValue(entity.getYValues(), double[].class);

            TabulatedFunctionFactory factory = getFactory(entity.getFunctionType());
            TabulatedFunction function = factory.create(xValues, yValues);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

            FunctionsIO.writeTabulatedFunction(bufferedOutputStream, function);
            bufferedOutputStream.flush();

            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize function", e);
        }
    }

    @Transactional
    public FunctionDTO deserializeFunctionFromBase64(String base64Data, String name, String functionType, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);

            TabulatedFunctionFactory factory = getFactory(functionType);
            TabulatedFunction function = FunctionsIO.readTabulatedFunction(bufferedInputStream, factory);

            double[] xValues = new double[function.getCount()];
            double[] yValues = new double[function.getCount()];

            int i = 0;
            for (Point point : function) {
                xValues[i] = point.x;
                yValues[i] = point.y;
                i++;
            }

            FunctionEntity entity = new FunctionEntity();
            entity.setName(name);
            entity.setFunctionType(functionType);
            entity.setCount(function.getCount());
            entity.setUser(user);
            entity.setXValues(objectMapper.writeValueAsString(xValues));
            entity.setYValues(objectMapper.writeValueAsString(yValues));

            entity = functionRepository.save(entity);
            return entityToDTO(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize function", e);
        }
    }

    public double applyFunction(Long id, double x, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FunctionEntity entity = functionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found"));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        try {
            double[] xValues = objectMapper.readValue(entity.getXValues(), double[].class);
            double[] yValues = objectMapper.readValue(entity.getYValues(), double[].class);

            TabulatedFunctionFactory factory = getFactory(entity.getFunctionType());
            TabulatedFunction function = factory.create(xValues, yValues);

            return function.apply(x);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize function values", e);
        }
    }

    @Transactional
    public FunctionDTO insertPoint(Long id, double x, double y, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FunctionEntity entity = functionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found"));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        try {
            double[] xValues = objectMapper.readValue(entity.getXValues(), double[].class);
            double[] yValues = objectMapper.readValue(entity.getYValues(), double[].class);

            TabulatedFunctionFactory factory = getFactory(entity.getFunctionType());
            TabulatedFunction function = factory.create(xValues, yValues);

            // Проверяем, что функция поддерживает Insertable
            if (!(function instanceof functions.Insertable)) {
                throw new UnsupportedOperationException("This function type does not support insertion");
            }

            functions.Insertable insertableFunction = (functions.Insertable) function;
            insertableFunction.insert(x, y);

            // Сохраняем обновленные значения
            double[] newXValues = new double[function.getCount()];
            double[] newYValues = new double[function.getCount()];

            int i = 0;
            for (Point point : function) {
                newXValues[i] = point.x;
                newYValues[i] = point.y;
                i++;
            }

            entity.setXValues(objectMapper.writeValueAsString(newXValues));
            entity.setYValues(objectMapper.writeValueAsString(newYValues));
            entity.setCount(function.getCount());

            entity = functionRepository.save(entity);
            return entityToDTO(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize/serialize function values", e);
        }
    }

    @Transactional
    public FunctionDTO removePoint(Long id, int index, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FunctionEntity entity = functionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found"));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        try {
            double[] xValues = objectMapper.readValue(entity.getXValues(), double[].class);
            double[] yValues = objectMapper.readValue(entity.getYValues(), double[].class);

            TabulatedFunctionFactory factory = getFactory(entity.getFunctionType());
            TabulatedFunction function = factory.create(xValues, yValues);

            // Проверяем, что функция поддерживает Removable
            if (!(function instanceof functions.Removable)) {
                throw new UnsupportedOperationException("This function type does not support removal");
            }

            functions.Removable removableFunction = (functions.Removable) function;
            removableFunction.remove(index);

            // Сохраняем обновленные значения
            double[] newXValues = new double[function.getCount()];
            double[] newYValues = new double[function.getCount()];

            int i = 0;
            for (Point point : function) {
                newXValues[i] = point.x;
                newYValues[i] = point.y;
                i++;
            }

            entity.setXValues(objectMapper.writeValueAsString(newXValues));
            entity.setYValues(objectMapper.writeValueAsString(newYValues));
            entity.setCount(function.getCount());

            entity = functionRepository.save(entity);
            return entityToDTO(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize/serialize function values", e);
        }
    }

    /**
     * Возвращает список доступных математических функций с их локализованными именами
     */
    public List<java.util.Map<String, String>> getMathFunctionTypes() {
        return mathFunctionScanner.getAllFunctions().stream()
                .map(meta -> {
                    java.util.Map<String, String> map = new java.util.HashMap<>();
                    map.put("type", meta.getClassName());
                    map.put("name", meta.getLocalizedName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Сериализует функцию в XML формат
     */
    public String serializeFunctionToXml(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FunctionEntity entity = functionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found"));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        try {
            FunctionDTO dto = entityToDTO(entity);
            XmlMapper xmlMapper = new XmlMapper();
            return xmlMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize function to XML", e);
        }
    }

    /**
     * Десериализует функцию из XML формата
     */
    @Transactional
    public FunctionDTO deserializeFunctionFromXml(String xmlData, String name, String functionType, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            XmlMapper xmlMapper = new XmlMapper();
            FunctionDTO dto = xmlMapper.readValue(xmlData, FunctionDTO.class);

            FunctionEntity entity = new FunctionEntity();
            entity.setName(name);
            entity.setFunctionType(functionType);
            entity.setCount(dto.getCount());
            entity.setUser(user);
            entity.setXValues(objectMapper.writeValueAsString(dto.getXValues()));
            entity.setYValues(objectMapper.writeValueAsString(dto.getYValues()));

            entity = functionRepository.save(entity);
            return entityToDTO(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize function from XML", e);
        }
    }

    /**
     * Сериализует функцию в JSON формат
     */
    public String serializeFunctionToJson(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FunctionEntity entity = functionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Function not found"));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        try {
            FunctionDTO dto = entityToDTO(entity);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize function to JSON", e);
        }
    }

    /**
     * Десериализует функцию из JSON формата
     */
    @Transactional
    public FunctionDTO deserializeFunctionFromJson(String jsonData, String name, String functionType, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            FunctionDTO dto = objectMapper.readValue(jsonData, FunctionDTO.class);

            FunctionEntity entity = new FunctionEntity();
            entity.setName(name);
            entity.setFunctionType(functionType);
            entity.setCount(dto.getCount());
            entity.setUser(user);
            entity.setXValues(objectMapper.writeValueAsString(dto.getXValues()));
            entity.setYValues(objectMapper.writeValueAsString(dto.getYValues()));

            entity = functionRepository.save(entity);
            return entityToDTO(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize function from JSON", e);
        }
    }

    private FunctionDTO entityToDTO(FunctionEntity entity) {
        try {
            double[] xValues = objectMapper.readValue(entity.getXValues(), double[].class);
            double[] yValues = objectMapper.readValue(entity.getYValues(), double[].class);

            return new FunctionDTO(
                    entity.getId(),
                    entity.getName(),
                    entity.getFunctionType(),
                    xValues,
                    yValues,
                    entity.getCount()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize function values", e);
        }
    }
}
