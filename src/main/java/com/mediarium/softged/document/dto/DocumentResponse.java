package com.mediarium.softged.document.dto;

import com.mediarium.softged.document.businessmodel.DocumentStatus;
import com.mediarium.softged.document.businessmodel.GedDocument;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        Long projectId,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        DocumentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static DocumentResponse from(GedDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getProjectId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getStatus(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}