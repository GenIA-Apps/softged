package com.mediarium.softged.ingestion.controller;

import com.mediarium.softged.ingestion.businessmodel.ExtractionResult;
import com.mediarium.softged.ingestion.service.IngestionService;
import com.mediarium.softged.shared.security.FirebasePrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents/{documentId}/extraction")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ExtractionResult extract(
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        FirebasePrincipal principal = (FirebasePrincipal) authentication.getPrincipal();
        return ingestionService.extractDocument(documentId, principal.uid());
    }
}