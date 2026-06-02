package com.mediarium.softged.indexing.controller;

import com.mediarium.softged.indexing.businessmodel.IndexingReport;
import com.mediarium.softged.indexing.dto.IndexingResponse;
import com.mediarium.softged.indexing.service.DocumentIndexingService;
import com.mediarium.softged.shared.security.FirebasePrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents/{documentId}/indexing")
@RequiredArgsConstructor
public class IndexingController {

    private final DocumentIndexingService documentIndexingService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public IndexingResponse index(
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        FirebasePrincipal principal = (FirebasePrincipal) authentication.getPrincipal();

        IndexingReport report = documentIndexingService.indexDocument(
                documentId,
                principal.uid()
        );

        return IndexingResponse.from(report);
    }
}