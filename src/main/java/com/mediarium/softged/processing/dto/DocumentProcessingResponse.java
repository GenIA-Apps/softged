package com.mediarium.softged.processing.dto;

import com.mediarium.softged.processing.businessmodel.DocumentProcessingReport;

public record DocumentProcessingResponse(
        Long documentId,
        int extractedPages,
        int summarizedPages,
        int indexedPages
) {
    public static DocumentProcessingResponse from(DocumentProcessingReport report) {
        return new DocumentProcessingResponse(
                report.documentId(),
                report.extractedPages(),
                report.summarizedPages(),
                report.indexedPages()
        );
    }
}