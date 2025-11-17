package controller.api;

import dto.function.FunctionTypeDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import service.FunctionTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/v1/function-types")
public class FunctionTypeController {

    private static final Logger logger = LoggerFactory.getLogger(FunctionTypeController.class);

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
        logger.info("Created FunctionType with ID={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FunctionTypeDto> updateFunctionType(
            @PathVariable Long id,
            @RequestBody FunctionTypeDto dto) {
        FunctionTypeDto updated = functionTypeService.update(id, dto);
        logger.info("Updated FunctionType ID={}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@tabulatedFunctionService.canModify(authentication, #id)")
    public ResponseEntity<Void> deleteFunctionType(@PathVariable Long id) {
        functionTypeService.deleteById(id);
        logger.info("Deleted FunctionType ID={}", id);
        return ResponseEntity.noContent().build();
    }
}
