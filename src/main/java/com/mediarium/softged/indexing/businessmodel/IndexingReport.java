package com.mediarium.softged.indexing.businessmodel;

public record IndexingReport(
        Long documentId,
        int indexedPages
) {
}