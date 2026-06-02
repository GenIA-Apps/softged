package com.mediarium.softged.processing.controller;

import com.mediarium.softged.processing.businessmodel.DocumentProcessingReport;
import com.mediarium.softged.processing.dto.DocumentProcessingResponse;
import com.mediarium.softged.processing.service.DocumentProcessingService;
import com.mediarium.softged.shared.security.FirebasePrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents/{documentId}/processing")
public class DocumentProcessingController {

    private final DocumentProcessingService documentProcessingService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DocumentProcessingResponse process(
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        FirebasePrincipal principal = (FirebasePrincipal) authentication.getPrincipal();

        DocumentProcessingReport report = documentProcessingService.processDocument(
                documentId,
                principal.uid()
        );

        return DocumentProcessingResponse.from(report);
    }
}