package com.mediarium.softged.rag.dto;

import com.mediarium.softged.rag.businessmodel.RagSource;

public record RagSourceResponse(
        String documentId,
        String originalFilename,
        String pageId,
        String pageNumber,
        String imagePath
) {
    public static RagSourceResponse from(RagSource source) {
        return new RagSourceResponse(
                source.documentId(),
                source.originalFilename(),
                source.pageId(),
                source.pageNumber(),
                source.imagePath()
        );
    }
}