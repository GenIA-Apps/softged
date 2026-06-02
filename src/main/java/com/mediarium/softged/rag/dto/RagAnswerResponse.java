package com.mediarium.softged.rag.dto;

import com.mediarium.softged.rag.businessmodel.RagAnswer;

import java.util.List;

public record RagAnswerResponse(
        String answer,
        List<RagSourceResponse> sources
) {
    public static RagAnswerResponse from(RagAnswer ragAnswer) {
        return new RagAnswerResponse(
                ragAnswer.answer(),
                ragAnswer.sources()
                        .stream()
                        .map(RagSourceResponse::from)
                        .toList()
        );
    }
}