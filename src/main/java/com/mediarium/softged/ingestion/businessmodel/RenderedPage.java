package com.mediarium.softged.ingestion.businessmodel;

public record RenderedPage(
        String imagePath,
        int width,
        int height
) {
}