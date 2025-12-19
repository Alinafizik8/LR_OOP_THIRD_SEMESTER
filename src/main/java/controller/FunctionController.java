package controller;

import dto.ApplyRequest;
import dto.CreateFunctionFromArraysRequest;
import dto.CreateFunctionFromMathRequest;
import dto.DifferentiationRequest;
import dto.FunctionDTO;
import dto.InsertPointRequest;
import dto.OperationRequest;
import dto.UpdateFunctionRequest;
import dto.UploadFunctionRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import service.FunctionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/functions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class FunctionController {

    @Autowired
    private FunctionService functionService;

    @PostMapping("/from-arrays")
    public ResponseEntity<?> createFromArrays(
            @Valid @RequestBody CreateFunctionFromArraysRequest request,
            Authentication authentication) {
        try {
            FunctionDTO function = functionService.createFunctionFromArrays(request, authentication.getName());
            return ResponseEntity.ok(function);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/from-math")
    public ResponseEntity<?> createFromMath(
            @Valid @RequestBody CreateFunctionFromMathRequest request,
            Authentication authentication) {
        try {
            FunctionDTO function = functionService.createFunctionFromMath(request, authentication.getName());
            return ResponseEntity.ok(function);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/from-math/preview")
    public ResponseEntity<?> previewFromMath(
            @Valid @RequestBody CreateFunctionFromMathRequest request) {
        try {
            FunctionDTO function = functionService.previewFunctionFromMath(request);
            return ResponseEntity.ok(function);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserFunctions(Authentication authentication) {
        try {
            List<FunctionDTO> functions = functionService.getUserFunctions(authentication.getName());
            return ResponseEntity.ok(functions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFunction(@PathVariable Long id, Authentication authentication) {
        try {
            FunctionDTO function = functionService.getFunctionById(id, authentication.getName());
            return ResponseEntity.ok(function);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFunction(
            @PathVariable Long id,
            @RequestBody UpdateFunctionRequest request,
            Authentication authentication) {
        try {
            FunctionDTO function = functionService.updateFunction(id, request, authentication.getName());
            return ResponseEntity.ok(function);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFunction(@PathVariable Long id, Authentication authentication) {
        try {
            functionService.deleteFunction(id, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Function deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/math-types")
    public ResponseEntity<?> getMathFunctionTypes() {
        return ResponseEntity.ok(functionService.getMathFunctionTypes());
    }

    @PostMapping("/differentiate")
    public ResponseEntity<?> differentiate(
            @Valid @RequestBody DifferentiationRequest request,
            Authentication authentication) {
        try {
            FunctionDTO derivedFunction = functionService.differentiateFunction(request, authentication.getName());
            return ResponseEntity.ok(derivedFunction);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/operate")
    public ResponseEntity<?> operate(
            @Valid @RequestBody OperationRequest request,
            Authentication authentication) {
        try {
            FunctionDTO resultFunction = functionService.performOperation(request, authentication.getName());
            return ResponseEntity.ok(resultFunction);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadFunction(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String base64Data = functionService.serializeFunctionToBase64(id, authentication.getName());
            return ResponseEntity.ok(Map.of("base64Data", base64Data));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFunction(
            @Valid @RequestBody UploadFunctionRequest request,
            Authentication authentication) {
        try {
            FunctionDTO function = functionService.deserializeFunctionFromBase64(
                    request.getBase64Data(),
                    request.getName(),
                    request.getFunctionType(),
                    authentication.getName()
            );
            return ResponseEntity.ok(function);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<?> applyFunction(
            @PathVariable Long id,
            @Valid @RequestBody ApplyRequest request,
            Authentication authentication) {
        try {
            double result = functionService.applyFunction(id, request.getX(), authentication.getName());
            return ResponseEntity.ok(Map.of("result", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/insert")
    public ResponseEntity<?> insertPoint(
            @PathVariable Long id,
            @Valid @RequestBody InsertPointRequest request,
            Authentication authentication) {
        try {
            FunctionDTO function = functionService.insertPoint(
                    id,
                    request.getX(),
                    request.getY(),
                    authentication.getName()
            );
            return ResponseEntity.ok(function);
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Эта функция не поддерживает вставку"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/remove/{index}")
    public ResponseEntity<?> removePoint(
            @PathVariable Long id,
            @PathVariable int index,
            Authentication authentication) {
        try {
            FunctionDTO function = functionService.removePoint(id, index, authentication.getName());
            return ResponseEntity.ok(function);
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Эта функция не поддерживает удаление"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/download/xml")
    public ResponseEntity<?> downloadFunctionAsXml(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String xmlData = functionService.serializeFunctionToXml(id, authentication.getName());
            return ResponseEntity.ok(Map.of("xmlData", xmlData));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/upload/xml")
    public ResponseEntity<?> uploadFunctionFromXml(
            @Valid @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            FunctionDTO function = functionService.deserializeFunctionFromXml(
                    request.get("xmlData"),
                    request.get("name"),
                    request.get("functionType"),
                    authentication.getName()
            );
            return ResponseEntity.ok(function);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/download/json")
    public ResponseEntity<?> downloadFunctionAsJson(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String jsonData = functionService.serializeFunctionToJson(id, authentication.getName());
            return ResponseEntity.ok(Map.of("jsonData", jsonData));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/upload/json")
    public ResponseEntity<?> uploadFunctionFromJson(
            @Valid @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            FunctionDTO function = functionService.deserializeFunctionFromJson(
                    request.get("jsonData"),
                    request.get("name"),
                    request.get("functionType"),
                    authentication.getName()
            );
            return ResponseEntity.ok(function);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
