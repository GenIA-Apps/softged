package com.mediarium.softged.ingestion.businessmodel;

public record ExtractionResult(
        Long documentId,
        int pageCount,
        int extractedPages
) {
}