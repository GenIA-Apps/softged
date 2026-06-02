package com.mediarium.softged.vision.service;

import com.mediarium.softged.document.businessmodel.DocumentStatus;
import com.mediarium.softged.document.businessmodel.GedDocument;
import com.mediarium.softged.document.dataservice.DocumentDataService;
import com.mediarium.softged.document.service.DocumentService;
import com.mediarium.softged.ingestion.businessmodel.DocumentPage;
import com.mediarium.softged.ingestion.dataservice.DocumentPageDataService;
import com.mediarium.softged.shared.exception.BadRequestException;
import com.mediarium.softged.vision.businessmodel.VisualSummaryReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisualSummaryService {

    private final DocumentService documentService;
    private final DocumentDataService documentDataService;
    private final DocumentPageDataService documentPageDataService;
    private final PageVisionService pageVisionService;

    @Transactional
    public VisualSummaryReport generateVisualSummaries(Long documentId, String ownerUid) {
        GedDocument document = documentService.findById(documentId, ownerUid);

        if (document.getStatus() != DocumentStatus.EXTRACTED) {
            throw new BadRequestException("Document must be EXTRACTED before visual summary generation");
        }

        documentDataService.updateStatus(
                documentId,
                ownerUid,
                DocumentStatus.SUMMARIZING
        );

        List<DocumentPage> pages = documentPageDataService.findAllByDocument(
                documentId,
                ownerUid
        );

        int analyzedPages = 0;

        for (DocumentPage page : pages) {
            String summary = pageVisionService.summarizeArchitecturePage(
                    Path.of(page.getImagePath()),
                    page.getExtractedText()
            );

            documentPageDataService.updateVisualSummary(
                    page.getId(),
                    ownerUid,
                    summary
            );

            analyzedPages++;
        }

        documentDataService.updateStatus(
                documentId,
                ownerUid,
                DocumentStatus.SUMMARIZED
        );

        return new VisualSummaryReport(documentId, analyzedPages);
    }
}