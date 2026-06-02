package com.mediarium.softged.processing.businessmodel;

public record DocumentProcessingReport(
        Long documentId,
        int extractedPages,
        int summarizedPages,
        int indexedPages
) {
}