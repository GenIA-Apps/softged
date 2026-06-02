package com.mediarium.softged.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @NotBlank(message = "Le nom du projet est obligatoire")
        @Size(max = 255, message = "Le nom du projet ne doit pas dépasser 255 caractères")
        String name,
        String description
) {
}
