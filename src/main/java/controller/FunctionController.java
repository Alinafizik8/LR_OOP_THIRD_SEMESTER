package controller;

import dto.function.*;
import service.TabulatedFunctionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@Validated
public class FunctionController implements
        controller.api.FunctionApi,
        controller.api.FunctionOperationApi {

    private final TabulatedFunctionService functionService;

    public FunctionController(TabulatedFunctionService functionService) {
        this.functionService = functionService;
    }

    // ———————— CREATE ————————
    @Override
    @PostMapping("/from-points")
    public ResponseEntity<FunctionMetadataResponse> createFromPoints(@Valid @RequestBody CreateFunctionFromPointsRequest request) {
        FunctionMetadataResponse func = functionService.createFromPoints(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(func);
    }

    @Override
    @PostMapping("/from-math")
    public ResponseEntity<FunctionMetadataResponse> createFromMath(@Valid @RequestBody CreateFunctionFromMathRequest request) {
        FunctionMetadataResponse func = functionService.createFromMath(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(func);
    }

    // ———————— READ ————————
    @Override
    @GetMapping
    public ResponseEntity<Page<FunctionListDto>> listFunctions(Pageable pageable) {
        return ResponseEntity.ok(functionService.listFunctions(pageable));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<FullFunctionResponse> getFunction(@PathVariable Long id) {
        return ResponseEntity.ok(functionService.getFunction(id));
    }

    // ———————— UPDATE ————————
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<FunctionMetadataResponse> updateFunction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFunctionRequest request) {
        return ResponseEntity.ok(functionService.updateFunction(id, request));
    }

    // ———————— DELETE ————————
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        functionService.deleteFunction(id);
        return ResponseEntity.noContent().build();
    }

    // ———————— OPERATIONS ————————
    @Override
    @PostMapping("/add")
    public ResponseEntity<FunctionMetadataResponse> add(@Valid @RequestBody BinaryOperationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(functionService.add(request));
    }

    @Override
    @PostMapping("/subtract")
    public ResponseEntity<FunctionMetadataResponse> subtract(@Valid @RequestBody BinaryOperationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(functionService.subtract(request));
    }

    @Override
    @PostMapping("/multiply")
    public ResponseEntity<FunctionMetadataResponse> multiply(@Valid @RequestBody BinaryOperationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(functionService.multiply(request));
    }

    @Override
    @PostMapping("/divide")
    public ResponseEntity<FunctionMetadataResponse> divide(@Valid @RequestBody BinaryOperationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(functionService.divide(request));
    }

    @Override
    @PostMapping("/differentiate")
    public ResponseEntity<FunctionMetadataResponse> differentiate(@Valid @RequestBody UnaryOperationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(functionService.differentiate(request));
    }
}
