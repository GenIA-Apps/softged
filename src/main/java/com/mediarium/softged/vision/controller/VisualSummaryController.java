package com.mediarium.softged.vision.controller;

import com.mediarium.softged.shared.security.FirebasePrincipal;
import com.mediarium.softged.vision.businessmodel.VisualSummaryReport;
import com.mediarium.softged.vision.dto.VisualSummaryResponse;
import com.mediarium.softged.vision.service.VisualSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents/{documentId}/visual-summary")
@RequiredArgsConstructor
public class VisualSummaryController {

    private final VisualSummaryService visualSummaryService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VisualSummaryResponse generate(
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        FirebasePrincipal principal = (FirebasePrincipal) authentication.getPrincipal();

        VisualSummaryReport report = visualSummaryService.generateVisualSummaries(
                documentId,
                principal.uid()
        );

        return VisualSummaryResponse.from(report);
    }
}