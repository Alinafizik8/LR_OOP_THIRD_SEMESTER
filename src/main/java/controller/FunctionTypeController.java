package controller;

import dto.function.FunctionTypeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import service.FunctionTypeService;
import mapper.FunctionTypeMapper;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FunctionTypeController implements controller.api.FunctionTypeApi {

    private final FunctionTypeService functionTypeService;
    private final FunctionTypeMapper mapper;

    public FunctionTypeController(FunctionTypeService functionTypeService, FunctionTypeMapper mapper) {
        this.functionTypeService = functionTypeService;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<List<FunctionTypeDto>> listFunctionTypes() {
        List<FunctionTypeDto> dtos = functionTypeService
                .findAllTypesSortedByPriority()
                .stream()
                .map(mapper::toDto) // маппинг Entity → DTO
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
