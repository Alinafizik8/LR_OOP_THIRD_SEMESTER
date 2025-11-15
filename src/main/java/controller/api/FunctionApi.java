package controller.api;

import dto.function.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/functions")
public interface FunctionApi {

    // Создание: из точек
    @PostMapping("/from-points")
    ResponseEntity<FunctionMetadataResponse> createFromPoints(@RequestBody CreateFunctionFromPointsRequest request);

    // Создание: из математической функции
    @PostMapping("/from-math")
    ResponseEntity<FunctionMetadataResponse> createFromMath(@RequestBody CreateFunctionFromMathRequest request);

    // Список функций текущего пользователя (с пагинацией и сортировкой)
    @GetMapping
    ResponseEntity<Page<FunctionListDto>> listFunctions(Pageable pageable);

    // Получение полной функции (x/y)
    @GetMapping("/{id}")
    ResponseEntity<FullFunctionResponse> getFunction(@PathVariable Long id);

    // Обновление имени
    @PutMapping("/{id}")
    ResponseEntity<FunctionMetadataResponse> updateFunction(@PathVariable Long id, @RequestBody UpdateFunctionRequest request);

    // Удаление
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteFunction(@PathVariable Long id);
}
