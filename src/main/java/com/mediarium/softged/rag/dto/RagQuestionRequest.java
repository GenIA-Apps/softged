package com.mediarium.softged.rag.dto;

import jakarta.validation.constraints.NotBlank;

public record RagQuestionRequest(
        @NotBlank(message = "La question est obligatoire")
        String question
) {
}