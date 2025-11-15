package controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dto.function.*;

@RequestMapping("/api/v1/functions/operations")
public interface FunctionOperationApi {

    @PostMapping("/add")
    ResponseEntity<FunctionMetadataResponse> add(@RequestBody BinaryOperationRequest request);

    @PostMapping("/subtract")
    ResponseEntity<FunctionMetadataResponse> subtract(@RequestBody BinaryOperationRequest request);

    @PostMapping("/multiply")
    ResponseEntity<FunctionMetadataResponse> multiply(@RequestBody BinaryOperationRequest request);

    @PostMapping("/divide")
    ResponseEntity<FunctionMetadataResponse> divide(@RequestBody BinaryOperationRequest request);

    @PostMapping("/differentiate")
    ResponseEntity<FunctionMetadataResponse> differentiate(@RequestBody UnaryOperationRequest request);
}
