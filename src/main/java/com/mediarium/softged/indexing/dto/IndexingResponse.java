package com.mediarium.softged.indexing.dto;

import com.mediarium.softged.indexing.businessmodel.IndexingReport;

public record IndexingResponse(
        Long documentId,
        int indexedPages
) {
    public static IndexingResponse from(IndexingReport report) {
        return new IndexingResponse(
                report.documentId(),
                report.indexedPages()
        );
    }
}