package controller.api;

import dto.function.FunctionTypeDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.FunctionTypeService;

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

    @GetMapping("/sorted")
    public ResponseEntity<List<FunctionTypeDto>> getFunctionTypesSortedByPriority() {
        return ResponseEntity.ok(functionTypeService.findAllSortedByPriority());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FunctionTypeDto> getFunctionTypeById(@PathVariable Long id) {
        return functionTypeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<FunctionTypeDto> getFunctionTypeByName(@PathVariable String name) {
        return functionTypeService.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/localized")
    public ResponseEntity<List<FunctionTypeDto>> searchByLocalizedName(
            @RequestParam String fragment) {
        return ResponseEntity.ok(functionTypeService.searchByLocalizedNameFragment(fragment));
    }

    @GetMapping("/search/any")
    public ResponseEntity<List<FunctionTypeDto>> searchByNameOrLocalizedName(
            @RequestParam String fragment) {
        return ResponseEntity.ok(functionTypeService.searchByNameOrLocalizedNameFragment(fragment));
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
        FunctionTypeDto updated = functionTypeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunctionType(@PathVariable Long id) {
        functionTypeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}