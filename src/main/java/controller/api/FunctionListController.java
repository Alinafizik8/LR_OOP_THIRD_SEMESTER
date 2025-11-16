package controller.api;

import dto.function.FunctionListDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.FunctionListService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/functions")
public class FunctionListController {

    private final FunctionListService functionListService;

    public FunctionListController(FunctionListService functionListService) {
        this.functionListService = functionListService;
    }

    @GetMapping
    public ResponseEntity<List<FunctionListDto>> getAllFunctions() {
        return ResponseEntity.ok(functionListService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FunctionListDto> getFunctionById(@PathVariable Long id) {
        return functionListService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FunctionListDto> createFunction(@RequestBody FunctionListDto dto) {
        FunctionListDto created = functionListService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FunctionListDto> updateFunction(
            @PathVariable Long id,
            @RequestBody FunctionListDto dto) {
        return ResponseEntity.ok(functionListService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        functionListService.delete(id);
        return ResponseEntity.noContent().build();
    }
}