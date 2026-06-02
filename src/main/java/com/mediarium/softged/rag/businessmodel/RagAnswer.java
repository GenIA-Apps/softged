package com.mediarium.softged.rag.businessmodel;

import java.util.List;

public record RagAnswer(
        String answer,
        List<RagSource> sources
) {
}