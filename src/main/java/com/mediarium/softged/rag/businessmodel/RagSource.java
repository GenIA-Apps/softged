package com.mediarium.softged.rag.businessmodel;

public record RagSource(
        String documentId,
        String originalFilename,
        String pageId,
        String pageNumber,
        String imagePath
) {
}