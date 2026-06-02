package com.mediarium.softged.vision.businessmodel;

public record VisualSummaryReport(
        Long documentId,
        int analyzedPages
) {
}