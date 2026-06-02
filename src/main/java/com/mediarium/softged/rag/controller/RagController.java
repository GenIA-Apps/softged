package com.mediarium.softged.rag.controller;

import com.mediarium.softged.rag.businessmodel.RagAnswer;
import com.mediarium.softged.rag.dto.RagAnswerResponse;
import com.mediarium.softged.rag.dto.RagQuestionRequest;
import com.mediarium.softged.rag.service.RagService;
import com.mediarium.softged.shared.security.FirebasePrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;


    @PostMapping
    public RagAnswerResponse ask(
            @PathVariable Long projectId,
            @Valid @RequestBody RagQuestionRequest request,
            Authentication authentication
    ) {
        FirebasePrincipal principal = (FirebasePrincipal) authentication.getPrincipal();
        RagAnswer answer = ragService.ask(
                projectId,
                principal.uid(),
                request.question()
        );
        return RagAnswerResponse.from(answer);
    }
}