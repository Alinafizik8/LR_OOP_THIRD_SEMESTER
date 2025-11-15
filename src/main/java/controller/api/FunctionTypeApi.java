package controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import dto.function.FunctionTypeDto;

import java.util.List;

@RequestMapping("/api/v1/function-types")
public interface FunctionTypeApi {

    @GetMapping
    ResponseEntity<List<FunctionTypeDto>> listFunctionTypes();
}
