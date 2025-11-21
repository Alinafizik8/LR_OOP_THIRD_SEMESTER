package com.example.alina.dto.function;

import lombok.Data;
import java.time.Instant;

// метаданные функции для списка

@Data
public class FunctionMetadataResponse {

    private Long id;
    private String name;
    private String type;
    private String localizedTypeName;
    private Instant createdAt;
}
