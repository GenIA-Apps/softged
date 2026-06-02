package com.mediarium.softged.vision.dto;

import com.mediarium.softged.vision.businessmodel.VisualSummaryReport;

public record VisualSummaryResponse(
        Long documentId,
        int analyzedPages
) {
    public static VisualSummaryResponse from(VisualSummaryReport report) {
        return new VisualSummaryResponse(
                report.documentId(),
                report.analyzedPages()
        );
    }
}