package controller.api;

import dto.function.FunctionTypeDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/function-types")
public class FunctionTypeController {

    private final FunctionTypeService functionTypeService;

    public FunctionTypeController(FunctionTypeService functionTypeService) {
        this.functionTypeService = functionTypeService;
    }

    @GetMapping
    public ResponseEntity<List<FunctionTypeDto>> getAllFunctionTypes() {
        return ResponseEntity.ok(functionTypeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FunctionTypeDto> getFunctionTypeById(@PathVariable Long id) {
        return functionTypeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FunctionTypeDto> createFunctionType(@RequestBody FunctionTypeDto dto) {
        FunctionTypeDto created = functionTypeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FunctionTypeDto> updateFunctionType(
            @PathVariable Long id,
            @RequestBody FunctionTypeDto dto) {
        return ResponseEntity.ok(functionTypeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunctionType(@PathVariable Long id) {
        functionTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}